# Benchmark qualité — Mon Mois Clair face aux apps 2026

Date : 2026-10-22 (état code : **6.52**).  
App lue au départ : `0.1.0-6.41`. Le dépôt a avancé : formes **6.43**, PDF facture **6.44/6.49**, nuit **6.44/6.46**, polish **6.45**, OCR **6.47**, PDF résumé **6.48/6.51**, widget **6.49/6.50**, finitions **6.52**.  
Décisions vivantes : [`CHARTE_ET_ARCHITECTURE.md`](../CHARTE_ET_ARCHITECTURE.md) · [`PLAN_PRODUIT_FINI.md`](PLAN_PRODUIT_FINI.md).

| Statut | Contenu |
|--------|---------|
| **Fait** | `tnum`, fond `#F6F3E8`, cartes 16 dp + ombre, pastilles capsule, boutons 12 dp, socle `PaperWarm`, FAIT forêt, fondu mois 180 ms, ombre sticky, fondu couleur ligne FAIT ~150 ms |
| **Fait** | PDF à côté de la photo — **toute** facture (ajout + édition, retry si échec) |
| **Fait** | Encre de nuit + choix Clair / Sombre / téléphone (Réglages) + cache 1er frame |
| **Fait** | OCR `+++` sur la photo — jamais le montant |
| **Fait** | PDF du résumé (même texte, gros caractères 18/22 pt — 6.51) |
| **Fait** | Widget : Reste + prochaine ligne non FAIT (crème/encre fixes) |

Ce n’est pas un plan à coder. Lots A→F et feedback 6.39+ : [`PLAN_PRODUIT_FINI.md`](PLAN_PRODUIT_FINI.md), [`archive/FEEDBACK_TEST_FAMILLE_2026-10-21.md`](archive/FEEDBACK_TEST_FAMILLE_2026-10-21.md). Audit dossiers (figé) : [`archive/AUDIT_ALEX_MAMAN_2026-10-21.md`](archive/AUDIT_ALEX_MAMAN_2026-10-21.md). Recherche figée : [`archive/RECHERCHE_OUTILS_2026-10-21.md`](archive/RECHERCHE_OUTILS_2026-10-21.md).

---

## 1. Comment lire

Deux familles d’apps, pas une.

| Famille | À quoi elles servent | Exemples lus | Rapport avec nous |
|---------|----------------------|--------------|-------------------|
| **Argent complet** | Banque branchée, catégories, patrimoine, souvent un abonnement | Monarch, YNAB, Copilot, Origin | Barème **visuel** et « objet fini ». Mauvais modèle de fonctions |
| **Carnet de factures** | Rappels, récurrence, payé, souvent hors banque | Smart Bill Reminder, Billiary, Flying Bills, Bills Reminder & Organizer, AllPaid | Vrais pairs. C’est ici qu’un manque compte |

« Premium » pour maman sur un Galaxy n’est pas le look Copilot. Le bon barème : **un objet papier qui a l’air terminé**, calme, avec un seul chiffre qui commande.

Sources (pages publiques, octobre 2026) : NerdWallet / WalletHub best budget apps 2026 ; Origin bill apps guide ; Copilot design ; Smart Bill Reminder ; Billiary ; Flying Bills ; Bills Reminder & Organizer. Mint et Prism sont morts.

---

## 2. Ce que les meilleures font — et ce qu’on a déjà

### Argent complet (à ne pas copier comme produit)

| App | Ce qui fait sa note en 2026 | Ici |
|-----|------------------------------|-----|
| **Monarch / YNAB / Copilot / Origin** | Banque, catégories, patrimoine, coach | Hors charte. On garde « ouvrir = que reste-t-il ? » |

### Carnets de factures (pairs réels)

| Fonction des pairs | Eux | Mon Mois Clair |
|--------------------|-----|----------------|
| Sans compte, hors ligne | Oui | Oui, **sans pub** |
| Récurrence mois / N / an | Oui | Oui. Pas de semaine |
| FAIT depuis la notification | Oui | Oui + Remettre ~10 s |
| Photo de facture | Oui | Oui, WebP, suit le modèle |
| **PDF** collé à la facture | Oui | **Oui (6.44 + 6.49)** — ajout + édition, ouverture externe |
| Vue calendrier grille | Oui | Non — liste + Menu (volontaire) |
| Widget | Souvent premium | **Oui (6.49)** — Reste + prochaine ligne |
| OCR montant | Smart Bill | Non — on lit seulement `+++` (6.47) |
| Mode sombre | Oui | **Oui (6.44 + 6.46)** — encre de nuit ; choix Clair / Sombre / téléphone |
| Catégories / graphiques | Premium | Refusé |
| Export | JSON / CSV / PDF | JSON + ZIP photos ; résumé = texte **et** PDF (6.48) |

### Déjà au-dessus des carnets Android (belge)

Communication `+++` + mod 97 · QR → IBAN · reste d’accord aux centimes FAIT · courrier sans montant hors Reste · Mettre de côté · résumé texte/PDF · biométrie / `FLAG_SECURE` · contraste ≥ 7:1 · cibles ≥ 48 dp · widget local.

---

## 3. Fonctions — état réel

### Fait

| # | Sujet | Lot |
|---|--------|-----|
| 1 | PDF à côté de la photo (toute facture, ajout + édition) | **6.44 + 6.49** |
| 2 | OCR seulement la communication `+++` (jamais le montant) | **6.47** |
| 3 | Chiffres tabulaires `tnum` | **6.43** |
| 4 | Fondu ~180 ms au changement de mois | **6.43** |
| 5 | Widget : Reste + prochaine ligne non FAIT | **6.49** |
| 6 | PDF du résumé (même texte, gros caractères) | **6.48 + 6.51** |
| — | Encre de nuit | **6.44** |
| — | Choix Clair / Sombre / téléphone (Réglages) | **6.46** |

### Laisser dehors

Banque, catégories, graphiques, synchro cloud, thèmes fluo, OCR montant, snowball, grille calendrier.

---

## 4. Le visuel — état réel

Fond jour `#F6F3E8`, cartes `#FFFDF9`, encre `#1E1B18`, teal `#1B4D4E`. Nuit : fond encre, texte papier, **même** teal (`A11yTokens.Night`).

| Id | Constat d’origine | État code |
|----|-------------------|-----------|
| **V01** | Cartes = page | **Fait** — ombre 2 dp sur cartes + ombre sticky Reste |
| **V02** | Tout en 12 dp | **Fait** — boutons 12, cartes 16, pastilles `StampShape` |
| **V03** | Pastille = phrase | **Fait** — `labelLarge` + padding serré ; **18 sp mini** (a11y, pas plus petit) |
| **V04** | Barre = page | **Fait** — socle `PaperWarm` |
| **V05** | FAIT = secondaire | **Fait** — plein forêt ; Remettre en trait |
| **V06** | Chiffres qui dansent | **Fait** — `tnum` sur la typo |
| **V07** | Pas de mouvement | **Fait** — fondu mois 180 ms + couleur ligne FAIT ~150 ms |
| **V08** | Nuit = feuille allumée | **Fait** — encre de nuit + préférence app |

### Ne pas « embellir »

Pas de dégradé teal, pas d’icône seule Menu/FAIT, pas de 2ᵉ hero, pas de rouge sang, compact S26 = contrainte hauteur.

---

## 5. Ordre (historique)

1–4 formes → **6.43**. PDF facture → **6.44**. Nuit → **6.44/6.46**. Polish sticky/FAIT → **6.45**. OCR `+++` → **6.47**. PDF résumé → **6.48**, lecture 18/22 pt → **6.51**. Widget + PDF à l’ajout → **6.49**. Partage PDF (`ClipData`) + fonds widget fixes → **6.50**. Finitions retry PDF / cache thème / ZIP / docs → **6.52**.

---

## 6. Ce qui reste pour le tenir en main

Le carnet a les fonctions des bons carnets 2026 (rappel, FAIT, photo, PDF, widget, nuit, `+++`). Ce qui n’est **pas** encore au niveau « objet fini dans la main » :

| Reste | Pourquoi ça compte |
|-------|--------------------|
| Smoke Galaxy encore incomplet | S25 (`SM-S938B`) : accueil nuit, étape 6.51 et partage du PDF résumé vus le 2026-10-22 (code actuel 6.52). Pas encore : widget posé, OCR `+++`, PDF sur une facture, S26 de maman |
| Tests d’écran | Pas de parcours instrumenté (ajout, FAIT, widget, PDF) |

Volontairement dehors : banque, catégories, graphiques, grille calendrier, OCR du montant, synchro, thème fluo.

---

## 7. Limite

Pas de nouvelle session doigt pour cette note. Smokes : [`AUDIT_STATUS.md`](AUDIT_STATUS.md). Les apps citées peuvent avoir bougé ; les décisions tiennent aux mécanismes, pas aux notes d’étoiles.
