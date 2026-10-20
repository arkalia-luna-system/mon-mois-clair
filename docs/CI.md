# CI — feuille de route (progressive)

Principe : **une garde à la fois**. On active un contrôle seulement quand le code est déjà prêt
(ou après une petite session de nettoyage dédiée), pour éviter un mur de corrections.

## Phase 1 — build + tests (bloquant) ✅

Workflow : [`.github/workflows/ci.yml`](../.github/workflows/ci.yml)

À chaque push / PR sur **`develop`** (et `main` si merge explicite) :

- compile `assembleDebug`
- exécute `:app:testDebugUnitTest`
- actions à jour (Node 24) : `setup-android@v4`, `upload-artifact@v7`

Travail quotidien = **`develop`** uniquement (voir charte §5).
Échec = le push casse le build ou les tests → à corriger avant de considérer la branche OK.

## Phase 2 — qualité (en cours, soft)

1. ✅ `:app:lintDebug` en CI avec `continue-on-error: true` (rapport artifact, **ne bloque pas**).
2. ⏳ Ensuite : corriger les findings utiles, puis éventuellement lint **bloquant**.
3. ⏳ Detekt (style / smells Kotlin) — règles soft au début — **pas encore**.

## Phase 3 — sécurité (soft) ✅ partiel

1. ✅ Dependabot Actions → PRs hebdo.
2. ✅ Dependabot Gradle → PRs hebdo (rapport / revue manuelle, pas de garde CI).
3. ⏳ Plus tard : scan CVE (ex. dependency-check) en **rapport** puis éventuellement en garde.
4. Garder hors CI : secrets (`keystore`, `local.properties`) — déjà dans `.gitignore` / `SECURITY.md`.

## Phase 4 — plus corsé (optionnel)

- Tests instrumentés / emulator (lent, coûteux) — seulement si vraiment utile.
- Vérif ProGuard/R8 release **unsigned** (`assembleRelease` sans keystore) pour détecter les shrink breaks.

## Ce qu’on ne fait pas pour l’instant

- Exiger 0 warning Lint (lint soft seulement)
- Detekt
- Emulator sur chaque push
- Scan CVE bloquant
- Couverture de code obligatoire

Quand tu voudras la suite : on active **un** contrôle, on corrige, on commit, puis on attend.
