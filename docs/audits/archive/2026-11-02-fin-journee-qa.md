> **ARCHIVÉ** 2026-11-05 (7.33) — craft clos · ne plus lire pour l’état vivant. Vivants : `visual-expert-10` · smoke-checklist · CHARTE / JOURNAL / README.

# Fin de journée QA — 2026-11-02 · 7.15.1

Audit code chemins critiques (pas redesign). Device SM-S938B, install release sans wipe.

## Chemins vérifiés (code)

| Chemin | Verdict | Notes |
|--------|---------|--------|
| **FAIT** | OK | `requestToggleDone` / undo / blocage aperçu futur ; dialogue mois passé |
| **Share résumé** | OK | Texte + PDF Réglages & Dossiers ; `purgeStale` share/ (pas wipe ON_STOP) ; note ≠ sauvegarde |
| **Restore** | OK | Cleartext rejetée → message plain FR ; sans `.mmc` / `6.81` |
| **Courses / épargne Accueil** | OK | Sheets sticky ; `ExpenseOverdue` ignore « Mettre de côté » |
| **Settings hub** | OK | Essentiel → Code/copie ; Données (échéances, résumé si utile) ; Confort = Apparence+Alertes |

## P0 corrigés (7.15.1)

1. **QuietLink « Aussi dans Dossiers »** — masqué si `dossierCount == 0` (résumé pour déficit seul → plus de cul-de-sac).
2. **Copy FR hors-brand** — share PDF / Dossiers / backup / restore : plus de jargon `.mmc` ni `≥ 6.81`.
3. **Aide « Résumé du mois »** — Réglages d’abord ; Dossiers seulement si accord/courrier.

## Hors scope

Craft PNG / theme / launcher (7.15). Pas de redesign visuel.

## Preuves

- Tests après patch copy : **414** exécutés / 415 (1 skipped). Après hygiène code mort : **413** / 414 (1 skipped), 93 XML, 0 failure (`--rerun-tasks`).
- Device : `0.1.0-7.15.1` · `versionCode` 121 · `adb install -r` OK (SM-S938B, sans wipe).
