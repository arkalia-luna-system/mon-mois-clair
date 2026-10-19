# CI — feuille de route (progressive)

Principe : **une garde à la fois**. On active un contrôle seulement quand le code est déjà prêt
(ou après une petite session de nettoyage dédiée), pour éviter un mur de corrections.

## Phase 1 — actuelle (léger)

Workflow : [`.github/workflows/ci.yml`](../.github/workflows/ci.yml)

À chaque push / PR sur **`develop`** (et `main` si merge explicite) :

- compile `assembleDebug`
- exécute `:app:testDebugUnitTest`

Travail quotidien = **`develop`** uniquement (voir charte §5).
Échec = le push casse le build ou les tests → à corriger avant de considérer la branche OK.

## Phase 2 — qualité (quand tu es prête)

Ajouter **sans** tout bloquer d’un coup :

1. `./gradlew :app:lintDebug` (warnings Android Lint) — d’abord en `continue-on-error: true`, puis bloquant.
2. Detekt (style / smells Kotlin) — règles soft au début.

## Phase 3 — sécurité (sans abuser)

1. Dependabot Actions (déjà actif) → PRs hebdo pour les actions.
2. Plus tard : Dependabot Gradle ou scan CVE (ex. dependency-check) en **rapport** puis en garde.
3. Garder hors CI : secrets (`keystore`, `local.properties`) — déjà dans `.gitignore` / `SECURITY.md`.

## Phase 4 — plus corsé (optionnel)

- Tests instrumentés / emulator (lent, coûteux) — seulement si vraiment utile.
- Vérif ProGuard/R8 release **unsigned** (`assembleRelease` sans keystore) pour détecter les shrink breaks.

## Ce qu’on ne fait pas pour l’instant

- Exiger 0 warning Lint
- Emulator sur chaque push
- Scan CVE bloquant
- Couverture de code obligatoire

Quand tu voudras passer à la phase suivante, dis-le : on active **un** contrôle, on corrige, on commit, puis on attend.
