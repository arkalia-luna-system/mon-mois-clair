# Vérif audits Accueil — 7.08

| Champ | Valeur |
|-------|--------|
| Date | 2026-11-02 |
| Version | `0.1.0-7.08` · `versionCode` 113 |
| Branche | `develop` |
| Nature | Checklist code + ship (pas smoke doigt Athalia) |
| Tests | **410** exécutés / 411 (1 skipped), 92 XML, 0 failure |

Sources relues puis archivées dans [`archive/`](archive/) : expert-01…06, accueil-sticky, aide, reglages-hub, retard-accord, resume-mois, premium-00 / clean-10. Synthèse plan : [`2026-11-02-expert-00-synthese.md`](2026-11-02-expert-00-synthese.md).

---

## Checklist P0 / P1 (fait / pas fait)

| Id | Claim | Statut code 7.08 | Preuve |
|----|-------|------------------|--------|
| **P0-STICKY** | Sticky basse · liste visible | **FAIT** | `DisplayFit` pad 6 / gap 2 / hero 34 ; statut hors sticky (tête liste) |
| **P0-OUTILS Accueil** | Courses \| épargne **1 tap Accueil**, pas Menu | **FAIT** | `StickyHeroTools` à **côté** du Reste (`HomeStickyHeader`) ; retirés de `HomeMenuSheet` |
| **P0-FICHE-SPACER** | Spacer ~172 retiré | **FAIT** | `EntryFormScaffold` commentaire 7.07 ; Spacer bas sticky seul |
| **P0-FICHE-GRID** | Grille jours repliable | **FAIT** | `DayOfMonthGrid` chip « Jour N ▾ » |
| **P0-AIDE** | Chemins exacts ; Courses ≠ Menu | **FAIT** | `HowItWorksCopy` « Accueil, à côté du Reste » |
| **P1-SHADOW Dossiers** | Filet seul | **FAIT** | `DossiersScreen` border, pas `.shadow` |
| **P0 retard épargne** | Jamais « En retard » | **FAIT** | `ExpenseOverdue.VOLUNTARY_SAVINGS_LABEL` |
| **P0 résumé share** | Boutons Réglages branchés | **FAIT** | `SettingsSummaryScreen` → share texte/PDF |
| **P0-SMOKE-*** | Doigt Accueil / restore / S26 | **PAS FAIT** | Athalia (hors code) |

---

## Placement Courses / épargne (7.08)

| | |
|--|--|
| Où | **Colonne droite du hero Reste** (même zone sticky), pas Menu, pas bandeau forcé sous sticky |
| Gestes | 1 tap › → sheet Courses / Mettre de côté |
| Sticky pliée estimée | mois 48 + hero/outils ~48–96 + Détails QuietLink si extras + pads ≈ **~110–160 dp** Ultra |
| Liste | Statut « Factures ouvertes » en tête de liste (scroll) |

---

## MD

| Action | Fichiers |
|--------|----------|
| **Gardés vivants** | `expert-00-synthese` · **ce fichier** · `screenshots/` · `smoke/` · CHARTE / PRIVACY / SECURITY / JOURNAL |
| **Archivés** | expert-01…06 · thématiques 2026-11-02 · premium 2026-11-01 → `docs/audits/archive/` |
