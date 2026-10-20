# Charte & Architecture — Mon Mois Clair

Mise à jour : 2026-10-20 (lot **6.x** + affichage multi-Galaxy).

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

### Freeze Home

Pas de nouvelles options quotidiennes sur l’accueil.  
**Exception L** : bandeau « À prévoir le mois prochain » seulement si utile.

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
| **6.4** | Affichage S26 / Ultra (charte §6) | ✅ |

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

- Si `screenHeightDp ≤ 800` → **chrome Home compact** (paddings / titres / hints secondaires réduits) pour laisser de la place à la liste sur S26.
- Sinon → mode confort Ultra.
- Bandeau L : fond contrasté (`PaperCream` sur sticky `PaperWarm`), max 3–4 lignes.

Réf. code : `DisplayFit` · `A11yTokens.bottomSystemClearance`.
