# Mon Mois Clair

> Suivi des paiements mensuels — **local, privé, hors ligne**  
> Kotlin · Jetpack Compose · Room · Samsung Galaxy (One UI)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2026%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![CI](https://github.com/arkalia-luna-system/mon-mois-clair/actions/workflows/ci.yml/badge.svg)](https://github.com/arkalia-luna-system/mon-mois-clair/actions/workflows/ci.yml)
[![Tests](https://img.shields.io/badge/tests-122%2F122-success)](#statut)
[![Device](https://img.shields.io/badge/validé-S25%20Ultra%20(SM--S938B)-success)](dist/CHECKLIST_S25.md)
[![Version](https://img.shields.io/badge/version-0.1.0--6.6-informational)](#statut)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Application Android pour gérer revenus et dépenses du mois **sans banque, sans cloud, sans compte**.  
Saisie manuelle · backup JSON local · rappels exacts · distribution familiale par APK.

> **Statut dépôt :** privé (usage familial). README / charte / tests tenus à jour comme pour un dépôt public.

---

## Statut

| Élément | État |
|---------|------|
| Version | `0.1.0-6.6` |
| Dernière étape | **6.6** Réglages utiles + navigation année |
| Device | Validé sur **Galaxy S25 Ultra** (`SM-S938B`) ; cible maman **S26 6,3″** |
| Distribution | Sideload familial **release signé** (hors Play Store) |
| Tests unitaires | **122/122** OK |

Journal : [`PROGRESS_LOG.md`](PROGRESS_LOG.md) · Charte : [`CHARTE_ET_ARCHITECTURE.md`](CHARTE_ET_ARCHITECTURE.md)

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
| **EN RETARD** + relance overdue | ✅ |
| **Prélèvement auto** (icône + notif) | ✅ |
| **Récurrence** une fois / mois / N mois / an | ✅ |
| **Liste chronologique** du mois | ✅ |
| **FAIT ✓** (confirm sur mois passé) | ✅ |
| **Ajout / édition / suppression** | ✅ |
| **Backup SAF** (3 versions, SHA-256) | ✅ (validé device) |
| **Restaurer** (dernière sauvegarde / fichier JSON) | ✅ |
| **Biométrie** + `FLAG_SECURE` multitâche | ✅ |
| **Hub Réglages** (aide · bio · Santé · échéances · sauvegarde) | ✅ |
| **Revenir au mois en cours** (historique) | ✅ |
| **Rappels** J−3 / Jour J / overdue + reconcile update/heure/feu | ✅ |
| **Prochaine échéance (mois)** trimestriel / annuel | ✅ |
| **Partage copie de sécurité** (WhatsApp / Mail) | ✅ |
| **Onboarding** SAF + permissions + Santé | ✅ |
| **Home freeze** (pas de nouvelles options quotidiennes) | ✅ |
| **Comment ça marche** (Réglages → aide) | ✅ |
| **Scanner facture** (QR EPC Belgique) | ✅ |
| **Communication + Copier** (banque) | ✅ |
| **Photo facture** (WebP SAF) | ✅ |
| **Bandeau « mois prochain »** (périodiques) | ✅ |

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

```text
[█████████████████████████]  ~95 %

1.x–3.5.1  socle + hub + bio + nav mois              ✅
4.0–4.6  EN RETARD · divers · auto · récurrence      ✅
5.0–5.4  freeze Home · T5 JSON R8 · ancre mois ·     ✅
         OVERDUE chain · partage WhatsApp/Mail       ✅
5.4.1  Aide « Comment ça marche » (Réglages)         ✅
     Validation S25 Ultra (SM-S938B)                 ✅
```


---

## Build / install

```bash
export COPYFILE_DISABLE=1
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
