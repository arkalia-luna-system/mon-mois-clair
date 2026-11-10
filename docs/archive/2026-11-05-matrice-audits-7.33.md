> **ARCHIVÉ** 2026-11-10 — remplacé par [`../audits/2026-11-09-matrice-audits-7.35.md`](../audits/2026-11-09-matrice-audits-7.35.md). Ne plus lire pour l’état courant.

# Matrice audits → statut code (7.33)

**Date :** 2026-11-05 · **Base :** `0.1.0-7.32` → ship `0.1.0-7.33`  
**Nature :** inventaire cross-audits + preuves code. **Smoke doigt Athalia = NON VÉRIFIÉ** (hors claim craft).

Légende : **FAIT** = code + tests / docs alignés · **PARTIEL** = code OK, preuve doigt ouverte · **NON FAIT** = hors code (humain) · **OBSOLÈTE** = remplacé / volontairement fermé.

---

## Vivants à garder

| Doc | Rôle |
|-----|------|
| [`2026-11-05-smoke-checklist-7.24.md`](2026-11-05-smoke-checklist-7.24.md) | Checklist doigt Athalia (cible 7.33) |
| [`2026-11-02-visual-expert-10.md`](2026-11-02-visual-expert-10.md) | Réf craft kit PNG 7.15 |
| [`../AUDIT_STATUS.md`](../AUDIT_STATUS.md) | Historique smoke Galaxy |
| CHARTE · PROGRESS · README · PLAY · IAU · NOTICE | Vivants produit |

---

## Synthèse domaines (tous audits)

| Point / lot | Statut | Preuve / écart |
|-------------|--------|----------------|
| Kit brand launcher/splash/notif/mark (7.14→7.15) | **FAIT** | PNG kit · `visual-expert-10` · assets `brand-7.15/` |
| Accueil sticky basse + Courses/épargne 1 tap | **FAIT** | `HomeStickyHeader` · `StickyHeroTools` · CHARTE § Accueil + densités alignées 7.33 |
| FAIT / undo / blocage futur | **FAIT** | `ToggleExpenseDone` + tests |
| Retard ≠ épargne volontaire | **FAIT** | `ExpenseOverdue.VOLUNTARY_SAVINGS_LABEL` |
| Résumé texte/PDF + QuietLink Dossiers conditionnel | **FAIT** | Settings / Dossiers · purge share TTL |
| Restore copy FR sans jargon `.mmc` | **FAIT** | `RestoreMessages` · fin-journee-qa archivé |
| Settings hub 3 groupes · Aide chemins | **FAIT** | Hub + `HowItWorksCopy` |
| Offline · pas INTERNET · IAU Immediate | **FAIT** | Manifest `tools:node=remove` · `PlayInAppUpdater` |
| B4/B6/B8 templates / aperçu / accords | **FAIT** | Use cases 7.29–7.32 + tests |
| Alarmes cancelFor delete multi-mois | **FAIT** | 7.32 |
| Tips mist / Primary / bandeaux erreur | **FAIT** | 7.30–7.31 |
| Widget mist nuit + fallback lecture | **FAIT** | `CarnetWidgetText.unavailable()` 7.33 |
| startActivity réglages / share sans crash silencieux | **FAIT** | Permissions / Alertes / Accueil / Scan 7.33 |
| Smoke S26 / restore papier / notif réelle | **NON FAIT** | Athalia — checklist vivante |
| TalkBack device / Device Care doigt | **NON FAIT** | Athalia |
| Claim « 10/10 dans la main » | **NON FAIT** | Interdit sans smoke |
| brand-7.14 vectors / audits passage-final | **OBSOLÈTE** | Archivés 7.33 |
| Courses « hors sticky → Menu » (expert-01 Pack B) | **OBSOLÈTE** | Refusé Athalia · Accueil 1 tap tenu |
| Twin VM Add/Edit load (P2 archi) | **PARTIEL** | Volontaire charte · pas rewrite |
| EntryFormScaffold params | **PARTIEL** | Dettes archi acceptées post-GO |

---

## Archives 7.33 (ce ship)

| Action | Fichiers |
|--------|----------|
| → `docs/audits/archive/` | `brand-logo-unisson` · `fin-journee-qa` · `passage-final` |
| → `archive/assets-brand-7.14/` | kit travail 7.14 |
| → `archive/screenshots-7.14/` | captures adb 7.14 |

Audits déjà archivés (expert-00…06, premium, nuit, final) : **OBSOLÈTE** pour l’état craft — utiles en historique seulement.
