# Feedback test famille — 21 oct. 2026 (soir)

Source : Athalia après usage doigt de `0.1.0-6.38` (SM-S938B).  
But de ce fichier : **ne rien perdre**, croiser chaque demande avec le **code actuel**, et noter **comment** corriger / améliorer **sans casser** maman (accueil simple) ni Alex (dossiers).

État à la rédaction : **6.38**.  
**Livré en 6.39** (`0.1.0-6.39`) ; polish sticky **6.40** ; aperçu + PDF **6.44** (`0.1.0-6.44`, **251/251**) — les pistes « Comment faire » ci-dessous décrivent ce qui a été codé.

---

## Comment lire

| Colonne | Sens |
|---------|------|
| **Demande** | Ce qu’Athalia a vu / veut |
| **Code aujourd’hui** | Pourquoi ça fait ça (fichiers) |
| **Décision à trancher** | Produit : oui / non / variante |
| **Comment faire** | Piste concrète si on dit oui |

Ordre suggéré plus bas (§ Priorité).

---

## 1 — Signaler un bug / envoi vers Athalia (capture, texte)

### Demande
Alex ou maman doivent pouvoir **envoyer facilement** capture, texte ou autre dès qu’ils voient un problème → **directement**  
`arkalia.luna.system@gmail.com`.

### Code aujourd’hui
- **Rien** pour le support. Pas d’adresse Athalia dans l’app, pas de bouton « Signaler ».
- Les mails / partages existants sont **métier** : backup ZIP, résumé créancier, `mailto:` vers le **créancier** (`CreditorMailComposer`), pas vers Athalia.
- App **sans** permission `INTERNET` → pas d’upload serveur ; seul canal possible = intent système (Mail / Gmail / WhatsApp) qui quitte l’app.

### Décision à trancher
- Où le mettre **sans** polluer l’accueil maman ?
- Faut-il joindre version + appareil automatiquement dans le corps du mail ?

### Comment faire (recommandé)
1. **Réglages → carte « Aide »** (déjà là) : bouton secondaire  
   **« Écrire à Athalia »** / **« Un souci avec l’app ? »**.
2. Intent `ACTION_SEND` ou `ACTION_SENDTO` :
   - destinataire : `arkalia.luna.system@gmail.com`
   - sujet : `Mon Mois Clair — retour` (+ `versionName` / `versionCode`)
   - corps prérempli : mois affiché, modèle téléphone (`Build.MODEL`), 2–3 lignes vides pour le message
3. Gmail / Photos permettent d’**attacher une capture** après ouverture du brouillon (on ne force pas la caméra).
4. **Interdit** : bouton plein sur Home ; jargon « feedback » / « ticket ».

Fichiers : `SettingsScreen.kt`, éventuellement petit `SupportMailComposer.kt` (miroir de `CreditorMailComposer`).

---

## 2 — Dossiers soldés / « cloturés » (retard, huissier, accord fini)

### Demande
Quand un dossier est **terminé**, le ranger quelque part. Plus tard, pouvoir **retrouver** tout l’historique des dossiers cloturés.

### Code aujourd’hui
- Un dossier **soldé** = reste dû ≤ 0 (`DossierRemaining.isSettled`).
- Il **reste** dans **Mes dossiers** (liste : ouverts puis soldés, pastille « Soldé »).
- Les mois déjà **FAIT** restent en base ; le moteur **ne crée plus** de ligne les mois suivants (`MonthCycleEngine` + `AgreementFuturesSync`).
- **Pas** d’écran séparé « Archives ».
- **Pas** de bouton « Réouvrir » : en pratique, **ajouter des frais** ou **Remettre** un FAIT refait monter le reste.

### Décision à trancher
- Suffit-il de **séparer visuellement** (section « Soldés » repliée) dans Mes dossiers ?
- Ou faut-il une **archive** hors du compteur Menu `(N)` ?

### Comment faire
1. **Court** (peu de risque) : dans `DossiersScreen`, deux blocs — **En cours** / **Soldés (replié)**. Le compteur Menu peut rester `ouverts seulement` ou `tous` (à choisir).
2. **Moyen** : entrée Menu **« Dossiers soldés »** seulement si N_soldés > 0 (même règle zéro pixel).
3. **Ne pas** supprimer les templates soldés (sinon on perd l’historique).
4. Aide : une phrase « Un dossier soldé reste ici ; les mois payés restent dans le calendrier. »

Fichiers : `ListDossiersUseCase`, `DossiersScreen`, `HomeMenuSheet`, `DossierRemaining`.

---

## 3 — Date réelle de fin + recalcul si on paie plus (ou moins)

### Demande
Aujourd’hui : « encore environ N mois **si** … » **sans date**.  
Souhait : une **date réelle** (ex. fin vers mars 2027), et si un mois ils paient **40 €** au lieu de **20 €**, **recalculer** tout de suite.

### Code aujourd’hui
- `DossierRemaining.pacePhrase` : **estimation conditionnelle**, commentaire explicite « jamais une date », « jamais fin prévue ».
- Si **un** FAIT ≠ forfait → la phrase **disparaît** (pour ne pas mentir avec un compteur faux).
- Affichée fiche + Mes dossiers + résumé ; **jamais** sur l’accueil (charte).

C’est un **choix produit** écrit (audit §8 / plan §6), pas un oubli.

### Décision à trancher
Athalia demande l’inverse utile : **garder une projection même après un versement différent**, avec date.

Variante sûre (reste honnête) :
- Toujours dire **« vers »** / **« si le rythme revient à X »**, jamais « fini le JJ/MM certain ».
- Recalcul = `reste ÷ forfait` (ou ÷ montant moyen des FAIT) → `YearMonth` projeté.

### Comment faire
1. Étendre `DossierRemaining` :
   - `projectedEndYearMonth(remaining, forfait, intervalMonths, from: YearMonth): YearMonth?`
   - Phrase type : **« Vers mars 2027 si le versement reste de 20 €. »**
2. Si FAIT ≠ forfait : **ne plus masquer** ; recalculer avec le **reste actuel** et le **forfait du modèle** (ou dernier montant « normal »), + mention courte :  
   **« Un versement a changé : la date a été recalculée. »**
3. Afficher **sous** le reste sur fiche / Mes dossiers (pas Home).
4. Tests unitaires : 600 € / 50 € → +12 mois ; après FAIT 100 € → reste 500 → +10 mois ; forfait 0 / soldé → null.

Fichiers : `DossierRemaining.kt`, `EditEntryViewModel`, `ListDossiersUseCase`, `EntryFormScreen`, `DossiersScreen`, tests `DossierRemainingTest`.

---

## 4 — Voir les mois futurs (factures + dossiers + revenus déjà prévus)

### Demande
Pouvoir feuilleter le **futur** et y voir déjà les factures / dossiers mensuels, 3 mois, annuels, et aussi les **revenus** récurrents. Aujourd’hui ce n’est pas possible.

### Code aujourd’hui (livré **6.39** + **6.41**)
- **6.39** : Menu → **« À venir »** (liste lecture seule M+1…M+3).
- **6.41** : flèche › + chips Menu ouvrent aussi **Nov / Déc / Jan** comme un mois Home : `ensureMonthExists` matérialise factures + rentrées ; bandeau **« Aperçu — pas encore ce mois »** ; **pas** de FAIT / + Ajouter / courses.
- `AddEntryUseCase` refuse toujours d’**ajouter** dans un mois futur (sécurité).
- Bandeau « À prévoir… » + Réglages → Échéances : toujours là.

### Décision
**B + A lecture seule** (choix Athalia après test) — plafonné à 3 mois.

### Comment faire — fait
1. `MonthNavigation.FUTURE_MONTHS = 3` · `isFuturePreview`
2. `HomeMonthChoices` : futurs en tête du Menu
3. Home : aperçu read-only
4. Écran « À venir » conservé pour la vue liste des 3 mois

Fichiers : `MonthNavigation`, `HomeViewModel`, `HomeScreen`, `HomeMenuSheet`, `ListUpcomingMonthsUseCase`.

---

## 5 — Bannière d’accueil : épargne visible + modifier / retirer avec phrase d’accroche

### Demande
Sur la bannière du haut (sticky) :
- voir s’il y a un montant **Mettre de côté** ;
- pouvoir **modifier** ce montant (ex. après un retrait réel à la banque) pour que le carnet reste juste ;
- message d’avertissement / accroche **uniquement** quand ils **retirent** ou baissent l’épargne  
  (ex. « Un euro par mois peut tout changer » / « Important de refaire une épargne »).

### Code aujourd’hui
- Sticky = Reste, À payer, Après les FAIT, Après courses — **pas** l’épargne.
- Épargne = Réglages → « Mettre de côté » → devient une **facture mensuelle** sur la timeline (`ConfigureSavingsUseCase`).
- Modifier / retirer = Réglages (Activer / Mettre à jour / Retirer) ou fiche de la ligne.
- Alerte déficit **avant** enregistrement d’un montant trop haut : déjà là.
- Charte : *tant que ce n’est pas activé, l’accueil ne change pas* — une fois activé, la ligne apparaît dans la liste, pas dans le sticky.

### Décision à trancher
Montrer l’épargne dans le sticky **contredit** un peu « Home freeze », mais Athalia le demande après test réel.

### Comment faire
1. Si `savingsActive` : ligne sticky discrète  
   **« Mis de côté : X € / mois »** (tap → Réglages carte épargne ou fiche).
2. Sur **Retirer** / baisse de montant : dialogue  
   titre + phrase d’accroche + **Confirmer** / Annuler  
   (textes dans `HowItWorksCopy` / `DeviceCareCopy` style, pas de « huissier »).
3. Modification du montant : déjà « Mettre à jour » en Réglages — raccourcir le chemin depuis le sticky.
4. Ne pas inventer une cagnotte : rester une **facture** vers un autre compte (aide déjà claire).

Fichiers : `HomeScreen` (sticky), `HomeViewModel`, `ConfigureSavingsUseCase`, `SettingsScreen` / `SettingsViewModel`, `HowItWorksCopy`.

---

## 6 — Mieux voir sur l’accueil les factures mensuelles / 3 mois / annuelles

### Demande
Repérer plus facilement le type de récurrence (couleur ou autre) sur l’écran d’accueil.

### Code aujourd’hui
- Pastilles Home : **Accord**, **Prélèvement**, **En retard**, **FAIT le …**, photo.  
- **Pas** de pastille Mensuel / Trimestre / An.
- La fréquence se choisit sur la **fiche** (`RecurrenceLabel` chips).

### Décision à trancher
Pastilles rythme sur **toutes** les lignes = risque clutter pour maman.  
Alternative : seulement pour intervalle **≠ 1** (les rares taxes / assurances).

### Comment faire
1. **Discret** : sous le montant, une ligne muted `tous les 3 mois` / `1× par an` si `intervalMonths != 1` (données déjà sur le template ; à remonter dans `TimelineItem`).
2. Ou pastille outlined secondaire (même famille visuelle qu’Accord, autre teinte Paper).
3. Garder l’ordre **par jour** (charte) — pas de re-tri par type.
4. Contraste à valider doigt (pastille Accord déjà volontairement discrète).

Fichiers : `TimelineItem`, `HomeViewModel` (build timeline), `HomeScreen`, éventuellement `A11yTokens`.

---

## 7 — Courses / divers : trop bas + parcours un peu compliqué

### Demande
Le bloc courses est **tout en bas** → peu pratique. Remonter (bouton / coin). Revoir si le suivi divers est assez simple.

### Code aujourd’hui
- `HomeScreen` : item `divers-controls` **après** toute la timeline (`LazyColumn`).
- Replié : « Noter des courses » → ouvrir → champ + Ajouter / Retirer.
- Sticky « Après courses » seulement si montant > 0.
- Divers **hors** du Reste après factures (voulu).

### Décision à trancher
Charte historique : factures d’abord, divers en dessous. Athalia priorise l’accès **rapide**.

### Comment faire
1. **Accès** : chip / bouton secondaire sous le sticky (ou à côté de Chercher) **« Courses »** → scrollTo `divers-controls` **ou** bottom sheet dédié (montant + Ajouter / Retirer + total noté).
2. Garder le détail en bas **ou** uniquement en sheet (moins de scroll).
3. Clarifier les libellés : **« J’ai dépensé »** / **« J’ai retiré une erreur »** si « Ajouter / Retirer » reste opaque.
4. Ne pas mélanger divers et Reste (règle métier).

Fichiers : `HomeScreen.kt`, `HomeViewModel`, `AdjustDiversUseCase`, aide `HowItWorksCopy`.

---

## 8 — Envoyer / mettre à jour l’APK sans brancher le téléphone

### Demande
Pouvoir envoyer l’app et les **mises à jour** à Alex / maman (mail ou autre) **sans** récupérer leur téléphone sur le Mac.

### Code aujourd’hui
- Distribution = **sideload release signé** (`assembleRelease` → APK ~14 Mo ARM).
- Install doc = `adb install -r` (USB).
- **Pas** Play Store (hors produit écrit).
- App **sans** Internet → pas de mise à jour OTA in-app.
- Partage in-app = backup / résumé, **pas** l’APK.

### Décision à trancher
Play Internal testing un jour ? Ou rester 100 % hors store ?

### Comment faire (sans casser, dès maintenant)
1. **Processus famille** (1 page `docs/` ou mail type) :
   - Athalia build release sur le Mac ;
   - envoie `MonMoisClair-release.apk` par **Drive / WhatsApp / Mail** (pièce jointe si < limite) ;
   - sur le Galaxy : ouvrir le fichier → autoriser install apps inconnues (une fois) → Installer ;
   - mise à jour = **même signature** (même keystore) → « Mise à jour » écrase l’ancienne **sans** perdre les données Room.
2. **Pièges à écrire clairement** :
   - ne jamais envoyer l’APK **debug** ;
   - ne pas rétrograder un schéma Room (ex. APK figé en 6 sur un tel en schéma 8) ;
   - taille ~14 Mo : Mail parfois refuse → préférer Drive / WhatsApp.
3. **Option plus tard** : Play Store **tests internes** (compte Google famille) = updates auto, plus d’APK à la main — décision produit séparée.
4. **Ne pas** ajouter un bouton « Télécharger la mise à jour » dans l’app (pas d’INTERNET).

Fichiers / docs : `README.md`, `docs/testeurs/`, nouveau `docs/DISTRIBUTION_FAMILLE.md` (à créer quand on formalise).

---

## Priorité suggérée (pour ne pas tout mélanger)

| Prio | Point | Effort | Risque maman |
|------|-------|--------|--------------|
| P0 | **1** Feedback mail Athalia | Petit | Faible (Réglages) |
| P0 | **8** Doc envoi APK sans USB | Doc | Nul code |
| P1 | **7** Courses accessibles | Moyen | Moyen (Home) |
| P1 | **2** Section Soldés claire | Petit | Faible |
| P2 | **3** Date « vers … » + recalcul | Moyen | Faible si hors Home |
| P2 | **5** Sticky épargne + dialogue retrait | Moyen | Moyen |
| P3 | **6** Rythme visible Home | Petit | Moyen (clutter) |
| P3 | **4** Vue mois futurs | Gros | Élevé |

---

## Phrases d’accroche (brouillon, point 5)

À n’afficher **que** sur baisse / retrait d’épargne, pas en permanence :

- « Même 1 € par mois, remis de côté, change la suite du mois. »
- « Si vous retirez tout, pensez à en remettre un peu dès que possible. »
- « Ce n’est pas dans l’app : c’est sur votre autre compte. L’app note juste la ligne. »

À valider avec Athalia (ton maman, pas moralisateur).

---

## Hors scope (rappel)

Toujours refusés sauf nouvelle décision écrite : Play public, banque, cloud sync, graphiques, snowball, PDF CPAS, pastille « Huissier », date de fin **certaine** sans « si/vers ».

---

## Suite

1. Athalia coche / amende les **Décision à trancher**.  
2. On ouvre des étapes journal (`6.39`…) **une par une**, tests `--rerun-tasks`, pas de claim avant vert.  
3. Pas d’automation adb pendant qu’ils utilisent le téléphone (leçon campagne 21 oct.).
