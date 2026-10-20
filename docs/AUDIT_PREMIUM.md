# Audit premium — points encore ouverts

Date : 2026-10-20.  
Code : étape **6.24** (`0.1.0-6.24`).  
Mesure tests : **159/159** (46 fichiers, Gradle `--rerun-tasks`). Cette note ne remplace pas un passage au doigt sur le Galaxy.

But : ce qu’il reste à corriger pour que l’app se lise comme un objet fini, pas comme un chantier.  
Hors sujet (charte, ne pas les ajouter) : banque, cloud, pub, graphiques, report automatique du solde, troisième bouton sur l’accueil.

Les lots 6.16–6.22 tiennent déjà : mois d’ajout, reste nommé, un seul bouton teal, photo dans l’app, polices Source, ancre trimestrielle, rappels (jour 31, titres, action FAIT), recherche, divers trop retirés, archive ZIP, restauration qui dit si les photos sont revenues.

## Repères (pour ne pas tourner en rond)

Comparé à des carnets du même genre, pas à une banque.

| Repère | Ce qu’ils font | Ici |
|--------|----------------|-----|
| [AllPaid](https://lagerland-apps.github.io/apps/allpaid/) | Factures sans banque. « Payé » d’un geste, y compris depuis la notification, avec retour arrière immédiat. Recherche qui se vide. | FAIT dans l’app a un « Annuler » très court. Le FAIT de la notification ne revient pas en arrière. |
| [Wayflyer — réversibilité](https://design.wayflyer.com/latest/design-guides/reversibility-7dUsBU7U) | Annuler au moins 5 secondes pour un geste courant. Confirmer seulement ce qui détruit, en nommant la chose. Ne pas perdre un formulaire en le quittant. | Snackbar FAIT = `Short` (environ 4 s). Supprimer confirme, sans le nom (C05). Le geste Retour jette la fiche. |
| [Appthetics](https://www.appthetics.com/blog/budgeting-apps-ux-patterns) | Un état vide dit la suite. Une alerte dit l’action, pas seulement le fait. | Mois vide : déjà une phrase + le bouton du bas. « À prévoir » ne dit pas « Voir » (A09). |

**Ne pas copier**, même si ces apps les ont : connexion bancaire, graphiques, catalogue Netflix, cloud, widget d’écran d’accueil, couleurs du fond d’écran. La charte les écarte. Les noter à nouveau ne fait pas avancer le produit.

---

## Lecture

### A01 — Le menu affiche une seule année au-dessus de deux ans de mois — **corrigé**

**Vu dans** `HomeMenuSheet`. Le titre sous « Mois » était `current.year` (ex. 2026). Les puces remontaient 24 mois (`HomeMonthChoices`). « Déc 2025 » se trouvait sous l’en-tête « 2026 ».

**Fait.** Puces groupées par année (`yearsInOrder` : 2026 → 2025 → 2024). Chaque groupe a son en-tête. Chips = mois seul (`monthChipShort`).

### A02 — « EN RETARD » crie sur la carte — **corrigé**

**Vu dans** `HomeScreen` (`StatusChip`) et dans `HowItWorksCopy` (titre de section).

**Fait.** Badge « En retard » (ivoire sur brique). Titre d’aide aligné.

### A03 — Courses et divers ne portent pas le même nom — **corrigé**

**Vu dans** `HomeScreen` et `HowItWorksCopy`.

**Fait.** Un seul mot : courses. Boutons « Retirer » / « Ajouter ». Champ « Montant (€) ». Lignes « Après courses ». Aide idem.

### A04 — « Auto » ne dit pas ce que c’est — **corrigé**

**Vu dans** `HomeScreen`, pastille or.

**Fait.** Pastille « Prélèvement » (fond or, encre noire).

### A05 — Le menu ne défilait pas — **corrigé**

**Fait.** `HomeMenuSheet` défile (`verticalScroll`). Les outils restent atteignables en grand texte.

---

## Langue

### B01 — Des écrans parlent encore en jargon — **corrigé**

**Vu dans** `ReminderHealthScreen`, `PermissionsSetupScreen`, `HowItWorksCopy`, menu, Réglages.

**Fait.** Un nom : « Alertes » (menu, santé, réglages, aide). Phrases : notifications, réveil à l’heure, batterie non limitée. Timing : « 3 jours avant à 10 h, le jour même à 9 h, puis chaque matin à 10 h tant que ce n’est pas payé. » Samsung : « Dans Samsung, retirez Mon Mois Clair de la mise en veille. »

### B02 — La version affichée ressemble à un numéro de chantier — **corrigé**

**Vu dans** `SettingsScreen`.

**Fait.** Affiche « Mon Mois Clair » puis, en dessous, l’étape (`6.22`) via `AppVersionDisplay`. Pas de `0.1.0-` à l’écran. `versionName` Gradle inchangé.

---

## Fiabilité des phrases

### C01 — Un échec de dossier ou d’envoi peut montrer du texte technique — **corrigé**

**Vu dans** `PersistSafFolderUseCase` et `ShareBackupUseCase`.

**Fait.** Phrases fixes (`SafFolderMessages`, `ShareBackupMessages`). Détail technique dans le log seulement.

### C02 — Après une restauration, le bouton dit « OK » — **corrigé**

**Vu dans** `RestoreBackupScreen`.

**Fait.** Bouton « Voir le mois » (même geste).

### C03 — Un échec d’empreinte affiche la phrase du système

**Vu dans** `BiometricAuthGatewayImpl.onAuthenticationError`. Hors annulation, l’écran montre `errString` tel quel. Selon le téléphone, c’est une phrase Android, parfois en anglais.

**Correction.** Phrase fixe : « Déverrouillage impossible. Réessayez, ou utilisez le code du téléphone. » Le code d’erreur reste dans le log.

### C04 — Sur un mois passé, le bouton ne dit pas FAIT

**Vu dans** `HomeScreen`, dialogue « Mois passé ». On a touché « FAIT » ou « Remettre ». Le bouton de confirmation dit « Modifier ».

**Correction.** « Marquer FAIT » ou « Remettre à payer », selon le geste. « Annuler » reste.

### C05 — Supprimer ne nomme pas la ligne

**Vu dans** `EditEntryScreen`. Le dialogue dit « Supprimer cette entrée ? » sans le nom. Wayflyer : nommer l’action (« Supprimer Loyer ? »), pas « Confirmer ».

**Correction.** Titre avec le nom saisi. Garder la phrase qui dit si les mois suivants partent aussi.

---

## Lecture (encore ouvert)

Le menu défile (`verticalScroll`) et les années sont groupées : ne pas rouvrir A01 ni A05.

### A06 — La photo de facture occupe la moitié de la fiche

**Vu dans** `EntryFormScreen`. L’aperçu fait au minimum 280 dp, jusqu’à 480 dp, alors qu’un dialogue existe déjà.

**Correction.** Vignette d’environ 120 dp et « Touchez pour agrandir ». Le grand format reste le dialogue.

### A07 — La fiche d’ajout a quatre titres du même poids

**Vu dans** `EntryFormScaffold`. Le titre d’écran et « Fréquence », « Prochaine échéance », « Facture papier » sont tous en `titleLarge`. Les autres écrans utilisent `headlineMedium` pour le titre de page.

**Correction.** Un seul titre d’écran, en `headlineMedium`. Les blocs en `bodyLarge`, encre atténuée.

### A08 — Un revenu crie plus fort qu’une facture

**Vu dans** `IncomeRow`. Le montant est en `titleLarge` vert. Le Reste, en serif, doit rester le seul chiffre dominant.

**Correction.** Même taille et même encre que les factures. Le vert reste pour « FAIT » et pour « À payer » à zéro.

### A09 — « À prévoir le mois prochain » ne se voit pas comme un bouton

**Vu dans** `HomeScreen`. Texte ambre cliquable, sans mot d’action. Appthetics : une alerte dit quoi faire.

**Correction.** Finir par « Voir ». Zone d’au moins 48 dp.

### A10 — La recherche ne se vide pas d’un geste

**Vu dans** `HomeScreen`. AllPaid : la recherche se vide d’un tap. Ici il faut effacer le mot à la main, et le clavier cache la barre du bas.

**Correction.** Une croix dans le champ dès qu’il y a du texte.

### A11 — « Copier » allonge chaque facture qui a une communication

**Vu dans** `ExpenseRow`. Un bouton de 48 dp s’ajoute sous la note.

**Correction.** « Copier » sur la même ligne que la communication, toujours ≥ 48 dp. Pas de swipe.

### A12 — « À payer 0,00 € » ne dit pas que c’est fini

**Vu dans** `HomeScreen`. S’il ne reste rien, la ligne passe au vert mais garde « À payer 0,00 € ».

**Correction.** S’il y a des factures et que le reste à payer vaut 0 : « Tout est payé ». Mois vide : ne pas dire ça.

---

## Langue (encore ouvert)

### B03 — « EPC » n’est pas un mot de facture

**Vu dans** `AddEntryViewModel` et `HowItWorksCopy`. Le scan dit déjà « Ce QR n’est pas une facture belge. »

**Correction.** La même phrase partout. L’aide dit « le QR de la facture belge », sans EPC.

### B04 — La sauvegarde parle en statut machine

**Vu dans** `BackupHealthCopy`. « État : sauvegarde OK. »

**Correction.** « La copie dans Documents est à jour. » « La copie ne s’écrit plus. Choisissez à nouveau le dossier. » « Aucun dossier Documents choisi. »

### B05 — « Mois d’ancre » est un mot de code

**Vu dans** `EditTemplateScreen`. Titre « Mois d’ancre (prochaine échéance) ». Puces « Jan. » alors que l’ajout écrit « Janvier ».

**Correction.** « Mois de la prochaine échéance ». Mois en toutes lettres. L’année affichée reste.

---

## Geste papier

### D01 — Scanner une facture sans lumière

**Vu dans** `ScanEpcScreen`. Pas de lampe. Une facture papier se scanne souvent le soir.

**Correction.** Bouton « Lampe », contour. Flash allumé le temps du scan, éteint en quittant.

### D02 — Caméra refusée : plus de sortie

**Vu dans** `ScanEpcScreen`. Après un refus définitif, Android n’affiche plus la demande. Le bouton relance une demande qui ne vient pas. Pas de lien vers les réglages de l’app.

**Correction.** « Ouvrir les réglages » (contour) vers la page de Mon Mois Clair.

---

## Trous de geste

### E01 — Quitter la fiche jette ce qui a été saisi — **corrigé**

**Fait.** `BackHandler` + dialogue « Quitter sans enregistrer ? » (Rester / Quitter) sur ajout et édition. Même geste pour Annuler.

### E02 — Une échéance qui n’est pas ce mois-ci ne peut pas être retirée — **corrigé**

**Fait.** `ArchiveExpenseTemplateUseCase` + bouton « Retirer cette échéance » (confirmation avec le nom). Désactive le modèle ; mois déjà notés inchangés.

### E03 — « Annuler » après FAIT disparaît trop vite

**Vu dans** `HomeScreen`. `SnackbarDuration.Short`, environ 4 secondes. Wayflyer : au moins 5 secondes. « Remettre » n’a pas d’annulation : les rappels repartent.

**Correction.** Bandeau environ 10 secondes (`SnackbarDuration.Long`) après FAIT et après Remettre. Le texte dit ce qui vient d’être fait.

### E04 — FAIT sur la notification ne se reprend pas

**Vu dans** `ReminderReceiver.handleMarkDone`. AllPaid revient en arrière juste après, aussi depuis la notification. Ici il faut ouvrir l’app et toucher « Remettre ».

**Correction.** Une notification courte « Loyer marqué FAIT » avec l’action « Annuler ». Pas une seconde façon de payer.

### E05 — La flèche vers le passé n’a pas de fin

**Vu dans** `MonthNavigation.canGoPrev` (toujours vrai) et `HomeViewModel.prevMonth`. Chaque tap crée un mois vide en base. Le menu s’arrête à 24 mois.

**Correction.** Même borne : 24 mois. Au-delà, la flèche est inactive. Ne pas créer de mois plus loin.

### E06 — L’erreur d’enregistrement est au-dessus du pli

**Vu dans** `EntryFormScaffold`. « Vérifiez le montant et le jour » est dans le défilement, sous la grille des 31 jours. « Enregistrer » est en bas. On dirait que le bouton ne fait rien.

**Correction.** L’erreur juste au-dessus d’Enregistrer, toujours visible.

### E07 — Un montant avec trois décimales est coupé en silence — **corrigé**

**Fait.** `parseToCents` refuse >2 décimales (`null`).

### E08 — La photo prise reste dans le cache — **corrigé**

**Fait.** Le jpg temporaire est effacé après lecture ou annulation.

---

## À vérifier au doigt (pas un correctif de code)

À faire sur le Galaxy, dans cet ordre. Tant que ce n’est pas fait, ne pas écrire « validé téléphone » dans le journal.

1. Notification avec au moins 4 factures : la bonne ligne est sous le doigt.
2. Échéance « tous les 3 mois » : corriger seulement le nom, les mois suivants restent.
3. Mois vide : un seul bouton teal.
4. Recherche « zzzz » : une phrase, les totaux du haut inchangés.
5. Retrait de courses plus grand que le total : message qui dit le montant réel.
6. Splash : M teal entier.
7. Menu → Restaurer, et Menu → Comment ça marche.
8. Envoyer une copie : archive avec photos ; restaurer cette archive et lire le message sur les photos.
9. Menu → mois : années groupées (2026 / 2025 / …), pastilles En retard / Prélèvement, courses unifiées.
10. Échec dossier / envoi : phrase fixe (pas de jargon SAF).
11. Restaurer → « Voir le mois ». Réglages : « Mon Mois Clair » + étape, pas `0.1.0-`.
12. Fiche à moitié remplie : le geste Retour demande confirmation. Échéance annuelle hors mois : on peut la retirer.
13. FAIT : « Annuler » reste longtemps. FAIT depuis la notification : une action Annuler.
14. Scan : lampe, et caméra refusée → réglages.

---

## Ordre

| Ordre | Ids | Pourquoi | État |
|-------|-----|----------|------|
| 1 | C01, A01–A05, B01, B02, C02 | Phrases techniques, menu, courses, prélèvement, alertes, version | **corrigé** |
| 2 | E02, E01, E08 (+ E07) | Échéance impossible à retirer, fiche jetée, photo dans le cache | **corrigé** |
| 3 | E03, E04, E05, E06 | Annuler trop court, mois sans fin, erreur cachée | ouvert |
| 4 | A06, A07, A11, A12 | Fiche, cartes, « tout est payé » | ouvert |
| 5 | A08, A09, A10, B03–B05, C03–C05, D01, D02 | Mots, recherche, scan | ouvert |
| 6 | Liste téléphone | Après le code | ouvert |
