# Charte & Architecture — Mon Mois Clair

Mise à jour : 2026-10-20 (lot **6.x** + **fidélité outil quotidien** / étude 2026).

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

| Zone lecture (haut ~30 %) | Zone liste (centre) | Zone pouce (bas — barre fixe) |
|---------------------------|---------------------|--------------------------------|
| Reste + à payer (gros) | Cartes échéances espacées ≥12 dp | **Menu** · **+ Ajouter** · (outils) |
| ‹ › mois (sous le résumé) | FAIT / Remettre à droite (pouce) | Calendrier + Santé + Réglages dans Menu |
| Bandeau L si utile | Divers + liste | |

**Interdit** : empiler Santé / Réglages / aide dans le scroll de la liste ; boutons critiques collés en haut hors zone pouce.  
**Marque** : légende discrète — jamais plus grand que « Reste ».  
**Futures fonctions** : Menu ou écran dédié — **jamais** un nouveau bouton sur le sticky.

### Stack

Kotlin + Compose + Room + Hilt · ML Kit Barcode offline + CameraX · `Europe/Brussels` · `be.monmoisclair.app`

## 2. Décisions

**A–I + T1–T8** : inchangés (lots 1–5). **J–L + T9–T10** : lot 6.

## 3. Données

Room / backup schema **3** : `paymentNote`, `receiptRelativePath` sur `expense_instances`.

## 4. Plan

| Étape | Contenu | État |
|-------|---------|------|
| **6.0–6.3.1** | Assistant papiers + fiabilité | ✅ |
| **6.4–6.5** | Affichage S26 / Ultra + UX smoke | ✅ |
| **6.6** | Hub Réglages utile + navigation année | ✅ |
| **6.7** | Home ledger + Menu (fin du fourre-tout) | ✅ |
| **6.8** | Fidélité outil (étude 2026) : FAIT daté, palette, barre pouce | ✅ |

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
- Montant « Reste » = signal n°1 ; titre marque = légende (sp nettement plus petit).
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
| Rappels qui ne sonnent pas | Menu → Santé | Un seul hub |

Interdit : ajouter un 2ᵉ hub Device Care dans Réglages ; diluer Santé ; remettre Santé / Réglages dans le scroll Home.

## 8. Fidélité outil quotidien — décisions étude 2026 (verrouillé)

Constat marché : **>75 % abandonnent une app budget sous 60 jours** si la saisie coûte plus que le bénéfice immédiat.  
Ce qui retient : **réduction de charge cognitive** + réponses instantanées à  
1) *« Qu’est-ce que je dois payer maintenant ? »*  
2) *« Combien me reste-t-il vraiment ? »*

### Adopter (codé / obligatoire produit)

| Idée étude | Décision MMC |
|------------|--------------|
| Safe-to-Spend (PocketGuard) | Sticky **Reste après factures** + **À payer** = signaux n°1 |
| Check-off effect | **Marquer FAIT ✓** visible ; baisse immédiate de « À payer » |
| Preuve anti-doute | Afficher **« FAIT le JJ/MM »** (`doneAtEpochMs`) pas seulement « FAIT » |
| Liste > graphiques | **Jamais** camembert / courbes — liste verticale + couleurs franches |
| Zone pouce One UI | Barre basse **Menu · + Ajouter** ; sticky haut = lecture seule |
| Boutons explicites | Pas de swipe / appui long caché ; tout est écrit |
| Montant variable | Édition **ce mois-ci seul** par défaut (`applyToFollowing = false`) |
| Mois indépendants | **Pas de report auto** du reste mois N → N+1 (erreur cumulée) |
| Palette AMOLED | Fond papier chaud, teal calme, russet (pas rouge sang), sage FAIT |
| Local-first | Zéro banque, zéro cloud, zéro pub (déjà) |

### Refuser (pièges)

| Idée | Pourquoi non |
|------|----------------|
| Connexion bancaire / abonnement | Contredit local-first familial |
| Enveloppes virtuelles (Goodbudget) | Charge mentale trop haute pour persona maman |
| Graphiques analytiques | Consultés 1ʳᵉ semaine puis oubliés |
| Report automatique du solde | Dérive si courses non notées |
| Menu hamburger icône seule | Découvrabilité ÷2 — toujours **texte** « Menu » |
| Rouge `#FF0000` / blanc `#FFFFFF` | Anxiété + halo AMOLED |

### Différer (pas bloquant 6.8)

| Idée | Quand |
|------|-------|
| Grille jours 1–31 à la place du clavier jour | Lot saisie (après validation famille) |
| Pastille rouge Santé sur l’onglet Menu | Quand Santé expose un score « KO » simple |
| Photo / dictée IA (Moneko) | Hors scope — on a déjà scan EPC + photo facture offline |

### Palette « Carnet apaisé » (codes)

| Rôle | Hex | Token |
|------|-----|--------|
| Fond | `#F7F4EB` | `PaperCream` |
| Cartes | `#FFFDF9` | `PaperCard` |
| Encre | `#1E1B18` | `Ink` |
| Boutons / bandeau | `#1B4D4E` | `ButtonFill` |
| FAIT fond | `#E3EFE6` | `AccentOkSoft` |
| FAIT badge | `#1E5631` | `AccentOk` |
| Retard | `#B84A27` | `AccentDanger` |
| Auto | `#D99B26` | `AccentAuto` |
