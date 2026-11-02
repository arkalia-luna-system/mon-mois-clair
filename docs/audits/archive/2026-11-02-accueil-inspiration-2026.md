> **ARCHIVÉ** 2026-11-02 (fin de journée ~7.15) — ne plus lire pour l’état craft. Vivants : `visual-expert-10` · `brand-logo-unisson` · CHARTE / JOURNAL / PRIVACY / SECURITY / NOTICE / CI.

# Accueil — inspirations budget 2025–2026 (étape 1)

**Date :** 2026-11-02 · **Produit :** Mon Mois Clair Accueil · **Autorisé :** Athalia — ne plus coller au « carnet papier belge » pour ce redesign.

Sources consultées (inspirations, **pas** copie pixel) :

- [ScreensDesign — Budget app dashboards](https://screensdesign.com/articles/budget-app-dashboard-design/) (EveryDollar, Budget app, SubPilot, BuyBye)
- [ScreensDesign — YNAB UI](https://screensdesign.com/showcase/ynab)
- [Priceless Tay — What Money Apps Look Like Inside](https://pricelesstay.com/compare/screens) (YNAB, Goodbudget, PocketGuard écrans marketing 2025–2026)
- [Emma 3.0 Designs](https://community.emma-app.com/t/emma-3-0-designs/5008) — core actions sur le 1er écran
- [Samsung One UI — basic layout / a11y typography](https://developer.samsung.com/one-ui/layout/basic.html)

---

## 7 patterns applicables (60+)

| # | Pattern | Vu chez | Application Accueil MMC |
|---|---------|---------|-------------------------|
| 1 | **Un seul chiffre-décision** (reste / left to spend / Ready to Assign) en hero, période au-dessus | YNAB, PocketGuard, EveryDollar, One UI viewing area | Hero **Reste** serif très fort ; mois en chip de navigation |
| 2 | **Langage humain**, pas jargon métier | YNAB (« Cost to Be Me », « Ready to Assign ») | Remplacer « Factures ouvertes » → **« Encore à payer »** |
| 3 | **Actions rapides à icône** près du hero (pas enterrées dans un menu) | Emma 3.0 (core sur 1er écran), bank homes One UI | Courses (chariot) + Mettre de côté (tirelire) = **chips icône** à côté du Reste |
| 4 | **Liste = zone d’interaction** sous une viewing area aérée | One UI (viewing vs interaction), SubPilot transactions | Sticky condensé ; cartes liste premium, bien visibles |
| 5 | **Cartes / focus blocks** avec air, coins souples, hiérarchie jour · titre · montant · action | Goodbudget envelopes, Budget app category cards | Cartes blanches sur canvas cool ; FAIT toujours 1 tap |
| 6 | **Typo : montant display + UI sans** ; contraste fort ; touch ≥ 48 | One UI large text, YNAB plan amounts | Source Serif 4 (déjà) pour le Reste ; Source Sans pour UI ; chips ≥ 48 dp |
| 7 | **Barre basse stable** Menu / + (interaction reachability) | One UI interaction area basse | Socle Accueil soft, pas sandwich crème/papier |

Hors scope volontaire : graphiques catégories, sync banque, net worth — le job Accueil reste **Reste + factures du mois + 2 outils + FAIT**.

---

## Direction visuelle Accueil (local, non-CHARTE carnet)

- **Canvas** cool mist (gris-bleu très clair) — **pas** crème papier, **pas** purple, **pas** cream/terracotta cliché IA.
- **Hero** carte blanche douce, montant ink ardoise, label « Reste » muted.
- **Outils** chips teintés (ciel Courses / sauge tirelire) avec pictos clairs.
- **Liste** cartes blanches, filet soft ou ombre très légère, pastilles statut conservées.
- **Tokens code :** `HomeTokens` (jour/nuit) — Accueil seulement ; le reste de l’app peut rester `A11yTokens` carnet.

Contraste cible : **≥ 7:1** texte courant / surfaces Accueil (même exigence familiale).
