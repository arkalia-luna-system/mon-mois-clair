# CI — feuille de route (progressive)

Principe : **une garde à la fois**. On active un contrôle seulement quand le code est déjà prêt
(ou après une petite session de nettoyage dédiée), pour éviter un mur de corrections.

## Phase 1 — build + tests (bloquant) ✅

Workflow : [`.github/workflows/ci.yml`](../.github/workflows/ci.yml)

À chaque push / PR sur **`develop`** (et `main` si merge explicite) :

- exécute `:app:testDebugUnitTest` (compile debug + ressources + tests — **bloquant**)
  avec **retry ×3** (pause 15/30 s) — mitige le flake Robolectric
  `MavenArtifactFetcher` / Maven Central **429** (ex. `PaymentNotificationIconsTest`
  `classMethod` IOException) ; ce n’est **pas** un bug app
- `:app:lintDebug` soft (`continue-on-error`)
- **pas** d’`assembleDebug` en CI (APK/dex/strip trop lent pour le signal quotidien ; à lancer en local / release)
- accélération : `gradle/actions/setup-gradle` (cache) + `org.gradle.caching` / `parallel` **en env CI seulement**
- wrapper **Gradle 8.14.4** (AGP **8.7.3**, min AGP = 8.9) — avis « 8.11.1 obsolète » levé
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

**Mitigation repo (6.80+) :** `build.gradle.kts` + `settings.gradle.kts` forcent via
`resolutionStrategy` (transitifs UTP/AGP — **pas** dans l’APK runtime) :
`protobuf-java:3.25.9`, `commons-io:2.22.0`, `commons-compress:1.26.0`,
`commons-lang3:3.18.0` (**#60**), `guava:33.7.2-jre` (**#61** CVE-2026-102554),
`io.netty:*:4.1.139.Final`, `bcprov-jdk18on:1.86`, `bcpkix-jdk18on:1.86`,
`jdom2:2.0.6.1`, `jose4j:0.9.7`.

**Alerte #54 `kotlin-gradle-plugin` (CVE-2026-53914 / GHSA-r937-wjx7-w2jp) :**
premier patch Dependabot = **`2.4.20-Beta1`** (désérialisation cache build Kotlin).
**Rescan 2026-11-01 (7.01) :** advisory GitHub / OSV listent **toutes** les versions
`&lt; 2.4.20-Beta1` (dont **2.0.21** et **2.1.x**) — **aucun backport** 2.0.x / 2.1.x.
Politique JetBrains « security support » = stdlib **2.4+** seulement, pas le
gradle-plugin des lignes antérieures. Bump Kotlin 2.4.x exige KSP 2.3 + Room 2.8 +
Hilt ≥2.59 → **AGP 9** — **incompatible** stack actuelle (AGP **8.7.3**, Kotlin /
KSP / Compose plugin **2.0.21**, Hilt **2.52**). Tentative bump **non safe**
aujourd’hui (casserait le build). **Pin documenté :** rester **2.0.21** jusqu’à
migration AGP 9 coordonnée. Risque limité au **classpath build** (pas APK
runtime ; CVSS local + privilèges élevés).
**2026-11-02 (7.05) :** alerte GitHub **dismissed** (`tolerable_risk`) +
`dependabot.yml` ignore Kotlin/KSP/Compose plugin **≥2.1** (target `develop`).
PR bot Kotlin **2.4.21** / Gradle wrapper **9.8.2** **closes** (AGP 8).
Revisite uniquement avec migration AGP 9.

**Alerte #61 `com.google.guava:guava` (CVE-2026-102554 / GHSA-xxph-c9ww-hj94) :**
HIGH — CompactHashMap/CompactHashSet/MapMakerInternalMap deserialization OOM.
Vulnérable `≥4.0` / `≤33.7.1` ; premier patch **`33.7.2`** (`33.7.2-jre` /
`33.7.2-android`). Force classpath build via `settings.gradle.kts` →
`33.7.2-jre` (7.01). Pas d’`implementation` directe Guava dans l’APK.

Vérifier avec `./gradlew :app:dependencies`. Alertes GitHub peuvent rester rouges
jusqu’au **rescan** Dependabot même si le force local est OK.

## Publish Play Internal (push `main`)

Workflow : [`.github/workflows/publish-play-internal.yml`](../.github/workflows/publish-play-internal.yml)

- Déclenché sur **push `main`** (+ `workflow_dispatch` avec `confirm=PUBLISH`).
- Build `bundleRelease` signé → upload piste **`internal` uniquement** (`r0adkll/upload-google-play`, `status: completed`).
- **Jamais** Production.
- Push **sans** fichier release (`app/`, Gradle, wrapper) → job upload **skippé** (run vert) : évite un second upload du même `versionCode` après un ff `main` docs-only.
- Sans les 5 secrets → skip propre (pas de rouge). Guide + noms exacts : [`PLAY_INTERNAL_TESTING.md`](PLAY_INTERNAL_TESTING.md) § CI auto-deploy.
- Bump `versionCode` **sur `develop` avant** merge `main` (pas d’auto-bump CI).

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
