> **ARCHIVÉ** 2026-11-02 (fin de journée ~7.15) — ne plus lire pour l’état craft. Vivants : `visual-expert-10` · `brand-logo-unisson` · CHARTE / JOURNAL / PRIVACY / SECURITY / NOTICE / CI.

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

1. **QuietLink « Aussi dans Dossiers »** — masqué si `dossierCount == 0` (résumé ouvert pour déficit seul → plus de cul-de-sac Menu sans dossiers).
2. **Copy FR hors-brand** — messages share PDF / Dossiers / backup / restore : plus de jargon `.mmc` ni `≥ 6.81`.
3. **Aide « Résumé du mois »** — chemin Réglages d’abord ; Dossiers seulement si accord/courrier.

## Hors scope

Craft PNG / theme / launcher (7.15). Pas de redesign visuel.

## Preuves

- Tests unitaires : voir `PROGRESS_LOG` §7.15.1 (recompte `--rerun-tasks`).
- Version device : `0.1.0-7.15.1` · `versionCode` 121.
