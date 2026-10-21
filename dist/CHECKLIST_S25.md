# Checklist validation Samsung (One UI)

> **Figé le 2026-10-19, version 5.4.1.** Ce n’est pas la preuve du 6.36. Smoke actuel : `PROGRESS_LOG.md` et `docs/AUDIT_STATUS.md`.

APK familial : `dist/MonMoisClair-release.apk` (éviter le debug au quotidien)

## Appareil validé (2026-10-19)

| Champ | Valeur |
|-------|--------|
| Modèle adb | `SM-S938B` (Galaxy **S25 Ultra** — libellé système « S25 Ultra de Athalia ») |
| Serial | non recopié (appareil personnel) |
| Android | 16 (API 36) |
| Compte | user 0 (pas Secure Folder) |
| Version app | `0.1.0-5.4.1` (historique : 5.4 / 4.6 / 3.5.1) |

### Résultats automatisés / observés

| Check | Résultat |
|-------|----------|
| `adb install -r` | Success |
| Lancement `MainActivity` | Resumed, pas de crash |
| SAF → dossier (ex. `Teste/`) | OK |
| Fichiers `mon-mois-clair-backup-1..3.json` | Présents + rotation |
| Home reste-à-vivre + liste | OK |
| Toggle FAIT | OK (encore à payer mis à jour) |
| Édition (tap ligne) | OK |
| Ajout entrée | OK (persistée + backup) |
| Écran Santé | 4× OK + hint Device Care |
| Écran Restore + confirm | OK |
| Réglages biométrie (dispo hardware) | OK (écran + libellé « Disponible ») |
| Hub Réglages (SAF / Restore / Device Care) | OK (Home allégé : Ajouter · Santé · Réglages) |
| Notifications | granted |
| Alarmes exactes | granted (`USE_EXACT_ALARM`) |
| Exemption batterie | OK (whitelist idle) |
| FATAL logcat | aucun |

> Note : le téléphone branché est un **S25 Ultra** (`SM-S938B`). La charte « cible S25 » reste valide.

Screenshots : `dist/verify/` (`v_sante_ok.png`, `home.png`, `restore_screen.png`, …).

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
