# Plan — du carnet fini à un outil qui tient aussi les retards

Date : 2026-10-21, mis à jour le soir après relecture du code.  
Départ : carnet tenu (journal jusqu’à **6.31**).  
Lots **A et B** sont clos dans le journal (`0.1.0-6.30`, `0.1.0-6.31`).  
Lot **C** est commencé dans l’arbre (Room **6**) et **pas clos** : pas de suite `--rerun-tasks` enregistrée, `versionName` encore `0.1.0-6.31`.  
On n’anticipe **pas** D, E, F dans `versionName`, le README ou les badges. La charte et le journal ne ferment une étape que lorsque le lot est vraiment fini **et** les tests relancés. Le détail des trous : [`AUDIT_ALEX_MAMAN_2026-10-21.md`](AUDIT_ALEX_MAMAN_2026-10-21.md) partie 7.

Lectures liées :

- Constat et arbitrages : [`AUDIT_ALEX_MAMAN_2026-10-21.md`](AUDIT_ALEX_MAMAN_2026-10-21.md)
- Sources : [`RECHERCHE_OUTILS_2026-10-21.md`](RECHERCHE_OUTILS_2026-10-21.md)

---

## 1. Ce que « fini » veut dire

Deux personnes, **un** écran.

| Personne | Fini, c’est quand… |
|----------|--------------------|
| **Maman** (S26 6,3″) | L’accueil qu’elle a aujourd’hui ne gagne **aucun** bouton, aucun onglet, aucun mot « huissier », tant qu’elle n’a pas créé de dossier ni activé « Mettre de côté ». Elle continue : mois, gros reste, FAIT, courses, menu = Réglages + mois |
| **Alex** (S25 Ultra, même UI) | Un retard a : à qui, pour quoi, référence copiable comme la banque la veut, IBAN, photo qui **revient** le mois suivant, e-mail prérempli, versement du mois réservé dans le Reste dès le 1er, reste dû qui baisse du montant **réellement** coché FAIT, arrêt quand c’est soldé |
| **Les deux** | Une ligne « Mettre de côté » possible, traitée comme une facture, vers un autre compte. Pas une cagnotte à l’écran |
| **Un mois impossible** | Le déficit reste honnête. Un texte partageable (revenus, charges, dossiers) sert à écrire au créancier ou à un service de médiation. L’app ne promet pas de rendre le mois positif |

Hors produit, même à la fin : banque, cloud, pub, graphiques, enveloppes multiples, snowball, taux d’intérêt, conseil juridique, formulaire CPAS officiel.

Le smoke déjà noté au journal (notification avec plusieurs factures, échéance trimestrielle au doigt) reste à faire sur le Galaxy. Il ne bloque pas la conception. Il bloque l’idée que « tout le visuel 6.x est revérifié ».

---

## 2. Principes de construction (non négociables)

1. **Zéro usage = zéro pixel.** Chaque lot se teste avec une base sans dossier : les écrans de maman sont les mêmes.
2. **Le mois ne montre pas le reste dû.** Seulement le versement de ce mois.
3. **L’ordre des lignes reste le jour.** Pastille « Accord », pas un re-tri.
4. **Les centimes FAIT font foi.** Pas un compteur « mois 4 sur 12 » qui ment si le montant a changé.
5. **Les courriers et la référence vivent sur le modèle**, et chaque nouveau mois les reprend.
6. **Un seul bouton plein** par écran. Le reste en trait.
7. **Le schéma bouge avec les colonnes**, le même jour : migration, backup, tests de restauration, puis journal. Pas de colonne « en avance » dans le JSON. A a posé le 4, B le 5. Le 6 est déjà dans le code pour C, sans clôture : c’est le problème ouvert, pas un modèle à recopier.
8. **Pas de `if (S25)`.** `DisplayFit` suffit. On vérifie une hauteur courte (S26) et une hauteur confort (Ultra).
9. **Phrases de la recherche §9.** Pas de menace de saisie. Pas de date de fin sans « si ».

---

## 3. Ordre des lots

Chaque lot est livrable seul. On peut s’arrêter après le A : maman y gagne déjà (la communication et la photo suivent le trimestre). Alex n’y gagne le dossier qu’à partir du B.

### Lot A — La mémoire du papier (invisible) — clos au journal 6.30

Le comportement ci-dessous est dans le code. Deux précisions apparues en le vérifiant : à la création, la photo d’une facture qui revient est écrite tout de suite sur le modèle (il n’y a pas encore la case « ce mois seulement ») ; à l’édition, elle ne suit que si la case est cochée. La note, elle, suit toujours.

**Problème.** Aujourd’hui la communication et la photo sont sur l’instance du mois. Le mois suivant est recréé sans elles (`MonthCycleEngine`).

**Comportement.**

- Enregistrer une communication sur une facture répétée la range aussi sur le modèle.
- Le mois nouveau la réaffiche, avec Copier.
- La photo du modèle se revoit sur les mois suivants (pastille « Facture »). Une photo prise « seulement ce mois-ci » ne s’impose pas aux autres mois : la case existe déjà dans l’esprit de « Aussi les mois suivants ». Par défaut, une référence et une lettre d’accord **suivent** ; on le dit dans la phrase du bouton, pas dans un nouveau réglage.
- Copier : si le texte est une communication structurée **valide** (modulo 97), le presse-papiers reçoit les **12 chiffres**. L’écran garde `+++…+++`. Si le contrôle échoue : « Vérifiez sur le courrier. » et on copie le texte tel quel. Une note libre (« loyer ») est copiée telle quelle.

**Données.** Colonnes optionnelles sur `expense_templates` : `paymentNote`, `receiptRelativePath` (une photo canonique). Schéma **4**. Les instances gardent leurs champs (override du mois).

**Tests.** Matérialiser février après un modèle de janvier avec note : la note est là. Modulo 97 : `+++000/0000/00097+++` valide si le calcul le dit ; un chiffre faux ne devient pas 12 chiffres. Note libre inchangée. Backup schéma 3 encore lisible.

**Maman.** Aucun libellé nouveau si elle n’utilise pas Copier. Si elle copie déjà une communication, le collage banque devient plus sûr.

### Lot B — Le dossier, replié — clos au journal 6.31

Le comportement ci-dessous est dans le code. Le reste inclut désormais les frais ajoutés (colonne du lot C) : départ + frais − FAIT. Trou restant : modifier le montant du mois **après** que le mois suivant a été retiré ne recrée pas la ligne. « Remettre » ne la recrée que s’il reste un reliquat une fois le montant de ce mois réservé.

**Problème.** Un seul nom. Pas de reste. La ligne continue après la fin de la dette.

**Comportement.**

- Sur la fiche d’ajout, sous les fréquences actuelles, une ligne repliée : **« C’est un retard ou un accord ? »**. Fermée, la fiche a la même hauteur utile qu’aujourd’hui.
- Ouverte, trois champs : **À qui** (le nom déjà là), **Pour quoi**, **Combien reste-t-il aujourd’hui ?**
- Montant du mois = le montant de la ligne (le versement). Il entre dans le Reste dès le 1er, comme une facture.
- Accueil : ligne 1 = à qui + montant, ligne 2 = pour quoi, pastille **Accord**. Pas d’euros de dette. Pas de « 3 sur 12 » dès qu’un mois a un montant différent du modèle.
- FAIT déduit **ce** montant du reste. Remettre à payer le remet.
- Reste ≤ 0 : plus de génération les mois suivants. Fiche : « Soldé ». L’historique des mois déjà créés ne disparaît pas.
- Pas encore d’entrée de menu. On ouvre le dossier en touchant la ligne du mois (la fiche d’édition, déjà le geste actuel). L’écran « Mes dossiers » arrive au lot D, avec son entrée de menu, les deux ensemble. Un bouton sans écran ne part pas.

**Données.** `causeLabel`, `openingRemainingCents` (null = pas un dossier). Le reste n’est pas une colonne qu’on édite à la main : il se calcule.

**Tests.** 600 €, versement 50 €, un mois passé à 30 € puis FAIT → reste 570 €, pas 550 €. Douzième mois qui tombe à 0 → le mois d’après n’a pas de ligne. 0 dossier → le menu n’a pas la ligne. Création sans ouvrir le bloc → template sans `openingRemainingCents`, accueil identique.

**Maman.** Elle n’ouvre pas le bloc. Rien ne change.

### Lot C — Fiche anti-confusion — commencé, pas clos

Le schéma 6, la migration, le backup, les champs IBAN / e-mail dans le bloc dossier, « Copier l’IBAN » et « Écrire » en édition, le cumul de frais sur un accord déjà créé, et le rangement d’une deuxième photo dans `extraReceiptPaths` sont dans l’arbre. Ça ne suffit pas à fermer le lot. Voir l’audit, partie 7.3. Il reste : voir et retirer les photos en plus, tests (texte du mail, frais, chemins), suite Gradle complète, geste réel « Écrire » sur le téléphone, phrase d’aide, puis seulement le journal et la version. Ne pas enchaîner D tant que ce n’est pas fait — ou tant que le schéma 6 n’est pas retiré si on abandonne le lot.

**Problème.** IBAN jeté après le scan. Pas d’e-mail. Une seule photo. Les frais ajoutés n’ont pas de geste propre.

**Comportement.**

- Le QR belge remplit aussi l’IBAN, gardé sur le modèle.
- Fiche : **Copier la communication**, **Copier l’IBAN** (deux gestes, la banque a deux cases).
- Champ e-mail. Bouton **Écrire** ouvre l’app de mail : destinataire, objet = la référence, corps court (qui, pour quoi, montant du mois). Aucun fil stocké.
- Plusieurs photos sur le dossier (`receipts/…webp`). Ajouter ne remplace pas sans demander. Qualité : garder le cadre lisible (2048 / 90). Si le fichier dépasse environ 400 Ko, baisser la qualité, ne pas viser 80 Ko au prix des chiffres.
- **« Ils ont ajouté un montant »** : on saisit un plus, avec trois mots de motif. Ça augmente le reste. Ça n’efface pas les FAIT passés.

**Tests.** `applyEpc` conserve l’IBAN. Reste après frais. Suppression d’une photo ne supprime pas les autres. Chemin hors `receipts/` rejeté (déjà la logique d’archive). Intent mail : on teste le texte construit, pas l’app Mail.

**Maman.** Ces boutons ne sont pas sur une facture normale. Le scan continue de remplir nom, montant, communication.

### Lot D — Mes dossiers, et le tiroir — pas commencé

**Problème.** Quatre accords ne se voient bien que si on quitte le mois **volontairement**. Une lettre reçue sans plan ne doit pas inventer un montant.

**Comportement.**

- Écran ouvert depuis le menu. Hero : **« 4 dossiers · 180 € ce mois »** (somme des versements prévus encore actifs). Pas la somme des dettes.
- Carte : à qui, pour quoi, barre (déjà versé / départ + frais), en dessous le reste en corps de texte, phrase « encore environ N mois si le versement reste de 50 € ». Dernier FAIT daté. Accès aux photos.
- Si le versement a déjà varié : pas de N mois. Seulement la barre et le reste.
- Section **« Courriers sans plan »** : photo + qui + pour quoi, **montant vide**. Ça n’entre pas dans le Reste et ne crée pas de ligne de mois.
- Dès qu’ils fixent un versement, le courrier devient un dossier du lot B.
- S’il existe au moins un courrier sans plan : **une** ligne sur l’accueil, même famille que « À prévoir », « 1 courrier sans plan · Voir ». Pas de montant.

**Tests.** Courrier sans `agreement` : mois inchangé, reste inchangé, ligne d’accueil présente. 0 courrier en attente : pas de ligne. Hero = somme des versements du mois, pas des restes. Pourcentage affiché cohérent avec les centimes.

**Maman.** 0 dossier et 0 courrier : pas de ligne, pas d’écran.

### Lot E — Mettre de côté — pas commencé

**Problème.** Ils ne réservent rien pour eux. Une enveloppe de plus les perdrait.

**Comportement.**

- Réglages : **« Mettre de côté »**. On choisit un montant (ils peuvent mettre 5 €) et un jour. Ça crée une facture mensuelle ordinaire, marquée épargne.
- Elle est dans le gros Reste dès le 1er. FAIT quand le virement vers l’**autre compte** est fait.
- Aide : « Ce n’est pas de l’argent gardé dans l’application. »
- Avant d’enregistrer, si le mois passe en déficit : phrase avec le montant du trou. On laisse enregistrer.
- Pas de total « disponible » sur l’accueil. Sur la fiche, en petit : somme des FAIT de cette ligne, libellé « déjà viré, pas disponible ici ».
- Le résumé du lot F ne la compte **pas** comme une dette.

**Tests.** Mois avec salaire 1 000 et loyer 800, épargne 50 → reste 150, et 150 encore dans « à payer » tant que ce n’est pas FAIT. Déficit annoncé si le montant est trop haut. Résumé : la ligne est sous « mis de côté », pas sous « dettes ». Absent de Réglages tant que… non : le **réglage** est dans Réglages (maman peut le trouver). L’**accueil** ne change qu’après activation. C’est le seul écart volontaire : une ligne d’aide dans un écran qu’elle ouvre peu. Pas sur l’accueil.

**Charte.** Au moment du code, une phrase en §8 : les enveloppes multiples restent refusées ; une facture « Mettre de côté » est admise.

### Lot F — Texte à montrer ou à envoyer — pas commencé

**Problème.** Un mois négatif ou un rendez-vous médiation demande une liste propre. Le formulaire CPAS de Liège demande créancier, nature, solde, arrangement, plus les revenus et les charges.

**Comportement.**

- Bouton **« Préparer un résumé »** dans Mes dossiers.
- Aussi dans Réglages si le mois affiché est en déficit ou s’il existe un dossier. Pas sur l’accueil.
- Texte brut, partage Android (mail, WhatsApp) :

  - mois ;
  - revenus (nom, montant) ;
  - factures du mois (nom, montant, FAIT ou non) ;
  - dossiers (à qui, pour quoi, versement, reste, référence) ;
  - mis de côté, à part ;
  - une ligne : les photos des courriers sont dans l’application.

- Pas de PDF imitant un CPAS. Pas de numéro national. Pas de pièce d’identité.

**Tests.** Le texte contient les quatre colonnes dette et **ne** contient pas le chemin fichier ni un total de dettes en titre. Déficit : le mot « déficit » et le montant du Reste, sans consigne du genre « payez d’abord… ».

---

## 4. Après ces lots — ce qu’on ne planifie pas

| Envie possible | Décision |
|----------------|----------|
| Widget « à payer aujourd’hui » | Plus tard, seulement si le carnet est ennuyeux à ouvrir. Pas dans ce plan |
| Lire les mails des créanciers | Non. On ouvre un nouveau message |
| Calculer les intérêts | Non. Ils saisissent les frais quand la lettre arrive |
| Dire quel accord payer en premier | Non. L’ordre reste le calendrier. La pastille distingue, elle ne commande pas |
| Mode sombre, graphique annuel | Déjà hors charte |
| Compte Play Store | Hors sujet (sideload familial) |

---

## 5. Finition « premium » — liste de sortie

Un lot n’est pas fini parce que l’écran existe. Il est fini quand :

1. `./gradlew :app:testDebugUnitTest --rerun-tasks` est vert, et le journal dit le **nombre réel** de tests.
2. Une base **sans** dossier : l’accueil et le menu ne montrent pas Accord, dossier, huissier, reste dû. Écart déjà accepté : le bouton replié sur la fiche dépense, et la section d’aide « Retard ou accord ».
3. Le parcours Alex tient sur une hauteur ~800 dp (chrome compact) : la ligne à deux textes ne pousse pas FAIT hors de la carte ; les cibles restent ≥ 48 dp.
4. Copier une vraie communication structurée, coller dans un champ texte : 12 chiffres, rien d’autre.
5. Photo d’octobre visible en novembre sans reprendre la photo.
6. Mois à 30 € au lieu de 50 € : le reste baisse de 30 €.
7. Reste à 0 : décembre n’a plus la ligne ; novembre FAIT est encore dans l’historique.
8. Restauration d’un backup **schéma 3** réussit encore. Le schéma annoncé par le journal (5 au 6.31, puis 6 quand C sera clos) ramène notes, photos et dossiers. Un schéma dans Room sans ligne de journal n’est pas une restauration prouvée.
9. `CHARTE_ET_ARCHITECTURE.md`, `PROGRESS_LOG.md`, `README.md` décrivent le lot **après** les tests, pas avant.
10. Smoke au doigt sur le S25 Ultra, et un coup d’œil texte large sur une largeur S26 (ou émulateur 360 dp × hauteur courte) : menu, fiche repliée, ligne Accord.

---

## 6. Risques connus

| Risque | Parade |
|--------|--------|
| Le bloc « retard ou accord » s’ouvre par erreur et fait peur | Fermé par défaut. Aucun champ huissier tant qu’il est fermé. On peut le refermer sans enregistrer (déjà : quitter sans enregistrer) |
| Deux restes (colonne + calcul) qui divergent | Une seule source : calcul. Pas de colonne « reste actuel » |
| La photo modèle + la photo du mois se contredisent | Le mois affiche la sienne s’il en a une, sinon celle du modèle |
| Archive ZIP > 45 Mo à cause des lettres | Plafond déjà là. Qualité baissée au-dessus de ~400 Ko (`ReceiptPhotoStore`). On ne stocke pas l’original 12 Mpx |
| Schéma 6 posé avant la fin du lot C | Finir C ou retirer la migration avant d’installer sur un téléphone déjà en 5. Room ne redescend pas |
| Deuxième photo rangée et invisible | Le lot C n’est pas clos tant qu’on ne peut pas la revoir |
| Motif de frais écrasé par le suivant | Une phrase d’aide, ou une liste. Pas un cumul silencieux de motifs |
| Maman active « Mettre de côté » trop haut | Alerte déficit **avant** enregistrement |
| Le résumé part par WhatsApp (pas chiffré de bout en bout selon les réglages) | Le bouton dit « à envoyer à un créancier ou à un conseiller », pas « partager à tout le monde ». On ne peut pas empêcher le geste. On n’y met pas l’identité |
| Promesse de date | Phrase avec « si ». Test qui échoue si l’UI contient « fin prévue » sans condition |

---

## 7. Où on en est, et le prochain geste

A et B sont le produit qui aide déjà : la référence et la photo reviennent, le versement est réservé dans le Reste, le reste dû baisse du montant coché, la ligne s’arrête quand c’est soldé, l’accueil de maman n’a pas gagné de menu.

Le prochain geste est de **finir le lot C** (ou de le retirer proprement), pas d’ouvrir Mes dossiers.

Ordre concret :

1. Rendre les photos supplémentaires visibles, et permettre d’en enlever une sans effacer la première.
2. Décider du motif de frais : une seule phrase (« le motif affiché est le dernier ») **ou** une courte liste. Pas les deux à moitié. La somme des centimes, elle, est déjà le bon modèle.
3. Tests du texte de mail, du reste après frais, d’un chemin de photo en plus. Puis `./gradlew :app:testDebugUnitTest --rerun-tasks`, compter les `TEST-*.xml`, et seulement alors écrire le journal, la charte, le README et `versionName`.
4. Au doigt : Écrire ouvre un vrai brouillon ; la ligne Accord sur une hauteur courte ne pousse pas FAIT hors de la carte ; quitter le bloc dossier sans reste dû ne crée rien.
5. Ensuite seulement le lot D. E et F après. L’épargne et le résumé n’aident pas si la deuxième lettre est déjà perdue.
