# Audit — maman, Alex, et le brief « dossiers »

Date du premier constat : 2026-10-21.  
Relecture du code : le même jour ; lots A/B/C ensuite clos au journal (**6.32**).

Dernière étape **fermée** dans le journal : **6.32** (`0.1.0-6.32`, `versionCode` 37), schéma **6**, tests **198/198** (`--rerun-tasks`).  
Lots A/B/C tenus. Ouvert : smoke « Écrire » sur téléphone, lots D/E/F.

Cette note dit ce que le code fait vraiment, ce que le brief demande, et ce qu’il faut garder après comparaison avec d’autres outils (voir [`archive/RECHERCHE_OUTILS_2026-10-21.md`](archive/RECHERCHE_OUTILS_2026-10-21.md)).  
Le plan : [`PLAN_PRODUIT_FINI.md`](PLAN_PRODUIT_FINI.md).

La partie 1 est le constat du matin (schéma 3). Elle reste pour comprendre pourquoi les lots existent. **L’état actuel est la partie 7.** La partie 5 dit ce qu’il ne faut pas coder même si le brief le demande.

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

## 1. Constat du code (premier passage — matin, schéma 3)

Figé. Ne pas s’en servir pour décider du prochain correctif : la partie 7 le remplace.

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

## 6. Preuves code du matin (schéma 3)

Figées, pour l’historique. L’arbre n’est plus celui-là. Voir la partie 7.

- Modèles : `ExpenseTemplateEntity`, `ExpenseInstanceEntity`, `MonthLedgerEntity`. À ce moment-là, `AppDatabase` version 3. Migrations 1→2 et 2→3 seulement.
- Calcul : `MonthSummaryCalculator` — courses **hors** du reste. Toujours vrai.
- Mois suivant : `MonthCycleEngine` ne recopiait ni la note ni la photo. **Corrigé au lot A.**
- QR : `EpcPayment` a `iban` et `remittance`. Le matin, `applyEpc` jetait l’IBAN. **Le chantier C le garde dans l’état du formulaire.**
- Photo : `receipts/{id}.webp`, max 2048 px, qualité 90, et maintenant une baisse de qualité au-dessus d’environ 400 Ko.
- Fréquences UI : `RecurrenceChoice` = Once, Monthly, Every3Months, Yearly. Pas de cinquième. Toujours vrai.
- Backup : le matin `CURRENT_SCHEMA_VERSION = "3"`. Aujourd’hui `"6"`, codec accepte 1 à 6.

---

## 7. État réel — relecture du soir

### 7.1 Ce qui tient (lots A et B, journal 6.30 et 6.31)

Le carnet de maman est toujours l’accueil : mois, gros reste, FAIT, courses, menu = Réglages + mois. Pas d’onglet. Pas de « Mes dossiers ». Pas d’euros de dette sur la ligne.

| Besoin | Dans le code | Limite honnête |
|--------|----------------|----------------|
| La communication suit les mois suivants | Oui. À l’enregistrement elle va sur le modèle **et** sur les instances à venir encore à payer, même sans cocher « Aussi les mois suivants » | Une note libre du type « loyer 000000000097 » n’est pas réduite à 12 chiffres. Seule une chaîne purement structurée et valide (mod 97) l’est. Sinon le texte part tel quel et l’écran dit « Vérifiez sur le courrier. » |
| La photo suit | À la **création** d’une facture qui revient, le fichier est écrit sur l’instance et sur le modèle. À l’**édition**, la photo ne suit que si « Aussi les mois suivants » est coché. Effacer la photo de ce mois ne vide pas le modèle | Le dossier des photos reste `receipts/`, pas un dossier `pieces/`. Qualité : 2048 px, départ à 90, descente par pas de 5 jusqu’à 55 si le fichier dépasse ~400 Ko. On ne vise pas 80 Ko |
| Copier pour la banque | 12 chiffres seulement si le contrôle passe | L’écran garde les `+++` |
| Un accord a un « pour quoi » et un reste | `causeLabel`, `openingRemainingCents` null = pas un dossier. Reste = départ + frais − somme des montants **FAIT** (centimes réels, pas le forfait) | Le reste n’est pas une colonne. On ne le réécrit pas à la main |
| Dernier mois partiel | Le versement affiché et créé est le minimum du forfait et du reste | Un FAIT de 30 € sur un plan de 50 € baisse le reste de 30 € |
| Soldé | Plus de nouvelle ligne. Les lignes à venir déjà créées et encore à payer sont retirées au moment du FAIT qui finit la dette. L’historique déjà FAIT reste | « Remettre » ne recrée le mois suivant que s’il reste quelque chose **après** avoir réservé le montant de ce mois. Si octobre à 50 € couvre toute la dette, novembre ne revient pas |
| Accueil | Deuxième ligne = pour quoi. Pastille **Accord** (fond chaud, texte encre). Jamais « 3 sur 12 », jamais le total dû | Le contraste de la pastille est discret. C’est voulu. Pas revu au doigt sur le S26 dans cette relecture |
| Fiche | Bloc replié « C’est un retard ou un accord ? ». Ouvert : à qui, pour quoi, « Combien reste-t-il aujourd’hui ? ». Accord déjà créé : « Encore X € à rembourser. » ou « Soldé ». Refermer le bloc ne vide pas la cause d’un accord existant | Un accord doit revenir (mensuel, 3 mois, annuel). Une fois seule est refusée |
| Aide | Section « Retard ou accord » dans Comment ça marche, après « Facture papier » | Les mots sont dans l’aide même avec zéro dossier. Ils ne sont pas sur l’accueil |

Tests ciblés relancés pendant la correction du lot B (moteur, bascule FAIT, hors-mois, communication, aide, mise à jour d’instance, dossiers) : verts. Ce n’est **pas** la suite complète.

### 7.2 Le trou de journal (erreur, pas un détail)

Le code a migré. Les documents de version, non.

| Endroit | Ce qu’il dit | Ce que le code fait |
|---------|----------------|---------------------|
| `PROGRESS_LOG.md`, README, `versionName` | 6.31 clos, schéma 5, 183 tests | `versionName` est encore `0.1.0-6.31` |
| `AppDatabase`, `AppMetaKeys`, `Migrations.MIGRATION_5_6`, `DatabaseModule` | — | Schéma **6** déjà enregistré |
| `CHARTE_ET_ARCHITECTURE.md` §3 (avant cette relecture) | Schéma 5, reste = départ − FAIT | Le calcul ajoute déjà `addedFeesCents` |

Conséquence : installer cet arbre sur un téléphone en schéma 5 **monte** la base en 6. Revenir au commit 6.31 ensuite casse l’ouverture (Room ne redescend pas). On ne ferme pas une version 6.32 dans le journal tant que la suite complète n’a pas été relancée et que le lot C n’est pas réellement fini. On ne laisse pas non plus la charte affirmer « schéma 5 » pendant que la base est en 6.

Smoke carnet : [`AUDIT_STATUS.md`](AUDIT_STATUS.md). Punch list visuelle figée : [`archive/AUDIT_PREMIUM.md`](archive/AUDIT_PREMIUM.md).

### 7.3 Lot C — clos au journal 6.32

Ce qui est branché :

- Colonnes et migration 5→6. Backup JSON lit et écrit les champs. Schémas 1 à 6 acceptés.
- Le scan QR remplit l’IBAN dans l’état d’ajout et d’édition (`applyEpc`).
- Dans le bloc dossier ouvert : champ IBAN, champ e-mail. En **édition** : « Copier l’IBAN » et « Écrire ». « Écrire » construit un brouillon (`CreditorMailComposer` : bonjour, qui, pour quoi, montant du mois, référence) et lance `ACTION_SENDTO` vers `mailto:`. Aucun fil n’est stocké.
- À la création, IBAN et e-mail ne sont enregistrés que si un reste dû est saisi. Bloc ouvert mais reste vide : ces deux champs partent vides. C’est cohérent avec « pas de dossier sans chiffre », et ça surprend si on a tapé l’IBAN avant le montant.
- Sur un accord **déjà** créé : « Ils ont ajouté un montant » + motif. Les centimes s’ajoutent au cumul. Le motif nouveau remplace l’ancien s’il n’est pas vide ; un motif vide garde l’ancien. Ce n’est pas une liste d’événements datés (ce que la recherche décrivait). Deux lettres de frais deviennent une somme et une seule phrase.
- Si l’accord a déjà une photo et qu’on en prend une autre, le fichier part dans `extraReceiptPaths` au lieu de remplacer la première. La compression ~400 Ko est dans `ReceiptPhotoStore`.

Clos au **6.32** (UI voir/enlever, aide, suite **198/198**). Reste ouvert hors clôture :

- L’ajout n’a pas « Copier l’IBAN » ni « Écrire ». Il faut rouvrir la fiche. Acceptable (dit dans l’aide).
- `ACTION_SENDTO` + extras : smoke téléphone encore à faire.
- Frais : seulement sur un accord déjà créé ; motif = dernier saisi (pas une liste datée).
- Lettre sans plan / `agreementCents` : lot D, pas un oubli du C.

### 7.4 Trous encore vrais après A et B

| Trou | Pourquoi ce n’est pas « juste du polish » |
|------|-----------------------------------------------|
| Baisser le montant de ce mois **après** que le mois suivant a été effacé (dette finie) | `UpdateInstanceUseCase` ne relance pas la même synchro que la bascule FAIT. Le mois suivant ne revient pas tout seul. Il revient seulement via « Remettre », et seulement s’il reste un reliquat après ce mois |
| Écran « Mes dossiers » | Quatre accords ne se comparent qu’en ouvrant chaque mois. Pas d’entrée de menu. Voulu jusqu’au lot D |
| Courrier sans plan | Une lettre reçue sans versement fixé n’a pas de tiroir. Si on met un montant pour « ne pas l’oublier », elle entre dans le Reste. C’est le piège que le lot D doit fermer |
| Mettre de côté | Pas de ligne dans Réglages, pas de facture épargne, pas de phrase « déjà viré, pas disponible ici ». La charte refuse toujours les enveloppes multiples ; l’exception « une facture » n’est pas écrite |
| Résumé à montrer | Pas de texte partageable (revenus, charges, à qui, pour quoi, versement, reste, référence). Pas de faux PDF CPAS — et il ne faut pas en ajouter |
| Smoke téléphone | Le journal 6.27 laissait ouvert : notification avec plusieurs factures, nom seul sur une échéance trimestrielle. `AUDIT_STATUS.md` les coche ensuite en manuel. Cette relecture **n’a pas** refait le geste, et n’a pas regardé la ligne Accord sur un écran court |
| Checklist « zéro dossier = zéro mot » | L’accueil tient. Le formulaire d’une dépense a **un** bouton de plus, replié : « C’est un retard ou un accord ? ». L’aide contient la section. C’est l’écart accepté au lot B. Ce n’est pas zéro pixel au sens strict du plan §5 point 2 |
| Suite de tests | Clos au 6.32 : **198/198** avec `--rerun-tasks` |

### 7.5 Réflexions à ne pas perdre

Le produit utile n’est pas « une app de dettes ». C’est le carnet, plus une mémoire de papier, plus un reste qui baisse du vrai virement. Maman paie ses retards par mail puis par virement, comme Alex. Elle n’a pas besoin d’un autre écran. Elle a besoin que la référence et la photo reviennent le mois d’après, et que le gros chiffre du 1er ait déjà réservé le versement. Ça, A et B le font.

Alex a les deux vies : un accord à montant à peu près fixe, et des mois où il verse ce qu’il a. Le modèle « ouverture + frais − FAIT réels » tient les deux, à condition qu’on ne montre pas « mois 4 sur 12 ». Ce compteur a été refusé et il n’est pas dans l’UI. Il ne faut pas le réintroduire au lot D sous prétexte d’une barre. La barre, si elle vient, est « déjà versé / (départ + frais) ». Le nombre de mois n’apparaît qu’avec « si le versement reste de X », et disparaît dès qu’un mois a divergé.

Ils ne mettent même pas 1 € de côté. Une cagnotte à l’écran serait dépensée dans la tête, ou ignorée. L’épargne prévue est une facture de plus, virée vers un autre compte, absente du résumé « dettes ». Pas maintenant. C est clos (6.32) : D/E/F un à la fois, avec migration + suite tests le même jour.

Le bouton replié sur chaque nouvelle dépense est le seul pixel nouveau que maman voit sans l’avoir demandé. Le garder fermé, sans le mot huissier, est la bonne limite. L’ouvrir par erreur puis quitter sans enregistrer ne doit rien créer : c’est le cas si le reste dû reste vide (`openingRemainingCents` null). Si elle remplit le reste par curiosité, elle crée un dossier. La phrase sous le champ (« Laissez vide si ce n’est pas un accord ») porte cette responsabilité. Elle est facile à rater sur un écran court. À relire au doigt, pas à rallonger avec un second avertissement.

La pastille « Accord » sur fond chaud est lisible parce que le texte est encre, pas parce que le fond crie. Sur le S26, avec le texte large, la deuxième ligne (« pour quoi ») plus la pastille peut pousser le montant. Personne ne l’a mesuré dans cette passe. Le plan le demande encore (sortie, point 3).

Les frais en une somme et un motif unique suffisent pour une lettre. Ils ne suffisent pas pour trois lettres. Quand la troisième arrivera, le motif de la première aura disparu. Soit on accepte ça et on l’écrit dans l’aide (« le motif affiché est le dernier »), soit le lot C se termine avec une vraie liste. Je penche pour la liste courte seulement si l’affichage des photos multiples est fait en même temps : les deux sont « la lettre d’après ». Sinon on ferme le lot avec la somme, une phrase d’aide, et les photos visibles. Pas les deux chantiers à moitié.

Le mail ne doit pas devenir une boîte de réception. Le brouillon actuel est le bon niveau. S’il n’arrive pas dans Gmail à cause de `ACTION_SENDTO`, le correctif est l’intent, pas un client mail dans l’app.

### 7.6 Verdict

| Couche | État |
|--------|------|
| Carnet du mois | Tenu. C’est le produit de maman |
| Lot A — mémoire du papier | Tenu dans le code et dans le journal 6.30 |
| Lot B — accord replié | Tenu dans le code et dans le journal 6.31, avec le trou « modifier le montant après coup ne recrée pas le mois suivant » |
| Lot C — IBAN, mail, frais, plusieurs photos | Clos **6.32** — schéma 6, UI courriers, **198/198** |
| Lots D, E, F | Pas commencés |

Prochaine action utile : smoke Galaxy « Écrire » ; lot D seulement si besoin « Mes dossiers ». Pas de pastille « Huissier ».
