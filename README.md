# Mon Mois Clair

**Le carnet du mois, sur le téléphone — pas une banque.**

Reste après factures · FAIT ✓ · rappels exacts · photo & PDF · **100 % hors ligne**.  
Pas de compte. Pas de cloud. Pas de pub. Sideload familial (Galaxy / One UI).

[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2026%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![CI](https://github.com/arkalia-luna-system/mon-mois-clair/actions/workflows/ci.yml/badge.svg)](https://github.com/arkalia-luna-system/mon-mois-clair/actions/workflows/ci.yml)
[![Tests](https://img.shields.io/badge/tests-346%2F346-success)](#statut-technique)
[![Device](https://img.shields.io/badge/validé-S25%20Ultra%20(SM--S938B)-success)](docs/AUDIT_STATUS.md)
[![Version](https://img.shields.io/badge/version-0.1.0--6.84-informational)](#statut-technique)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Un bloc-notes ne suffit pas pour voir **ce qu’il reste à vivre** et **ce qui reste à payer**.  
Mon Mois Clair reste sur l’appareil : saisie manuelle, sauvegarde locale (JSON + ZIP photos), distribution par APK.

Bug, idée, question → Accueil → **Menu** → **Arkalia Système**.

> Dépôt privé (usage familial). Docs / charte / tests tenus comme un dépôt public.

---

## Ce que l’app fait

| | |
|--|--|
| **Reste sticky** | Gros chiffre du mois · À payer · Courses · Mettre de côté |
| **Factures** | Récurrence · FAIT le JJ/MM · En retard · Prélèvement · grille jour 1–31 |
| **Papier belge** | QR EPC · communication mod 97 · photo WebP · PDF courrier · OCR `+++` |
| **Accords** | Pastille · reste dû · IBAN · Écrire · frais datés · Mes dossiers |
| **Sérénité** | Backup SAF · biométrie · `FLAG_SECURE` · widget · encre de nuit · Alertes |

Hors scope volontaire : banque, catégories, graphiques, synchro, Play Store.

---

## Confidentialité

Voir **[`PRIVACY.md`](PRIVACY.md)** — pas d’`INTERNET`, backups SAF, presse-papiers sensible.  
Sécurité / keystore : [`SECURITY.md`](SECURITY.md).

---

## Statut technique

| Élément | État |
|---------|------|
| Version | `0.1.0-6.84` |
| Dernière étape | **6.84** Perfection code (filets · anti-doublon Add/Edit · découpe god-files · sécu résiduelle) |
| Device | Smoke S25 Ultra partiel. **S26 / restore doigt : à faire Athalia** — pas claim « 9/10 dans la main » |
| Distribution | Sideload **release signé** — [`docs/DISTRIBUTION_FAMILLE.md`](docs/DISTRIBUTION_FAMILLE.md) |
| Tests unitaires | **346/346** OK (`--rerun-tasks`, 84 fichiers XML) |
| Schéma Room | **8** |

Journal (détail) : [`PROGRESS_LOG.md`](PROGRESS_LOG.md) · Charte : [`CHARTE_ET_ARCHITECTURE.md`](CHARTE_ET_ARCHITECTURE.md) · Docs : [`docs/README.md`](docs/README.md) · Benchmark : [`docs/BENCHMARK_QUALITE_2026.md`](docs/BENCHMARK_QUALITE_2026.md) · Contribuer : [`CONTRIBUTING.md`](CONTRIBUTING.md)

Archives figées (historique) : [`docs/archive/`](docs/archive/) — noms de personas d’époque ; le contact vivant = **Arkalia**.

---

## Stack

Kotlin · Jetpack Compose · Room · Hilt · SAF + WorkManager · `AlarmManager` exact · `Europe/Brussels` · `be.monmoisclair.app`

---

## Build / install

```bash
export COPYFILE_DISABLE=1
# Volume T7 : préférer ./scripts/git-safe.sh <commande git>
./gradlew :app:testDebugUnitTest --rerun-tasks
./gradlew :app:assembleRelease
adb install -r dist/MonMoisClair-release.apk
```

`local.properties` / `keystore.properties` / `*.jks` jamais versionnés. APK dans `dist/` ignoré.  
CI : push **`develop`** → compile + tests. Feuille de route : [`docs/CI.md`](docs/CI.md).

---

## Licence

[MIT](LICENSE) — © 2026 **Arkalia Luna** / [arkalia-luna-system](https://github.com/arkalia-luna-system).

<p align="center">
  <sub>Belgique · local-first · sideload familial · validé S25 Ultra</sub>
</p>
