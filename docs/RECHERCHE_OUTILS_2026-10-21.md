# Recherche — affichage, dettes, épargne, outils comparables

Date : 2026-10-21.  
Sert l’audit [`AUDIT_ALEX_MAMAN_2026-10-21.md`](AUDIT_ALEX_MAMAN_2026-10-21.md) et le plan [`PLAN_PRODUIT_FINI.md`](PLAN_PRODUIT_FINI.md).  
Règle : une affirmation « les gens font X » a une source. Un blog marketing n’est pas une preuve. Quand la source est un retour produit, c’est marqué comme tel.

---

## 1. Ce que vivent maman et Alex (cadre, pas une étude sur eux)

Faits donnés par Athalia, 2026-10-21 :

- Alex : Galaxy S25 Ultra, **même affichage** qu’Athalia. Maman : Galaxy S26 6,3″, déjà la cible n°1.
- Retard de papier : ils écrivent en général **par e-mail** au créancier, et paient **par virement** le mois où l’argent est là.
- Ils ne mettent pas de côté, même 1 €, et n’iraient pas rechercher cet euro pour le redépenser s’il était vraiment sorti du compte du quotidien.

Conséquence produit : le scénario « 12 × 50 € jusqu’à extinction » est **un** cas. L’autre cas est « pas encore d’accord, ou montant selon ce qui reste, mais un virement quand même, avec la bonne référence ». Les deux doivent tenir dans **un** modèle (reste en centimes), pas dans deux apps.

---

## 2. Effet autruche — ce que le papier dit vraiment

**Source :** Karlsson, Loewenstein et Seppi, « The ostrich effect: Selective attention to information », *Journal of Risk and Uncertainty*, 2009.  
<https://doi.org/10.1007/s11166-009-9060-6>

Ils mesurent des **connexions** à un compte titres, pas une app de factures. Les investisseurs regardent plus souvent quand le marché monte que quand il baisse. L’idée : on évite une information qu’on croit pénible, même si la regarder aiderait à décider.

**Ce qu’on a le droit d’en tirer :** un gros total de dettes sur l’écran d’ouverture peut faire fermer l’app.  
**Ce qu’on n’a pas le droit d’en tirer :** « cacher tous les chiffres guérit la dette ». Leur papier dit aussi que éviter l’info peut **dégrader** la décision.

**Traduction pour Mon Mois Clair :**

- L’écran d’ouverture (le mois) montre l’**action** : ce versement, cette facture, FAIT.
- Le total encore dû est sur l’écran ouvert exprès (« Mes dossiers », fiche).
- On ne cache pas la mensualité. La cacher, ce serait l’autruche **contre** le paiement.

Les blogs « Focus mode » (Payoff, etc.) racontent la même direction avec des personnages inventés (« Sarah, 18 500 $ »). On garde l’idée d’interface. On ne cite pas leurs pourcentages ni leurs histoires comme des faits.

Retour design **Undebt.it** (Mario Gil, retour de tests, pas une publication) : les gens aimaient l’info mais se sentaient submergés ; les graphiques n’étaient pas compris. <https://www.mariogil.design/work/undebit>  
Ça confirme la charte : pas de courbes.

Étude de concept **One Payment** (Devon Latzen) : après épuration, la barre de navigation elle-même a été jugée de trop ; le paiement du mois passe avant le solde de chaque carte. <https://www.devonlatzen.com/one-payment>  
Utile pour refuser un onglet « Dossiers » permanent.

---

## 3. Épargne qu’on ne va pas rechercher

**Sources :**

- Shefrin et Thaler, « The Behavioral Life-Cycle Hypothesis », *Economic Inquiry*, 1988. L’argent n’est pas traité comme fongible : le revenu courant se dépense plus facilement que l’argent déjà mis dans un autre « compte » mental (avenir).
- Thaler, « Mental accounting matters », *Journal of Behavioral Decision Making*, 1999. <https://www.anderson.ucla.edu/faculty_pages/keith.chen/negot.%20papers/Thaler_MentalAccounting99.pdf>
- Bankrate, « Pay yourself first » : virer vers l’épargne **avant** de voir l’argent comme disponible. Ce n’est pas une étude. C’est la règle pratique qui colle au papier. <https://www.bankrate.com/personal-finance/pay-yourself-first-budgeting/>

**Traduction :**

- Le geste utile est un **virement vers un autre compte**, le même geste qu’une facture.
- L’app réserve le montant dans « Reste après factures » dès le 1er. S’il reste « en option » à la fin du mois, il ne partira pas : c’est déjà le constat d’Athalia.
- Montrer une cagnotte dépensable sur l’accueil annule le mécanisme (l’argent redevient du revenu courant).
- Plusieurs enveloppes (vacances, voitures, cadeaux) : charge mentale, refusée par la charte §8. **Une** ligne suffit.

Les articles de banques (« plus de comptes = plus d’épargne ») sont des contenus commerciaux. On ne s’en sert pas comme preuve. Le mécanisme de Thaler suffit.

---

## 4. Communication structurée belge (à copier juste)

**Source :** Febelfin, conditions pour proposer des virements.  
<https://febelfin.be/fr/themes/numerisation-et-innovation/regelgeving-voorwaarden/conditions-pour-proposer-des-virements>

- Papier : `+++123/1234/12345+++` (3, 4 et 5 chiffres, dont 2 de contrôle).
- Digital : **12 chiffres** sans `+` ni `/`.
- **Tout ajout** dans la zone de communication transforme le virement structuré en communication **libre**. Le rapprochement automatique côté créancier ne se fait plus.
- Contrôle : modulo 97 des 10 premiers chiffres ; reste 0 → contrôle `97`.

Guide QR Febelfin (PDF, version 1.0 du 1/2/2025, en vigueur EPC visée 1/2/2026) : la communication structurée belge remplace la référence ISO dans le QR de facture, et le QR se place sur la **facture**, pas sur le formulaire de virement.  
<https://febelfin.be/media/pages/publicaties/2023/febelfin-standaarden-voor-online-bankieren/249a1b9900-1744012292/febelfin-ig-qroninvoice.pdf>

L’app scanne déjà ce QR (`EpcQrParser`) et copie déjà la communication. Trous :

1. L’IBAN lu n’est pas enregistré.
2. « Copier » envoie la chaîne affichée. Si elle contient un espace ou un mot en plus, la banque peut sortir du mode structuré. Le geste sûr : à l’écran le format avec `+++`, dans le presse-papiers les 12 chiffres **si** le modulo 97 est valide.
3. Certaines apps (exemple bpost / Nickel, FAQ) préremplissent les `+`, d’autres non. Les 12 chiffres restent le dénominateur commun du format digital Febelfin.

---

## 5. Médiation et huissier en Belgique — ce que l’app peut préparer

**Sources :**

- Portail du surendettement en Wallonie, médiation amiable. Le médiateur liste les dettes, regarde le budget, propose un plan. Le créancier **peut refuser**. <https://surendettement.wallonie.be/home/quelles-aides/j-ai-besoin-d-une-mediation-de-dettes/la-mediation-de-dettes-amiable.html>
- Chambre des huissiers de justice, page médiation. En médiation amiable la personne **garde ses revenus** et fait elle-même les virements convenus. Il faut prévenir si la situation change. Ce n’est pas gratuit : on doit le dire, l’app ne le fait pas à la place du service. <https://www.huissiersdejustice.be/mediation>
- Formulaire CPAS de Liège (liste de pièces) : identité, revenus, charges fixes / variables / périodiques, et pour les dettes : **créancier, nature, solde, arrangement**. Pièces : un document par dette, extraits, bail, etc. <https://www.cpasdeliege.be/publications/brochures/formulaire_mediation-de-dettes-cpas-liege.pdf>
- CPAS d’Oupeye : « un document par dette est suffisant », envoi possible **par mail**. <https://www.oupeye.be/cpas/formulaires-et-listes-documents/listing-des-documents-pour-introduire-une-demande-daide>

**Traduction :**

- Le résumé partageable de l’app aligne ces colonnes : qui, pourquoi, versement du mois, reste, référence. Plus les revenus et les factures du mois (déjà dans le carnet).
- On n’imprime pas un faux formulaire CPAS.
- On ne joint pas la carte d’identité. Les photos de **courriers** restent dans l’app ; le texte dit qu’elles y sont.
- On n’écrit pas « saisie automatique si vous sautez un mois ». Ce n’est pas une règle unique. Le badge dit « Accord », le retard dit « En retard ». Le reste est le travail du médiateur ou de l’étude.

---

## 6. Comparaison d’outils — quoi voler, quoi laisser

Repères déjà utilisés par l’audit premium du projet (charte, `docs/AUDIT_PREMIUM.md`) : AllPaid, réversibilité Wayflyer, états vides Appthetics. On ne les rouvre pas. On ajoute le regard **dettes / acomptes**.

| Outil | Ce qu’il fait bien | Ce qu’on ne copie pas | Décision MMC |
|-------|--------------------|------------------------|--------------|
| **AllPaid** (carnet de factures, sans banque) | FAIT en un geste, rappel, photo/PDF **attaché à la facture** (pas à un seul mois), offline | Catégories, widgets, analytics Pro, streaks | La photo doit vivre sur le **modèle** (le dossier), pas seulement sur l’instance d’octobre. Déjà leçon pour le code actuel |
| **Safi** — dettes et acomptes <https://safimoney.com/features/debts-and-installments> | Un acompte n’est pas un abonnement : il a un **reste** et une **fin**. Rappel 3 jours avant. « Enregistrer un paiement » est le premier geste | Comptes bancaires, graphiques, sens « je dois / on me doit » | Le dossier a un reste. Le mois n’a que le versement. Rappel J−3 : **déjà dans l’app** |
| **Payoff / My Debt Coach / Endute / Tavera** | Montrent qu’un total énorme décourage ; comparent snowball et avalanche | Taux, stratégies, coach IA, BNPL américain, « debt-free date » vendue comme une promesse | On refuse les stratégies. On garde : total hors de l’écran d’ouverture |
| **Undebt.it** (retour tests) | Beaucoup de scénarios possibles | Trop d’info, graphiques opaques | Un écran dossiers = liste de cartes, pas un cockpit |
| **YNAB, Goodbudget, enveloppes** | L’argent a un métier | Saisie permanente, plusieurs pots | Déjà refusé. L’épargne = une facture envers soi-même |
| **Apps banque belges** | Le virement part d’ici, sur le même téléphone (charte) | Open banking, agrégateur | On remplit le presse-papiers et le mail. On ne se connecte pas |
| **Excel / tableau de dettes** | Colonnes créancier / nature / solde — exactement la page 4 du formulaire CPAS | Le total en gros en haut | Ces colonnes vivent dans « Mes dossiers » et dans le texte partagé |

---

## 7. Affichage — règles qui sortent de la comparaison

Pour un écran de **6,3″** (maman) et le même layout sur **6,9″** (Alex / Athalia). Les deux sont en largeur Compact. One UI : cibles ≥ 48 dp, marges ≥ 24 dp. Déjà la charte.

1. **Un hero par écran.** Accueil : « Reste après factures ». Dossiers : « X € à verser ce mois » pour l’ensemble des accords. Jamais deux display serif.
2. **L’ordre des lignes du mois reste le jour.** La priorité est une pastille, pas un second tri. Sinon on ne sait plus « le 12 c’est quoi ».
3. **Deux lignes de texte max** sur une carte du mois : qui, puis pourquoi. Le reste dû n’est pas la troisième.
4. **Progression** sur l’écran dossiers seulement : barre = part déjà versée depuis le départ saisi. Phrase : « encore environ N mois **si** le versement reste de … ». Pas « fin prévue » sans le « si ».
5. **Pourcentage plutôt que montagne d’euros** comme premier signal de la carte (leçon Focus mode, sans voler leur marketing). Le montant restant est lisible juste dessous, en corps de texte, parce qu’ils sont venus pour ça.
6. **État vide** : maman avec 0 dossier ne voit pas l’état vide des dossiers. L’écran n’existe pas. (Mieux qu’un empty state.)
7. **Une action principale** par fiche : pour une facture, FAIT est déjà sur l’accueil. Sur la fiche dossier : « Copier la communication », puis « Écrire », puis la photo. Pas une pile de boutons teal. La charte : un seul bouton plein par écran.
8. **Réversible.** Annuler un FAIT existe. Ajouter des frais est un geste nommé, pas une correction cachée. Wayflyer : nommer ce qu’on détruit. Déjà la pratique du projet.
9. **Pas de swipe** pour Copier. Déjà décidé (audit A11). On garde un bouton écrit.
10. **Clavier.** Les champs nouveaux (cause, IBAN, e-mail, reste) passent par le même principe : le champ en cours reste visible, une seule action épinglée.

---

## 8. Modèle de données suggéré par la recherche

Proposition du matin. Une partie est codée depuis (cause, ouverture, IBAN, e-mail, frais en somme, photo canonique recopiée). Ce qui suit n’est pas l’état du code : le statut est dans [`AUDIT_ALEX_MAMAN_2026-10-21.md`](AUDIT_ALEX_MAMAN_2026-10-21.md) partie 7. En particulier, `agreementCents` n’existe pas (le versement reste le montant du modèle) et les frais ne sont pas une liste d’événements datés.

À poser sur le **modèle** de dépense (il survit aux mois), pas sur une table parallèle « dettes » qui doublerait les factures.

Champs nouveaux, tous optionnels, ignorés si vides :

| Champ | Rôle |
|-------|------|
| `causeLabel` | Pour quoi (hôpital, eau, amende). Vide = facture normale, une seule ligne |
| `iban` | Gardé. Le QR pourra le remplir |
| `creditorEmail` | Pour ouvrir le mail. Vide = pas de bouton Écrire |
| `openingRemainingCents` | « Combien reste-t-il aujourd’hui ? » à la création. Null = pas un dossier |
| `agreementCents` | Montant prévu chaque mois. Null = pas encore d’accord (lettre en attente) |
| Ajustements de frais | Événements datés « + X centimes, motif court », jamais un écrasement du passé |

Reste dû affiché = `openingRemainingCents` + frais ajoutés − somme des instances **FAIT** de ce modèle.  
Si ce reste ≤ 0 : le modèle passe archivé / soldé, `MonthCycleEngine` ne crée plus de ligne.

Photos : plusieurs chemins `receipts/…webp` liés au **modèle**, plus la photo éventuelle déjà liée à une instance (on ne migre pas en cassant les factures actuelles).

Communication : reste `paymentNote`, recopiée sur chaque nouvelle instance. C’était le trou du matin. Le lot A (journal 6.30) le ferme.

Épargne : un booléen `isSavings` sur le modèle, ou simplement un libellé + le comportement facture. Le booléen évite de la compter comme une dette dans le résumé médiation. Pas de table « pots ».

Le matin, le schéma prévu était le **4**, le jour du code. Le journal a depuis clos le 4 (lot A) et le 5 (lot B). Le 6 est dans l’arbre sans clôture.

---

## 9. Phrases qu’on peut dire, phrases qu’on ne dit pas

On peut dire dans l’aide :

- « Le reste après factures n’est pas le solde de la banque. »
- « Mettre de côté, c’est un virement vers un autre compte. L’app ne garde pas cet argent. »
- « Accord : un versement convenu. Mieux vaut ne pas le sauter. »
- « Cette communication ne passe pas le contrôle. Vérifiez les chiffres sur le courrier. »
- « Encore environ 9 mois si le versement reste de 50 €. »

On ne dit pas :

- « Vous serez saisi. »
- « Dette soldée le 12 avril 2027 » sans condition.
- « Il faut payer l’huissier avant le loyer » (l’app ne connaît pas leur bail ni le droit du cas).
- « Vous avez économisé 400 € » sur l’accueil comme si c’était dépensable.

---

## 10. Limite de cette recherche

Pas d’entretien avec maman ni Alex au-delà de ce qu’Athalia a décrit. Pas de test au doigt dans ce passage (documents seulement). Les apps citées changent : les décisions ci-dessus tiennent à un mécanisme (cacher le total, garder le geste, copier 12 chiffres, une seule épargne), pas à une capture d’écran de 2026.
