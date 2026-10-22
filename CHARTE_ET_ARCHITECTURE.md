# Charte & Architecture — Mon Mois Clair

Mise à jour : 2026-10-22 (lot **6.51** — PDF résumé gros caractères).

## 1. Vision

App **locale, privée, hors-ligne** (Samsung One UI). Sideload **release signé**.

### Persona (verrouillé)

| Qui | Appareil | Priorité affichage |
|-----|----------|--------------------|
| **Maman** (cadeau) | **Galaxy S26** écran normal **6,3″ FHD+** | **Priorité n°1** |
| Athalia / Alex | **Galaxy S25 Ultra** **6,9″ QHD+** | Doit rester confortable |

| Question | Réponse |
|----------|---------|
| Factures | **Majoritairement papier** |
| Paiements | **App bancaire sur le même téléphone** |

### Accueil = ledger (verrouillé, anti fourre-tout)

L’écran d’accueil n’est **pas** un tableau de bord de toutes les fonctions.  
Il sert à **voir et agir sur le mois** : reste, liste des rentrées / sorties, divers, ajouter.

| Zone lecture (courte) | Zone liste (centre) | Zone pouce (bas) |
|------------------------|---------------------|------------------|
| Mois ‹ › puis **un** gros Reste | Cartes échéances espacées ≥12 dp | **Menu** · **+ Ajouter** |
| Lignes sous le chiffre (ci-dessous) | FAIT / Remettre à droite (pas sur aperçu futur) | Menu = **Réglages** + calendrier (passé + **3 mois à venir**) |
| Alerte en 2 lignes maximum | Liste d’abord, courses ensuite | Futur = bouton **Mois en cours** à la place de + Ajouter |

**Sticky (verrouillé 6.27) — maman lit sans calculer :**

| Ligne | Sens | Quand |
|-------|------|--------|
| Gros **Reste après factures** | Revenus − **toutes** les factures du mois (FAIT ou non) | Toujours |
| **À payer** | Somme des factures encore TODO | S’il y a des factures ; « Tout est payé » si 0 |
| **Après les FAIT** | Revenus − factures **déjà FAIT** (= Reste + À payer) | Seulement s’il reste à payer (> 0) ; ancien libellé « Hors à payer » |
| **Après courses** | Reste − courses / divers du mois | Seulement si courses > 0 |
| **Mis de côté** | Montant mensuel épargne (tap → Réglages) | Seulement si activé |
| Lien **Courses** | Ouvre le sheet (pas un 2ᵉ bouton plein) | Toujours |

Couleurs : Reste = encre (ou brique si déficit) ; À payer / Tout est payé = muted ou vert ; Après les FAIT = brun `#8A5A1E` ; Après courses = muted, **brique** si négatif.  
Pas de plaque colorée, pas de nom d’app dans l’en-tête.

**Interdit** : empiler Alertes / aide / restore dans le Menu (ils vivent dans **Réglages**) ; boutons critiques collés tout en haut.  
**Marque** : absente de l’accueil. Légende discrète au verrouillage et à l’installation.  
**Futures fonctions** : Réglages ou écran dédié — **jamais** un nouveau bloc sur l’en-tête.  
**Clavier** : le champ en cours reste visible. Une seule action épinglée (`Enregistrer`). Annuler et Supprimer reviennent quand le clavier se ferme.  
**Photo facture** : revoir / reprendre depuis la fiche **et** bouton « Voir la photo » sur la ligne (hors zone « Modifier »).  
**Fréquence** : mensuel / 3 mois / annuel / une fois — **modifiable** sur la fiche, sans supprimer pour recommencer.

### Stack

Kotlin + Compose + Room + Hilt · ML Kit Barcode offline + CameraX · `Europe/Brussels` · `be.monmoisclair.app`

## 2. Décisions

**A–I + T1–T8** : inchangés (lots 1–5). **J–L + T9–T10** : lot 6.

## 3. Données

Room / backup schéma **8** (étape 6.38) : … + `pending_letters` (7) + `expense_fee_events` (frais datés). Reste dû = départ + cumul frais − FAIT ; pas stocké. Accueil : pastille **Accord** + « pour quoi », jamais le reste en euros ; bandeau « courrier sans plan · Voir » seulement s’il y en a. Sur la fiche d’un accord : Copier l’IBAN, Écrire (brouillon mail), ajouter/voir/enlever des courriers, **liste datée des frais**.

## 4. Plan

| Étape | Contenu | État |
|-------|---------|------|
| **6.0–6.3.1** | Assistant papiers + fiabilité | ✅ |
| **6.4–6.5** | Affichage S26 / Ultra + UX smoke | ✅ |
| **6.6** | Hub Réglages utile + navigation année | ✅ |
| **6.7** | Home ledger + Menu (fin du fourre-tout) | ✅ |
| **6.8** | Fidélité outil (étude 2026) : FAIT daté, palette, barre pouce | ✅ |
| **6.9** | Relecture étude : contraste réel, grille des jours, cartes, alertes | ✅ |
| **6.10** | Calme visuel : en-tête court, clavier qui laisse voir le champ | ✅ |
| **6.11** | Rythme visuel : serif au chiffre, mois en toutes lettres, écrans allégés | ✅ |
| **6.12** | Parcours fini : Continuer épinglé, un seul bouton plein, cartes échéances | ✅ |
| **6.13** | Marque : icône M, rappel blanc, ouverture papier | ✅ |
| **6.14–6.26** | Audit premium, archive photos, menu années, mois vide, CI | ✅ |
| **6.27** | Sticky Sans les futures · Menu = Réglages · photo + fréquence éditables | ✅ |
| **6.28–6.29** | Punch list premium (photo au doigt, Copier hors Modifier, Alertes) | ✅ |
| **6.30** | Lot A : note/photo sur modèle · matérialisation · Copier 12 chiffres (mod 97) | ✅ |
| **6.31** | Lot B : dossier replié · reste calculé · pastille Accord · arrêt si soldé | ✅ |
| **6.32** | Lot C : IBAN · e-mail · frais · photos extras visibles (voir/enlever) | ✅ |
| **6.33** | Mes dossiers (Menu si N>0) · EuroFormat · mailto URI · garde IBAN ajout | ✅ |
| **6.34** | Courriers sans plan (`pending_letters`) · bandeau accueil · conversion → accord | ✅ lot D |
| **6.35** | Mettre de côté (Réglages, facture mensuelle, alerte déficit) | ✅ lot E |
| **6.36** | Résumé texte (Mes dossiers / Réglages si déficit ou dossier) | ✅ lot F |
| **6.37** | Phrase rythme (« encore environ N… si… ») fiche / Mes dossiers | ✅ |
| **6.38** | Historique daté des frais (`expense_fee_events`, schéma 8) | ✅ |
| **6.39** | Feedback famille (mail, soldés, Vers, À venir, courses, épargne) | ✅ |
| **6.40** | Polish sticky : Après courses danger + Courses en lien compact | ✅ |
| **6.41** | Aperçu mois futurs (flèche › / Menu, M+1…M+3, lecture seule) | ✅ |
| **6.42** | Sync templates sur mois déjà ouverts · Soldés repliés · dist famille | ✅ |
| **6.43** | Forme : `tnum`, cartes 16 dp, pastilles capsule, FAIT forêt, socle barre, fondu mois | ✅ |
| **6.44** | PDF (toute facture, fiche) + encre de nuit (système) | ✅ |
| **6.45** | Polish : ombre sticky Reste + fondu couleur ligne FAIT ~150 ms | ✅ |
| **6.46** | Apparence : Clair / Sombre / Comme le téléphone (Réglages) | ✅ |
| **6.47** | OCR « Lire +++ sur la photo » (jamais le montant) | ✅ |
| **6.48** | PDF du résumé (même texte, Mes dossiers / Réglages) | ✅ |
| **6.49** | Widget accueil : Reste + prochaine ligne non FAIT · PDF aussi à l’ajout | ✅ |
| **6.50** | ClipData sur PDF résumé · fonds widget crème / encre fixes | ✅ |
| **6.51** | PDF résumé : 18/22 pt, Source Sans/Serif, pied de page | ✅ |

Journal : `PROGRESS_LOG.md`.

## 5. Branches Git (verrouillé)

| Branche | Rôle |
|---------|------|
| **`develop`** | Travail quotidien |
| **`main`** | Stable — merge **seulement** sur demande explicite |

## 6. Affichage & écrans Galaxy (verrouillé)

### Specs officielles (Samsung)

| Appareil | Diagonale (officiel) | Résolution | Notes |
|----------|----------------------|------------|-------|
| **Galaxy S26** | **6,3″** plein rectangle / **6,1″** coins arrondis | **2340×1080 FHD+** · 120 Hz · Dynamic AMOLED 2X | Cible maman |
| **Galaxy S25 Ultra** | **6,9″** / **6,8″** coins | **3120×1440 QHD+** · 120 Hz · Dynamic AMOLED 2X | Athalia / Alex |

Sources : pages produit Samsung (S26 SM-S942… ; S25 Ultra SM-S938…).  
Mesure diagonale « full rectangle » ; zone visible un peu moindre (coins + trou caméra).

### Classes de fenêtre (Android / One UI)

- Phones portrait = **Compact** (largeur **&lt; 600 dp**) — S26 **et** Ultra.
- One UI Design Guide : marges latérales **≥ 24 dp** ; cibles tactiles **≥ 48 dp**.
- Texte en **sp** (scale accessibilité One UI jusqu’à ~200 %).
- **Interdit** : layouts figés en px / tests `if (model == Ultra)`.

### Zone boutons tactiles système (**obligatoire**)

La bande basse des **boutons système** (3 boutons ou geste) doit rester **libre** :

1. `safeDrawingPadding()` — encoches / barres système  
2. `WindowInsets.tappableElement` bas — zone tactile système  
3. **`A11yTokens.bottomSystemClearance` (28 dp)** — confort familial (évite double appui app + Android)

Appliqué **une seule fois** dans `MainActivity`.  
**Interdit** de re-ajouter `bottomSystemClearance` dans les LazyColumn / écrans (sinon double marge, surtout sur S26).

### Adaptation hauteur (`DisplayFit`)

- Si `screenHeightDp ≤ 800` → **chrome Home compact** (paddings réduits) pour laisser la place à la **liste**.
- Sinon → mode confort Ultra.
- Montant « Reste » = signal n°1 (32 sp si l’écran est court, 36 sp sinon). Serif **seulement** pour ce chiffre.
- Titres d’écran, noms de factures, boutons : sans-serif.
- Barre du bas : un seul bouton plein **teal** (« + Ajouter »), sur un socle `PaperWarm`. Menu, Copier, Remettre restent un trait. Coins des boutons : 12 dp.
- Sur la ligne, « FAIT ✓ » est un plein **forêt** (`AccentOk`) : c’est l’action de la facture, pas un deuxième bouton de barre. Une fois FAIT, le bouton redevient le trait « Remettre ».
- Cartes : coins 16 dp, ombre courte (encre ~8 %). Pastilles (jour, Accord, En retard, Prélèvement) : capsule, pas le même coin qu’un bouton.
- Chiffres en chasse fixe (`tnum`) : les montants s’alignent.
- Mois en toutes lettres (« Octobre 2026 »).
- Nom de l’app : hors de l’accueil. Sur l’écran d’accueil du téléphone : **M ivoire sur teal** (`#1B4D4E`). Les rappels utilisent la même silhouette en blanc.
- Bandeau L : fond contrasté, max 3–4 lignes.

Réf. code : `DisplayFit` · `A11yTokens.bottomSystemClearance` · `HomeMenuSheet`.

## 7. Hub Réglages & usage sur une année (verrouillé)

### Inventaire (indispensable vs redondant)

| Bloc | Indispensable ? | Notes |
|------|-----------------|-------|
| Comment ça marche | ✅ | Aide familiale ; Home freeze = pas sur l’accueil |
| Biométrie | ✅ optionnel | Déjà validé device ; défaut OFF |
| Santé des rappels | ✅ | Point unique notifs / batterie / Device Care |
| Device Care **doublé** dans Réglages | ❌ retiré (6.6) | Redondant avec Santé — un renvoi suffit |
| Échéances hors ce mois | ✅ | Assurances / taxes 1× an / trimestriel hors mois courant |
| Sauvegarde (dossier / envoyer / restaurer) | ✅ | Pipeline SAF + partage |

### Scénario « vraie année » (rentrées / sorties)

| Besoin | Où | Point de friction → réponse |
|--------|-----|-----------------------------|
| Loyer, salaire, courses | Home mois courant | OK (liste + reste + divers) |
| Assurance / taxe annuelle | Création 1× + bandeau L le mois d’avant | Hors mois : **Réglages → Échéances** (toujours visible, même vide) |
| Feuilleter 12 mois passés | Menu → calendrier + ‹ › | **Revenir au mois en cours** (Menu) |
| Rappels qui ne sonnent pas | Réglages → Alertes | Un seul hub (plus de doublon Menu / Réglages) |
| Aide / restore | Réglages | Menu ne liste plus Comment ça marche ni Restaurer |

Interdit : ajouter un 2ᵉ hub Device Care dans Réglages ; diluer Santé ; remettre Santé / Réglages / aide dans le scroll Home ; dupliquer Alertes ou Aide dans le Menu.

## 8. Fidélité outil quotidien — étude 2026 relue (verrouillé)

La leçon utile de l’étude tient : ce qui retient, c’est la **charge de saisie** face au bénéfice immédiat, et deux réponses visibles sans réfléchir —  
1) *« Qu’est-ce que je dois payer maintenant ? »*  
2) *« Combien me reste-t-il vraiment ? »*

Le chiffre « plus de 75 % abandonnent en 60 jours » est une formule de marché **sans source vérifiable**. On garde la leçon, pas le pourcentage comme fait mesuré.

L’étude parle d’un S25 / S25 Ultra. La cible n°1 reste le **Galaxy S26 6,3″** (maman). Les règles pouce One UI s’appliquent aux deux ; les cibles de 48 dp doivent tenir dans ~360 dp de large, pas dans un écran Ultra.

### Adopter (codé)

| Idée étude | Décision MMC |
|------------|--------------|
| Safe-to-Spend | Sticky **Reste après factures** + **À payer** + **Après les FAIT** (si besoin) + **Après courses** (si besoin). Gros chiffre = signal n°1 (36 sp compact / 42 sp confort) |
| Check-off | Bouton d’**action** « FAIT ✓ » à droite (≥ 96 dp), plein forêt tant que ce n’est pas fait. « À payer » baisse tout de suite. Le bouton ne s’appelle pas « À PAYER » : un bouton dit l’action |
| Preuve | Pastille **« FAIT le JJ/MM »** (`doneAtEpochMs`) |
| Liste > graphiques | Jamais camembert / courbes |
| Zone pouce | Barre basse **Menu · + Ajouter**, cachée pendant le clavier. Factures en tête ; divers en dessous |
| Jour d’échéance | Grille **1–31**, cibles ≥ 48 dp, qui se replie (7 colonnes de 48 dp ne tiennent pas sur le S26) |
| Montant | Clavier décimal. Édition **ce mois-ci seul** par défaut |
| Mois indépendants | Pas de report auto. Un revenu ponctuel « Reste du mois passé » si elle le veut |
| Alertes | Hub dans **Réglages → Alertes**. Le libellé bas « Menu » peut devenir « Menu / Alertes » seulement si notifs ou alarmes exactes refusées. Pas de pastille permanente pour la batterie Samsung |
| Local-first | Zéro banque, zéro cloud, zéro pub |

### Refuser

| Idée | Pourquoi non |
|------|----------------|
| Connexion bancaire / abonnement / enveloppes multiples / graphiques | Charge ou risque inutiles. Une seule facture « Mettre de côté » (virement vers un autre compte, pas une cagnotte à l’écran) est admise |
| Report automatique du solde | L’écart (espèces, courses non notées) s’accumule |
| 3ᵉ bouton bas « Réglages & Sauvegarde » | Étroit sur S26. Réglages est déjà dans Menu, en toutes lettres |
| Fiche intermédiaire Modifier / Supprimer / Fermer | Un tap de plus. La ligne ouvre déjà l’édition, avec Annuler et Supprimer écrits |
| Menu hamburger icône seule | Toujours le mot « Menu » |
| Rouge `#FF0000` / blanc `#FFFFFF` | Anxiété + halo AMOLED |
| Photo / dictée IA type Moneko qui remplit le montant | Hors scope — un OCR qui invente un euro fausse le Reste. Le QR EPC reste le geste sûr. « Lire +++ sur la photo » (6.47) lit **seulement** une communication `+++` valide, jamais le montant |
| Ocre `#D99B26` **en texte** | Mesuré à **~2,2:1** sur le papier : illisible. L’or est le **fond** de la pastille « Auto », le mot est en encre |
| Russet `#B84A27` tel quel | Mesuré à **~4,7:1** sur le papier, pas les ~7:1 annoncés. Remplacé par `#8E3418` (toujours brique, pas rouge sang) |

### Palette « Carnet apaisé » (codes mesurés)

Contraste via `WcagContrast` (WCAG 2.1). Encre, boutons et pastilles : **≥ 7:1**.  
Un reste négatif et le bandeau « à prévoir » restent en brun `#8A5A1E` (~5,4:1 sur le papier, au-dessus de 4,5).

| Rôle | Hex | Token | Contraste utile |
|------|-----|--------|-----------------|
| Fond | `#F6F3E8` | `PaperCream` | Brique dessus ≥ 7:1 (plancher du danger). Un cran plus soutenu que `#F7F4EB`, le relief vient surtout de l’ombre |
| Cartes | `#FFFDF9` | `PaperCard` | Encre dessus ~16,9:1 |
| Encre | `#1E1B18` | `Ink` | |
| Boutons | `#1B4D4E` | `ButtonFill` | Ivoire `#FFFDF9` dessus ~9,3:1 |
| FAIT fond de ligne | `#E3EFE6` | `AccentOkSoft` | |
| FAIT pastille | `#1E5631` | `AccentOk` | Ivoire dessus ~8,5:1 |
| Retard / erreur | `#8E3418` | `AccentDanger` | Ivoire dessus ~7,8:1 ; encre brique sur papier ~7,2:1 |
| Auto (fond de pastille) | `#D99B26` | `AccentAuto` | Encre dessus ~7,1:1 — jamais ce doré comme couleur de lettres |
| Socle barre basse | `#EDE8DC` | `PaperWarm` | Déjà le fond des champs. Sous Menu / + Ajouter, pour que la barre ne se confonde pas avec la page |

### Suite visuelle

Décisions du [`docs/BENCHMARK_QUALITE_2026.md`](docs/BENCHMARK_QUALITE_2026.md).

| Suite | Décision |
|-------|----------|
| Formes / FAIT / `tnum` / fondu mois | **Fait (6.43)** |
| PDF à côté de la photo | **Fait (6.44 + 6.49).** Ajout **et** édition, toute facture. Même dossier `receipts/`. « Voir le courrier » ouvre le fichier |
| Encre de nuit | **Fait (6.44 + 6.46).** Fond `#1E1B18`, texte papier, **même** teal. Choix en Réglages → Apparence (Clair / Sombre / Comme le téléphone). Pas Copilot, pas la couleur du fond d’écran |
| Ombre sticky + fondu ligne FAIT | **Fait (6.45)** |
| Widget | **Fait (6.49).** Un seul : Reste + prochaine ligne non FAIT. Masque les montants si biométrie. Suit Apparence (Clair / Sombre / comme le téléphone). Fonds crème et encre fixes, pas la couleur de nuit du lanceur |
| OCR de la communication | **Fait (6.47).** « Lire +++ sur la photo » — jamais le montant |
| PDF du résumé | **Fait (6.48 + 6.51).** Même texte, gros caractères (18/22 pt). Pas un formulaire CPAS |
| Grille calendrier, catégories, banque, graphiques, synchro | Non |
