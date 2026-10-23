# Audit produit / vente — Mon Mois Clair

| | |
|--|--|
| **Date** | 2026-10-23 (nuit) |
| **Profil** | Product lead — apps privacy-first / bill reminder Belgique |
| **État code** | `0.1.0-6.75` · schéma Room **8** · **305/305** unit · claim « 9/10 dans la main » **non** |
| **Sources** | `CHARTE_ET_ARCHITECTURE.md` · `PRIVACY.md` · `BENCHMARK_QUALITE_2026.md` · `PLAN_PRODUIT_FINI.md` · `DISTRIBUTION_FAMILLE.md` · `PROGRESS_LOG.md` · archives feedback / recherche outils |
| **Portée** | **AUDIT ONLY** — aucun commit code app |

---

## 1. Positionnement

### Une phrase

**Le carnet papier du mois belge, sur le téléphone — pas une banque, pas un budget US, pas un tracker de dettes.**

### Catégorie réelle

| Famille | MMC |
|---------|-----|
| Argent complet (YNAB, Copilot, Monarch) | **Non** — barème visuel seulement |
| Carnet de factures (AllPaid, Billiary, Smart Bill Reminder) | **Oui** — pairs |
| Niche BE | **Oui** — EPC / `+++` mod 97 / accords / courriers hors Reste |

### Différenciation défendable (aujourd’hui)

| Atout | Pourquoi ça vend (ou retient) | Limite |
|-------|-------------------------------|--------|
| **100 % offline, 0 INTERNET, 0 pub, 0 compte** | Promesse rare et **vérifiable** (manifeste) — crédible face aux « privacy washing » Play | Distribution sideload = friction install / MAJ |
| **Papier belge** (QR EPC, `+++`, OCR `+++` seulement) | Geste métier : coller → banque sur le **même** téléphone. Les pairs génériques ne font pas ça | Niche = marché plus petit ; peu d’effet viral hors BE/LU/FR frontaliers |
| **Accueil = ledger** (Reste · À payer · FAIT · Courses) | Répond en &lt; 2 s aux 2 questions qui retiennent : *quoi payer* / *combien reste* | Exige discipline produit : toute feature hors ledger tue la clarté |
| **Dossiers / accords** (reste dû, IBAN, Écrire, frais datés, Mes dossiers) | Cas Alex : retards / acomptes sans snowball ni coach dette | Invisible pour maman (0 dossier = 0 ligne Menu) — **bonne** chose, mais peu de démo store |
| **Preuve** (photo WebP + PDF courrier, résumé PDF gros caractères) | Confiance familiale + médiation / courrier — pas un camembert | PDF ≠ formulaire CPAS ; ne pas sur-promettre |
| **Sideload famille** (APK ~14 Mo, même keystore) | Cadeau / cercle restreint sans Play | Pas de découverte organique ; support = mail Arkalia |
| **Sérénité** (biométrie, `FLAG_SECURE`, backup SAF, contraste ≥ 7:1, One UI 48 dp) | Premium **crédible** sans abonnement SaaS | Smoke S26 (cible n°1) encore manquant |

### Ce que ce n’est **pas** (à dire clairement, y compris en vente)

- Pas le solde banque.
- Pas un outil d’open banking.
- Pas un coach « debt-free date ».
- Pas un Excel CPAS officiel.
- Pas une app Play Store grand public **tant que** la distribution reste sideload.

### Persona payant (hypothèse produit)

| Persona | Job-to-be-done | Willing-to-pay ? |
|---------|----------------|------------------|
| **Usage principal** (cadeau famille, S26 6,3″) | Voir le mois sans calculer, cocher FAIT, photo du papier | Oui si **calme** + zéro pub + install simple — prix **unique** ou très bas abonnement |
| **Ultra / accords** | Suivre un reste dû, copier IBAN/`+++`, écrire au créancier | Oui **plus** si dossiers / PDF / OCR `+++` restent fiables — justifies premium one-shot |
| **Acheteuse Arkalia** (construit + envoie APK) | Cadeau utile + contrôle données famille | Paie en **temps** aujourd’hui ; monétisation = plus tard ou licence familiale |

---

## 2. Features candidates (alignées charte)

Légende : **Avec** = digne d’une feuille de route produit · **Sans** = hors charte / hors vente crédible.

### 2.1 Avec — renforcent « indispensable »

| Idée | Pourquoi | Garde-fous charte |
|------|----------|-------------------|
| **Smoke S26 + polish doigt** (photo multi-ligne, sticky Plus, snackbar, notif, contact Menu) | Sans ça = pas vendable : claim qualité mensonger | Déjà planifié (`PLAN_PRODUIT_FINI`) — P0 |
| **Fiabilité rappels** (Santé Alertes / Device Care / alarmes exactes) | Une notif ratée = app « inutilisable » en 30 j | Un seul hub Alertes ; pas de pastille batterie permanente |
| **Backup / restore familial** (SAF + ZIP photos, guide 3 gestes) | Offline = *tu* es le cloud ; panne téléphone = perte totale sinon | Backups en clair = dire vrai dans PRIVACY |
| **Dossiers soldés / historique clair** | Feedback famille déjà formulé | Pas de 2ᵉ hero ; compteur Menu = ouverts seulement |
| **Onboarding 60 s** (« Reste ≠ banque », FAIT, photo, Menu Arkalia) | Réduit abandon ; protège le positionnement | Pas de plaque marketing sur Accueil ; pas de logo sticky |
| **Résumé PDF / texte** déjà là — **rituel mensuel** (Aide : « fin de mois ») | Habitude = rétention sans gamification | Pas de streak / badge / score |
| **Widget + biométrie** déjà là — **preuves store / fiche produit** | Différenciateur vs freewares à pub | Widget masque montants si lock |
| **Play Store tests internes** (option DISTRIBUTION) | MAJ auto sans USB — *après* objet fini | Hors produit **actuel** ; pas avant smoke + keystore/process figés |
| **Licence familiale 1–5 appareils** (modèle vente, pas feature UI) | Cadre légal/éthique sideload | Pas de licence serveur (pas d’INTERNET) → honneur + facture mail |
| **Export « mois » pour médiation** (déjà résumé) — phrase Aide « pour écrire, pas pour le CPAS » | Valeur accords sans juridiciser | Interdit : formulaire officiel, conseils juridiques |

### 2.2 Avec — nice-to-have (après indispensable)

| Idée | Note |
|------|------|
| Templates factures courantes BE (loyer, énergie, mutuelle) préremplis **vides** | Accélère saisie ; montants toujours manuels |
| Rappel J−3 déjà présent — copy plus claire « demain / aujourd’hui » | Polish, pas feature |
| Partage APK « notice 1 page » PDF dans `dist/` | Vente / cadeau, hors app |
| Apparence / nuit — déjà fait ; **captures** marketing jour+nuit | Asset vente |
| Scission god-files Home/Entry | Dette tech → vélocité, pas pitch client |

### 2.3 Sans — refus explicite (même sous pression « pour vendre »)

| Idée | Pourquoi non |
|------|----------------|
| Connexion banque / PSD2 / agrégateur | Charte §8 · risque + charge · tue privacy |
| OCR / IA du **montant** | Fausse le Reste · charte explicite |
| Catégories, enveloppes multiples, graphiques / camemberts | Abandon + hors ledger |
| Pubs, freemium agressif, compte cloud obligatoire | Contredit PRIVACY + positionnement premium |
| Snowball / avalanche / « date sans dette » | Promesse toxique · Undebt.it = leçon |
| Synchro multi-appareils serveur | INTERNET + compte |
| Grille calendrier 31 jours type Google Agenda | Charte : carnet mois, pas agenda |
| Report auto du solde mois→mois | Écart espèces s’accumule |
| Huissier / juridisme / formulaire CPAS | Feedback + recherche : hors produit |
| Streaks, gamification, social | Contre « carnet apaisé » |

### 2.4 Zone grise (décider plus tard, pas demain)

| Idée | Condition pour « Avec » |
|------|-------------------------|
| Play Store **public** | Privacy policy figée, screenshots S26, processus MAJ, support charge ; éventuellement IAP one-shot |
| Abonnement mensuel | Seulement si valeur **récurrente** claire (sinon one-shot) — app offline n’a pas de coût serveur |
| Chiffrement backups | Améliore menace ; complexifie restore famille |
| iOS | Autre produit ; pas le même sideload |

---

## 3. Pricing réaliste (Belgique / privacy-first / pas de pubs)

### Contexte marché (ordres de grandeur 2025–2026)

| Type | Fourchette typique | Lecture pour MMC |
|------|--------------------|------------------|
| Bill reminder Play (freemium + pubs / IAP) | 0 € + pubs, ou **2–5 €** unlock, ou **2–4 €/mois** Pro | MMC **ne peut pas** concurrencer sur le prix 0 € + pubs — concurrence sur **confiance** |
| Budget apps (YNAB-like) | **10–15 €/mois** | Trop cher et mauvaise catégorie — ne pas s’y comparer en prix |
| Apps BE « sérieuses » one-shot (productivité / santé) | **5–15 €** achat unique | **Zone crédible** pour un carnet fini |
| Cadeau familial sideload | Souvent **0 €** (lien affectif) | Monétisation ≠ jour 1 famille proche |

### Recommandation pricing (product lead)

| Phase | Modèle | Prix indicatif TTC BE | Pourquoi |
|-------|--------|----------------------|----------|
| **A — Cercle famille / beta** | Gratuit (cadeau) + feedback | 0 € | Preuve rétention 90 j ; smoke réel |
| **B — Première vente crédible** | **Achat unique** « licence foyer » (jusqu’à N Galaxy, honneur) | **9,90 €** ou **12,90 €** | Aligné « objet papier terminé » ; pas de mensuel à justifier sans cloud |
| **C — Si Play + IAP** | Même one-shot in-app ; évent. « Famille » **19,90 €** | Évite abonnement toxique pour app offline | |
| **À éviter** | Abonnement &lt; 3 €/mois « pour faire comme les autres » | Signal cheap + friction résiliation + pas de coût infra | |
| **À éviter** | 49 €+ positionnement « wealth » | Mensonge vs scope carnet | |

**Premium sans pubs** se monétise par : (1) **finition** (One UI, a11y, offline réel), (2) **niche BE** (EPC/`+++`/dossiers), (3) **prix unique honnête** — pas par un paywall qui retire le FAIT.

Seuil psychologique BE : autour de **10 €** « je m’offre un carnet qui reste à moi » ; au-delà de **15–20 €** il faut une preuve sociale (avis, médiateur, presse locale) que MMC n’a pas encore.

### Ce qui doit être vrai avant d’afficher un prix

1. Smoke **S26** (persona n°1) vert sur P0 doigt.
2. Install sideload **&lt; 5 min** avec `DISTRIBUTION_FAMILLE` (ou Play interne).
3. Une phrase PRIVACY lisible par maman.
4. Pas de claim « 9/10 » tant que le journal dit le contraire.

---

## 4. Risques (produit / vente)

| Risque | Sévérité | Mitigation |
|--------|----------|------------|
| **Objet pas fini dans la main** (S26, photo, notif) | Haute | P0 demain = smoke, pas features |
| **Sideload = plafond d’échelle** | Haute pour « vendable grand public » | Assumer niche ; Play interne plus tard |
| **Perte de données** (pas de cloud) | Haute émotionnellement | Backup ritualisé + Aide + test restore |
| **Scope creep** (banque, graphs, OCR montant) sous pression « pour vendre » | Haute | Charte § Refuser = contrat vente aussi |
| **Positionnement flou** (« encore une app budget ») | Moyenne | Copy README / fiche : *carnet du mois, pas une banque* |
| **Support mail seul** (Arkalia) | Moyenne | QuietLink Menu déjà là ; volume = limiter cercle ou FAQ |
| **Keystore / MAJ cassée** | Haute ops | Même keystore ; doc DISTRIBUTION ; jamais debug famille |
| **Backups en clair** | Moyenne confiance | Transparence PRIVACY ; chiffrement = phase ultérieure |
| **Dossiers sous-utilisés** (maman) | Basse–moyenne | OK produit ; pitch marketing séparé « si accords » |
| **Dette tech god-files** | Moyenne vélocité | Après smoke ; pas bloquant vente foyer |

---

## 5. Roadmap vers « indispensable & vendable »

### Définition de done (product)

| Niveau | Critère | État ~6.75 |
|--------|---------|------------|
| **Utile famille** | Reste + FAIT + photo + rappel + backup | Code ≈ oui · doigt partiel |
| **Indispensable** | 2+ mois d’usage sans repli Excel/papier ; notifs tenues ; restore testé | **Pas prouvé** |
| **Vendable** | Install sans Arkalia à côté + prix + PRIVACY + captures S26 + zéro mensonge qualité | **Non** |

### Séquence recommandée (pas de dates fantômes)

```
P0  Smoke doigt 6.75 (S25 puis S26) + correctifs bloquants
P1  Rituel backup/restore + onboarding 60 s + notice APK
P2  Preuves vente (captures, 1 page « pourquoi pas banque », PRIVACY courte)
P3  Décision canal : rester sideload foyer  OU  Play tests internes
P4  Prix one-shot + licence foyer (hors app ou IAP si Play)
P5  Polish dossiers soldés / templates BE — seulement si P0–P2 verts
```

**Interdit sur cette route :** ouvrir banque, OCR montant, graphs, abonnement « parce que les autres ».

### Score honnête (product lead)

| Axe | /10 | Commentaire |
|-----|-----|-------------|
| Différenciation BE / privacy | **8,5** | Rare et documentée |
| Complétude carnet (code) | **8–9** | Journal : ~8,5–9 code |
| Indispensable (usage prouvé) | **5** | Manque rétention mesurée + S26 |
| Vendable (prix + canal + preuves) | **3–4** | Cadeau oui · produit payant non |
| Risque scope | **Faible si charte tenue** | Fort si « feature pour le store » |

---

## 6. P0 produit — demain

Ordre strict (un agent, un Gradle, pas de bump version « premium » sans smoke) :

1. **Smoke doigt 6.75** sur S25 (et S26 si dispo) — checklist minimale :
   - Photo multi-ligne (loyer3) : voir / ouvrir à part
   - Sticky compact : Courses visibles ; épargne derrière Plus
   - Snackbar FAIT court
   - Notif silhouette + canal
   - Menu QuietLink → Arkalia Système
   - PageTitle InkMuted (Lock / Scan / fiches / calendrier)
2. **Logger** résultats dans `AUDIT_STATUS.md` / journal — **sans** claim 9/10 si rouge.
3. **Correctifs uniquement** si smoke casse un geste indispensable (photo, FAIT, rappel, restore).
4. **Ne pas** demain : Play Store, pricing IAP, nouvelles features dossiers, OCR élargi, refonte Accueil.

### Question produit à trancher cette semaine (pas dans le code)

> Est-ce qu’on vend **d’abord** un cadeau familial irréprochable (0 €, sideload), ou un **one-shot ~10 €** dès que S26 est vert ?

Recommandation : **cadeau → preuve 60–90 j → one-shot**. Vendre avant indispensable = brûler la confiance privacy.

---

## 7. Synthèse exécutive

Mon Mois Clair est déjà **au-dessus** des carnets génériques sur l’axe Belgique + offline réel + ledger calme + dossiers. Ce n’est **pas** encore un produit « indispensable & vendable » : il manque la preuve doigt sur la cible S26, le rituel backup comme promesse tenue, et un canal/prix assumés **sans** trahir la charte.

La vente premium crédible sans pubs, c’est **l’objet papier terminé à ~10 € foyer** — pas un abonnement, pas des features banque.

---

*Fin audit nuit-03 · Product lead · 2026-10-23*
