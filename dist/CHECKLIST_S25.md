# Checklist validation Samsung (One UI)

APK : `dist/MonMoisClair-debug.apk`

## Appareil validé (2026-10-19)

| Champ | Valeur |
|-------|--------|
| Modèle adb | `SM-S938B` (Galaxy **S25 Ultra** — libellé système « S25 Ultra de Athalia ») |
| Serial | `R3CY60BJ3ZM` |
| Android | 16 (API 36) |
| Compte | user 0 (pas Secure Folder) |
| Version app | `0.1.0-3.2` |

### Résultats automatisés / observés

| Check | Résultat |
|-------|----------|
| `adb install -r` | Success |
| Lancement `MainActivity` | Resumed, pas de crash |
| SAF → dossier (ex. `Teste/`) | OK |
| Fichiers `mon-mois-clair-backup-1..3.json` | Présents + rotation |
| `BackupWorker` | `Worker result SUCCESS` |
| Home reste-à-vivre + liste | OK (ex. 800 € / encore 500 €) |
| Notifications | OK |
| Alarmes exactes | OK |
| Exemption batterie | OK (whitelist idle) |
| Écran Santé | « Tout est prêt pour les rappels » |
| Hint Device Care Samsung | Affiché (à confirmer manuellement une fois) |

> Note : le téléphone branché est un **S25 Ultra** (`SM-S938B`), pas un S26. La charte « cible S25 » reste valide ; One UI Ultra se comporte de la même famille.

---

## Installation (autre téléphone)

1. Autoriser sources inconnues / USB debug.
2. `adb install -r dist/MonMoisClair-debug.apk`
3. Ouvrir Mon Mois Clair.

## Dossier sauvegarde (SAF)

1. « Choisir un dossier » → **Documents** (pas Download — Android refuse souvent Download).
2. Créer/choisir un dossier dédié (ex. `MonMoisClair`).
3. Vérifier `mon-mois-clair-backup-1.json` après une action.

## Batterie + Device Care (critique One UI)

1. Autoriser notifications.
2. Autoriser alarmes exactes.
3. Désactiver optimisation batterie (dialogue système).
4. **Device Care** : Paramètres → Batterie → Limites d’utilisation en arrière-plan  
   - Retirer Mon Mois Clair de « Mise en veille » / « Mise en veille profonde ».
5. Écran « Santé des rappels » : tout OK.
