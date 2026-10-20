# Audit premium — suite

Date : 2026-10-20.  
Fait suite à `docs/AUDIT_PREMIUM_2026-10-20.md`.  
Version lue ensuite dans `app/build.gradle.kts` : **0.1.0-6.21**.  
Le journal indique **6.21** comme dernière étape close (S04 archive + smoke Galaxy).

Ce fichier garde la trace : ce que le premier audit demandait, ce qui est dans le code.

Comptage confirmé avant push 6.19 : **141/141** (41 fichiers XML, 0 échec).

---

## 1. Premier audit — état

| Id | Demande | État dans le code |
|----|---------|-------------------|
| F01 | Ajouter dans le mois affiché | Tenu. Route `entry/add/{ym}`, `AddEntryUseCase.targetYearMonth`, titre « Nouvelle entrée · … » si ce n’est pas le mois courant. Le futur est refusé. |
| F02 | Revoir la photo | Tenu. Aperçu sur la fiche. Puce « Photo » et ouverture depuis la ligne. |
| F03 | Photo lisible | Tenu. Plus de miniature système. Côté long 1600 px, WebP qualité 88. |
| F04 | Copie envoyée sans les photos | Tenu. L’envoi est une archive ZIP (mois + photos). Les originaux restent aussi dans Documents. |
| F05 | Nommer le gros chiffre | Tenu. « Reste après factures » ou « Déficit après factures », puis « À payer », puis « Après divers » s’il y en a. |
| F06 | « À prévoir » actionnable | Tenu. La phrase ouvre Réglages. |
| F07 | Modifier une échéance hors mois | Tenu. La carte ouvre l’édition du modèle. |
| V01 | Un seul bouton teal sur l’accueil | Tenu. « FAIT ✓ » / « Remettre » sont en contour. « + Ajouter » reste le plein. |
| V02 | Coins 12 dp | Tenu sur le thème, les cartes, le menu, la caméra. |
| V03 | Barre d’état crème | Tenu. `#F7F4EB` dans le thème et dans `MainActivity`. |
| V04 | Libellés de champ ≥ 18 sp | Tenu. `bodySmall` / `labelSmall` / `titleSmall` à 18 sp. |
| V05 | Dialogues dans le système de boutons | Tenu sur l’accueil. La suppression de fiche n’utilise plus `TextButton`. |
| V06 | Barre du bas à 24 dp | Tenu. |
| V07 | Puces de mois courtes | Tenu. `DayLabelFormat.monthChip`. |
| V08 | Réglages en cartes + version | Tenu. `SettingsCard`, ligne « Mon Mois Clair · version ». |
| V09 | États vides calmes | Tenu dans l’esprit (texte, pas d’illustration). |
| V10 | Filet or dans la zone sûre | Tenu. Le M s’arrête vers y=70, le filet vers y=73–77. |
| V11 | Marque sur l’écran verrouillé | Tenu. `ic_mark` (M teal) au-dessus du titre. |
| V12 | Cadre de scan + QR non belge | Tenu. Cadre sur la preview. Message « Ce QR n’est pas une facture belge. » |
| V13 | « autre(s) » et « JSON » | Tenu. « 1 autre » / « N autres ». Restaurer ne dit plus JSON. |
| V14 | Reste négatif lisible | Tenu. Le chiffre négatif est en brique (`AccentDanger`), avec le mot « Déficit ». |
| V15 | Un seul format d’euro | Tenu. `1 234,50 €`, signe `−`. |
| V16 | Police choisie, pas le serif système | **Fait en 6.20.** Source Serif 4 + Source Sans 3 (OFL). |
| V17 | Splash avec le M | Tenu, après correction : le splash pointait vers `ic_mark`, fichier absent (M ivoire invisible sur crème, et ressource manquante). `ic_mark.xml` est le M teal + filet or. |
| V18 | Retour tactile | Tenu sur FAIT. |
| V19 | Annuler après FAIT | Tenu. Bandeau « Marqué FAIT » / Annuler. |
| V20 | Communication sur la ligne | Tenu. Note visible, « Copier » depuis la carte. |
| V21 | Changer la fréquence | Tenu sur l’édition du modèle / de la fiche récurrente. |
| V22 | Divers replié | Tenu. Le total est visible, le formulaire s’ouvre au tap. |
| V23 | Sauvegarde hors écran des rappels | Tenu. Santé des rappels ne liste plus « Sauvegarde Documents ». Si le dossier manque, une ligne renvoie aux Réglages. |

Correction en plus, après relecture : le bandeau « Annuler » utilisait l’or `#D99B26` comme couleur de lettres. La charte l’interdit. L’action est maintenant en ivoire sur l’encre.

---

## 2. Deuxième passe — ce qui n’est pas encore « objet cher »

Ces points ne figuraient pas comme bugs ouverts dans le premier tableau, ou ils restent après correction.

### S01 — Ouvrir la photo dépend d’une autre app

**Fait en 6.19.** Accueil : Dialog Compose + `loadReceiptBitmap`. Fiche édition : même Dialog (plus d’`ACTION_VIEW`).

### S02 — Le message « Copié » et le bandeau FAIT se partagent le même snackbar

**Fait en 6.19.** Pendant `faitUndoActive`, les messages info (« Copié ») sont ignorés.

### S03 — Police encore système

**Fait en 6.20.** Source Serif 4 (display) + Source Sans 3 (corps), fichiers dans `res/font/`, licences `docs/licenses/`.

### S04 — Le partage de sécurité ne peut pas reconstruire les photos

**Fait en 6.21.** « Envoyer une copie » construit `mon-mois-clair-securite.zip` (mois + photos webp). Restauration URI accepte ZIP ou JSON. Textes Réglages / Comment ça marche alignés. Au-delà de 45 Mo : copier le dossier Documents.

### S05 — Smoke téléphone toujours ouvert

Encore ouvert. À faire au doigt (Galaxy), indépendamment du code 6.21.

### S06 — « Courses / divers » ne dit pas s’il est ouvert

**Fait en 6.19.** Fermé : « Noter des courses » + « Ouvrir ». Ouvert : « Courses / divers » + « Fermer ».

---

## 3. Prochaine vague (quand on code encore)

1. ~~S01~~ ~~S02~~ ~~S03~~ ~~S04~~ ~~S06~~ (6.19–6.21).  
2. S05 smoke Galaxy (au doigt).

Hors vague, comme dans le premier audit : banque, graphiques, report automatique, troisième bouton, fiche intermédiaire.
