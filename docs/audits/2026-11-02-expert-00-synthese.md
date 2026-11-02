# Synthèse expert-00 — P0 dédupliqués · plan vagues (7.07)

| Champ | Valeur |
|-------|--------|
| Date | 2026-11-02 |
| Sources | expert-01…06 @ `0.1.0-7.06` · Athalia Accueil « quasi pas changé » |
| Cible version | `0.1.0-7.07` |
| Nature | Synthèse + plan d’exécution (pas audit device) |

---

## Notes /10 (rappel)

| # | Domaine | Note | Signal |
|---|---------|-----:|--------|
| 01 | Accueil sticky | **4,5** | Sticky ~230–285 dp Ultra ; liste étouffée |
| 02 | UI globale | **7,5** craft | Fiche + Aide longues ; ombre Dossiers |
| 03 | Fonctions | **7,4** | Smoke / restore / notifs OEM ouverts |
| 04 | Fiche forms | **5,5** | Spacer ~172 + grille 31 viewport |
| 05 | Nav / sheets | **8,2** | Copy Aide ≠ Menu Dossiers |
| 06 | A11y polish | **8,7** craft | Smoke S26 / Device Care ouverts |

---

## Top P0 dédupliqués (code + doigt)

| Id | P0 | Source | Vague |
|----|----|--------|-------|
| **P0-STICKY** | Sticky Accueil ≤ **~110–140 dp** Ultra (pack **B** : densités agressives + hero 1 ligne + statut hors sticky + **outils hors sticky**) | 01 · 02 P0-MAIN | **Accueil** |
| **P0-FICHE-SPACER** | Retirer spacer mort ~172 dp entre grille et Fréquence | 04 | **Fiche** |
| **P0-FICHE-GRID** | Grille jours moins viewport-eating (collapse / chips) | 04 | **Fiche** |
| **P0-AIDE** | Aide plus courte ; chemins Menu/Réglages exacts | 02 · 05 | **UI/Aide/Nav** |
| **P0-SMOKE-*** | Re-smoke Accueil / résumé / restore / notifs / S26 | 03 · 06 | Athalia (hors code) |

**Hors vague code 7.07 :** restore `.mmc`, Device Care OEM, PDF résumé doigt, S26 fontScale — restent smoke Athalia.

---

## Plan vagues (ordre)

### Vague Accueil (PRIORITÉ) — pack B expert-01

1. Forcer densités sticky « liste d’abord » **même** si hauteur > 800 (Ultra).
2. Hero Reste **1 ligne** (montant dominant ; libellé court ; TalkBack complet).
3. Statut « Factures ouvertes » **hors** sticky (tête de liste).
4. Courses \| Mettre de côté **hors sticky** → Menu QuietLinks (capacité préservée).
5. Détails = QuietLink (disclosure).

**Cible estimation pliée Ultra :** ~110–140 dp (Détails fermés).

### Vague Fiche

1. Spacer intermédiaire → **0** (garder clearance bas sticky seule).
2. Grille jours **repliée** si jour déjà choisi (chip « Jour N » → dépli).

### Vague UI / Aide / Nav (P0–P1 safe)

1. Corps Aide raccourcis ; Courses/épargne = Menu ; Dossiers conditionnel.
2. Alertes : chemin hub réel (Confort) si mentionné.
3. Dossiers : ombre → **filet** seul (comme Accueil).

---

## Garde-fous CHARTE

Carnet crème · pas purple · pas logo sticky · FAIT forêt · outils ≥ 48 dp · serif = chiffre Reste seul.
