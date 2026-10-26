# Audit — crash ouverture post-6.89 → fix 6.90

**Date :** 2026-10-26  
**Device :** SM-S938B (`R3CY60BJ3ZM`)  
**Package :** `be.monmoisclair.app`

## Cause

Deux défauts en chaîne au cold start Hilt → `DatabaseModule` :

1. **Keystore IV (bloquant)** — `AesGcmBox.encrypt` passait un `GCMParameterSpec` (IV caller).
   La clé wrap (`mmc_db_wrap_v1`) est créée avec `setRandomizedEncryptionRequired(true)`.
   → `InvalidAlgorithmParameterException: Caller-provided IV not permitted`
   → `Unable to create application MonMoisClairApp` (FATAL immédiat, pas de PID stable).

2. **Migration SQLCipher → Room onCreate** — une fois (1) corrigé, log
   `DB migrée vers SQLCipher` puis
   `SQLiteConstraintException: UNIQUE constraint failed: app_meta.key`.
   `sqlcipher_export` ne conserve pas toujours `user_version` → Room voit 0 →
   `onCreate` + `INSERT` seeds déjà présents dans la DB exportée.

## Fix

| Fichier | Changement |
|---------|------------|
| `AesGcmBox.kt` | ENCRYPT : `cipher.init(ENCRYPT_MODE, key)` puis préfixe `cipher.iv` |
| `DatabaseModule.kt` | Seeds `INSERT OR IGNORE` |
| `EncryptedDbMigrator.kt` | Recopie `user_version` (PRAGMA attach + filet post-rename) |
| `settings.gradle.kts` / `build.gradle.kts` | Force `commons-lang3:3.18.0` (#60) |
| `docs/CI.md` | #54 pin Kotlin 2.0.21 documenté (pas de bump) |

## Preuves

- `adb logcat` : cause (1) puis (2) reproduites avant fix.
- `./gradlew :app:testDebugUnitTest --rerun-tasks` → **368 exécutés** / 369 (1 skipped), 87 XML, 0 failure.
- `assembleRelease` + `adb install -r` (sans wipe) → `versionName=0.1.0-6.90` / `versionCode=95`.
- Post-install : `pidof` OK · `topResumedActivity=…MainActivity` · logcat **sans FATAL**.

## Dependabot

| Alerte | Statut |
|--------|--------|
| **#60** commons-lang3 | **Forcé** `3.18.0` (resolutionStrategy) |
| **#54** kotlin-gradle-plugin | **Pin documenté** 2.0.21 — patch min Dependabot = `2.4.20-Beta1` (AGP 9) |

## Version / SHA

- Version installée : **`0.1.0-6.90`** (versionCode **95**)
- SHA : **`1faeaa2`** (`develop` = `main`, ff-only)
