# Audit premium — Mon Mois Clair

Date : 2026-10-20.  
Arbre lu : étape journal **6.15** (`PROGRESS_LOG.md`).  
**Suivi :** lot **6.16–6.18**. Le détail de ce qui est tenu et de la passe suivante est dans `docs/AUDIT_PREMIUM_2026-10-20-SUITE.md`. Polices custom et animations riches restent différées.  
But : passer d’un carnet familial correct à un produit fini, vendable, de gamme haute.  
Ce fichier **ne code rien**. Il garde l’analyse pour ne pas la perdre.

Méthode : lecture des écrans, du thème, de la navigation, des cas d’usage d’ajout / photo / partage / mois.  
Pas de smoke sur Galaxy. Un point marqué « vu dans le code » n’a pas été revu au doigt.

Hors de cet audit (déjà refusés par la charte, ne pas les « améliorer ») : banque, cloud, pub, graphiques, report automatique du solde, 3ᵉ bouton bas, fiche Modifier / Supprimer / Fermer, menu en icône seule.

---

## Verdict

Le métier utile est là : mois indépendants, reste, à payer, FAIT daté, rappels, sauvegarde, QR belge, photo, biométrie. Les tests unitaires de la 6.15 sont ceux du journal (ne pas les recopier ici comme une nouvelle mesure).

Ce qui fait encore « app correcte » et pas « objet cher » :

1. Le chiffre principal n’a pas de nom à lui. La phrase du dessous mélange « Reste » et « à payer ».
2. « + Ajouter » écrit **toujours dans le mois calendaire courant**, même si l’écran montre un autre mois.
3. La photo de facture est enregistrée et **jamais rouverte**. La copie WhatsApp / Mail n’emporte **pas** les photos.
4. L’accueil aligne beaucoup de boutons teal « FAIT ✓ ». Un produit cher n’a qu’un bouton plein par écran ; le reste est une marque calme.
5. Les coins, le blanc de la barre d’état, les libellés de champ trop petits et les dialogues Material cassent le système visuel.

---

## Déjà au niveau voulu (ne pas défaire)

- Palette mesurée (encre, teal, forêt, brique). L’or n’est pas une couleur de lettres.
- Serif réservé au gros montant. Mois en toutes lettres.
- Marges 24 dp, cibles 48 dp, clavier qui cache la barre du bas et ne laisse qu’Enregistrer.
- Un bouton plein teal pour l’action principale des écrans secondaires.
- Mois sans report auto. Édition « ce mois seulement » par défaut.
- Menu en toutes lettres. Santé des rappels séparée de l’accueil.
- Icône : M ivoire sur teal, filet or, rappel en blanc.

---

## P0 — Confiance (un produit cher ne se trompe pas de mois, ni de photo)

### F01 — Ajouter ignore le mois affiché

**Vu dans le code.** `HomeScreen` appelle `onAdd` sans mois. `AppNavHost` ouvre `entry/add`. `AddEntryUseCase` enregistre dans `AppClock.currentYearMonth()`, jamais dans `selectedYearMonth`.

**Ce que vit la personne.** Elle est sur mars 2026 (flèche ou Menu), elle touche « + Ajouter », elle croit noter mars. La ligne arrive dans le mois d’aujourd’hui. L’historique a l’air vide ou faux.

**Correction.** Passer le mois affiché à l’écran d’ajout. Si ce mois n’est pas le mois courant, l’écrire en titre (« Nouvelle entrée · Mars 2026 »). Refuser le futur (déjà la règle). Test unitaire : ajout depuis un ledger passé ≠ ledger courant.

### F02 — La photo ne se revoit pas

**Vu dans le code.** `ReceiptPhotoStore.openUri` existe. Aucun écran ne l’appelle. `EditEntryScreen` ne sait que « Reprendre la photo ». Pas d’aperçu.

**Ce que vit la personne.** Elle photographie une facture papier pour la retrouver au moment de payer. Elle ne peut pas l’ouvrir. Le bouton a l’air d’avoir marché.

**Correction.** Sur la fiche, si `receiptRelativePath` n’est pas vide : aperçu (image) + « Voir la facture ». Depuis la ligne d’accueil, un accès court (« Facture ») si une photo existe — sans rallonger l’en-tête.

### F03 — La photo est une vignette, pas une facture lisible

**Vu dans le code.** `TakePicturePreview` (bitmap réduit par le système), puis `MAX_SIDE_PX = 800` et WebP qualité 75.

**Ce que vit la personne.** Le QR, l’IBAN ou la communication deviennent flous. Pour des factures papier, c’est le cœur du produit.

**Correction.** `TakePicture` vers un fichier plein cadre (ou au minimum côté long ≥ 1600 px, qualité plus haute). Garder le poids raisonnable, mais le texte doit se lire.

### F04 — « Envoyer une copie » n’envoie pas les photos

**Vu dans le code.** `ShareBackupUseCase` lit le dernier JSON et l’envoie. Les fichiers `receipts/*.webp` restent dans le dossier Documents, hors du message.

**Ce que vit la personne.** Téléphone cassé, seule la copie Mail / WhatsApp : les mois reviennent, les factures papier non. Le texte d’aide dit pourtant que la photo est « stockée à côté de la sauvegarde ».

**Correction.** Soit un envoi qui inclut les photos (archive), soit une phrase honnête : la copie JSON ne contient pas les photos ; elles restent dans le dossier Documents. Les deux textes (Réglages, Comment ça marche) doivent dire la même chose.

### F05 — Le gros chiffre n’est pas nommé

**Vu dans le code.** `HomeScreen` : le montant est `formatCents(remainingCents)` sans libellé. La ligne suivante est « Reste · à payer … · divers … ». L’aide dit que le gros chiffre est le reste après factures. À l’écran, le mot « Reste » est collé à « à payer ».

`remainingCents` = revenus − **toutes** les factures (payées ou non). Les divers ne sont pas dedans. « Après divers » est en bas de liste, à côté du formulaire.

**Ce que vit la personne.** Elle ne sait pas si le grand nombre est ce qu’il reste ou ce qu’elle doit encore sortir. La vraie réponse « après courses » est loin du chiffre.

**Correction (sans rallonger en bannière).**

- Un mot au-dessus ou juste sous le chiffre : « Reste après factures ».
- Ligne 2, seule : « À payer … ».
- Si des divers existent : « Après divers … » sur cette même zone haute (une ligne), pas seulement au-dessus du champ.

Le chiffre reste le plus grand. Pas de plaque colorée. Pas du nom de l’app.

### F06 — « À prévoir » n’ouvre rien

**Vu dans le code.** `nextMonthWarning` est un `Text` non cliquable. Le mois suivant est interdit (`MonthNavigation`). L’échéance annuelle du mois d’après n’est pas une ligne.

**Correction minimale.** Rendre la phrase actionnable vers Réglages → Échéances hors ce mois (déjà la liste). Ne pas inventer un mois futur tant que la charte l’interdit. Le dire dans l’audit de décision si on veut plus tard un mois suivant en lecture seule.

### F07 — Échéances hors mois : lecture seule

**Vu dans le code.** `SettingsScreen`, les cartes `offMonthPeriodics` n’ont pas de `click`. Pour corriger un montant ou un mois, il faut attendre que la ligne tombe sur l’accueil, ou supprimer / recréer.

**Correction.** Un tap ouvre l’édition du modèle (montant, jour, mois d’ancre), avec la même règle « mois suivants » écrite en clair.

---

## P1 — Système visuel (le même objet partout)

### V01 — Plusieurs boutons teal sur l’écran le plus vu

**Vu dans le code.** Chaque `ExpenseRow` pose un `PrimaryA11yButton` « FAIT ✓ » ou « Remettre ». Une liste de huit factures = huit pastilles teal + « + Ajouter ». La charte dit un seul bouton plein par écran. L’action FAIT doit rester évidente et ≥ 48 dp.

**Correction.** Garder « + Ajouter » comme seul plein teal. « FAIT ✓ » devient un bouton contour (encre) ou une coche forêt avec le mot « FAIT » (pas une icône seule). « Remettre » reste contour, plus discret. La ligne FAIT garde le vert doux et « FAIT le JJ/MM ».

### V02 — Coins qui ne sont pas 12 dp

Le thème est à 12 dp (`AppShapes`). Le dessin local s’en écarte :

| Endroit | Coin actuel |
|---------|-------------|
| Cartes accueil `IncomeRow` / `ExpenseRow` | 10 dp |
| Pastilles Auto / FAIT / EN RETARD | 8 dp |
| Grille des jours, puces Dépense / Revenu | 10 dp |
| Aperçu caméra | 16 dp |
| Pastille OK / À régler (Santé) | 8 dp |

**Correction.** 12 dp pour cartes, puces, jours, pastilles, caméra. Un seul chiffre dans le code (le shape du thème), pas des `RoundedCornerShape` recopiés.

### V03 — Barre d’état blanche sur un papier crème

**Vu dans le code.** `themes.xml` et `MainActivity.enableEdgeToEdge` : statut `#FFFFFF`, navigation `#F7F4EB`. Un bandeau blanc au-dessus du crème se voit sur AMOLED.

**Correction.** Statut crème `#F7F4EB`, icônes système sombres (`windowLightStatusBar` déjà vrai). Le contraste des icônes tient sur ce crème.

### V04 — Libellés de champ en taille Material par défaut

**Vu dans le code.** `AppTypography` ne définit pas `bodySmall`, `labelSmall`, `titleSmall`. Le libellé flottant d’`OutlinedTextField` retombe sur le défaut Material (~12 sp). La charte demande ≥ 18 sp.

**Correction.** Définir ces styles en sans-serif, ≥ 18 sp, encre / encre atténuée. Vérifier Nom, Montant, Communication, Divers.

### V05 — Dialogues en TextButton, pas dans le système de boutons

Mois passé, suppression, restauration : `AlertDialog` + `TextButton`. Hauteur souvent sous 48 dp, style Material, pas `Primary` / `Danger`.

**Correction.** Boutons du thème, libellés inchangés (Annuler, Modifier, Supprimer, Restaurer, OK). Supprimer et Restaurer en danger, pas en lien gris.

### V06 — Barre du bas plus étroite que le reste

Contenu accueil : 24 dp. Barre Menu / Ajouter : `padding(horizontal = 12.dp)`. Les boutons touchent presque le bord.

**Correction.** 24 dp, comme `screenPadding`.

### V07 — Le mois du Menu est une phrase, pas un choix

`HomeMenuSheet` affiche `DayLabelFormat.monthTitle` (« Octobre 2026 ») dans chaque puce, en `bodyLarge`. Douze puces = un mur de mots. L’accueil a déjà le mois en toutes lettres.

**Correction.** Puces courtes (« Oct. », « Nov. ») + année une fois au-dessus du groupe. La puce sélectionnée reste teal. Cible toujours ≥ 48 dp.

### V08 — Réglages = long texte + boutons contour

`SettingsScreen` : titres, paragraphes, filet, boutons, sans cartes. Même densité pour l’aide, l’empreinte, les rappels, les échéances et la sauvegarde. Pas de version. Pas de marque.

**Correction.** Une carte ivoire par bloc (comme les échéances hors mois, déjà en carte). En bas, une ligne discrète « Mon Mois Clair · 0.1.0-… » lue depuis le `versionName`, pas écrite en dur.

### V09 — États vides et chargement

« Chargement… », « Vérification… », « Aucune entrée ce mois-ci. » sont du texte brut. L’état vide n’a pas de geste à côté de la phrase (le bouton est en bas, facile à rater la première fois).

**Correction.** Même typo, mais une seule phrase calme + le rappel du bouton du bas. Pas d’illustration gadget. Le chargement peut rester une ligne, alignée sur les marges, pas un spinner Material violet (le `CircularProgressIndicator` de `MainActivity` suit `primary` teal : le garder teal si on le montre).

### V10 — Filet or de l’icône près du rognage

`ic_launcher_foreground.xml` : M jusqu’à y=76, barre or y=82–86. La zone sûre adaptive est environ 21–87. Un masque rond Samsung peut manger le filet.

**Correction.** Remonter le filet (sous le M, au-dessus de y=78) ou le supprimer si le M suffit. Vérifier sur le lanceur après réinstall (le cache d’icône Samsung garde l’ancienne).

### V11 — Écran verrouillé sans marque

`BiometricLockScreen` : le mot « Mon Mois Clair » en `labelLarge` atténué, pas le M. Premier écran si l’empreinte est active.

**Correction.** Le même M que l’icône (vector dans l’app), petit, teal, puis le titre « Application verrouillée ». Un seul bouton plein : « Déverrouiller maintenant ».

### V12 — Scanner sans cadre

`ScanEpcScreen` : preview plein, phrase d’état, Annuler. Pas de cadre de visée. Un QR qui n’est pas EPC est ignoré sans phrase (`EpcQrParser.parse` null → rien).

**Correction.** Cadre ivoire léger au centre. Si un QR est lu mais pas EPC : « Ce QR n’est pas une facture belge. » Une fois.

### V13 — « JSON » et grammaire

- Restaurer : « Choisir un fichier JSON… » — mot de développeur.
- Bandeau : « et N autre(s) » — parenthèses visibles.

**Correction.** « Choisir une sauvegarde… ». « et 2 autres » / « et 1 autre ».

### V14 — Contraste du reste négatif

`AccentWarn` `#8A5A1E` sur le papier est ~5,4:1. Le gros chiffre passe dans cette couleur si le reste est négatif. Pour un signal d’alerte, viser ≥ 7:1 sans virer au rouge sang (la charte l’interdit).

**Correction.** Assombrir légèrement le brun du **chiffre** seulement, ou garder le brun et ajouter le mot « Déficit » pour ne pas compter sur la couleur seule.

### V15 — Deux façons d’écrire l’euro

L’en-tête utilise `NumberFormat` devise (`formatCents`). Les lignes utilisent `formatSignedCents` (« - 470 € »). Les espaces, le symbole et les décimales peuvent différer (fr-BE).

**Correction.** Un seul formateur pour tout l’écran : `1 240,50 €`, signe `−` typographique pour une dépense, `+` pour un revenu.

---

## P2 — Tenue « objet » (ce qui sépare 1 € et une gamme)

### V16 — Typo système

Serif et SansSerif Android. Sur Samsung, le serif par défaut est rarement celui d’un carnet. Un produit cher a une paire choisie (un serif pour le chiffre, un sans pour le reste), embarquée, tailles déjà définies.

### V17 — Pas d’écran de démarrage Android 12

Le fond de fenêtre est crème (bien). Il n’y a pas de SplashScreen avec le M centré. Le premier frame est un aplat.

### V18 — Aucun retour tactile

FAIT, Enregistrer, mois : pas de haptic léger. Un clic premium confirme sans bruit.

### V19 — Pas d’annulation après FAIT

`requestToggleDone` bascule tout de suite (dialogue seulement sur un mois passé). Une erreur = chercher « Remettre ». Un bandeau « Annuler » 4 secondes éviterait la peur, sans swipe caché.

### V20 — Note de paiement absente de la ligne

La communication structurée est dans le formulaire. Le jour du paiement, la ligne d’accueil ne la montre pas et ne propose pas « Copier ». Il faut ouvrir, copier, revenir.

**Correction.** Si la note n’est pas vide : une ligne sous le montant + « Copier » en contour, dans la carte. Pas un nouvel écran.

### V21 — Fréquence figée après création

L’édition le dit : pour changer « 1× par an » en mensuel, supprimer et recréer. Honnête, mais ça casse la confiance. À traiter avec F07 (édition du modèle), pas par un second formulaire caché.

### V22 — Formulaire Divers dans la liste

Utile, et voulu. Visuellement, l’accueil finit en formulaire (deux boutons + champ) sous les factures. Pour la gamme : le bloc ne s’ouvre qu’au tap « Courses / divers », le total reste visible fermé. Le clavier ne concerne alors que ce bloc (déjà le cas une fois ouvert).

### V23 — Santé des rappels mélange la sauvegarde

`HealthStatusRow("Sauvegarde Documents")` est dans l’écran des alarmes. Ce n’est pas un rappel. Le déplacer vers le bloc Sauvegarde des Réglages, déjà porteur de `BackupHealthCopy`.

---

## Ordre de travail proposé

Ne pas tout faire d’un coup. Chaque vague : code, test Gradle `--rerun-tasks`, journal, puis smoke téléphone (pas encore fait pour 6.10–6.15).

| Vague | Contenu | Pourquoi en premier |
|-------|---------|---------------------|
| **A** | F01, F05, V01, V02, V03, V06, V15 | Le mois, le chiffre, l’allure de l’accueil |
| **B** | F02, F03, F04, V12, V20 | La facture papier, promesse centrale |
| **C** | V04, V05, V07, V08, V09, V13, V14, F06, F07 | Le reste des écrans au même niveau |
| **D** | V10, V11, V16–V19, V21, V22, V23 | Marque, geste, calme |

---

## Smoke à faire sur le Galaxy (pas fait ici)

- Icône après réinstall (cache lanceur) : le filet or est-il entier ?
- Barre d’état : le blanc se voit-il au-dessus du crème ?
- Mois passé → Ajouter → la ligne est-elle dans le mauvais mois ? (F01)
- Photo → rouvrir la fiche : aucun aperçu (F02)
- Huit factures : mur de « FAIT ✓ » teal (V01)
- Clavier sur Divers et sur Montant : le champ reste-t-il visible ?
- Réglages : le scroll et Retour ne se battent pas avec les 3 boutons système
- Scan d’un QR non EPC : silence (V12)

---

## Fichiers concernés (pour la vague A–B)

- `app/src/main/java/be/monmoisclair/app/ui/home/HomeScreen.kt`
- `app/src/main/java/be/monmoisclair/app/ui/home/HomeViewModel.kt`
- `app/src/main/java/be/monmoisclair/app/ui/navigation/AppNavHost.kt`
- `app/src/main/java/be/monmoisclair/app/domain/usecase/AddEntryUseCase.kt`
- `app/src/main/java/be/monmoisclair/app/ui/entry/EntryFormScreen.kt`
- `app/src/main/java/be/monmoisclair/app/data/receipt/ReceiptPhotoStore.kt`
- `app/src/main/java/be/monmoisclair/app/domain/usecase/ShareBackupUseCase.kt`
- `app/src/main/java/be/monmoisclair/app/ui/theme/MonMoisClairTheme.kt`
- `app/src/main/java/be/monmoisclair/app/ui/theme/A11yTokens.kt`
- `app/src/main/res/values/themes.xml`
- `app/src/main/java/be/monmoisclair/app/MainActivity.kt`

---

## Vérification 6.18 (après correction)

Mesure : `./gradlew :app:testDebugUnitTest --rerun-tasks` → **141/141** (41 fichiers).  
Version : `0.1.0-6.18`. Smoke Galaxy toujours à faire (liste ci-dessus).

| ID | État | Note |
|----|------|------|
| F01 | ✅ | `targetYearMonth` + titre « Nouvelle entrée · Mois » + test |
| F02 | ✅ | Aperçu + « Voir la facture » + chip Facture accueil |
| F03 | ✅ | TakePicture plein cadre, côté ≥ 1600 |
| F04 | ✅ | Textes honnêtes (JSON sans photos) — pas d’archive zip |
| F05 | ✅ | « Reste après factures » / « À payer » / « Après divers » |
| F06 | ✅ | Bandeau → Réglages |
| F07 | ✅ | Tap carte → `EditTemplateScreen` |
| V01 | ✅ | FAIT secondaire |
| V02 | ✅ | shapes.medium / 12 dp |
| V03 | ✅ | Statut crème |
| V04 | ✅ | bodySmall / labelSmall / titleSmall ≥ 18 sp |
| V05 | ✅ | Dialogues Primary / Secondary / Danger |
| V06 | ✅ | Barre bas 24 dp |
| V07 | ✅ | Chips courts + année |
| V08 | ✅ | Cartes ivoire + « Mon Mois Clair · version » |
| V09 | ✅ | Empty + CTA ; chargement teal |
| V10 | ✅ | Filet or sous y=78 |
| V11 | ✅ | M teal + « Application verrouillée » |
| V12 | ✅ | Cadre ivoire + message non-EPC une fois |
| V13 | ✅ | « sauvegarde » / « et N autre(s) » corrigé |
| V14 | ✅ | AccentDanger + mot « Déficit » |
| V15 | ✅ | Un formateur `formatCents` |
| V16 | ⏸ | Polices système (TTF embarquées différées) |
| V17 | ✅ | SplashScreen API |
| V18 | ✅ | Haptic FAIT |
| V19 | ✅ | Snackbar Annuler |
| V20 | ✅ | Note + Copier sur la ligne |
| V21 | ✅ | Fréquence modèle éditable |
| V22 | ✅ | Divers repliable |
| V23 | ✅ | Sauvegarde hors Santé rappels |
