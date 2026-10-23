# CI — feuille de route (progressive)

Principe : **une garde à la fois**. On active un contrôle seulement quand le code est déjà prêt
(ou après une petite session de nettoyage dédiée), pour éviter un mur de corrections.

## Phase 1 — build + tests (bloquant) ✅

Workflow : [`.github/workflows/ci.yml`](../.github/workflows/ci.yml)

À chaque push / PR sur **`develop`** (et `main` si merge explicite) :

- exécute `:app:testDebugUnitTest` (compile debug + ressources + tests — **bloquant**)
- `:app:lintDebug` soft (`continue-on-error`)
- **pas** d’`assembleDebug` en CI (APK/dex/strip trop lent pour le signal quotidien ; à lancer en local / release)
- accélération : `gradle/actions/setup-gradle` (cache) + `org.gradle.caching` / `parallel` **en env CI seulement**
- wrapper **Gradle 8.11.1** (AGP 8.7.3) — évite l’avis « 8.9 obsolète » de `setup-gradle`
- runner **`ubuntu-24.04`** (pin, pas `ubuntu-latest`)
- actions à jour (Node 24) : `setup-android@v4`, `upload-artifact@v7`
- **Pas** de `concurrency` : chaque push termine son run. Sinon GitHub annule
  (true = kill le run en cours ; false = kill les pending) et l’UI montre
  « 1 erreur / Canceling since a higher priority… » sans échec de test.

Travail quotidien = **`develop`** uniquement (voir charte §5).
Échec = le push casse le build ou les tests → à corriger avant de considérer la branche OK.

## Phase 2 — qualité (en cours, soft)

1. ✅ `:app:lintDebug` en CI avec `continue-on-error: true` (rapport artifact, **ne bloque pas**).
2. ⏳ Ensuite : corriger les findings utiles, puis éventuellement lint **bloquant**.
3. ⏳ Detekt (style / smells Kotlin) — règles soft au début — **pas encore**.

## Phase 3 — sécurité (soft) ✅ partiel

1. ✅ Dependabot Actions → PRs hebdo.
2. ✅ Dependabot Gradle → PRs hebdo (rapport / revue manuelle, pas de garde CI).
3. ✅ Automatic Dependency Submission (Gradle) : `gradlew` **doit** être exécutable dans git
   (`100755`). Sinon le job `submit-gradle` échoue avec `Permission denied` /
   « Gradle validation failed in the repository root ».
4. ⏳ Plus tard : scan CVE (ex. dependency-check) en **rapport** puis éventuellement en garde.
5. Garder hors CI : secrets (`keystore`, `local.properties`) — déjà dans `.gitignore` / `SECURITY.md`.

**Bruit Dependabot « Updates » (security) :** GitHub peut lancer un job nommé
`gradle in /. for com.google.protobuf…, io.netty…` qui tente de patcher des jars
**transitifs** (AGP / plugins Google) — **pas** nos `implementation` directs.
Échec typique : `dependency_not_found` dans le snapshot. **Ce n’est pas le
workflow CI** (`ci.yml` / tests). La garde produit reste `:app:testDebugUnitTest`.

Un `ignore:` dans `.github/dependabot.yml` **n’éteint pas** ces jobs Security Updates
(ils sont distincts des PRs « version updates » hebdo).

**Procédure Arkalia (GitHub UI)** — pour faire taire le rouge sans faux correctif app :

1. Ouvrir le dépôt → **Security** → **Dependabot** / alertes.
2. Pour une alerte sur `netty` / `protobuf` / `commons-io` / `bouncycastle` **sans**
   ligne directe dans `app/build.gradle.kts` : **Dismiss** (raison : transitive / AGP)
   ou désactiver **Dependabot security updates** si le bruit reste permanent.
3. Garder Dependabot **version updates** (Actions + Gradle) dans `dependabot.yml`.

**Mitigation repo (6.80+) :** `build.gradle.kts` racine force via `resolutionStrategy`
(transitifs UTP/AGP — **pas** dans l’APK runtime) :
`protobuf-java:3.25.9`, `commons-io:2.22.0`, `io.netty:*:4.1.139.Final`,
`bcprov-jdk18on:1.86`, `jdom2:2.0.6.1`, `jose4j:0.9.7`.
Vérifier avec `./gradlew :app:dependencies`. Si Dependabot Security reste rouge malgré
le force : dismiss UI (transitive) — la garde produit reste les tests. Alertes GitHub
peuvent rester jusqu’au **rescan** Dependabot même si le classpath local est patché.

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
