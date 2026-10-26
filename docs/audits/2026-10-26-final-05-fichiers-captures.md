# Audit final — fichiers · captures · dist · AppleDouble · assets

| | |
|--|--|
| **Date** | 2026-10-26 · final-05 |
| **Profil** | Hygiène repo / volume T7 / sideload famille |
| **Produit** | Mon Mois Clair · `0.1.0-6.82` · `versionCode` 87 · **332/332** unit (claim journal) |
| **Portée** | **AUDIT ONLY** — doc seule. **Pas de code.** Pas de `git clean` exécuté ici. |
| **Sources** | inventaire disque · `.gitignore` · `docs/audits/smoke/` · `dist/` · `app/src/main/res/` · statut git |

**Question unique :** *Quels fichiers locaux (captures, APK, AppleDouble, tmp) encombrent le volume ou risquent un commit / une fuite PII, et quels chemins exacts supprimer ou laisser gitignorés ?*

---

## 0. Verdict exécutif

| Lot | Sur disque | Déjà gitignoré ? | Action |
|-----|------------|------------------|--------|
| Captures smoke PNG | **Oui** · 8 PNG · **~3,7 Mo** utiles (+ 1 `._*`) | **Oui** (`/docs/audits/smoke/**/*.png`) | **Supprimer** les PNG (PII foyer) ; garder le `.md` de synthèse |
| APK `dist/` | **Oui** · **~42 Mo** | **Oui** (`/dist/*.apk` + `*.apk`) | **Supprimer** l’APK local (rebuild Gradle) ; garder `dist/verify/*.md` |
| AppleDouble `._*` | **Oui** · **118** hors `.gradle` (~468 Kio) | **Oui** (`**/._*`) | **Supprimer** (bruit T7) ; règle déjà OK |
| `tmp/` repo | **Absent** | **Oui** (`/tmp/`) | Rien à faire ; règle OK |
| `/captures/` | **Absent** | **Oui** (`/captures/`) | Rien à faire ; règle OK |
| Assets Android morts | **Aucun** détecté | N/A | **Ne pas** supprimer de drawable / font / layout / mipmap / xml |
| Secrets signing locaux | `keystore.properties` + `release/*.jks` | **Oui** | **Garder** local · **jamais** committer |

**Go hygiène :** le `.gitignore` couvre déjà le gros risque. Le travail reste = **fichiers présents sur le T7** (smoke PII + APK 42 Mo + 118 `._*`) à **effacer disque**, pas à versionner.

---

## 1. Captures smoke (`docs/audits/smoke/`)

Référencées par [`2026-10-23-smoke-device.md`](2026-10-23-smoke-device.md). PNG = **données foyer** (montants, libellés, widget) — ne pas pousser, ne pas archiver hors machine de confiance.

| # | Chemin exact | Taille |
|--:|--------------|-------:|
| 1 | `docs/audits/smoke/01_accueil.png` | 247 147 |
| 2 | `docs/audits/smoke/02_menu.png` | 207 783 |
| 3 | `docs/audits/smoke/03_reglages.png` | 239 443 |
| 4 | `docs/audits/smoke/04_dossiers.png` | 241 102 |
| 5 | `docs/audits/smoke/05_a_venir.png` | 208 260 |
| 6 | `docs/audits/smoke/06_calendrier.png` | 214 513 |
| 7 | `docs/audits/smoke/07_fiche.png` | 229 358 |
| 8 | `docs/audits/smoke/08_widget.png` | 2 274 964 |
| — | `docs/audits/smoke/._08_widget.png` | 4 096 |

**Total dossier** ~**4,3 Mo** (du) / ~**3,7 Mo** fichiers listés.

**À supprimer (disque) :** les **9** chemins du tableau.

**À garder :**
- `docs/audits/2026-10-23-smoke-device.md` (rapport textuel ; liens PNG cassés après purge = OK)
- Règles `.gitignore` L42–44

**Déjà gitignoré :**
```
/docs/audits/smoke/**/*.png
/docs/audits/smoke/**/*.xml
/docs/audits/smoke/**/*.webp
```

**Gap mineur (optionnel gitignore) :** ignorer aussi tout le dossier média, ex. `/docs/audits/smoke/**` sauf si un `.gitkeep` / `.md` est voulu plus tard. Aujourd’hui seuls PNG/XML/WebP sont listés — suffisant pour l’existant.

**Statut git :** aucun `docs/audits/smoke/*` tracké ; `git check-ignore` confirme les PNG.

---

## 2. APK / dist

| Chemin exact | Taille | Statut |
|--------------|-------:|--------|
| `dist/MonMoisClair-release.apk` | 43 601 800 (~**42 Mo**) | présent · gitignoré |
| `dist/._MonMoisClair-release.apk` | 4 096 | AppleDouble · gitignoré |
| `dist/verify/README.md` | — | **garder** · tracké |
| `dist/verify/RESET_TESTS_FROM_ZERO.md` | — | **garder** · tracké |

**À supprimer :**
- `dist/MonMoisClair-release.apk`
- `dist/._MonMoisClair-release.apk`

**Pas de dumps** sous `dist/verify/` (pas de PNG/XML/JSON/campagne) — hygiène verify **propre**.

**Déjà gitignoré :**
```
*.apk
*.ap_
*.aab
/dist/*.apk
/dist/verify/**/*.png
/dist/verify/*.png
/dist/verify/**/*.xml
/dist/verify/**/*.json
/dist/verify/**/*.txt
/dist/verify/**/*.log
/dist/verify/**/*.py
/dist/verify/RAPPORT_*.md
/dist/verify/**/RAPPORT_*.md
/dist/verify/campagne-*/
```

**Rebuild :** `./gradlew :app:assembleRelease` (ou flux famille habituel) → recopier l’APK dans `dist/` si besoin sideload.

---

## 3. `tmp/` · `captures/`

| Chemin | Existe ? | `.gitignore` |
|--------|----------|--------------|
| `tmp/` (racine repo) | **Non** | `/tmp/` |
| `captures/` (racine) | **Non** | `/captures/` |

Rien à supprimer. Les règles restent utiles pour les prochaines campagnes adb / Studio.

> Note : `/tmp/` dans `.gitignore` = dossier **`tmp/` à la racine du repo**, pas `/private/tmp` système.

---

## 4. AppleDouble (`._*`) — volume T7 / macOS

| Zone | Nombre | Ordre de grandeur |
|------|-------:|-------------------|
| Hors `.gradle` | **118** | ~**468 Kio** |
| Dans `.gradle` | **~14** | régénérés · ignorer / purge optionnelle |
| Trackés dans git | **0** | `**/._*` OK |

**Déjà gitignoré :** `**/._*`

### 4.1 Racine repo — **supprimer**

```
._.gitignore
._CHARTE_ET_ARCHITECTURE.md
._PRIVACY.md
._PROGRESS_LOG.md
._README.md
._SECURITY.md
._build.gradle.kts
._settings.gradle.kts
```

### 4.2 `.github` / `app` meta — **supprimer**

```
.github/workflows/._ci.yml
app/._build.gradle.kts
app/._proguard-rules.pro
```

### 4.3 `dist` / smoke — **supprimer** (aussi listés §1–2)

```
dist/._MonMoisClair-release.apk
docs/audits/smoke/._08_widget.png
```

### 4.4 `docs/` — **supprimer** (23)

```
docs/._AUDIT_STATUS.md
docs/._BENCHMARK_QUALITE_2026.md
docs/._CI.md
docs/._DISTRIBUTION_FAMILLE.md
docs/._PLAN_PRODUIT_FINI.md
docs/._README.md
docs/audits/._2026-10-23-evaluation-cadeau-maman.md
docs/audits/._2026-10-23-nuit-00-SYNTHESE-PRIORITES.md
docs/audits/._2026-10-23-nuit-01-da-visuel.md
docs/audits/._2026-10-23-nuit-02-ux-famille.md
docs/audits/._2026-10-23-nuit-03-produit-vente.md
docs/audits/._2026-10-23-nuit-04-securite-privacy.md
docs/audits/._2026-10-23-nuit-05-ingenierie-android.md
docs/audits/._2026-10-23-nuit-06-a11y-oneui.md
docs/audits/._2026-10-23-nuit-07-github-carriere.md
docs/audits/._2026-10-23-nuit-08-domaine-belgique.md
docs/audits/._2026-10-23-smoke-device.md
docs/audits/._2026-10-23-valorisation.md
docs/audits/._2026-10-26-final-01-affichage.md
docs/audits/._2026-10-26-final-02-securite.md
docs/audits/._2026-10-26-final-04-docs-md.md
docs/audits/._2026-10-26-final-06-livrable-famille.md
docs/audits/._2026-10-26-securite-chiffrement.md
```

### 4.5 `app/src` — **supprimer** (84)

Fichiers `._*` à côté des sources (pas du code). Liste complète hors `.gradle` :

```
app/src/main/java/be/monmoisclair/app/data/._crypto
app/src/main/java/be/monmoisclair/app/data/backup/._BackupModels.kt
app/src/main/java/be/monmoisclair/app/data/backup/._BackupRestorer.kt
app/src/main/java/be/monmoisclair/app/data/backup/._SafBackupStore.kt
app/src/main/java/be/monmoisclair/app/data/biometric/._BiometricAuthGateway.kt
app/src/main/java/be/monmoisclair/app/data/biometric/._BiometricAuthGatewayImpl.kt
app/src/main/java/be/monmoisclair/app/data/crypto/._AesGcmBox.kt
app/src/main/java/be/monmoisclair/app/data/crypto/._BackupEnvelope.kt
app/src/main/java/be/monmoisclair/app/data/crypto/._DbPassphraseVault.kt
app/src/main/java/be/monmoisclair/app/data/crypto/._EncryptedDbMigrator.kt
app/src/main/java/be/monmoisclair/app/data/crypto/._KeystoreAesKey.kt
app/src/main/java/be/monmoisclair/app/data/crypto/._LocalFileCipher.kt
app/src/main/java/be/monmoisclair/app/data/crypto/._ShareSecretVault.kt
app/src/main/java/be/monmoisclair/app/data/receipt/._ReceiptPhotoStore.kt
app/src/main/java/be/monmoisclair/app/data/reminder/._ReminderSchedulerImpl.kt
app/src/main/java/be/monmoisclair/app/data/widget/._CarnetWidgetProvider.kt
app/src/main/java/be/monmoisclair/app/data/widget/._CarnetWidgetRefresher.kt
app/src/main/java/be/monmoisclair/app/di/._DatabaseModule.kt
app/src/main/java/be/monmoisclair/app/domain/._EpcQrParser.kt
app/src/main/java/be/monmoisclair/app/domain/._MonthCycleEngine.kt
app/src/main/java/be/monmoisclair/app/domain/._PaymentNotePolicy.kt
app/src/main/java/be/monmoisclair/app/domain/._ShareFileIntents.kt
app/src/main/java/be/monmoisclair/app/domain/usecase/._AddEntryUseCase.kt
app/src/main/java/be/monmoisclair/app/domain/usecase/._AgreementFuturesSync.kt
app/src/main/java/be/monmoisclair/app/domain/usecase/._BiometricPreferenceUseCase.kt
app/src/main/java/be/monmoisclair/app/domain/usecase/._ConvertPendingLetterToDossierUseCase.kt
app/src/main/java/be/monmoisclair/app/domain/usecase/._PrepareMonthSummaryUseCase.kt
app/src/main/java/be/monmoisclair/app/domain/usecase/._ShareBackupUseCase.kt
app/src/main/java/be/monmoisclair/app/domain/usecase/._UpdateInstanceUseCase.kt
app/src/main/java/be/monmoisclair/app/ui/components/._A11yButton.kt
app/src/main/java/be/monmoisclair/app/ui/components/._AppWordmark.kt
app/src/main/java/be/monmoisclair/app/ui/entry/._AddEntryViewModel.kt
app/src/main/java/be/monmoisclair/app/ui/entry/._EditEntryViewModel.kt
app/src/main/java/be/monmoisclair/app/ui/entry/._EntryFormChrome.kt
app/src/main/java/be/monmoisclair/app/ui/entry/._EntryFormScreen.kt
app/src/main/java/be/monmoisclair/app/ui/home/._HomeCoursesSheet.kt
app/src/main/java/be/monmoisclair/app/ui/home/._HomeMenuSheet.kt
app/src/main/java/be/monmoisclair/app/ui/home/._HomeSavingsSheet.kt
app/src/main/java/be/monmoisclair/app/ui/home/._HomeScreen.kt
app/src/main/java/be/monmoisclair/app/ui/home/._HomeViewModel.kt
app/src/main/java/be/monmoisclair/app/ui/home/._StickyCoursesLabel.kt
app/src/main/java/be/monmoisclair/app/ui/restore/._RestoreBackupScreen.kt
app/src/main/java/be/monmoisclair/app/ui/restore/._RestoreBackupViewModel.kt
app/src/main/java/be/monmoisclair/app/ui/restore/._RestoreMessages.kt
app/src/main/java/be/monmoisclair/app/ui/settings/._HowItWorksCopy.kt
app/src/main/java/be/monmoisclair/app/ui/settings/._SettingsHubCopy.kt
app/src/main/java/be/monmoisclair/app/ui/settings/._SettingsPages.kt
app/src/main/java/be/monmoisclair/app/ui/settings/._SettingsScreen.kt
app/src/main/java/be/monmoisclair/app/ui/settings/._SettingsViewModel.kt
app/src/main/java/be/monmoisclair/app/ui/theme/._A11yTokens.kt
app/src/main/java/be/monmoisclair/app/ui/theme/._DisplayFit.kt
app/src/main/java/be/monmoisclair/app/ui/upcoming/._UpcomingScreen.kt
app/src/main/res/drawable/._widget_carnet_bg.xml
app/src/main/res/drawable/._widget_carnet_bg_night.xml
app/src/main/res/layout/._widget_carnet.xml
app/src/main/res/values/._colors.xml
app/src/main/res/xml/._carnet_widget_info.xml
app/src/main/res/xml/._file_paths.xml
app/src/test/java/be/monmoisclair/app/data/._crypto
app/src/test/java/be/monmoisclair/app/data/._widget
app/src/test/java/be/monmoisclair/app/data/backup/._BackupCoordinatorTest.kt
app/src/test/java/be/monmoisclair/app/data/backup/._BackupRestorerTest.kt
app/src/test/java/be/monmoisclair/app/data/crypto/._AesGcmBoxTest.kt
app/src/test/java/be/monmoisclair/app/data/crypto/._BackupEnvelopeTest.kt
app/src/test/java/be/monmoisclair/app/data/crypto/._EncryptedDbMigratorTest.kt
app/src/test/java/be/monmoisclair/app/data/crypto/._LocalFileCipherTest.kt
app/src/test/java/be/monmoisclair/app/data/db/._RoomMigrationTest.kt
app/src/test/java/be/monmoisclair/app/data/reminder/._ReminderSchedulerImplTest.kt
app/src/test/java/be/monmoisclair/app/domain/._EpcQrParserTest.kt
app/src/test/java/be/monmoisclair/app/domain/._MonthCycleEngineTest.kt
app/src/test/java/be/monmoisclair/app/domain/._PaymentNotePolicyTest.kt
app/src/test/java/be/monmoisclair/app/domain/._ShareFileIntentsTest.kt
app/src/test/java/be/monmoisclair/app/domain/usecase/._AddEntryUseCaseTest.kt
app/src/test/java/be/monmoisclair/app/domain/usecase/._OnboardingUseCasesTest.kt
app/src/test/java/be/monmoisclair/app/domain/usecase/._PendingLetterUseCaseTest.kt
app/src/test/java/be/monmoisclair/app/domain/usecase/._PrepareMonthSummaryUseCaseTest.kt
app/src/test/java/be/monmoisclair/app/domain/usecase/._ToggleExpenseDoneUseCaseTest.kt
app/src/test/java/be/monmoisclair/app/domain/usecase/._UpdateDeleteEntryUseCaseTest.kt
app/src/test/java/be/monmoisclair/app/ui/home/._StickyCoursesLabelTest.kt
app/src/test/java/be/monmoisclair/app/ui/restore/._RestoreMessagesTest.kt
app/src/test/java/be/monmoisclair/app/ui/settings/._HowItWorksCopyTest.kt
app/src/test/java/be/monmoisclair/app/ui/theme/._WcagContrastTest.kt
```

**Commande disque proposée** (à lancer **plus tard**, hors cet audit) :

```bash
# depuis la racine du repo — ne touche pas .git
find . -name '._*' -not -path './.git/*' -delete
rm -f docs/audits/smoke/*.png
rm -f dist/MonMoisClair-release.apk
```

Les `._*` sous `.gradle/` peuvent être inclus dans le `find` (sans danger produit) ou laissés : cache local.

---

## 5. Assets Android — morts ?

Scan `app/src/main/res` (drawable, mipmap, font, layout, xml) hors `._*` : **21** ressources · **0** sans référence `R.*` / `@drawable|@mipmap|@font|@layout|@xml`.

| Ressource | Verdict |
|-----------|---------|
| `book_launcher` / `book_lockup` / `book_mark` / `book_mono` | **Vivants** (splash, launcher, wordmark) |
| `ic_calendar_month`, `ic_chevron_*`, `ic_expand_*`, `ic_notification` | **Vivants** |
| `widget_carnet*` + `carnet_widget_info` + `file_paths` | **Vivants** (widget / FileProvider) |
| `ic_launcher` (v26 + v33) | **Vivants** |
| `source_sans3_*` / `source_serif4_*` | **Vivants** (thème Compose + PDF résumé) |

**Aucun asset mort à supprimer.** (Le widget n’utilise plus `@font/` RemoteViews depuis 6.79, mais les TTF restent utilisés in-app / PDF.)

---

## 6. Fichiers « non référencés » (hors junk)

| Candidat | Verdict |
|----------|---------|
| Kotlin « orphelins » par nom de fichier (Hilt / Compose / Nav) | **Faux positifs** — ne pas supprimer |
| `docs/archive/**` | Archive **volontaire** — garder |
| `docs/PLAN_PRODUIT_FINI.md` vs `docs/archive/PLAN_PRODUIT_FINI.md` | Doublon **doc** (vivant vs archive) — hors scope purge binaire |
| `keystore.properties` · `release/monmoisclair-release.jks` | Locaux · gitignorés · **garder** pour sideload |
| `local.properties` | SDK local · gitignoré · garder |
| `docs/audits/2026-10-23-evaluation-cadeau-maman.md` | Doc utile · **untracked** à committer quand demandé — **pas** junk |

**Pas de `.log` / `.tmp` / `*~` / `.bak` / `.DS_Store`** trouvés hors caches build.

---

## 7. Checklist actions (ordre recommandé)

### Supprimer maintenant (disque, ~46 Mo)

1. `docs/audits/smoke/01_accueil.png` … `08_widget.png` + `docs/audits/smoke/._08_widget.png`
2. `dist/MonMoisClair-release.apk` + `dist/._MonMoisClair-release.apk`
3. Les **118** `._*` hors `.git` (listes §4) — ou le `find` ci-dessus

### Ne pas supprimer

- `dist/verify/README.md` · `dist/verify/RESET_TESTS_FROM_ZERO.md`
- `docs/audits/2026-10-23-smoke-device.md` et autres audits `.md`
- Tout `app/src/main/res/**` réel (hors `._*`)
- `keystore.properties` · `release/monmoisclair-release.jks` (machine de build)

### Gitignore — état

| Règle | Suffisante ? |
|-------|--------------|
| `**/._*` | **Oui** |
| `/docs/audits/smoke/**/*.png` (+ xml/webp) | **Oui** pour l’existant |
| `/dist/*.apk` + `*.apk` | **Oui** |
| `/tmp/` · `/captures/` | **Oui** (préventif) |
| secrets `keystore` / `*.jks` | **Oui** |

**Optionnel** (pas urgent) : `/docs/audits/smoke/**` pour tout dump futur non listé ; rien d’autre bloquant.

---

## 8. Hors périmètre (rappel)

- Purge `app/build/` · `.gradle/` · `.kotlin/` (caches normaux, déjà ignorés)
- Exécution du `find` / `rm` (cet audit **n’a rien effacé**)
- Commit / push des audits `2026-10-26-final-*`
- Claim « repo propre sur GitHub » sans preuve `git status` post-purge

---

*Audit final-05 · 2026-10-26 · AUDIT ONLY · zéro code.*
