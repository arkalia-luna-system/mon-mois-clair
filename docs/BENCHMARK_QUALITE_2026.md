# Benchmark qualité — Mon Mois Clair face aux apps 2026

Date : 2026-10-22 (état code : **6.61**).  
Décisions vivantes : [`CHARTE_ET_ARCHITECTURE.md`](../CHARTE_ET_ARCHITECTURE.md) · [`PLAN_PRODUIT_FINI.md`](PLAN_PRODUIT_FINI.md).  
Smokes : [`AUDIT_STATUS.md`](AUDIT_STATUS.md). Lots A→F (archive) : [`archive/PLAN_PRODUIT_FINI.md`](archive/PLAN_PRODUIT_FINI.md).

| Statut | Contenu |
|--------|---------|
| **Fait** | … · **6.60** densité Secondary + QuietLink · **6.61** smoke S25 polish Accueil/fiche · WDG02 accepté |
| **Pas fait** | S26 en main · biométrie / widget home / scan caméra au doigt |

Ce n’est pas un plan à coder. Feedback famille (figé) : [`archive/FEEDBACK_TEST_FAMILLE_2026-10-21.md`](archive/FEEDBACK_TEST_FAMILLE_2026-10-21.md).

---

## 1. Comment lire

Deux familles d’apps, pas une.

| Famille | À quoi elles servent | Exemples | Rapport avec nous |
|---------|----------------------|----------|-------------------|
| **Argent complet** | Banque, catégories, patrimoine | Monarch, YNAB, Copilot, Origin | Barème **visuel** seulement |
| **Carnet de factures** | Rappels, récurrence, payé | Smart Bill Reminder, Billiary, Flying Bills, AllPaid | Vrais pairs |

« Premium » pour maman sur Galaxy = **objet papier terminé**, calme, un chiffre qui commande — pas le look Copilot.

---

## 2. Pairs — ce qu’on a

| Fonction | Pairs | Mon Mois Clair |
|----------|-------|----------------|
| Hors ligne, sans compte | Oui | Oui, **sans pub** |
| Récurrence mois / N / an | Oui | Oui (pas de semaine) |
| FAIT + Remettre | Oui | Oui |
| Photo + **PDF** | Souvent | Oui (ajout + édition) |
| Widget | Souvent payant | Oui — Reste + prochaine ligne |
| OCR montant | Parfois | Non — seulement `+++` |
| Mode sombre | Oui | Oui + choix Apparence |
| Catégories / graphiques | Premium | Refusé |

Déjà au-dessus des carnets belges : `+++` mod 97 · QR EPC · reste d’accord · courrier hors Reste · Mettre de côté · résumé texte/PDF · biométrie / `FLAG_SECURE` · contraste ≥ 7:1 · cibles ≥ 48 dp.

---

## 3. Visuel — V01–V08 (clos)

Fond jour `#F6F3E8`, cartes `#FFFDF9`, encre `#1E1B18`, teal `#1B4D4E`. Nuit : fond encre, texte papier, **même** teal.

| Id | État |
|----|------|
| V01–V08 | **Fait** (cartes/ombre, rayons, pastilles, socle, FAIT forêt, `tnum`, fondus, nuit) |

Pas de dégradé teal, pas d’icône seule Menu/FAIT, pas de 2ᵉ hero, pas de rouge sang.

---

## 4. Ce qui reste au doigt

Prochain geste produit : [`PLAN_PRODUIT_FINI.md`](PLAN_PRODUIT_FINI.md). Smoke : [`AUDIT_STATUS.md`](AUDIT_STATUS.md).

| Note technique | |
|----------------|--|
| Widget nuit hors process | Android ne livre pas `CONFIGURATION_CHANGED` au manifeste — refresh au prochain réveil de l’app |

## 5. Inspection des pages — 2026-10-22

Accueil vu sur le S25 (jour). Les autres pages lues dans le Compose.

| Page | À modifier ? |
|------|----------------|
| Accueil | Sticky allégé (Plus/Moins) ; Courses unifiées en liste ; tips sans boîte ; BrandMark hors alertes |
| Menu | Titre seul · tip sans box · Mois sans carte-titre · `SheetBottomClearance` · puces capsule |
| Réglages, Aide | Tips courants = texte InkMuted (boîtes = états seulement) |
| Dossier de sauvegarde | Non. Il n’y a pas de page Téléchargements : un bouton « Choisir un dossier » |
| Alertes fiables | Pastilles Prêt/À régler = StampShape |
| Restaurer | CTA Danger (hors teal) ; tip intro sans boîte |
| Mise à jour | Il n’y a pas cet écran (APK familial) |

Corrigé quand même, parce que ce n’était pas au niveau du papier : le splash n’était pas le crème du carnet, et le premier frame suivait la nuit du téléphone même si Apparence est Clair.

Volontairement dehors : banque, catégories, graphiques, grille calendrier, OCR montant, synchro, thème fluo.
