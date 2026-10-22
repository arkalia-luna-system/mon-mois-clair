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

**Bruit Dependabot « Updates » (security) :** GitHub peut lancer un job qui tente de
patcher des jars **transitif** (netty, bouncycastle, protobuf…) venant de l’AGP /
des plugins Google — **pas** de nos `implementation` directs. Le job échoue alors
avec `dependency_not_found` dans le snapshot. **Ce n’est pas le workflow CI**
(`ci.yml` / tests unitaires). Ne pas confondre avec un build rouge : la garde
produit reste `:app:testDebugUnitTest`. Remède possible plus tard : monter AGP /
contraintes Gradle ciblées — pas un hotfix app.

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
