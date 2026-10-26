# Benchmark qualité — Mon Mois Clair face aux apps 2026

Date : 2026-10-26 (état code : **6.83**).  
Décisions vivantes : [`CHARTE_ET_ARCHITECTURE.md`](../CHARTE_ET_ARCHITECTURE.md) · [`PLAN_PRODUIT_FINI.md`](PLAN_PRODUIT_FINI.md) · [`PRIVACY.md`](../PRIVACY.md).  
Smokes : [`AUDIT_STATUS.md`](AUDIT_STATUS.md). Lots A→F (archive) : [`archive/PLAN_PRODUIT_FINI.md`](archive/PLAN_PRODUIT_FINI.md).

| Statut | Contenu |
|--------|---------|
| **Fait** | … · **6.81** crypto · **6.82** polish copy · **6.83** quieting sticky/cartes/sheets + notice famille |
| **Pas fait** | S26 en main · restore doigt code de copie · biométrie / notif au doigt · claim premium terminé · scission god-files · rescan Dependabot GitHub |

Ce n’est pas un plan à coder. Feedback famille (figé) : [`archive/FEEDBACK_TEST_FAMILLE_2026-10-21.md`](archive/FEEDBACK_TEST_FAMILLE_2026-10-21.md).

---

## 1. Comment lire

Deux familles d’apps, pas une.

| Famille | À quoi elles servent | Exemples | Rapport avec nous |
|---------|----------------------|----------|-------------------|
| **Argent complet** | Banque, catégories, patrimoine | Monarch, YNAB, Copilot, Origin | Barème **visuel** seulement |
| **Carnet de factures** | Rappels, récurrence, payé | Smart Bill Reminder, Billiary, Flying Bills, AllPaid | Vrais pairs |

« Premium » sur Galaxy (usage principal) = **objet papier terminé**, calme, un chiffre qui commande — pas le look Copilot.

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
Typo : voir charte **§4bis** (PageTitle InkMuted ≠ HubRow Ink).

---

## 4. Ce qui reste au doigt

Prochain geste : [`PLAN_PRODUIT_FINI.md`](PLAN_PRODUIT_FINI.md). Smoke : [`AUDIT_STATUS.md`](AUDIT_STATUS.md).

| Note technique | |
|----------------|--|
| Widget nuit | `ACTION_CONFIGURATION_CHANGED` enregistré dans `MonMoisClairApp` (refresh process vivant) ; hors process = prochain réveil app |
| Small icon notif | Silhouette claire `ic_notification` + teal canal |

## 5. Inspection des pages — 2026-10-23

| Page | État code 6.75 |
|------|----------------|
| Accueil | Sticky PaperWarm · Courses ; épargne sticky ou derrière Plus (compact) · snackbar compact · pas logo |
| Menu | PageTitle · Dossiers (N) · QuietLink « Un souci ? Écrire à Arkalia Système » · pas Aide/Restore |
| Réglages, Aide | Hub 3 groupes · HowItWorks corps courts · contact = Menu seulement |
| Lock / Scan / onboarding / fiches | PageTitle InkMuted (aligné Menu) |
| À venir | Liste → détail cartes papier (sans FAIT) |
| Alertes | PageTitle · DeviceCare court · pastilles |
| Widget | Pads / filet / 36 sp Reste |

Volontairement dehors : banque, catégories, graphiques, grille calendrier, OCR montant, synchro, thème fluo.
