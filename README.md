# Mon Mois Clair

> Suivi des paiements mensuels — **local, privé, hors ligne**  
> Kotlin · Jetpack Compose · Room · Samsung Galaxy (One UI)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2026%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![CI](https://github.com/arkalia-luna-system/mon-mois-clair/actions/workflows/ci.yml/badge.svg)](https://github.com/arkalia-luna-system/mon-mois-clair/actions/workflows/ci.yml)
[![Tests](https://img.shields.io/badge/tests-281%2F281-success)](#statut)
[![Device](https://img.shields.io/badge/validé-S25%20Ultra%20(SM--S938B)-success)](docs/AUDIT_STATUS.md)
[![Version](https://img.shields.io/badge/version-0.1.0--6.54-informational)](#statut)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Application Android pour gérer revenus et dépenses du mois **sans banque, sans cloud, sans compte**.  
Saisie manuelle · backup local (JSON + archive ZIP avec photos) · rappels exacts · distribution familiale par APK.

> **Statut dépôt :** privé (usage familial). README / charte / tests tenus à jour comme pour un dépôt public.

---

## Statut

| Élément | État |
|---------|------|
| Version | `0.1.0-6.54` |
| Dernière étape | **6.54** Photo+PDF bouton, verrou anti-tap, PDF busy — suite **6.53** |
| Device | Smoke **6.39–6.42** puis, le 2026-10-22, **S25 Ultra** (`SM-S938B`) : accueil nuit, widget posé, OCR `+++`, PDF à côté d’une photo (retiré, rien enregistré). S26 pas en main. Photo de loyer3 illisible |
| Distribution | Sideload familial **release signé** (hors Play Store) — voir [`docs/DISTRIBUTION_FAMILLE.md`](docs/DISTRIBUTION_FAMILLE.md) |
| Tests unitaires | **281/281** OK (`--rerun-tasks`) |
| Schéma Room | **8** — `expense_fee_events` (+ 7 : courriers ; 6 : IBAN / e-mail / cumul frais) |

Journal : [`PROGRESS_LOG.md`](PROGRESS_LOG.md) · Charte : [`CHARTE_ET_ARCHITECTURE.md`](CHARTE_ET_ARCHITECTURE.md) · Docs : [`docs/README.md`](docs/README.md) · Benchmark : [`docs/BENCHMARK_QUALITE_2026.md`](docs/BENCHMARK_QUALITE_2026.md) · Audit dossiers (figé) : [`docs/archive/AUDIT_ALEX_MAMAN_2026-10-21.md`](docs/archive/AUDIT_ALEX_MAMAN_2026-10-21.md) · Audit visuel (figé) : [`docs/archive/AUDIT_PREMIUM.md`](docs/archive/AUDIT_PREMIUM.md)

---

## Pourquoi

Un bloc-notes ne suffit pas pour voir clairement **ce qu’il reste à vivre** et **ce qui reste à payer**.  
Mon Mois Clair reste sur l’appareil : zéro serveur tiers, zéro connexion bancaire.

---

## Fonctionnalités

| Fonction | État |
|----------|------|
| **Reste après factures** sticky | ✅ |
| **Courses / divers** (+/−) | ✅ |
| **En retard** (badge, pas des capitales) + relance | ✅ |
| **Prélèvement auto** (icône + notif) | ✅ |
| **Récurrence** une fois / mois / N mois / an | ✅ |
| **Liste chronologique** du mois | ✅ |
| **FAIT ✓** (confirm sur mois passé) | ✅ |
| **Ajout / édition / suppression** | ✅ |
| **Backup SAF** (3 versions, SHA-256) | ✅ (validé device) |
| **Restaurer** (dernière sauvegarde / JSON ou archive ZIP) | ✅ |
| **Biométrie** + `FLAG_SECURE` multitâche | ✅ |
| **Hub Réglages** (aide · bio · Santé · échéances · sauvegarde) | ✅ |
| **Menu** (calendrier mois + outils, barre basse) | ✅ |
| **+ Ajouter** (barre pouce) | ✅ |
| **FAIT le JJ/MM** (preuve) | ✅ |
| **Jour 1–31** (grille, pas le clavier) | ✅ |
| **Menu · Alertes** (notifs / alarmes exactes) | ✅ |
| **Revenir au mois en cours** (Menu) | ✅ |
| **Rappels** J−3 / Jour J / overdue + reconcile update/heure/feu | ✅ |
| **Prochaine échéance (mois)** trimestriel / annuel | ✅ |
| **Partage copie de sécurité** (WhatsApp / Mail) | ✅ |
| **Onboarding** SAF + permissions + Santé | ✅ |
| **Home freeze** (pas de nouvelles options quotidiennes) | ✅ |
| **Comment ça marche** (Réglages → aide) | ✅ |
| **Scanner facture** (QR EPC Belgique) | ✅ |
| **Communication + Copier** (banque, mod 97) | ✅ |
| **Photo facture** (WebP SAF) + courriers en plus (accord) | ✅ |
| **PDF courrier** (ajout + édition, toute facture) | ✅ |
| **PDF du résumé** (Mes dossiers / Réglages) | ✅ |
| **Widget** (Reste + prochaine ligne) | ✅ |
| **Encre de nuit** (suit One UI / Réglages Apparence) | ✅ |
| **Lire +++ sur la photo** (OCR, jamais le montant) | ✅ |
| **Accord / retard** (replié, pastille, reste dû, arrêt si soldé) | ✅ |
| **IBAN · Écrire · frais** (fiche accord) | ✅ |
| **Bandeau « mois prochain »** (périodiques) | ✅ |
| **Mes dossiers** (Menu si N>0 ou courriers) | ✅ |
| **Courriers sans plan** (bandeau + conversion) | ✅ |
| **Mettre de côté** (Réglages, facture mensuelle) | ✅ |
| **Résumé à envoyer** (Mes dossiers / Réglages) | ✅ |
| **Phrase rythme** (fiche / Mes dossiers, avec « si ») | ✅ |

---

## Stack

| Couche | Choix |
|--------|--------|
| Langage / UI | Kotlin + Jetpack Compose |
| Persistance | Room (SQLite) |
| Async | Coroutines + Flow |
| DI | Hilt |
| Backup | SAF + WorkManager |
| Rappels | `AlarmManager` exact + BootReceiver |
| Fuseau | `Europe/Brussels` |
| Package | `be.monmoisclair.app` |

---

## Avancement

Le parcours familial est en place : mois, reste, FAIT, rappels, sauvegarde, facture papier.

```text
1–5       socle, ledger, rappels, sauvegarde, aide     ✅
6.0–6.29  scan, photo, Galaxy, audit premium           ✅
6.30      Lot A — mémoire papier (note/photo modèle)   ✅
6.31      Lot B — dossier / pastille Accord            ✅
6.32      Lot C — IBAN, mail, frais, courriers         ✅
6.33      Mes dossiers (liste) · EuroFormat · mailto   ✅
6.34      Lot D — courriers sans plan (schéma 7)       ✅
6.35      Lot E — Mettre de côté                       ✅
6.36      Lot F — résumé texte                         ✅
6.37      Phrase rythme dossier (si / environ)         ✅
Smoke « Écrire » + FAIT ≥ 48 dp (Ultra)                ✅ 2026-10-21
Hauteur courte type S26 (sim. 360×780 + texte 1,3)      ✅ 2026-10-21
```


---

## Build / install

```bash
export COPYFILE_DISABLE=1
# Sur le volume T7 : préférer ./scripts/git-safe.sh <commande git>
# (purge les sidecars AppleDouble ._ dans .git → évite « non-monotonic index » au push)
# Signature release (une fois) : copier keystore.properties.example → keystore.properties
# et générer release/monmoisclair-release.jks (jamais committer le .jks)
./gradlew :app:testDebugUnitTest --rerun-tasks
./gradlew :app:assembleRelease
adb install -r dist/MonMoisClair-release.apk
```

> `local.properties` / `keystore.properties` / `*.jks` jamais versionnés. APK dans `dist/` ignoré par git.

**CI (phase 1, léger) :** à chaque push sur **`develop`** (et `main` si merge explicite), GitHub Actions compile le debug et lance les tests unitaires.  
Feuille de route (lint / sécu plus tard) : [`docs/CI.md`](docs/CI.md). Travail quotidien = branche **`develop`** (charte §5).

---

## Confidentialité

- Données sur l’appareil + dossier Documents (SAF)
- JSON non chiffré (volontaire)
- Pas d’analytics / serveur / banque

Voir aussi [`SECURITY.md`](SECURITY.md).

---

## Licence

[MIT](LICENSE) — © 2026 Athalia / arkalia-luna-system.

---

<p align="center">
  <sub>Belgique · privacy-first · sideload familial · validé S25 Ultra</sub>
</p>
