> **ARCHIVÉ / FIGÉ** (2026-10-26) — ne plus lire pour l’état code. Vérité = `PROGRESS_LOG.md` + PRIVACY/SECURITY + audits `final-0*`.

# Audit UX praticité — famille · nuit 02 · 2026-10-23

| | |
|--|--|
| **Profil** | UX researcher senior · public **Senior 60+** · Galaxy **S26** (cible) / **S25 Ultra** (second) · One UI |
| **Périmètre** | Accueil · Courses · épargne · calendrier mois · Menu · hub Réglages · fiche · FAIT · snackbar · dossiers · À venir · onboarding |
| **Base** | Code ~**0.1.0-6.75** (lecture seule) · charte §6–7 · `DisplayFit` compact ≤ 800 dp |
| **Méthode** | Parcours mental « doigt » + copy visible · **pas** smoke téléphone · **pas** claim premium |
| **Livrable** | Frictions · gestes manquants · copy · top 12 demain · déjà clair |

**Hors scope de ce fichier :** commit, push, correctifs UI.

---

## 0. Cadre d’évaluation

Une utilisatrice 60+ sur One UI veut surtout : **voir ce qu’il reste**, **cocher quand c’est payé**, **retrouver le mois**, **ne pas se perdre dans les réglages Samsung**.  
Elle lit peu les phrases longues. Elle confond facilement « app » et « téléphone ». Elle a le pouce en bas (socle Menu / + Ajouter = bien). L’écran S26 est **plus court** : tout ce qui est derrière **Plus** ou à **3–4 niveaux** de Réglages est à risque.

Critères : 1) trouve-t-elle le geste en ≤ 2 taps depuis Accueil ? 2) le libellé dit-il l’effet en français familial ? 3) une erreur est-elle annulable sans panique ?

---

## 1. Frictions (par surface)

### Accueil (sticky + liste)

| Id | Friction | Pourquoi ça bloque à 60+ |
|----|----------|---------------------------|
| A1 | Sur hauteur courte (`compactHomeChrome`), **Mettre de côté** est derrière **Plus** | L’épargne n’est déjà pas un réflexe ; un 2ᵉ tap abstrait (« Plus ») la fait disparaître du premier regard |
| A2 | Libellé **Plus** / **Moins** | Ne dit pas *quoi* est caché (épargne, tip salaire, courrier…). Sensation de menu technique |
| A3 | Ligne Courses : `Courses 40 € · après 210 €` | « Après » = Reste − courses, pas « après avoir payé les factures ». Ambigu avec « Après les FAIT » (sous Plus) |
| A4 | Trois chemins pour changer de mois (‹ ›, icône calendrier, titre cliquable) + encore Menu → Choisir un mois | Surapprentissage OK pour power user ; pour Senior, **trop d’équivalents** sans bénéfice clair |
| A5 | Recherche facture seulement si ≥ 4 lignes | Invisible les premiers mois ; OK produit, mais surprise quand elle apparaît |
| A6 | Snackbar FAIT **sous** le sticky, durée Short (~4 s) | Yeux sur la carte / le pouce ; le bandeau peut disparaître avant lecture + Annuler |

### Courses (sheet sticky)

| Id | Friction | Pourquoi |
|----|----------|----------|
| C1 | Deux boutons **J’ai dépensé** / **Corriger** | « Corriger » = retirer / ajuster le cumul ? Pas évident sans essayer (peur d’effacer) |
| C2 | Sheet : « Hors du Reste » / « hors Reste » | Concept juste, mais **Reste** n’est nommé que dans le sticky au-dessus — à l’oral familial on dit « ce qu’il me reste » |
| C3 | Pas de confirmation après « J’ai dépensé » hors recalcul sticky | Manque un micro-feedback « Noté » (le sticky change, mais le sheet reste ouvert) |

### Épargne

| Id | Friction | Pourquoi |
|----|----------|----------|
| E1 | Montant Accueil (sheet) vs jour / retrait **Réglages → Compte & données → Mettre de côté** | 3–4 taps pour le jour du virement ; oubli fréquent |
| E2 | Copy sheet inactive : « Vers un autre compte — **compte** dans le Reste » | Phrase un peu tordue ; risque de lire « compte bancaire dans l’app » |
| E3 | FAIT sur la ligne épargne = « j’ai viré » | Pas dit sur l’Accueil ; seulement en fiche / Aide — confusion avec « j’ai déjà l’argent de côté » |
| E4 | Sur S26, découverte initiale de l’épargne = Plus ou Réglages | Contredit le besoin produit « ne pas rechercher 1 € » si la ligne n’est jamais vue |

### Calendrier mois (`Choisir un mois`)

| Id | Friction | Pourquoi |
|----|----------|----------|
| K1 | Années repliées + pastilles | Bon modèle carnet ; première ouverture = chercher l’année courante parmi ~4–5 |
| K2 | Futur = aperçu sans FAIT — dit dans le tip | Tip lu une fois ; ensuite le badge **Aperçu** sticky porte le poids — OK si smoke confirme la lisibilité |
| K3 | Pas de geste « ouvrir ce mois » depuis **À venir** détail | Double mental model : liste À venir ≠ calendrier Accueil |

### Menu

| Id | Friction | Pourquoi |
|----|----------|----------|
| M1 | Badge **!** sur Menu (alertes bloquées) | Petit, orange ; sans texte « Alertes » sur le socle — peut être ignoré ou pris pour une notif système |
| M2 | **Dossiers** absent si zéro dossier | Règle zéro-pixel juste ; mais « où sont mes dossiers ? » impossible à découvrir avant d’avoir créé un accord |
| M3 | QuietLink « Arkalia Système » | Marque inconnue = hésitation (« c’est qui ? ») même si le geste contact est bon |
| M4 | **Choisir un mois** aussi dans le Menu | Redondant avec sticky — peu nuisible, mais allonge le Menu |

### Hub Réglages

| Id | Friction | Pourquoi |
|----|----------|----------|
| R1 | Groupe **Compte & données** | L’app n’a **pas** de compte ; sonne « banque / login » |
| R2 | Profondeur hub → groupe → sujet | Acceptable en charte ; pour changer Apparence ou Alertes = 3 taps depuis Menu |
| R3 | **Résumé → Dossiers** (flèche dans le libellé) | Semble une navigation, pas une action « préparer un résumé » |
| R4 | Aide = Comment ça marche, long | Utile, mais rarement ouverte ; ne sauve pas un geste Accueil opaque |

### Fiche (ajout / édition)

| Id | Friction | Pourquoi |
|----|----------|----------|
| F1 | Fiche longue (fréquence, paiement, accord, frais…) | Sections repliées aident ; scroll + clavier One UI = fatigue et oubli d’**Enregistrer** |
| F2 | **Lire +++ sur la photo** | Jargon belge correct métier ; pour Senior = « lire quoi ? » |
| F3 | **Ranger sans plan** | Idiome produit ; pas le verbe du quotidien (« mettre de côté le courrier ») |
| F4 | **Aussi les mois suivants** (décoché = ce mois) | Double négation mentale ; erreur classique de montant |
| F5 | Photo multi-lignes / « Ouvrir à part » | Correctif 6.73 présent ; reste un parcours de secours — à **fumer** |

### FAIT

| Id | Friction | Pourquoi |
|----|----------|----------|
| T1 | Bouton **FAIT ✓** clair ; **Remettre** plus discret | Bien. Risque : tap accidentel sur petite carte Ultra vs densités |
| T2 | Mois passé = dialogue (bien) ; mois courant = snackbar seule | Incohérence légère de « gravité » — le dialogue rassure plus |
| T3 | Aperçu futur = snackbar d’interdiction | Message long ; OK |

### Snackbar

| Id | Friction | Pourquoi |
|----|----------|----------|
| S1 | Durée Short + dismiss | Annuler exige rapidité / attention visuelle |
| S2 | Placement haut (sous sticky) | Sur Ultra grand écran, loin du pouce qui vient de taper FAIT en bas de carte |
| S3 | Message « Noté FAIT » très court | Clair ; n’explique pas la date « FAIT le … » qui apparaît sur la carte |

### Dossiers

| Id | Friction | Pourquoi |
|----|----------|----------|
| D1 | Entrée uniquement Menu (si N>0) ou tip Accueil « courrier sans plan » | Accueil protégé = bien ; découverte lente |
| D2 | Ligne → instance du mois **ou** template | Deux destinations selon l’état — « j’ouvre le dossier » n’est pas toujours la même fiche |
| D3 | Soldés repliés | Bon. Symbole ▾/▴ peu familier hors apps |
| D4 | Empty : chemin « Retard ou accord » sur une facture | Correct mais multi-étapes avant la 1ʳᵉ liste Dossiers |

### À venir

| Id | Friction | Pourquoi |
|----|----------|----------|
| V1 | « Plus loin : Accueil → calendrier » | « Plus loin » = vague ; pas de bouton qui ouvre le picker |
| V2 | Détail lecture seule, cartes type Accueil **sans** FAIT | Cohérent ; frustration si elle veut cocher depuis là |
| V3 | Compteur « N lignes » | Technique ; « 3 factures » serait plus parlant |

### Onboarding (SAF + alertes)

| Id | Friction | Pourquoi |
|----|----------|----------|
| O1 | Enchaînement **Notifications → réveil exact → batterie non limitée** + sorties Réglages Samsung | Classic One UI ; chaque écran système casse le fil ; risque d’abandonner à « Continuer » avec rappels morts |
| O2 | Hint Device Care (Samsung) | Court et bon ; encore un 4ᵉ endroit mental |
| O3 | SAF « Documents, pas Téléchargements » | Juste ; beaucoup choisissent Téléchargements par habitude |
| O4 | Continuer avec warnings | Protège le flux ; crée des mois sans son — le **!** Menu doit alors porter 100 % de la reprise |

---

## 2. Gestes manquants (attentes 60+ / One UI)

Non-recommandations de feature creep — **lacunes ressenties** vs habitudes Galaxy :

1. **Ouvrir ce mois sur Accueil** depuis une ligne / détail **À venir** (1 tap).
2. **Retour mois en cours** toujours visible dès qu’on n’y est pas (déjà en aperçu futur + Menu ; moins évident en mois passé via sticky seul).
3. **Annuler FAIT** plus long ou ancré près du pouce (ou dialogue léger comme mois passé).
4. **Libellé explicite** sur le repli sticky (« Voir épargne et détails » au lieu de Plus) — geste présent, wording manquant.
5. **Raccourci jour d’épargne** depuis la sheet Accueil (aujourd’hui QuietLink « Jour et détail › » — discret).
6. **Marquer FAIT depuis la notification** (si déjà prévu côté alarmes : à valider au smoke ; sinon attente forte).
7. **Glisser pour FAIT** : *non* demandé ici — les 60+ se trompent souvent ; mieux vaut garder le bouton nommé.
8. **Aide contextuelle 1 phrase** sur Courses « après » au premier usage (coach mark) — absente.
9. **Depuis Dossiers empty**, pas de lien direct « Ajouter une facture » (seulement courrier / chemin fiche).
10. **Contact** : pas de rappel version/appareil dans le flux perçu (côté intent possible — à vérifier smoke) pour qu’elle n’ait pas à « expliquer le téléphone ».

---

## 3. Copy — points durs

| Zone | Copy actuelle (idée) | Risque | Piste familiale (audit, pas spec) |
|------|----------------------|--------|-----------------------------------|
| Sticky | Plus / Moins | Opaque | « Autres infos » / « Masquer » |
| Courses | · après X € | Ambigu | « il reste X € après courses » |
| Sticky secondaire | Après les FAIT … | Mieux qu’avant (« Hors à payer ») mais encore pro | Garder ; tester à voix haute |
| Épargne sheet | compte dans le Reste | Confusion compte | « ce montant est déjà retiré du reste du mois » |
| Réglages | Compte & données | Faux « compte » | « Données & argent » / « Argent & sauvegarde » |
| Réglages | Résumé → Dossiers | Flèche = nav | « Préparer un résumé » |
| Menu | Un souci ? Écrire à Arkalia Système | Marque | Garder Arkalia ; éventuellement « l’équipe de l’app » en sous-sens |
| Fiche | Lire +++ sur la photo | Jargon | « Lire la communication sur la photo » |
| Fiche | Ranger sans plan | Idiome | « Garder le courrier (sans montant) » |
| À venir | N lignes | Technique | « N paiements » / « N échéances » |
| Onboarding | Réveil à l’heure | ≈ exact alarm | Aligner libellés One UI vus sur S25/S26 au smoke |
| Alertes | Batterie → Limites d’arrière-plan | Bon | Vérifier chemin exact One UI version téléphone |

Ton global **déjà** plutôt familial (FAIT ✓, Tout est payé, Mois en cours, J’ai dépensé) — les durs sont surtout les **mots-système** et les **raccourcis de ledger**.

---

## 4. Top 12 — priorités « demain » (smoke + décisions)

Ordre **praticité famille**, pas backlog technique. Aucun item n’est « codé » ici.

1. **Smoke S26 (ou sim hauteur courte) : sticky** — Courses visible ? Épargne derrière Plus trouvée en ≤ 10 s ? Reste lisible avec police One UI agrandie ?
2. **Smoke FAIT + snackbar** — temps pour toucher Annuler ; lisibilité sous sticky ; haptique OK.
3. **Smoke mois passé / futur** — dialogue passé ; blocage aperçu ; bouton Mois en cours.
4. **Smoke Courses** — J’ai dépensé puis Corriger ; compréhension « hors Reste ».
5. **Smoke épargne** — sheet Accueil + lien Jour/détail ; FAIT ligne = virement.
6. **Smoke calendrier** — ouvrir année, pastille, retour mois en cours.
7. **Smoke Menu !** — alertes volontairement cassées → badge compris ?
8. **Smoke hub Réglages** — trouver Alertes / Sauvegarde / Mettre de côté sans Aide.
9. **Smoke fiche courte** — loyer simple Enregistrer ; puis un passage « Retard ou accord » sans se perdre.
10. **Smoke photo / Ouvrir à part** (chaîne multi-fichiers 6.73) — échec décode → secours.
11. **Smoke onboarding / reprise Alertes** — Continuer avec warning puis Menu → Alertes.
12. **Décision copy express** (30 min, sans coder le reste) : Plus ; Compte & données ; après courses ; Arkalia — 3–4 libellés max à trancher avant une passe code.

---

## 5. Ce qui est déjà clair (ne pas casser)

- **Un hero** : Reste (ou Déficit) après factures, gros chiffre, sticky papier sans logo.
- **Socle pouce** : Menu · + Ajouter (et Mois en cours en aperçu).
- **FAIT ✓** / Remettre + pastilles En retard / Prélèvement / Accord — langage métier familial tenu.
- **Tout est payé** / **À payer X €** — statut immédiat sous le Reste.
- **Courses toujours sur sticky** (hors aperçu) — entrée unique sheet.
- **Zéro pixel Dossiers** tant qu’il n’y a rien — Accueil non pollué.
- **Menu allégé** (plus de mur de mois) + contact en QuietLink bas.
- **Calendrier = carnet** (années + pastilles), pas grille agenda Google.
- **À venir = lecture seule** explicite — pas de faux FAIT.
- **Hub 3 groupes** — structure prévisible une fois apprise.
- **Cibles ≥ 48 dp**, contraste fort, `DisplayFit` sans `if (Ultra)`.
- **Mois passé** : confirmation avant bascule FAIT — respect du doute.
- **Aide / HowItWorks** : phrases courtes post-6.72 — bonne base si on y envoie rarement.

---

## 6. Synthèse chercheur

L’app se comporte déjà comme un **carnet du mois**, pas une banque — c’est le bon cadre mental pour Senior.  
Les risques praticité restants sont surtout : **(1)** épargne et détails **cachés derrière Plus** sur S26, **(2)** **profondeur Réglages** + libellé « Compte », **(3)** **snackbar FAIT trop brève / trop haute**, **(4)** **onboarding permissions** Samsung qui laisse des rappels morts, **(5)** quelques **mots ledger** (après, +++, Ranger).

Score honnête **lecture code seule** : praticité Accueil ~ **8/10** si smoke Plus/snackbar OK ; parcours secondaire (épargne jour, dossiers 1ʳᵉ fois, onboarding) ~ **6,5–7/10**.  
**Pas** un claim « 9/10 dans la main » — aligné journal 6.75.

---

*Fin audit nuit-02 · fichier seul · aucun code modifié.*
