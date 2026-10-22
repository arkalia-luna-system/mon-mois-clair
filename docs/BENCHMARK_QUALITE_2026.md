# Benchmark qualité — Mon Mois Clair face aux apps 2026

Date : 2026-10-22.  
App lue au départ : `0.1.0-6.41`, puis le dépôt a avancé (6.42 en parallèle).  
Les décisions de ce fichier sont reprises dans [`CHARTE_ET_ARCHITECTURE.md`](../CHARTE_ET_ARCHITECTURE.md) (forme de l’objet + suite visuelle) et dans [`PLAN_PRODUIT_FINI.md`](PLAN_PRODUIT_FINI.md).

**En code (cette passe) :** chiffres `tnum`, fond `#F6F3E8` (plancher 7:1 de la brique), cartes 16 dp + ombre courte, pastilles capsule, boutons 12 dp, socle `PaperWarm`, « FAIT ✓ » plein forêt, « Remettre » en trait, fondu 180 ms au changement de mois.  
**En code aussi (6.44) :** PDF à côté de la photo, sur la fiche de **toute** facture (pas seulement les accords / récurrences).  
**Pas encore codé :** encre de nuit, widget, OCR de la communication, PDF du résumé.

Ce n’est pas un plan à coder. Les lots A→F et le feedback 6.39–6.41 restent dans [`PLAN_PRODUIT_FINI.md`](PLAN_PRODUIT_FINI.md) et [`FEEDBACK_TEST_FAMILLE_2026-10-21.md`](FEEDBACK_TEST_FAMILLE_2026-10-21.md). L’audit dossiers reste dans [`AUDIT_ALEX_MAMAN_2026-10-21.md`](AUDIT_ALEX_MAMAN_2026-10-21.md). La recherche dettes du 21 octobre est figée dans [`archive/RECHERCHE_OUTILS_2026-10-21.md`](archive/RECHERCHE_OUTILS_2026-10-21.md) : on ne la rouvre pas.

---

## 1. Comment lire

Deux familles d’apps, pas une.

| Famille | À quoi elles servent | Exemples lus | Rapport avec nous |
|---------|----------------------|--------------|-------------------|
| **Argent complet** | Banque branchée, catégories, patrimoine, souvent un abonnement | Monarch, YNAB, Copilot, Origin | Barème **visuel** et « objet fini ». Mauvais modèle de fonctions |
| **Carnet de factures** | Rappels, récurrence, payé, souvent hors banque | Smart Bill Reminder, Billiary, Flying Bills, Bills Reminder & Organizer, AllPaid | Vrais pairs. C’est ici qu’un manque compte |

« Premium » pour maman sur un Galaxy n’est pas le look Copilot (fond minuit, étiquettes fluo, typo très fine, finaliste Apple Design Award 2024, refresh « liquid glass » en septembre 2025). Ce look est superbe sur iPhone et illisible comme carnet du soir. Le bon barème : **un objet papier qui a l’air terminé**, calme, avec un seul chiffre qui commande.

Sources (pages publiques, octobre 2026) :

- NerdWallet, « The Best Budget Apps for 2026 » — Monarch, YNAB, Copilot. <https://www.nerdwallet.com/finance/learn/best-budget-apps>
- WalletHub, « Best Budget Apps of 2026 » — mêmes trois, notes Play / App Store. <https://wallethub.com/best-budget-apps>
- Origin, « 10 Best Bill Management Apps in 2026 » — liste commerciale ; Prism y est encore cité alors qu’il a fermé le 20 février 2024. À prendre comme catalogue, pas comme vérité. <https://useorigin.com/resources/blog/10-best-bill-management-apps-tools-in-2026-u-s-guide>
- Copilot, design (Matt Ström-Awn) et note liquid glass. <https://matthewstrom.com/projects/copilotmoney/> · <https://www.copilot.money/dispatch/liquid-glass>
- Smart Bill Reminder (Play, MAJ 2 oct. 2026). <https://smartbillreminder.com/>
- Billiary 2.0. <https://billiary.com/>
- Flying Bills. <https://flyingbills.com/>
- Bills Reminder & Organizer (Play, MAJ 8 août 2026).

Mint est mort (23 mars 2024). Prism est mort. Les classer encore « meilleures apps 2026 » est une erreur de blog.

---

## 2. Ce que les meilleures font — et ce qu’on a déjà

### Argent complet (à ne pas copier comme produit)

| App | Ce qui fait sa note en 2026 | Prix public | Ici |
|-----|------------------------------|-------------|-----|
| **Monarch** | Comptes, budget flexible ou par catégorie, patrimoine, foyer partagé, rappels de factures. Play ~4,8. Android + web | ~100 $/an | Hors charte (banque, foyer cloud, graphiques) |
| **YNAB** | Chaque euro a un métier avant d’être dépensé. Saisie manuelle possible. Prêt avec simulateur | ~109 $/an | L’idée « réserver avant de dépenser » est déjà « Mettre de côté » + Reste. Le simulateur de dettes est refusé |
| **Copilot** | Le plus beau de la catégorie. Catégories auto, abonnements, objectifs. **Pas d’app Android** (iPhone, Mac, web) | ~95 $/an | Barème visuel seulement |
| **Origin** | Factures dans le patrimoine + conseiller IA | payant | Le conseil automatique est refusé (charte, et le mois impossible reste honnête) |

Leçon utile, une seule : ces apps gagnent parce que **ouvrir l’app répond tout de suite** à « qu’est-ce qui part ? » et « qu’est-ce qu’il me reste ? ». C’est déjà la charte §8. Elles perdent les gens qui ne veulent pas brancher une banque ni entretenir des catégories. C’est notre place.

### Carnets de factures (pairs réels)

| Fonction des pairs | Eux | Mon Mois Clair |
|--------------------|-----|----------------|
| Sans compte, hors ligne | Smart Bill, Flying Bills, Billiary | Oui, et **sans pub** (Smart Bill a des pubs + un premium) |
| Récurrence mois / trimestre / an | Oui, parfois aussi semaine et jour | Mois, N mois, an, une fois. Pas de semaine : inutile pour loyer, énergie, mutuelle |
| Marquer payé **depuis la notification**, avec retour arrière | Smart Bill, Bills Organizer | **Déjà là** : action FAIT + notification « Remettre » ~10 s (`PaymentNotificationHelper`) |
| Photo de facture | Oui | Oui, WebP, et elle **suit le modèle** (lot A). Mieux que beaucoup de carnets qui recollent la photo chaque mois |
| **PDF** collé à la facture | AllPaid, Billiary, Smart Bill, Bills Organizer | **Non.** Seulement une photo. Un courrier reçu par mail est souvent un PDF |
| Vue calendrier en plus de la liste | Billiary, Smart Bill, Flying Bills | Liste du mois + puces Menu. Pas une grille « quel jour » |
| Widget écran d’accueil | Presque tous, souvent en premium | Non. Charte : plus tard, seulement si ouvrir le carnet ennuie |
| OCR du ticket (montant, texte) hors ligne | Smart Bill (ML Kit) | QR EPC belge seulement. Plus sûr qu’un OCR qui invente un montant |
| Mode sombre / thèmes | Smart Bill (15 thèmes), Billiary, One UI | **Clair uniquement.** Choix charte (pas de blanc pur, pas de rouge) |
| Catégories, graphiques, séries, objectifs | Le premium de ces apps | Refusé. Une quinzaine de lignes par mois n’a pas besoin d’un camembert |
| Export | JSON, parfois CSV / PDF | JSON + archive ZIP avec photos, SHA-256, 3 versions. Résumé = texte partagé, pas un PDF |
| Plusieurs profils / synchro | Bills Organizer, Billiary (sync en option) | Un foyer, un téléphone. Volontaire |

### Là où on est déjà au-dessus des carnets Android

Ces points ne se voient pas dans les classements US. Ils sont le vrai premium **belge** :

- Communication structurée : l’écran garde `+++…+++`, le presse-papiers reçoit **12 chiffres** si le modulo 97 passe (Febelfin). Aucun carnet US ne fait ça.
- Le QR de facture remplit nom, montant, communication **et** IBAN.
- Un accord a un **reste** qui baisse du montant réellement coché FAIT, pas d’un compteur « mois 4 sur 12 ».
- Un courrier sans montant n’entre pas dans le Reste.
- « Mettre de côté » est une facture vers un autre compte, pas une cagnotte à re-dépenser.
- Résumé texte (créancier, nature, versement, reste, référence) pour un mail ou une médiation, sans faux formulaire CPAS.
- Biométrie, `FLAG_SECURE`, sauvegarde locale.
- Contraste visé ≥ 7:1, corps ≥ 18 sp, cibles ≥ 48 dp, un seul bouton plein. Les apps « belles » de 2026 ratent souvent ça (Copilot : corps en graisse 100).

---

## 3. Fonctions qui manquent encore — par priorité

Priorité = ce qu’une personne sent en usage, pas ce qui allonge la fiche Play.

### À prendre, sans casser la charte

**1. Joindre un PDF au même endroit que la photo.**  
C’est le trou le plus net face à AllPaid, Billiary et Smart Bill. Un accord ou une facture d’énergie arrive en PDF. Aujourd’hui il faut une capture d’écran, donc une photo floue. Le PDF resterait dans le dossier SAF, comme les WebP. Pas d’aperçu « lecteur ». Un bouton « Voir le courrier » qui ouvre le fichier. Maman sans PDF ne voit rien de plus.

**2. Reconnaître seulement la communication `+++` sur une photo, pas le montant.**  
Smart Bill lit le ticket entier et demande de vérifier. Ici le risque est pire : un mauvais montant entre dans le Reste. Le geste sûr, et belge : si la photo contient une communication structurée valide, proposer de la coller dans le champ. Le montant reste saisi ou lu par le QR. Ça reste hors ligne (ML Kit on-device). La charte refuse « photo / dictée IA type Moneko » pour remplir le carnet tout seul — pas pour aider à copier 12 chiffres déjà sur le papier.

**3. Chiffres tabulaires.**  
Aucun `fontFeatureSettings = "tnum"` dans l’app. « 1.111,11 € » et « 9,00 € » n’ont pas la même largeur de chiffres. Sur une liste, les montants dansent. Toutes les apps d’argent soignées verrouillent les chiffres. Source Sans 3 et Source Serif 4 le permettent. Un seul réglage de style, pas un nouvel écran.

**4. Le mois qui change devrait se voir.**  
Les flèches remplacent la liste d’un coup. Billiary fait défiler le mois. Un fondu court (~180 ms, déjà la durée du scan) suffit. Pas un geste horizontal en plus : sur 6,3″ le défilement vertical et un swipe de mois se battent.

### Plus tard, seulement si le carnet est tenu depuis des mois

**5. Un widget, un seul.**  
« Reste après factures » + le nom de la prochaine ligne non FAIT. Pas un tableau de bord. C’est la seule surface Samsung où l’app existe **sans** être ouverte. La charte a raison de le retarder : un widget faux (solde banque, camembert) ferait plus de mal que de bien.

**6. Un PDF du résumé, même texte qu’aujourd’hui.**  
Billiary vend le PDF. Pour un rendez-vous, un texte WhatsApp suffit souvent ; une feuille propre aide quand on imprime. Ce n’est pas un formulaire CPAS. Tant que le texte part déjà, ce n’est pas le premier chantier.

**7. Grille calendrier du mois.**  
Les pairs l’ont. Nous avons déjà le jour sur chaque ligne et les puces de mois dans le Menu. Une deuxième vue de l’accueil casse « un écran, une question ». À laisser.

### Laisser, même si les classements 2026 les vendent

| Envie | Pourquoi ça reste dehors |
|-------|--------------------------|
| Banque, open banking, « on a vu le prélèvement » | Charte. Et le FAIT manuel **est** le produit : la banque ne sait pas si le courrier est le bon |
| Catégories, tags, filtres | 15 lignes. Le jour est déjà le rangement |
| Graphiques, streaks, objectifs, coach | Undebt.it : les gens aimaient l’info et ne lisaient pas les courbes. Déjà tranché |
| Semaine / jour comme récurrence | Pas le rythme loyer–énergie–mutuelle |
| Synchro cloud, deuxième téléphone | Le backup ZIP part déjà par WhatsApp / Drive. Une synchro, c’est un compte |
| Thèmes fluo, 15 couleurs | Le papier est la marque. Copilot gagne sur iOS avec un autre public |
| OCR qui remplit le montant | Une erreur devient un Reste faux. Le QR EPC fait le cas fiable |
| Dire quelle dette payer d’abord | L’ordre reste le calendrier |

---

## 4. Le visuel — ce qui est déjà tenu, ce qui fait encore « brouillon »

Le fond n’est pas raté. `PaperCream` `#F7F4EB`, cartes `#FFFDF9`, encre `#1E1B18`, teal `#1B4D4E`, serif pour le Reste, sans pour le reste : c’est un parti pris, pas un oubli Material. Le logo (carnet, filet or) est sur le verrou, le menu, les réglages, l’aide, et en petit sur l’accueil. Les pastilles ne crient plus (« En retard », « Prélèvement », « Accord »).

Ce qui fait quand même moins fini qu’un objet 2026 :

### V01 — Les cartes se confondent avec la page

`PaperCard` et `PaperCream` sont très proches. Le seul relief est un filet `PaperBorder` `#E2DDD2` de 1 dp, **le même** sur la ligne, le bandeau, l’aide, le chargement, le champ. Rien ne s’avance. Copilot creuse ses surfaces ; un carnet premium, lui, a une feuille un peu plus claire **ou** une ombre très courte (2 dp, encre à ~8 %). Aujourd’hui tout est un formulaire.

Piste : garder le filet, renforcer un peu le contraste carte / page, et réserver l’ombre au sticky du Reste. Un seul niveau de profondeur, pas un Material générique.

### V02 — Tout a le même coin de 12 dp

`MonMoisClairTheme` met 12 dp sur extraSmall jusqu’à extraLarge. Une pastille « Accord », un bouton « FAIT », une carte et une feuille ont le même rayon. L’œil ne distingue plus un **tampon** d’un **bouton**.

Piste : pastilles en capsule (rayon plein), cartes à 16 dp, boutons à 12 dp. Trois formes, pas une.

### V03 — Le statut a la même taille que la phrase

`labelSmall`, `bodySmall` et `bodyMedium` sont tous à 18 sp (`A11yTokens.minBodySp`). C’est juste pour maman. Le coût : « Accord » et « En retard » ont le poids d’une phrase, pas d’un sceau. On ne descend pas sous 18 sp. On peut serrer le padding vertical des pastilles (déjà un peu en compact) et passer le mot en semibold, ce que `labelLarge` fait déjà — à condition que la forme (V02) fasse le reste.

### V04 — La barre du bas est la même couleur que la page

Menu et « + Ajouter » reposent sur `PaperCream`, avec un filet au-dessus. Le pouce trouve le teal. L’œil, moins : la barre ne forme pas un socle. Un fond `PaperWarm` sous les deux boutons suffirait, sans troisième bouton et sans icône hamburger (interdits).

### V05 — « FAIT ✓ » est un bouton secondaire comme les autres

Le geste le plus important du mois a le même trait que « Copier » et « Menu ». Les carnets soignés transforment la ligne (fond sauge — déjà là) **et** le bouton (plein forêt, ou un tampon « FAIT le JJ/MM » à la place du bouton). Le mot reste écrit. Seule la matière change. Un seul plein par écran : le teal « + Ajouter » peut rester le plein de la barre, et FAIT devenir plein **forêt** seulement sur la ligne, parce que ce n’est pas le même rôle.

### V06 — Le Reste est serif, les montants des lignes sont sans, et les chiffres ne sont pas alignés

Le hero serif est le bon signal (charte : un seul chiffre dominant). Les montants de ligne en `titleLarge` sans, non tabulaires, font deux systèmes. Aligner les chiffres (V03 de la section fonctions, le `tnum`) fait plus pour le luxe qu’un nouveau thème.

### V07 — Presque pas de mouvement

Haptique : oui (boutons, scan, FAIT). Animation : un fondu sur la communication, le scan en 180 ms. Changer de mois, cocher FAIT, ouvrir le menu : coupes sèches. One UI 7 / 8 et Material 3 de 2025–2026 bougent un peu. Ici, un fondu de liste et une couleur de ligne qui arrive en ~150 ms suffisent. Pas de rebond, pas de verre.

### V08 — La nuit, l’app est une feuille allumée

One UI est souvent en sombre sur un S26. L’app reste crème. La charte interdit le blanc `#FFFFFF` et le rouge pur, pas une **encre de nuit** (fond `#1E1B18`, cartes un cran plus clair, texte `#F7F4EB`, le même teal). Ce n’est pas le minuit Copilot. C’est le même carnet, lampe éteinte.

À trancher avant d’écrire une ligne : maman lit-elle le soir dans le noir ? Si oui, c’est le plus gros écart « pas premium » du visuel. Si elle lit le jour, V01–V06 passent avant. Ne pas suivre la couleur du fond d’écran : la charte a raison, le papier doit rester le papier.

### Ce qu’il ne faut pas « embellir »

- Pas de dégradé teal, pas de halo, pas d’illustration à la place du chiffre.
- Pas d’icône seule pour Menu, FAIT, Copier.
- Pas de deuxième hero (le Reste reste le seul serif grand).
- Pas de rouge « alerte banque ». La brique `#8E3418` est déjà le bon danger.
- Le compact S26 (padding 6 dp, filet de 2 dp) est une contrainte de hauteur, pas un style à étendre à l’Ultra.

---

## 5. Ordre suggéré, si on veut que ça se voie

Sans toucher à l’accueil de maman tant qu’elle n’a pas de dossier :

1. Chiffres tabulaires (invisible, toute l’app).
2. Trois rayons + carte un peu plus détachée du fond + socle `PaperWarm` sous Menu / Ajouter.
3. Bouton FAIT qui change de matière quand c’est fait, mot inchangé.
4. Fondu court au changement de mois.
5. PDF à côté de la photo, sur la fiche seulement.
6. Ensuite seulement : nuit encre, widget, OCR de la communication, PDF du résumé.

1–4 ne sont pas des fonctions. Ce sont eux qui séparent « outil correct » et « objet qu’on a envie de montrer ».

---

## 6. Limite

Pas de nouvelle session au doigt pour cette note. Les smokes Galaxy restent dans [`AUDIT_STATUS.md`](AUDIT_STATUS.md). Les captures des apps citées n’ont pas été refaites : les fonctions viennent de leurs pages et de la fiche Play à la date ci-dessus. Une app peut avoir bougé. Les décisions tiennent aux mécanismes (PDF, chiffres alignés, un seul relief, pas de banque), pas à une note d’étoiles.
