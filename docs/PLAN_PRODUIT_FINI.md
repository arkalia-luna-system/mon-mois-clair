# Plan — du carnet fini à un outil qui tient aussi les retards

Date : 2026-10-21.  
Départ : carnet tenu.  
Lots **A à F** + 6.37–**6.42** clos (`0.1.0-6.42`, schéma Room **8**, **245/245**).  
Détail : [`AUDIT_ALEX_MAMAN_2026-10-21.md`](AUDIT_ALEX_MAMAN_2026-10-21.md) §8.

Lectures liées :

- Constat et arbitrages : [`AUDIT_ALEX_MAMAN_2026-10-21.md`](AUDIT_ALEX_MAMAN_2026-10-21.md)
- Sources (archive) : [`archive/RECHERCHE_OUTILS_2026-10-21.md`](archive/RECHERCHE_OUTILS_2026-10-21.md)

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

Les smokes notés au journal (notification, trimestriel, « Écrire », hauteur courte simulée) sont faits sur Ultra ou en simulation. Le téléphone S26 de maman, tenu en main, n’est pas dans le carnet.

---

## 2. Principes de construction (non négociables)

1. **Zéro usage = zéro pixel.** Chaque lot se teste avec une base sans dossier : les écrans de maman sont les mêmes.
2. **Le mois ne montre pas le reste dû.** Seulement le versement de ce mois.
3. **L’ordre des lignes reste le jour.** Pastille « Accord », pas un re-tri.
4. **Les centimes FAIT font foi.** Pas un compteur « mois 4 sur 12 » qui ment si le montant a changé.
5. **Les courriers et la référence vivent sur le modèle**, et chaque nouveau mois les reprend.
6. **Un seul bouton plein teal** sur la barre (« + Ajouter »). « FAIT ✓ » sur la ligne est un plein forêt : ce n’est pas un second bouton de barre. Le reste est en trait.
7. **Le schéma bouge avec les colonnes**, le même jour : migration, backup, tests de restauration, puis journal. Pas de colonne « en avance » dans le JSON. A = 4, B = 5, C = 6, D = 7 (clos au journal). E n’a pas ajouté de colonne : le marqueur est dans `app_meta`.
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
- La photo du modèle se revoit sur les mois suivants. Sur le mois, le bouton s’appelle « Voir la photo » (il n’y a pas de pastille « Facture »). Une photo prise « seulement ce mois-ci » ne s’impose pas aux autres mois : la case existe déjà dans l’esprit de « Aussi les mois suivants ». Par défaut, une référence et une lettre d’accord **suivent** ; on le dit dans la phrase du bouton, pas dans un nouveau réglage.
- Copier : si le texte est une communication structurée **valide** (modulo 97), le presse-papiers reçoit les **12 chiffres**. L’écran garde `+++…+++`. Si le contrôle échoue : « Vérifiez sur le courrier. » et on copie le texte tel quel. Une note libre (« loyer ») est copiée telle quelle.

**Données.** Colonnes optionnelles sur `expense_templates` : `paymentNote`, `receiptRelativePath` (une photo canonique). Schéma **4**. Les instances gardent leurs champs (override du mois).

**Tests.** Matérialiser février après un modèle de janvier avec note : la note est là. Modulo 97 : `+++000/0000/00097+++` valide si le calcul le dit ; un chiffre faux ne devient pas 12 chiffres. Note libre inchangée. Backup schéma 3 encore lisible.

**Maman.** Aucun libellé nouveau si elle n’utilise pas Copier. Si elle copie déjà une communication, le collage banque devient plus sûr.

### Lot B — Le dossier, replié — clos au journal 6.31

Le comportement ci-dessous est dans le code. Le reste inclut désormais les frais ajoutés (colonne du lot C) : départ + frais − FAIT. Sync futures : `AgreementFuturesSync` recrée/efface aussi après édition montant ou frais (même si la ligne est encore DONE) — trou audit 7.4 fermé.

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

### Lot C — Fiche anti-confusion — clos au journal 6.32

Schéma 6 + migration + backup · QR → IBAN · bloc dossier IBAN/e-mail · « Copier l’IBAN » + « Écrire » · frais cumulés · photos extras **visibles** (voir / enlever) · « Ajouter un courrier » sans remplacer la première · aide · **198/198** `--rerun-tasks`. Smoke Ultra « Écrire » → Gmail Compose (**clos** 2026-10-21). Motif frais = dernier saisi.

**Problème.** IBAN jeté après le scan. Pas d’e-mail. Une seule photo. Les frais ajoutés n’ont pas de geste propre.

**Comportement.**

- Le QR belge remplit aussi l’IBAN, gardé sur le modèle.
- Fiche : **Copier la communication**, **Copier l’IBAN** (deux gestes, la banque a deux cases).
- Champ e-mail. Bouton **Écrire** ouvre l’app de mail : destinataire, objet = la référence, corps court (qui, pour quoi, montant du mois). Aucun fil stocké.
- Plusieurs photos sur le dossier (`receipts/…webp`). Ajouter ne remplace pas sans demander. Qualité : garder le cadre lisible (2048 / 90). Si le fichier dépasse environ 400 Ko, baisser la qualité, ne pas viser 80 Ko au prix des chiffres.
- **« Ils ont ajouté un montant »** : on saisit un plus, avec trois mots de motif. Ça augmente le reste. Ça n’efface pas les FAIT passés.

**Tests.** `applyEpc` conserve l’IBAN. Reste après frais. Suppression d’une photo ne supprime pas les autres. Chemin hors `receipts/` rejeté (déjà la logique d’archive). Intent mail : on teste le texte construit, pas l’app Mail.

**Maman.** Ces boutons ne sont pas sur une facture normale. Le scan continue de remplir nom, montant, communication.

### Lot D — Mes dossiers, et le tiroir — clos au journal 6.34

Schéma **7** + migration + backup · `pending_letters` · « Ranger sans plan » (ajout) + formulaire Mes dossiers · section liste · conversion versement+reste → accord · bandeau accueil · Menu si dossiers **ou** courriers · **214/214**. Phrase optionnelle « encore N mois si… » livrée au journal **6.37**.

**Problème.** Quatre accords ne se voient bien que si on quitte le mois **volontairement**. Une lettre reçue sans plan ne doit pas inventer un montant.

**Comportement.**

- Section **« Courriers sans plan »** : photo + qui + pour quoi, **montant vide**. Ça n’entre pas dans le Reste et ne crée pas de ligne de mois.
- Dès qu’ils fixent un versement, le courrier devient un dossier du lot B.
- S’il existe au moins un courrier sans plan : **une** ligne sur l’accueil, même famille que « À prévoir ».
- Phrase optionnelle « encore environ N mois si… » : **clos 6.37** (fiche, Mes dossiers, résumé ; pas l’accueil).

**Maman.** 0 dossier et 0 courrier : pas de bouton Menu (tenu).

### Lot E — Mettre de côté — clos au journal 6.35

Réglages : montant + jour, facture mensuelle « Mettre de côté », alerte déficit sans bloquer, « Retirer la ligne ». Fiche : « déjà viré — pas disponible ici ». Pas de cagnotte sur l’accueil. Marqueur `app_meta` (`savingsTemplateId`), pas de schéma nouveau. **222/222** `--rerun-tasks`.

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

### Lot F — Texte à montrer ou à envoyer — clos au journal 6.36

`PrepareMonthSummaryUseCase` · bouton Mes dossiers · Réglages si déficit ou dossier · partage Android · **226/226**. Pas de PDF CPAS.

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
| Widget « à payer aujourd’hui » | Plus tard. S’il arrive : un seul, Reste + prochaine ligne non FAIT. Voir la charte, suite visuelle |
| Lire les mails des créanciers | Non. On ouvre un nouveau message |
| Calculer les intérêts | Non. Ils saisissent les frais quand la lettre arrive |
| Dire quel accord payer en premier | Non. L’ordre reste le calendrier. La pastille distingue, elle ne commande pas |
| Graphique annuel | Hors charte |
| Encre de nuit, PDF courrier, widget, OCR `+++` | Décidés dans la charte (suite visuelle) et [`BENCHMARK_QUALITE_2026.md`](BENCHMARK_QUALITE_2026.md). Pas avant la finition formes / FAIT / chiffres |
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
8. Restauration d’un backup **schéma 3** réussit encore. Le schéma **7** (journal 6.34) ramène aussi les courriers sans plan.
9. `CHARTE_ET_ARCHITECTURE.md`, `PROGRESS_LOG.md`, `README.md` décrivent le lot **après** les tests, pas avant.
10. Smoke au doigt sur le S25 Ultra, et un coup d’œil texte large sur une largeur S26 (ou émulateur 360 dp × hauteur courte) : menu, fiche repliée, ligne Accord.

---

## 6. Risques connus

| Risque | Parade |
|--------|--------|
| Le bloc « retard ou accord » s’ouvre par erreur et fait peur | Fermé par défaut. Aucun champ huissier tant qu’il est fermé. On peut le refermer sans enregistrer (déjà : quitter sans enregistrer) |
| Deux restes (colonne + calcul) qui divergent | Une seule source : calcul. Pas de colonne « reste actuel » |
| La photo modèle + la photo du mois se contredisent | Le mois affiche la sienne. Un mois sans photo n’emprunte pas celle du modèle : « enlever ce mois » reste vrai |
| Archive ZIP > 45 Mo à cause des lettres | Plafond déjà là. Qualité baissée au-dessus de ~400 Ko (`ReceiptPhotoStore`). On ne stocke pas l’original 12 Mpx |
| Schéma 7 (clos 6.34) | Room ne redescend pas : un téléphone en 7 ne rouvre pas un APK figé en 6 |
| Motif de frais = dernier seulement | **Clos 6.38** — liste datée `expense_fee_events` |
| Maman active « Mettre de côté » trop haut | Alerte déficit **avant** enregistrement |
| Le résumé part par WhatsApp (pas chiffré de bout en bout selon les réglages) | Le bouton dit « à envoyer à un créancier ou à un conseiller », pas « partager à tout le monde ». On ne peut pas empêcher le geste. On n’y met pas l’identité |
| Promesse de date | Phrase **6.37** : **« si »**, jamais « fin prévue ». Masquée si un **FAIT** ≠ forfait. Cadence annuelle/trimestrielle = « versements » |

---

## 7. Où on en est, et le prochain geste

**A–F + 6.37–6.42** clos (`0.1.0-6.42`, schéma **8**, **245/245**). Feedback famille + sync mois futurs + dist (voir [`FEEDBACK_TEST_FAMILLE_2026-10-21.md`](FEEDBACK_TEST_FAMILLE_2026-10-21.md)).

**Pas clos, et il ne faut pas l’écrire comme fait :**
- le S26 de maman en main ;
- tests d’écran instrumentés.

**Prochain geste** : S26 en main.

Risques C : photos extras = voir / enlever ; frais = historique daté (6.38).
