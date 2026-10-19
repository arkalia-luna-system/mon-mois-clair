# Mon Mois Clair

> Suivi des paiements mensuels — **local, privé, hors ligne**  
> Kotlin · Jetpack Compose · Room · Samsung Galaxy (One UI)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2026%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Tests](https://img.shields.io/badge/tests-88%2F88-success)](#statut)
[![Device](https://img.shields.io/badge/validé-S25%20Ultra%20(SM--S938B)-success)](dist/CHECKLIST_S25.md)
[![Version](https://img.shields.io/badge/version-0.1.0--3.5.1-informational)](#statut)

Application Android pour gérer revenus et dépenses du mois **sans banque, sans cloud, sans compte**.  
Saisie manuelle · backup JSON local · rappels exacts · distribution familiale par APK.

---

## Statut

| Élément | État |
|---------|------|
| Version | `0.1.0-3.5.1` |
| Dernière étape | **3.5.1** Hub + bio + nav mois (Préc. lazy) |
| Device | Validé sur **Galaxy S25 Ultra** (`SM-S938B`, Android 16) — voir [checklist](dist/CHECKLIST_S25.md) |
| Distribution | Sideload familial (hors Play Store) |
| Tests unitaires | **88/88** OK |

Journal : [`PROGRESS_LOG.md`](PROGRESS_LOG.md) · Charte : [`CHARTE_ET_ARCHITECTURE.md`](CHARTE_ET_ARCHITECTURE.md)

---

## Pourquoi

Un bloc-notes ne suffit pas pour voir clairement **ce qu’il reste à vivre** et **ce qui reste à payer**.  
Mon Mois Clair reste sur l’appareil : zéro serveur tiers, zéro connexion bancaire.

---

## Fonctionnalités

| Fonction | État |
|----------|------|
| **Reste-à-vivre** sticky | ✅ |
| **Liste chronologique** du mois | ✅ |
| **FAIT ✓** (ligne reste en place) | ✅ |
| **Ajout / édition / suppression** | ✅ |
| **Backup SAF** (3 versions, SHA-256) | ✅ (validé device) |
| **Restaurer** (dernière sauvegarde / fichier JSON) | ✅ |
| **Biométrie optionnelle** (réglages) | ✅ |
| **Hub Réglages** (bio · Device Care · sauvegarde) | ✅ |
| **Double rappel** J−3 / jour J | ✅ moteur (Device Care à confirmer par usage) |
| **Onboarding** SAF + permissions + Santé | ✅ |

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
[█████████████████████]  ~80 %

1.x  Room / backup / restore                         ✅
2.1–2.3  Cycle / toggle / AlarmManager               ✅
2.4  Ajout / édition / suppression                   ✅
3.1  Onboarding SAF + permissions + Santé            ✅
3.2  Home liste + reste-à-vivre                      ✅
3.3  Restore UI (SAF + fichier JSON)                 ✅
3.4  Biométrie optionnelle (réglages)                ✅
3.5  Hub Réglages + Home allégé                      ✅
3.5.1 Nav mois Préc. lazy + titres FR + bio overlay  ✅
     Validation S25 Ultra (SM-S938B)                 ✅
```


---

## Build / install

```bash
export COPYFILE_DISABLE=1
./gradlew :app:testDebugUnitTest --rerun-tasks
./gradlew :app:assembleDebug
adb install -r dist/MonMoisClair-debug.apk   # après copie depuis outputs/
```

> `local.properties` jamais versionné. APK dans `dist/` ignoré par git (trop lourd) — rebuild local.

---

## Confidentialité

- Données sur l’appareil + dossier Documents (SAF)
- JSON non chiffré (volontaire)
- Pas d’analytics / serveur / banque

---

<p align="center">
  <sub>Belgique · privacy-first · sideload familial · validé S25 Ultra</sub>
</p>
