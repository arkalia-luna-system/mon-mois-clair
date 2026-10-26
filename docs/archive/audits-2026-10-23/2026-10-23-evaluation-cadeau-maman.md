> **ARCHIVÉ / FIGÉ** (2026-10-26) — ne plus lire pour l’état code. Vérité = `PROGRESS_LOG.md` + PRIVACY/SECURITY + audits `final-0*`.

# Évaluation cadeau — « si un pro te l’avait faite »

| | |
|--|--|
| **Date** | 2026-10-23 |
| **Profil** | Évaluation logiciels / devis apps Android — Belgique / UE francophone, 2025–2026 |
| **Produit** | Mon Mois Clair · `0.1.0-6.80` · Room **8** · ~**318** tests unitaires (claim journal 6.79) |
| **Portée** | **AUDIT ONLY** — doc seule. Estimation de **coût de construction** (devis pro), **pas** de prix Play. |
| **Sources** | `README` · `PROGRESS_LOG` · `docs/audits/2026-10-23-valorisation.md` · nuit-05 ingénierie · nuit-08 métier BE · inventaire `app/` |

---

## 0. Ce qu’on mesure (et ce qu’on ne mélange pas)

| Notion | Sens | Ordre de grandeur |
|--------|------|-------------------|
| **Coût de construction** | Ce qu’un freelance senior BE / une petite agence **facturerait** pour livrer un **équivalent utile** (même promesse : carnet hors ligne, métier belge, widget, backup, sérénité) | **Milliers d’euros** ← **chiffre cadeau** |
| **Prix de vente Play** | Licence foyer one-shot *si* l’app était vendue un jour | **~10 €** (voir audit valorisation) — **autre sujet** |
| **Valeur affective** | Cadeau à maman | **Inestimable** — hors devis |

**Règle de lecture :** dire « ça aurait coûté environ X € » = devis de **fabrication**.  
Ce n’est **pas** « l’app se vend X € », ni une valuation startup.

---

## 1. Objet estimé (état réel, sans gonfler)

### Ce qui est déjà là (charge réelle)

| Bloc | Preuve rapide | Charge relative |
|------|---------------|-----------------|
| Volume code | ~**21 500** LOC Kotlin `main` · ~**7 300** LOC tests · **~211** fichiers `.kt` main · **~87** tests | App **médiane-haute** pour du local-only |
| UI | Accueil (god-file) · fiche entrée · dossiers · À venir · scan EPC · réglages multi-pages · onboarding SAF/permissions · restore · biométrie · ~**18** routes | Plusieurs écrans **denses**, pas un CRUD 3 écrans |
| Domaine | **23** use cases · cycle de mois · FAIT / overdue · accords · hors-mois · politiques `+++` | Cœur métier **non trivial** |
| Papier belge | QR EPC · mod 97 · OCR `+++` · photo WebP · PDF courrier · prélèvement | Niche **spécifique BE** (rare en devis « todo app ») |
| OS Android | Widget RemoteViews · `AlarmManager` exact · WorkManager backup · biométrie · `FLAG_SECURE` | Surfaces OS = **jours** en plus |
| Données | Room schéma **8** · **8** entités · migrations sans destructive · backup SAF JSON+ZIP | Qualité **familiale sérieuse** |
| Qualité | ~**318** unit tests · CI · PRIVACY/SECURITY · polish Galaxy / One UI en cours | Au-dessus d’un MVP « demo » |
| Distribution | Sideload release signé — **pas** Play Store | Moins de process store, **pas** moins de craft |

### Ce qui n’est **pas** encore « produit boutique »

- Smoke doigt / polish Galaxy encore **ouverts** (journal : pas de claim « 9/10 dans la main »).
- Pas de compte, pas de cloud, pas de pub — **volontaire** ; ça **réduit** le devis serveur, ça **n’annule pas** le devis app.
- Licence MIT, usage familial — le devis ci-dessous imagine un **pro payé pour livrer l’équivalent**, pas une revente du repo.

**Verdict honnête :** objet **familial abouti en craft**, encore en polish — digne d’un vrai devis, **pas** d’un fantasme à 50 000 €, **pas** d’un « ça ne vaut rien ».

---

## 2. Hypothèses d’estimation

1. **Reproduction d’équivalent utile**, pas rejouage de chaque micro-commit autodidacte (apprentissage + allers-retours exclus du devis).
2. Stack connue du prestataire : Kotlin, Compose, Room, Hilt — **pas** de formation facturée.
3. Périmètre **charte** : offline, pas de banque, pas de synchro, pas de Play listing marketing.
4. Inclut : métier BE de base, widget, notifs exactes, backup/restore, biométrie UI, tests domaine utiles, polish raisonnable Galaxy.
5. Exclut : campagne Play, backend, support 2 ans, redesign agence « brand book » luxe, instrumentation UI complète.
6. Jour = **8 h**. Montants **HT** indicatifs (TVA BE à ajouter selon statut du prestataire).
7. Tarifs **marché BE 2025–2026** (fourchettes courantes, pas rates « Big Tech »).

---

## 3. Charge en heures (WBS condensé)

| Lot | Freelance senior (h) bas · méd · haut | Notes |
|-----|----------------------------------------|-------|
| Socle (projet, DI, thème, nav, horloge BE) | 16 · 24 · 32 | |
| Room + ledger + cycle de mois | 28 · 44 · 60 | Migrations soignées = haut |
| Accueil Compose (Reste, FAIT, sheets) | 36 · 52 · 72 | Densité UI réelle |
| Fiche entrée + photo + PDF joint | 40 · 64 · 88 | Gros fichier = effort |
| Métier BE (EPC, `+++`, OCR, prélèvement) | 20 · 36 · 52 | Niche |
| Dossiers / accords / courrier | 28 · 44 · 64 | |
| Rappels exacts + écran santé | 14 · 24 · 36 | |
| Widget RemoteViews | 12 · 20 · 32 | One UI = pièges |
| Backup SAF + restore | 14 · 24 · 36 | |
| Biométrie + surfaces privacy | 6 · 10 · 16 | |
| Réglages / Aide / onboarding / PDF résumé | 20 · 32 · 44 | |
| Tests unitaires domaine (~300) | 28 · 44 · 64 | |
| Polish a11y / DA carnet / device | 20 · 36 · 56 | Encore ouvert → haut |
| CI, signing, distro sideload | 6 · 10 · 16 | |
| **Sous-total freelance** | **~288 · ~464 · ~668 h** | |
| **Jours freelance (÷ 8)** | **~36 · ~58 · ~84 j** | |

**Petite agence** (design léger + PM + QA + handoffs) : multiplier typique **× 1,35 – 1,55** sur les heures.

| | Bas | Médian | Haut |
|--|-----|--------|------|
| **Heures freelance senior** | **~290 h** | **~460 h** | **~670 h** |
| **Heures petite agence** | **~400 h** | **~650 h** | **~950 h** |
| **Jours freelance** | **~36 j** | **~58 j** | **~84 j** |
| **Jours agence** | **~50 j** | **~80 j** | **~120 j** |

*Lecture :* le **médian freelance (~460 h / ~2,5–3 mois calendaires à temps plein)** est le scénario le plus crédible pour un livrable familial **équivalent**, sans rejouer toute la courbe d’apprentissage.

---

## 4. Tarifs BE réalistes (2025–2026)

| Profil | Journalier (ordre) | Horaire implicite | Usage dans ce devis |
|--------|--------------------|-------------------|---------------------|
| Freelance Android **mid** | **450 – 550 €/j** | ~55 – 70 €/h | Bas de fourchette |
| Freelance Android **senior** | **550 – 700 €/j** | ~70 – 90 €/h | **Cœur** du chiffre cadeau (**600 €/j** retenu) |
| Petite **agence** (blended design+dev+PM) | **850 – 1 100 €/j** | ~105 – 140 €/h | **950 €/j** retenu |

Pas de rate « 1 500 €/j startup US ». Pas de sous-tarification « étudiant 200 €/j » non plus.

---

## 5. Coût de construction (€ HT)

### Freelance senior (à **600 €/j**)

| | Jours | Montant |
|--|-------|---------|
| **Bas** | ~36 j | **~22 000 €** |
| **Médian** | ~58 j | **~35 000 €** |
| **Haut** | ~84 j | **~50 000 €** |

Le **haut brut** (~50 k€) est un plafond **technique** (tout polish + marge d’aléa métier).  
Pour un **cadeau / devis familial**, on ne cite **pas** ce plafond comme chiffre principal : trop proche d’un fantasme, et il inclut une qualité « boutique » que le journal dit encore ouverte.

### Petite agence (à **950 €/j**)

| | Jours | Montant |
|--|-------|---------|
| **Bas** | ~50 j | **~48 000 €** |
| **Médian** | ~80 j | **~76 000 €** |
| **Haut** | ~120 j | **~114 000 €** |

L’agence est **hors message cadeau** : utile pour comparer, **pas** pour dire à maman. Un foyer belge entend « freelance / petit devis », pas un forfait agence corporate.

### Fourchette **cadeau** (ce qu’on retient pour parler)

Hypothèse commerciale honnête : un freelance senior **fixerait** souvent un forfait un peu **plus serré** que le haut T&M (périmètre cadré, moins de rework autodidacte), autour du **bas–médian** technique, éventuellement avec un mid à 550 €/j sur une partie du lot.

| | Montant HT | Comment le lire |
|--|------------|-----------------|
| **Bas** | **8 000 – 10 000 €** | Devis « carnet offline + EPC + backup », polish correct, moins de dossiers/widget premium |
| **Médian** | **12 000 – 16 000 €** | **Équivalent utile** de Mon Mois Clair tel qu’on le donne en famille |
| **Haut (encore crédible en devis)** | **18 000 – 25 000 €** | Même scope + polish Galaxy soigné + tests + widget One UI + aléas métier BE |

**Chiffre unique « à dire à maman » :**  
**environ 14 000 €**  
(ou, en une phrase souple : *entre 10 et 20 000 €, dis quinze mille*).

*Pourquoi pas 5 000 € ?* Sous-estime le métier belge, le widget, les notifs exactes et ~300 tests.  
*Pourquoi pas 50 000 € ?* C’est le plafond T&M / agence — **pas** le devis qu’on annonce pour un cadeau familial.

---

## 6. Tableau récapitulatif

| Indicateur | Valeur |
|------------|--------|
| Heures freelance (bas · méd · haut) | **~290 · ~460 · ~670 h** |
| Heures petite agence (bas · méd · haut) | **~400 · ~650 · ~950 h** |
| € freelance T&M @ 600 €/j (bas · méd · haut) | **~22 k · ~35 k · ~50 k €** HT |
| € agence T&M @ 950 €/j (médian) | **~76 k €** HT — **hors message cadeau** |
| **Fourchette cadeau (devis forfaitaire réaliste)** | **10 000 – 14 000 – 22 000 €** HT |
| **Chiffre à dire** | **environ 14 000 €** |
| Prix Play (autre sujet) | **~10 €** one-shot foyer *si* vendue un jour |
| Prix cadeau famille aujourd’hui | **0 €** (sideload) |

---

## 7. Phrase prête à lire à voix haute

> Maman, si un développeur pro en Belgique t’avait faite cette appli — le carnet du mois sur le téléphone, sans compte ni internet, avec les factures belges, le petit widget et la sauvegarde — ça aurait coûté **environ quatorze mille euros**. Pas dix euros comme sur le Play Store : quatorze mille, c’est le prix de **fabriquer** l’outil. Je te l’ai faite, moi ; toi, tu l’as pour vivre plus légère.

Version courte (une seule respiration) :

> Si un pro te l’avait faite, ça aurait coûté **environ 14 000 €** — le prix de la construction, pas le prix d’une appli à 10 €.

---

## 8. Garde-fous (anti-mensonge)

- **Ne pas** dire « ça vaut 50 000 € » : plafond technique / agence, pas le devis cadeau.
- **Ne pas** dire « ça ne vaut rien » parce que c’est un cadeau ou une v0.1 : le craft est réel.
- **Ne pas** confondre avec le **~10 €** Play de l’audit valorisation.
- **Ne pas** présenter l’app comme produit Play fini : polish / smoke encore ouverts.
- Les montants sont des **ordres de grandeur de devis**, pas une facture, pas un avis d’expert-comptable, pas une valorisation d’entreprise.

---

## 9. Synthèse exécutive

Mon Mois Clair, à ~21 kLOC + métier belge + surfaces OS + ~318 tests, correspond à **plusieurs mois** de freelance senior. En **T&M** pur, on approche vite **20–35 k€** (voire plus). Pour un **cadeau familial**, le chiffre juste et tenable est :

**environ 14 000 €** (fourchette **10 – 22 000 €**),  
en séparant clairement le **coût de construction** du **prix de vente éventuel (~10 €)**.

---

*Fin évaluation cadeau-maman · devis construction Android BE · 2026-10-23 · état ~0.1.0-6.80.*
