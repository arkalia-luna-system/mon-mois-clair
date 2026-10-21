# Audit — maman, Alex, et le brief « dossiers »

Date : 2026-10-21.  
Code lu : étape **6.27** (`0.1.0-6.27`), schéma Room / backup **3**, tests annoncés au journal **165/165**.  
Cette note ne relance pas Gradle et ne change pas l’app. Elle dit ce que le code fait vraiment, ce que le brief demande, et ce qu’il faut garder après comparaison avec d’autres outils (voir [`RECHERCHE_OUTILS_2026-10-21.md`](RECHERCHE_OUTILS_2026-10-21.md)).  
Le plan qui en sort : [`PLAN_PRODUIT_FINI.md`](PLAN_PRODUIT_FINI.md).

Deux passages sont dans ce fichier. La partie 1 est le constat brut. La partie 5 est la relecture : ce qu’il ne faut **pas** coder même si le brief le demande.

---

## 0. Ce qu’on sait des deux personnes (mis à jour par Athalia)

| | Maman | Alex | Athalia |
|--|--------|------|---------|
| Téléphone | Galaxy **S26**, écran normal **6,3″** | Galaxy **S25 Ultra**, le même modèle qu’Athalia | S25 Ultra `SM-S938B` |
| Écran dans l’app | Déjà la cible n°1 (charte §6, `DisplayFit`) | **Le même affichage** qu’Athalia. Pas un troisième layout | Confort si l’écran est haut |
| Usage actuel voulu | Carnet du mois : loyer, factures, bouton **FAIT** | Le même carnet, plus les retards | Celle qui construit |
| Papiers en retard | En général un **e-mail** au créancier, puis un **virement** quand l’argent est là | Idem | — |
| Épargne | N’y pense pas. N’irait pas « rechercher » même 1 € mis de côté | Idem | L’idée à ajouter, sans en faire un second produit |

Ce n’est **pas** « uniquement des plans déjà négociés à montant fixe ».  
C’est les deux : un accord mensuel quand il existe, **et** des retards payés par virement le mois où l’argent est là, après un mail. Le modèle « 12 mensualités rigides » ne couvre pas la vraie vie. Le modèle « je verse ce que je peux, sans date » non plus : ils veulent être aidés à **payer à temps**.

Les deux téléphones sont des phones portrait **Compact** (largeur &lt; 600 dp). La charte interdit déjà `if (model == Ultra)`. On ne crée pas un écran Alex.

---

## 1. Constat du code (premier passage)

### 1.1 Ce qui est déjà le bon outil

L’accueil est un **carnet du mois**, pas un tableau de dettes. C’est verrouillé (charte §1) et c’est la bonne base pour maman.

| Besoin du brief | Déjà dans l’app | Où |
|-----------------|-----------------|----|
| Réserver l’argent dès le 1er, avant qu’il sorte de la banque | **Oui.** `remainingCents` = revenus − **toutes** les factures, FAIT ou non | `MonthSummaryCalculator` |
| Ne pas mélanger les courses avec ce chiffre | **Oui.** Les courses sont à part (« Après courses ») | `month_ledgers.diversSpentCents` |
| Voir ce qui n’est pas encore parti | **Oui.** « À payer » et « Sans les futures » | `StickyStillToPayLabel` |
| Cocher quand c’est payé, avec la date | **Oui.** FAIT + `doneAtEpochMs` | instances |
| Changer **ce mois-ci seulement** (30 € au lieu de 50 €) | **Oui**, tant que « Aussi les mois suivants » n’est pas coché | aide + édition |
| Rappeler avant, le jour J, puis chaque matin si ce n’est pas FAIT | **Oui** | `ReminderScheduler` |
| Copier une communication pour la banque | **Oui**, sur la ligne et dans la fiche | `paymentNote` |
| Photographier le papier, WebP, fichier SAF, pas dans le JSON | **Oui.** `receipts/{id}.webp`, archive ZIP | `ReceiptPhotoStore` |
| Lire un QR de facture belge (nom, montant, communication) | **Oui.** L’IBAN est **lu** puis **jeté** | `EpcQrParser` + `applyEpc` |
| Ne rien afficher qui ne sert pas | **Déjà le réflexe.** Courses masquées si 0. « Sans les futures » seulement s’il reste à payer. Menu = Réglages + mois | charte 6.27 |
| Les deux Galaxy | **Oui**, via la hauteur d’écran, pas via le nom du modèle | `DisplayFit` |
| Déficit visible sans rouge sang | **Oui.** Brique `#8E3418`, libellé « Déficit après factures » | accueil |

Le profil « optimiste qui oublie les prélèvements du 12 au 25 » est **déjà** traité par le gros chiffre. Il ne faut pas le refaire.

### 1.2 Ce qui manque pour Alex (et pour un retard de maman)

Rien dans Room ne représente un **dossier**.

`expense_templates` a : nom, montant, jour, actif, rappels, prélèvement auto, intervalle, ancre, dates.  
Pas de : créancier, cause, dette totale, reste, IBAN, e-mail, priorité, statut « en attente d’accord », plusieurs courriers.

`expense_instances` a la communication et **une** photo. Elles sont collées au **mois**, pas au dossier.

Conséquence concrète, déjà vraie aujourd’hui, avant même Alex :

`MonthCycleEngine` recrée la facture du mois suivant **sans** recopier `paymentNote` ni `receiptRelativePath` (lignes qui construisent `ExpenseInstanceEntity`).  
Une photo prise en octobre sur « Assurance » ne sera **pas** sur la ligne de janvier. La communication non plus, sauf si on la retape. Pour un loyer, maman s’en sort. Pour une lettre d’huissier qu’on a rangée, c’est le trou du brief.

Autres trous mesurés dans le code :

| Trou | Effet réel |
|------|------------|
| Un seul libellé | « Huissier Leroy » ne dit pas hôpital, eau ou amende |
| IBAN parsé puis oublié | Il faut le rechercher sur la photo à chaque virement |
| Une seule photo par ligne de mois | Un avenant (frais ajoutés) n’a pas de place à côté de la première lettre |
| Pas de reste dû | Cocher FAIT ne fait baisser que « À payer » du mois. Le « bout du tunnel » n’existe pas |
| Pas d’arrêt quand c’est soldé | Une mensualité active continue le mois d’après, même si la dette est finie |
| Pas d’e-mail | Ils écrivent au créancier dans l’app Mail. L’app ne prépare ni l’adresse, ni l’objet, ni la référence |
| Pas de priorité | Loyer, électricité et accord d’huissier ont le même poids visuel. Le tri reste le **jour** (c’est voulu) |
| Pas d’épargne | Rien n’invite à virer 5 ou 10 € vers un compte à part |
| Pas de résumé à montrer | Un médiateur (CPAS, service agréé) demande revenus, charges, et pour chaque dette : créancier, nature, solde, arrangement. L’app a le mois, pas la liste des dettes |
| Schéma backup **3** | Toute colonne nouvelle casse la restauration si on oublie migration + `CURRENT_SCHEMA_VERSION` |

### 1.3 Ce que le brief casserait si on le codait mot pour mot

| Idée du brief | Pourquoi c’est dangereux ici |
|---------------|------------------------------|
| Badge sur la ligne du mois : « Mensualité 3 sur 12 · **Reste 450 €** » | Le brief dit aussi de ne **jamais** mettre la dette globale sur l’accueil. Ce badge la remet, une fois par dossier. Quatre dossiers = quatre restes sous les yeux, sur l’écran que maman ouvre tous les jours |
| 4ᵉ pastille à côté de « Une fois / Tous les mois / 3 mois / an » | Maman voit « Dossier (Huissier…) » **à chaque** ajout. La charte vient de retirer le fouillis du menu pour cette raison |
| Onglet « Mes Dossiers » à côté de « Mon Mois » | La barre du bas est **Menu · + Ajouter**, à cause du S26. Un troisième onglet est déjà refusé |
| Barre de progression + « Fin prévue : avril 2027 » comme une promesse | Si novembre est à 30 €, ou si l’étude ajoute des frais, la date est fausse. Une date fausse fait pire qu’aucune date |
| Compression imposée ~80 Ko | Le code vise un cadre **lisible** (2048 px, qualité 90) pour l’IBAN et la communication. 80 Ko peut rendre la lettre illisible. Le plafond ZIP est déjà 45 Mo |
| Dossier `pieces/` | Le dossier réel est `receipts/`. Le renommer casse les sauvegardes déjà sur le téléphone. On garde `receipts/` |
| « Rompre l’accord annule le plan et déclenche la saisie » écrit dans l’app | Phrase juridique trop forte. La conséquence dépend de l’accord. L’app ne doit pas faire peur avec une règle qu’elle ne peut pas vérifier |
| Snowball, avalanche, taux, graphiques, coach | Les apps de dettes américaines optimisent des **cartes revolving**. Ici le danger est de **sauter un accord** ou de **perdre la référence**, pas de choisir la dette au plus fort taux. La charte refuse déjà graphiques et banque |

---

## 2. Les quatre profils du brief, revus sur cette app

| Profil | Déjà couvert | Encore ouvert | Décision |
|--------|--------------|---------------|----------|
| **1. Plusieurs dossiers chez le même huissier** | Communication + Copier + photo | Le nom unique ne sépare pas les causes. L’IBAN n’est pas gardé. La photo ne suit pas le mois suivant. Le collage peut abîmer une communication structurée (voir recherche Febelfin) | Fiche dossier : **à qui** + **pour quoi** + IBAN + communication. Copier le format que la banque attend |
| **2. Effet autruche** | L’accueil ne montre pas un total de dettes | Le brief se contredit en voulant le reste dû sur la ligne | Accueil = mensualité du mois seulement. Le reste dû est sur l’écran qu’on ouvre **exprès** |
| **3. Oublie les échéances futures en voyant le salaire** | **Déjà fait** par « Reste après factures » | Le chiffre n’est pas le solde banque : s’ils dépensent l’argent avant le virement, l’app ne le voit pas. L’aide le dit déjà en partie | Ne pas brancher la banque. Le dire clairement dans l’aide le jour où on parle des dossiers |
| **4. Paie une facture banale et saute l’accord** | Rien ne distingue les lignes | Un tri « priorité d’abord » casserait l’ordre des jours, qui dit **quand** l’argent part | Badge calme **« Accord »** sur la ligne, sans changer l’ordre. Le retard existant (« En retard ») suffit comme alarme |

---

## 3. Épargne — le besoin qu’ajoute Athalia, absent du brief

Ils ne mettent rien de côté, et ils n’iraient pas rechercher 1 € déjà « rangé » dans le mois pour le dépenser. C’est exactement le mécanisme utile (Thaler : l’argent d’un compte à part se dépense moins). Ce n’est **pas** une raison d’ajouter des enveloppes.

La charte §8 refuse les enveloppes. On la respecte :

- **Une** ligne mensuelle, qui se comporte comme une facture : « Mettre de côté — 10 € ».
- Elle est dans le gros chiffre **dès le 1er**, comme le loyer. Sinon ils verront un reste trop beau et ne vireront rien.
- FAIT = le virement vers **un autre compte** est fait. L’app ne garde pas l’argent.
- Pas de cagnotte à piocher sur l’accueil. Pas de « vous avez 400 €, servez-vous ».
- Le total « déjà viré » peut exister sur la fiche, avec la phrase : ce n’est pas de l’argent disponible ici.
- Création depuis **Réglages**, pas une pastille de plus sur « + Ajouter ». Tant que la ligne n’existe pas, l’accueil ne change pas.
- Si le montant met le mois en déficit, on le **dit avant** d’enregistrer. On n’interdit pas (ils peuvent quand même vouloir 5 €). On ne cache pas la conséquence.
- Maman peut ne jamais l’activer. Alex non plus. Zéro dossier et zéro épargne = l’app de 6.27.

On ne code pas : objectifs vacances, plusieurs pots, pourcentage du salaire, virement automatique (l’app n’a pas la banque).

---

## 4. Où poser les fonctions sans toucher à l’accueil de maman

Règle déjà écrite, à ne pas négocier : **aucune nouvelle fonction à 0 usage n’apparaît.**

| Fonction | Quand elle est invisible | Où elle apparaît quand elle existe |
|----------|--------------------------|-------------------------------------|
| Dossier | 0 dossier | Pas un onglet. Tant qu’il n’y a qu’une fiche : on l’ouvre depuis la ligne du mois. Dès que l’écran liste existe : une ligne dans le **Menu** : « Mes dossiers (4) ». L’accueil reste le mois |
| Champs huissier sur « + Ajouter » | Tant qu’on n’ouvre pas le bloc | En bas de la fiche, une ligne repliée : « C’est un retard ou un accord ? ». Les 4 fréquences actuelles ne bougent pas |
| Lettre sans plan | 0 lettre en attente | Dans « Mes dossiers », section du bas. Sur l’accueil, **une** ligne du même genre que « À prévoir », seulement s’il y en a : « 1 courrier sans plan · Voir ». Pas de montant, donc **pas** dans le Reste |
| Badge Accord | La ligne n’est pas un dossier | Sous le nom, une pastille. Le jour de la liste ne change pas |
| Reste dû, barre, photos | On est sur l’accueil | Fiche du dossier et écran « Mes dossiers » |
| Mettre de côté | Pas activé dans Réglages | Une ligne comme les autres, libellé « Mettre de côté » |
| Résumé à envoyer | — | Bouton dans « Mes dossiers » (et dans Réglages seulement si le mois est en déficit **ou** s’il y a un dossier). Jamais en gros sur l’accueil |
| Écrire au créancier | Pas d’e-mail saisi | Bouton « Écrire » qui ouvre **l’app Mail**. On ne stocke pas les échanges |

Sur la ligne du mois d’un dossier, le texte tient en deux lignes max (S26) :

1. **À qui** — « Étude Leroy » — et le montant du **mois**
2. **Pour quoi** — « Hôpital 2025 » — encre atténuée  
   Pastille « Accord » si c’est un plan. **Pas** le reste en euros. **Pas** « 3 sur 12 » si le montant change d’un mois à l’autre.

« 3 sur 12 » seulement si les deux sont vrais : un reste de départ connu, **et** un montant mensuel qui n’a pas encore été modifié. Sinon c’est un mensonge.

---

## 5. Relecture (deuxième passage)

Faite après la recherche (partie 6 de ce fichier et le document de recherche). On ne garde que ce qui survit.

### 5.1 Idées du brief gardées

- Affichage progressif : 0 dossier = 0 nouveau bouton.
- Double libellé : à qui / pour quoi.
- Copier la communication en un geste.
- Photo du courrier une fois, pour pouvoir ranger le papier. Fichier WebP, chemin en base, jamais l’image dans le JSON.
- Reste dû = point de départ − somme des FAIT de **ce** dossier. Un mois à 30 € déduit 30 €. L’historique des mois ne se réécrit pas.
- Quand le reste tombe à 0, plus de ligne les mois suivants. Le dossier reste consultable, marqué soldé.
- Écran calme « Mes dossiers » seulement s’il y en a au moins un. Le gros chiffre de **cet** écran est le total **du mois** (180 €), pas la somme de toutes les dettes.
- Lettres en attente, sans mensualité, pour ne pas oublier un tiroir.
- Un déficit négatif n’est pas un échec de l’app. C’est un état des lieux. On le rend copiable, on ne fait pas la leçon.
- Pas de banque, pas de cloud, pas de graphique.

### 5.2 Idées du brief corrigées

| Brief | Après relecture |
|-------|-----------------|
| « Montant total » + « Déjà payé avant l’app » | **Un seul chiffre à la création : « Combien reste-t-il aujourd’hui ? ».** Deux champs se contredisent. Les versements d’avant l’app sont déjà dans ce reste |
| Badge accueil avec le reste en euros | **Interdit sur l’accueil.** Le reste est dans la fiche, en taille normale. L’écran dossiers montre d’abord le pourcentage ou la barre, le montant restant en second |
| « Fin prévue : avril 2027 » | **« Encore environ N mois si le versement reste de 50 € ».** Une estimation, pas une date gravée |
| 4ᵉ mode toujours visible | Bloc replié « C’est un retard ou un accord ? » |
| Onglet | Ligne de menu |
| ~80 Ko et dossier `pieces/` | Lisibilité d’abord (réglage actuel). Plafonner le fichier si une photo dépasse ~400 Ko en baissant la qualité, sans descendre au point de perdre les chiffres. Chemin `receipts/` inchangé |
| Priorité « Huissier » en criant | Pastille **« Accord »**. Le mot huissier n’est pas une catégorie : un retard d’énergie ou un crédit a le même besoin |
| Plan figé 12 × 50 | Reste en centimes. Montant du mois éditable. L’étude peut **ajouter des frais** : un geste nommé « Ils ont ajouté un montant », pas une édition silencieuse du passé |
| Dette « en vrac » sans rien sur le mois | Deux états, pas dix. **Avec accord** : une ligne tombe chaque mois (montant prévu, modifiable). **Sans accord** : la lettre est dans « en attente », **aucun** euro dans le Reste, jusqu’au jour où ils fixent un versement |
| Photo unique sur l’instance | Les courriers appartiennent au **dossier** (le modèle). Chaque mois les **montre**. Plusieurs fichiers possibles |
| Copier le texte tel qu’il est affiché | Écran : `+++123/1234/12345+++`. Presse-papiers : les **12 chiffres** si le contrôle modulo 97 est bon (format digital Febelfin). Si le contrôle est faux : phrase « Vérifiez sur le courrier », et on copie quand même ce qui est écrit. Un caractère en trop transforme le virement en communication **libre** : le créancier ne rapproche plus tout seul |

### 5.3 Idées d’ailleurs qu’on refuse, même si elles sont à la mode

| Idée vue ailleurs | Pourquoi non |
|-------------------|--------------|
| Focus mode qui cache **aussi** la mensualité | Ici la mensualité est l’action. On cache le total, pas le geste |
| Snowball / avalanche / taux / « où mettre le bonus » | Mauvais problème. Un accord sauté coûte plus cher qu’un taux mal optimisé. On ne répartit pas l’argent à leur place |
| Graphiques, streaks, confettis | La charte et les tests Undebt.it : trop de données intimide. Un FAIT vert existe déjà |
| Widget, Siri, connexion banque | Hors charte. Charge et peur pour maman |
| Boîte mail dans l’app | Ils ont déjà le mail. On ouvre un message prérempli |
| Formulaire CPAS officiel en PDF | Ça vieillit, ça a l’air juridique, ce n’est pas notre rôle. Un **texte** clair suffit : revenus, charges du mois, chaque dossier (qui, pourquoi, versement, reste, référence) |
| Plusieurs enveloppes d’épargne | Refus charte. Une ligne « Mettre de côté » |
| Intérêts calculés par l’app | On n’a pas le taux fiable. Le reste bouge quand **eux** saisissent un paiement ou des frais ajoutés |
| Afficher le total de toutes les dettes en hero | C’est le chiffre qui fait fermer l’app (effet autruche, blogs produit + papier Karlsson, Loewenstein, Seppi 2009). Il reste disponible dans la fiche, pas en ouverture |

### 5.4 Ce qu’on ne prétend pas résoudre

Si les revenus du mois sont inférieurs au loyer + factures + accords, le Reste est négatif. L’app ne crée pas d’argent. Elle doit alors être le dossier qu’on peut montrer : liste propre, montants, photos. La médiation de dettes en Wallonie sert à ça (plan négocié, le créancier peut refuser). On ne remplace pas le médiateur et on ne donne pas de conseil juridique dans l’interface.

---

## 6. Preuves code (pour ne pas re-auditer à l’aveugle)

- Modèles : `ExpenseTemplateEntity`, `ExpenseInstanceEntity`, `MonthLedgerEntity`. Schéma `AppDatabase` version 3. Migrations 1→2 et 2→3 seulement.
- Calcul : `MonthSummaryCalculator` — courses **hors** du reste.
- Mois suivant : `MonthCycleEngine` vers la ligne `ExpenseInstanceEntity(` — pas de note, pas de photo.
- QR : `EpcPayment` a `iban` et `remittance`. `AddEntryViewModel.applyEpc` enregistre le nom, le montant, la communication. Pas l’IBAN.
- Photo : `ReceiptPhotoStore.relativePathFor` → `receipts/{expenseId}.webp`, max 2048 px, qualité 90.
- Fréquences UI : `RecurrenceChoice` = Once, Monthly, Every3Months, Yearly. Pas de cinquième.
- Backup : `CURRENT_SCHEMA_VERSION = "3"`. Un schéma 4 non lu = restauration refusée (comportement voulu).

---

## 7. Verdict

Le produit **carnet** est déjà fini pour maman : mois, reste, FAIT, rappels, papier, sauvegarde. Le smoke notif / trimestriel du journal reste un trou de **vérification téléphone**, pas un trou de conception.

Le produit n’est pas fini pour **un retard payé par virement**. Il manque une couche dossier, invisible à zéro, plus une ligne d’épargne optionnelle. Cette couche ne doit pas réécrire l’accueil.

Prochaine écriture utile : le plan. Pas une pastille « Huissier » sur l’écran de maman.
