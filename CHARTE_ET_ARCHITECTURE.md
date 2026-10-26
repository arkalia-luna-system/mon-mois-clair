# Charte & Architecture — Mon Mois Clair

Mise à jour : 2026-10-23 (lot **6.76** — P0 BE B01–B03 · sticky quiet · PRIVACY).

## 1. Vision

App **locale, privée, hors-ligne** (Samsung One UI). Sideload **release signé**.  
Contact bug / idée / question : Menu → **Arkalia Système** (voir [`PRIVACY.md`](PRIVACY.md)).

### Persona (verrouillé — anonymisé)

| Qui | Appareil | Priorité affichage |
|-----|----------|--------------------|
| **Usage principal** (cadeau famille) | **Galaxy S26** écran normal **6,3″ FHD+** | **Priorité n°1** |
| **Ultra** (dev / second téléphone) | **Galaxy S25 Ultra** **6,9″ QHD+** | Doit rester confortable |

| Question | Réponse |
|----------|---------|
| Factures | **Majoritairement papier** |
| Paiements | **App bancaire sur le même téléphone** |

### Accueil = ledger (verrouillé, anti fourre-tout)

L’écran d’accueil n’est **pas** un tableau de bord de toutes les fonctions.  
Il sert à **voir et agir sur le mois** : reste, liste des rentrées / sorties, divers, ajouter.

| Zone lecture (courte) | Zone liste (centre) | Zone pouce (bas) |
|------------------------|---------------------|------------------|
| ‹ **mois** › + icône **calendrier** (tap → « Choisir un mois ») puis **un** gros Reste | Cartes échéances espacées ≥12 dp | **Menu** · **+ Ajouter** |
| Lignes sous le chiffre (ci-dessous) | FAIT / Remettre à droite (pas sur aperçu futur) | Menu = **Réglages** · **À venir** · **Dossiers** (si N>0) · **Choisir un mois ›** · QuietLink contact bas (« Un souci ? Écrire à Arkalia Système »). Titre « Menu » = PageTitle InkMuted. **Pas** Aide/Restore dans Menu |
| Alerte en 2 lignes maximum | Liste d’abord, courses ensuite | Futur = bouton **Mois en cours** à la place de + Ajouter |

**Calendrier mois (6.71)** : feuille carnet (années + pastilles), **pas** agenda Google ni grille 31 jours. Horizon ~**24 mois** passé + **24 mois** futur (assez pour une facture annuelle l’année suivante). Futur = aperçu. **Pas** de logo / wordmark / nom d’app sur le sticky Accueil.

**Sticky (verrouillé 6.27, densité 6.56) — lecture sans calculer :**

| Ligne | Sens | Quand |
|-------|------|--------|
| Gros **Reste après factures** | Revenus − **toutes** les factures du mois (FAIT ou non) | Toujours |
| **À payer** | Somme des factures encore TODO | S’il y a des factures ; « Tout est payé » si 0 — **1 ligne statut max** visible |
| **Courses** (tap ≥48) | Montant divers du mois ; ouvre la sheet d’édition | Toujours hors aperçu futur (`Courses` ou `Courses X € · après Y €`) |
| **Mettre de côté** (tap ≥48) | Forfait mensuel ; sheet montant (jour/retrait = Réglages) | Hors aperçu futur — **pas** 2ᵉ hero ; chrome compact (≤800 dp) : derrière **Autres infos** (accessible) |
| **Après les FAIT** · alertes / courriers | Détail | Derrière **Autres infos / Masquer** (pas de mini-dashboard) |

Courses / épargne : sheets depuis le sticky (›). Pas de 2ᵉ formulaire en bas de liste ; pas de logo / wordmark sur le sticky.

Couleurs : Reste = encre (ou brique si déficit) ; À payer / Tout est payé = muted ou vert ; Après les FAIT = brun warn `#6B4515` (≥ 7:1 sur crème) ; Après courses = muted, **brique** si négatif.  
Pas de plaque colorée, pas de nom d’app dans l’en-tête. Tips courants = texte `InkMuted` (boîtes = erreur / ok / warn seulement).

**Interdit** : empiler Alertes / aide / restore dans le Menu (ils vivent dans **Réglages**) ; boutons critiques collés tout en haut.  
**Marque** : absente de l’accueil. Légende discrète au verrouillage et à l’installation.  
**Futures fonctions** : Réglages ou écran dédié — **jamais** un nouveau bloc sur l’en-tête.  
**Clavier** : le champ en cours reste visible. Une seule action épinglée (`Enregistrer`). Annuler et Supprimer reviennent quand le clavier se ferme.  
**Photo facture** : revoir / reprendre depuis la fiche **et** bouton « Voir la photo » sur la ligne (hors zone « Modifier »).  
**Fréquence** : mensuel / 3 mois / annuel / une fois — **modifiable** sur la fiche, sans supprimer pour recommencer.

### Stack

Kotlin + Compose + Room (SQLCipher) + Hilt · ML Kit Barcode offline + CameraX · `Europe/Brussels` · `be.monmoisclair.app`  
Chiffrement local (6.81+) : DB SQLCipher + Keystore ; copies SAF/WhatsApp AES-GCM (code de copie) ; pièces jointes MRF1. Pas un coffre parfait — voir `PRIVACY.md` / `SECURITY.md`.

## 2. Décisions

**A–I + T1–T8** : inchangés (lots 1–5). **J–L + T9–T10** : lot 6.

## 3. Données

Room / backup schéma **8** (étape 6.38) : … + `pending_letters` (7) + `expense_fee_events` (frais datés). Reste dû = départ + cumul frais − FAIT ; pas stocké. Accueil : pastille **Accord** + « pour quoi », jamais le reste en euros ; bandeau « courrier sans plan · Voir » seulement s’il y en a. Sur la fiche d’un accord : Copier l’IBAN, Écrire (brouillon mail), ajouter/voir/enlever des courriers, **liste datée des frais**.

## 4. Plan (étapes)

Détail lots A→F : [`docs/archive/PLAN_PRODUIT_FINI.md`](docs/archive/PLAN_PRODUIT_FINI.md). État vivant : [`docs/PLAN_PRODUIT_FINI.md`](docs/PLAN_PRODUIT_FINI.md). Journal : `PROGRESS_LOG.md`.

| Étape | Contenu | État |
|-------|---------|------|
| **6.0–6.29** | Carnet, hub Réglages, home ledger, audit premium, sticky / Menu | ✅ |
| **6.30–6.36** | Lots A→F (papier, dossier, IBAN/frais, Mes dossiers, épargne, résumé) | ✅ |
| **6.37–6.42** | Rythme, frais datés (schéma 8), feedback famille, aperçu futurs, sync | ✅ |
| **6.43** | Forme : `tnum`, cartes 16 dp, pastilles, FAIT forêt, fondu mois | ✅ |
| **6.44–6.46** | PDF facture · nuit · Apparence Clair/Sombre/téléphone | ✅ |
| **6.47–6.51** | OCR `+++` · PDF résumé · widget · ClipData · 18/22 pt | ✅ |
| **6.52–6.53** | Retry PDF · cache thème · docs · copy Réglages/Aide | ✅ |
| **6.54** | hasReceipt photo+PDF · verrou anti-tap · PDF `finally` · VIEW ClipData · widget nuit | ✅ |
| **6.55** | 1er frame Apparence · cache empreinte · OCR sans collage · widget si le processus vit | ✅ |
| **6.56** | Feuille Menu : marge basse hors défilement (plancher 48 dp) · polish carnet (sticky, tips) | ✅ |
| **6.57** | Accueil H05–H14 · Réglages radio/case · SCAN/LOCK/LOAD · widget 32 sp | ✅ |
| **6.58** | Matrice audit · tip-boxes hors états retirées · dialogs PaperCream | ✅ |
| **6.59** | FORM01 rythme fiche (Paiement & preuve) · SET01 Réglages filets | ✅ |
| **6.60** | Densité Secondary fiche · QuietLink · typo titres · À venir / CheckRow sans boîte | ✅ |
| **6.61** | Smoke S25 polish Accueil/fiche (socle, Annuler QuietLink) | ✅ |
| **6.62** | Fiche : grille sous sticky · Paiement fermé · EXIF JPEG · tips courts | ✅ |
| **6.65–6.66** | Accueil Courses sticky (claim) · secondaires dossiers/widget | ✅ code fusionné en **6.67** |
| **6.67** | Hub Réglages + sous-pages · Courses sticky branché · Menu allégé · une version | ✅ |
| **6.68–6.69** | Audits typo/Menu/Courses/a11y · CarnetStateBanner · tipPad | ✅ |
| **6.70** | Menu mois récents/autres · À venir liste→détail · hub 3 groupes | ✅ |
| **6.71** | Calendrier mois Accueil · Menu lien calendrier · widget carnet · horizon ~2 ans | ✅ |
| **6.72** | PageTitle InkMuted · widget/notif · sticky · snackbar FAIT · contact Menu · épargne Accueil | ✅ |
| **6.73** | Photo multi-ligne · sticky S26 Plus · snackbar compact · Dossiers/Aide · À venir cartes | ✅ |
| **6.74** | PageTitle Lock/Scan/onboarding/fiches · SectionTitle Fréquence · charte tokens typo | ✅ |
| **6.75** | Identité Arkalia · PRIVACY · polish docs/contact | ✅ |
| **6.76** | B01–B03 métier BE · sticky quiet · widget serif · fontScale · FAIT 96 | ✅ |

## 4bis. Hiérarchie typo (verrouillé 6.72–6.74)

| Rôle | Composant / token | Style | Couleur | Usage |
|------|-------------------|-------|---------|-------|
| **PageTitle** | `CarnetPageTitle` / `SettingsPageTitle` | `titleLarge` | **InkMuted** | Menu, hub Réglages, groupes, sous-pages, À venir, sheets (Courses / épargne / calendrier), Alertes, Lock, Scan, onboarding, Restore, EditTemplate, fiche, HowItWorks **titre de page** |
| **SectionTitle** | `CarnetSectionTitle` | `titleMedium` | **InkMuted** | En-têtes non tappables (Dossiers, Fréquence, Prochaine échéance, années calendrier) |
| **HubRow** | `CarnetHubRow` | `titleLarge` + › | **Ink** (label) | Lignes cliquables hub (Menu, Réglages) |
| **Disclosure** | titre + ▾/▴ | `titleLarge` | **Ink** | Accordion HowItWorks, sections fiche repliables — **≠** PageTitle |
| **Tip / Body** | texte courant | `bodyMedium` / `bodyLarge` | **InkMuted** | Empty Dossiers, intros, tips (pas de tip-box sauf ok/warn/erreur) |
| **QuietLink** | `QuietLinkA11y` | `bodyLarge` | **Link** | Copier / Photo / Contact Menu (« Un souci ? Écrire à Arkalia Système ») |

**Nuit :** `InkMuted` ≠ `Ink` (écart PageTitle / corps) ; contraste muted ≥ 7:1 sur crème nuit.  
**Interdit :** PageTitle en Ink (look HubRow) ; logo / wordmark sur sticky Accueil ; Aide/Restore dans Menu Accueil (contact QuietLink OK).

## 5. Branches Git (verrouillé)

| Branche | Rôle |
|---------|------|
| **`develop`** | Travail quotidien |
| **`main`** | Stable — merge **seulement** sur demande explicite |

## 6. Affichage & écrans Galaxy (verrouillé)

### Specs officielles (Samsung)

| Appareil | Diagonale (officiel) | Résolution | Notes |
|----------|----------------------|------------|-------|
| **Galaxy S26** | **6,3″** plein rectangle / **6,1″** coins arrondis | **2340×1080 FHD+** · 120 Hz · Dynamic AMOLED 2X | Cible principale |
| **Galaxy S25 Ultra** | **6,9″** / **6,8″** coins | **3120×1440 QHD+** · 120 Hz · Dynamic AMOLED 2X | Ultra (dev / second) |

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
**Exception** : `ModalBottomSheet` (fenêtre dialogue) — insets souvent à 0 sur Galaxy edge-to-edge → `rememberSheetBottomClearance()` (plancher 48 dp + clearance).

### Adaptation hauteur (`DisplayFit`)

- Si `screenHeightDp ≤ 800` → **chrome Home compact** (paddings réduits) pour laisser la place à la **liste**.
- Sinon → mode confort Ultra.
- Montant « Reste » = signal n°1 (34 sp si l’écran est court, ~40 sp sinon ; étude 36–42). Serif **seulement** pour ce chiffre.
- Titres d’écran, noms de factures, boutons : sans-serif.
- Barre du bas : un seul bouton plein **teal** (« + Ajouter »), sur un socle `PaperWarm`. Menu, Copier, Remettre restent un trait. Coins des boutons : 12 dp.
- Sur la ligne, « FAIT ✓ » est un plein **forêt** (`AccentOk`) : c’est l’action de la facture, pas un deuxième bouton de barre. Une fois FAIT, le bouton redevient le trait « Remettre ».
- Cartes : coins 16 dp, ombre courte (encre ~8 %). Pastilles (jour, Accord, En retard, Prélèvement) : capsule, pas le même coin qu’un bouton.
- Chiffres en chasse fixe (`tnum`) : les montants s’alignent.
- Mois en toutes lettres (« Octobre 2026 »).
- Nom de l’app : hors de l’accueil. Sur l’écran d’accueil du téléphone : **M ivoire sur teal** (`#1B4D4E`). Les rappels utilisent la même silhouette en blanc.
- Bandeau L : fond contrasté, max 3–4 lignes.

Réf. code : `DisplayFit` · `A11yTokens.bottomSystemClearance` · `SheetBottomClearance` · `HomeMenuSheet` · `DisableFrameworkScrollbars`.

## 7. Hub Réglages & usage sur une année (verrouillé)

**Forme (6.70 / 6.76) :** hub = **3 groupes** (Données & sauvegarde / Confort / Aide) → sujets (Apparence, Alertes→Santé, Échéances, Mettre de côté, Confidentialité, Préparer un résumé si utile, Sauvegarde). Aide ouvre Comment ça marche. Max **2 niveaux** après le hub. Pas tout empilé sur une page.

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
| Feuilleter / sauter un mois | Accueil ‹ › + **calendrier** (ou Menu → Choisir un mois) | **Mois en cours** (Menu / calendrier) |
| Rappels qui ne sonnent pas | Réglages → Confort → Alertes | Un seul hub (pas de doublon Menu) |
| Aide / restore | Réglages → Aide / Données & sauvegarde → Sauvegarde | Menu ne liste plus Comment ça marche ni Restaurer |

Interdit : ajouter un 2ᵉ hub Device Care dans Réglages ; diluer Santé ; remettre Santé / Réglages / aide dans le scroll Home ; dupliquer Alertes ou Aide dans le Menu.

## 8. Fidélité outil quotidien — étude 2026 relue (verrouillé)

La leçon utile de l’étude tient : ce qui retient, c’est la **charge de saisie** face au bénéfice immédiat, et deux réponses visibles sans réfléchir —  
1) *« Qu’est-ce que je dois payer maintenant ? »*  
2) *« Combien me reste-t-il vraiment ? »*

Le chiffre « plus de 75 % abandonnent en 60 jours » est une formule de marché **sans source vérifiable**. On garde la leçon, pas le pourcentage comme fait mesuré.

L’étude parle d’un S25 / S25 Ultra. La cible n°1 reste le **Galaxy S26 6,3″** (usage principal). Les règles pouce One UI s’appliquent aux deux ; les cibles de 48 dp doivent tenir dans ~360 dp de large, pas dans un écran Ultra.

### Adopter (codé)

| Idée étude | Décision MMC |
|------------|--------------|
| Safe-to-Spend | Sticky **Reste après factures** + **À payer** + **Après les FAIT** (si besoin) + **Courses** (sticky, sheet unique). Gros chiffre = signal n°1 (**34 sp** compact / **~40 sp** confort ; étude 36–42) |
| Check-off | Bouton d’**action** « FAIT ✓ » à droite (≥ 96 dp), plein forêt tant que ce n’est pas fait. « À payer » baisse tout de suite. Le bouton ne s’appelle pas « À PAYER » : un bouton dit l’action |
| Preuve | Pastille **« FAIT le JJ/MM »** (`doneAtEpochMs`) |
| Liste > graphiques | Jamais camembert / courbes |
| Zone pouce | Barre basse **Menu · + Ajouter**, cachée pendant le clavier. Factures en liste ; **Courses** dans le sticky (pas un 2ᵉ formulaire en bas) |
| Jour d’échéance | Grille **1–31**, cibles ≥ 48 dp, qui se replie (7 colonnes de 48 dp ne tiennent pas sur le S26) |
| Montant | Clavier décimal. Édition **ce mois-ci seul** par défaut |
| Mois indépendants | Pas de report auto. Un revenu ponctuel « Reste du mois passé » si elle le veut |
| Alertes | Hub **Réglages** (3 groupes : Données & sauvegarde / Confort / Aide) → **Confort → Alertes**. Bouton bas reste **« Menu »** ; badge `!` si notifs / alarmes bloquées + ligne Alertes en tête du sheet. Pas de pastille permanente pour la batterie Samsung |
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
