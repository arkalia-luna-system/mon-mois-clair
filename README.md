# Mon Mois Clair

> Suivi des paiements mensuels — **local, privé, hors ligne**  
> Kotlin · Jetpack Compose · Room · Samsung Galaxy (One UI)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2026%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Tests](https://img.shields.io/badge/tests-59%2F59-success)](#statut)
[![Statut](https://img.shields.io/badge/statut-en%20construction-orange)](#statut)

Application Android pour gérer revenus et dépenses du mois **sans banque, sans cloud, sans compte**.  
Saisie manuelle · backup JSON local · rappels exacts · distribution familiale par APK.

---

## Statut

| Élément | État |
|---------|------|
| Version | `0.1.0-2.3` |
| Dernière étape terminée | **2.3** — AlarmManager exact + BootReceiver + santé rappels (API) |
| Prochaine étape | **3.1** — UI Compose (Home / Month) |
| UI Compose | Pas encore |
| Distribution | Sideload familial (hors Play Store) |
| Tests unitaires | **59/59** OK (Gradle `--rerun-tasks` avant push) |

Journal : [`PROGRESS_LOG.md`](PROGRESS_LOG.md) · Charte : [`CHARTE_ET_ARCHITECTURE.md`](CHARTE_ET_ARCHITECTURE.md)

---

## Pourquoi

Un bloc-notes ne suffit pas pour voir clairement **ce qu’il reste à vivre** et **ce qui reste à payer**.  
Mon Mois Clair reste sur l’appareil : zéro serveur tiers, zéro connexion bancaire.

---

## Fonctionnalités (cible)

| Fonction | Description |
|----------|-------------|
| **Reste-à-vivre** | Revenus − sorties prévues + montant encore à payer |
| **Liste chronologique** | Une seule timeline du 1 au dernier jour du mois |
| **FAIT ✓** | La ligne reste en place (vert doux), pas de disparition |
| **Double rappel** | J−3 à 10:00 et jour J à 09:00 (`Europe/Brussels`) |
| **Backup SAF** | JSON clair, 3 versions rotatives, checksum SHA-256 |
| **Droit à l’erreur** | Modifier / supprimer ; option « mois suivants » si template |

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

## Décisions verrouillées

| ID | Décision |
|----|----------|
| **A** | Kotlin natif + Room + Compose — aucune surcouche hybride |
| **B** | SQLite = source de vérité + auto-backup JSON clair (pas Keystore) |
| **C** | `AlarmManager` exact + boot + écran « Santé des rappels » (Samsung) |
| **D** | Sideload APK familial |
| **E** | Ouverture directe ; biométrie optionnelle dans les réglages |

---

## Avancement technique

```text
[██████████████░░░░░░]  ~40 %  (data + backup + cycle + toggle + rappels)

1.1  Scaffold Room / Hilt / MutatingRepository     ✅
1.2  SAF auto-backup + rotation + checksum           ✅
1.3  Restore depuis backup                          ✅
2.1  MonthCycleEngine (templates → instances)       ✅
2.2  Toggle FAIT (courant + précédent)              ✅
2.3  AlarmManager + Boot + ReminderHealth           ✅
3.1  UI Compose Home / Month                         ⏳
```

---

## Build local

Prérequis : Android Studio / SDK 35, JDK 17.

```bash
export COPYFILE_DISABLE=1
./gradlew :app:testDebugUnitTest --rerun-tasks
./gradlew :app:assembleDebug
```

> `local.properties` (chemin SDK) reste local — jamais versionné.

---

## Confidentialité

- Données **uniquement** sur l’appareil + dossier Documents choisi (SAF)
- Export JSON **non chiffré** (volontaire : récupérable si le téléphone casse)
- Pas de analytics, pas de serveur, pas d’API bancaire

---

## Licence

Usage personnel / familial pour l’instant. Licence ouverte à définir si le dépôt passe en public.

---

<p align="center">
  <sub>Belgique · privacy-first · sideload familial</sub>
</p>
