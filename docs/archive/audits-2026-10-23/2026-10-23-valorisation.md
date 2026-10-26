> **ARCHIVÉ / FIGÉ** (2026-10-26) — ne plus lire pour l’état code. Vérité = `PROGRESS_LOG.md` + PRIVACY/SECURITY + audits `final-0*`.

# Audit valorisation — Mon Mois Clair

| | |
|--|--|
| **Date** | 2026-10-23 |
| **Profil** | Analyste pricing — apps privacy-first / sideload Belgique |
| **État produit** | `0.1.0-6.77` · Room **8** · **315/315** unit · branche `develop` |
| **Sources** | `nuit-00` synthèse · `nuit-03` produit/vente · `nuit-04` privacy · `nuit-07` carrière · `nuit-08` métier BE · `PROGRESS_LOG` · `README` · `DISTRIBUTION_FAMILLE` · `BENCHMARK_QUALITE_2026` |
| **Portée** | **AUDIT ONLY** — aucun code. Fourchettes **indicatives**, pas un business plan ni une promesse de CA. |

---

## 0. Verdict en 30 secondes

À **6.77**, Mon Mois Clair est un **objet familial crédible en code** (métier BE P0 fait, privacy documentée, ledger calme) — **pas** encore un produit payant mature, et **pas** une entreprise rentable.

| Canal de « valeur » | Fourchette honnête aujourd’hui | Condition pour le haut de fourchette |
|---------------------|--------------------------------|--------------------------------------|
| **Vente one-shot** (foyer) | **0 €** (cadeau) → **7–13 €** crédible · **15–20 €** seulement avec preuves | Smoke S26 + install sans Arkalia + PRIVACY courte + captures |
| **Location / abo** | **À éviter** · si forcé : **0 €** ou **2–4 €/mois** max (faible rétention attendue) | Seulement avec valeur récurrente claire — **absente** sans cloud / support lourd |
| **Don** | **0–5 €** ponctuel · **5–15 €** « merci » rare | Lien affectif + confiance privacy ; pas un modèle d’échelle |
| **Portfolio / carrière** | **Fort en craft, faible en vitrine** (~8/10 caché, ~4/10 public) | Bio/pins Kotlin + évent. showcase sans ouvrir archives PII |
| **Rentabilité réelle** | **Non** à ce stade | Canal + volume + support + rétention 90 j — tous ouverts |

Synthèse nuit-00 (tenu) : vendable ~**3–4**/10 · dans la main ~**5–6** (smoke ouvert) · carnet code ~**9** · métier ~**9** · DA ~**8,5**.

---

## 1. Ce qu’on valorise réellement (sans mensonge)

### Actif

| Actif | Lecture pricing |
|-------|-----------------|
| App Android offline, 0 INTERNET, 0 pub, 0 compte | Différenciateur **vérifiable** — justifie un prix **au-dessus** des freewares à pubs, pas un prix « wealth » |
| Niche BE (EPC, `+++` mod 97, accords, courrier hors Reste) | Premium **géographique** : marché petit, willingness-to-pay plus haute **par utilisatrice touchée** |
| Sideload familial (~14 Mo, même keystore) | Valeur **cadeau / cercle** dominante aujourd’hui ; plafond d’échelle structurel |
| 315 tests, CI, charte, PRIVACY/SECURITY | Valeur **ingénierie / confiance** — monétisable surtout en **portfolio** et en trust premium, pas en CA immédiat |
| Licence MIT | Facilite partage / fork ; **ne** crée **pas** de moat juridique sur le binary sideloadé |

### Non-actif (ne pas monétiser comme si c’était vrai)

| Claim tentant | Réalité 6.77 |
|---------------|--------------|
| « 9/10 dans la main » | **Non** — smoke S26 ouvert (nuit-00) |
| « Produit Play Store prêt » | **Non** — distribution sideload ; Play = option ultérieure |
| « Coffre-fort / chiffré de bout en bout » | **Non** — Room + backups SAF en clair ; bio = porte UI (nuit-04) |
| « Indispensable prouvé » | **Non** — rétention 60–90 j non mesurée (nuit-03) |
| « Business rentable » | **Non** — 0 canal payant, 0 volume, support = mail Arkalia seul |

---

## 2. Fourchettes — vente one-shot

### Marché de référence (ordres de grandeur BE / EU 2025–2026)

| Comparaison | Prix typique | Pertinence MMC |
|-------------|--------------|----------------|
| Bill reminder Play (unlock / Pro) | ~**2–5 €** one-shot ou **2–4 €/mois** | Concurrent « cheap » ; MMC gagne sur confiance, pas sur 0 € |
| Productivité / santé one-shot « sérieuse » | ~**5–15 €** | **Zone crédible** pour un carnet fini |
| Budget SaaS (YNAB-like) | ~**10–15 €/mois** | **Mauvaise** catégorie de prix — ne pas s’y comparer |
| Cadeau familial sideload | souvent **0 €** | Phase actuelle réelle |

### Fourchette recommandée pour MMC

| Palier | Prix TTC BE (indicatif) | Quand c’est honnête |
|--------|-------------------------|---------------------|
| **Cadeau / beta cercle** | **0 €** | Aujourd’hui — preuve usage, pas CA |
| **Licence foyer one-shot** | **7–10 €** (bas) · **9,90–12,90 €** (cible nuit-03) | Après smoke S26 + notice install + PRIVACY Senior |
| **Licence « Famille » (1–5 appareils, honneur)** | **15–19,90 €** | Seulement si Play ou process MAJ clair + preuves sociales minimes |
| **Au-delà de ~20 €** | **Non crédible** sans presse / médiateurs / avis | Scope = carnet, pas wealth / banque |
| **49 €+ « premium lifestyle »** | **Mensonge de positionnement** | À refuser |

**Seuil psychologique BE** (aligné nuit-03) : autour de **~10 €** « un carnet qui reste à moi » ; au-delà de **15–20 €**, il faut une preuve sociale que MMC n’a **pas**.

**Modèle de licence sans INTERNET :** facture / mail + honneur (pas de serveur de licence). Plafond éthique : cercle où la confiance humaine tient — pas un store anonyme à grande échelle sans IAP.

---

## 3. Fourchettes — location / abonnement

| Modèle | Fourchette | Verdict analyste |
|--------|------------|------------------|
| Abo mensuel « Pro » | **2–4 €/mois** (si on y allait quand même) | **Déconseillé** : pas de coût serveur ; friction résiliation ; signal « SaaS » sur un objet offline |
| Abo annuel | **15–25 €/an** | Équivalent one-shot déguisé — préférer **dire** one-shot |
| « Location » APK limitée dans le temps | **Techniquement fragile** sans INTERNET (horloge locale = contournable) | **À ne pas vendre** comme DRM sérieux |
| Essai payant puis unlock | Possible via Play IAP plus tard | Hors produit actuel |

**Conclusion location :** pour un carnet privacy-first offline, la **location n’ajoute pas de valeur économique réelle** — elle ajoute de la friction et une promesse de récurrence non tenue. One-shot (ou don) > abo « parce que les autres ».

---

## 4. Fourchettes — don / pourboire

| Situation | Fourchette réaliste | Lecture |
|-----------|---------------------|---------|
| Famille proche après cadeau APK | **0 €** (norme) ou **chocolat / retour feedback** | Pas un KPI |
| Proche élargi « merci, ça m’aide » | **5–15 €** ponctuel (virement / cash) | Rare, affectif, **non prévisible** |
| Page don publique (Buy Me a Coffee, etc.) | **Médiane apps indie** souvent **&lt; 50–200 €/an** sans audience | Nécessite vitrine + trafic — absents |
| Mécénat / asbl médiation | **Hors scope** tant que pas de partenaire | Ne pas compter dans un plan |

**Don ≠ rentabilité.** Utile comme thermomètre de gratitude, pas comme P&L.

---

## 5. Valeur portfolio / carrière (hors CA app)

Croisement **nuit-07** : craft Android réel ; vitrine GitHub le **cache** (dépôt privé, bio Python/Flutter, pins AZ).

| Dimension | Estimation qualitative | Monétisation indirecte |
|-----------|------------------------|------------------------|
| Preuve Kotlin / Compose / Room / Hilt | **Haute** (si visible) | Entretiens, missions, CDI mobile BE |
| Preuve privacy-first (0 INTERNET, PRIVACY.md) | **Haute et rare** | Différenciation vs CRUD tutorials |
| Preuve métier BE (EPC, mod 97, dossiers) | **Niche forte** | Crédibilité produit fintech locale / civic tech |
| Vitrine publique actuelle | **~4/10** | **0 €** de signal marché tant que caché |
| Craft caché | **~8/10** contenu | Valeur **optionnelle** : un showcase scrubbé peut valoir plus qu’un one-shot foyer en carrière |

**Fourchette « valeur carrière » (honête, non comptable) :**  
pas un prix de revente du repo — plutôt un **multiplicateur d’employabilité** si bio/pins/README profil alignés **sans** ouvrir les archives PII. Ouvrir le dépôt trop tôt (PII archives) **détruit** de la valeur (nuit-04 / nuit-07).

**Ne pas confondre :** valorisation portfolio ≠ valuation startup. Pas de multiple de CA (CA = 0).

---

## 6. Conditions pour le **haut** de fourchette

### One-shot 12–20 € (haut crédible)

Tout doit être vrai **en même temps** :

1. **Smoke S26** (persona n°1) vert — sticky, fontScale, FAIT/snackbar, photo, widget, notif, QuietLink (checklist nuit-00).
2. **Install / MAJ &lt; 5 min** sans Arkalia à côté (`DISTRIBUTION_FAMILLE` ou Play interne).
3. **Backup / restore** ritualisé et testé (offline = *elle* est le cloud).
4. **PRIVACY** Senior-lisible : clair sur Room, backups, bio = UI, montants notif.
5. **Captures** jour + nuit + widget ; phrase unique : *carnet du mois, pas une banque*.
6. **Zéro claim** « 9/10 / premium parfait » tant que le journal dit le contraire.
7. **Support** borné (FAQ + QuietLink) — volume compatible cercle ou Play.

Sans 1–3 : plafond honnête = **cadeau 0 €** ou **&lt; 10 €** « early access » assumé.

### Portfolio « haut »

1. Bio / pins Kotlin + privacy BE (nuit-07).  
2. Scrub archives PII **avant** tout public.  
3. README produit = empowerment, pas « galère argent perso ».  
4. Option showcase séparé plutôt que dump du monorepo familial.

---

## 7. Ce qui manque pour une **rentabilité réelle**

Définition retenue : **revenus récurrents ou one-shot répétés** qui couvrent (a) temps de maintenance Android/One UI, (b) support, (c) device de test, (d) évent. frais Play — avec marge, pas juste « un virement de cousin ».

| Manque | Pourquoi ça bloque le P&L | Ordre de grandeur d’effort |
|--------|---------------------------|----------------------------|
| **Preuve doigt S26** | Sans confiance qualité → pas de prix défendable | Jours, pas semaines — mais **bloquant** |
| **Rétention 60–90 j** mesurée (2+ foyers) | One-shot sans rétention = bouche-à-oreille mort | Mois calendaires |
| **Canal** (Play interne/public ou vente mail répétée) | Sideload seul = dizaines d’installs max, pas des centaines | Semaines process + politique privacy |
| **Volume** | À ~10 €, il faut **des dizaines** de ventes pour « un salaire d’un jour » ; **des centaines** pour un complément mensuel | Audience inexistante aujourd’hui |
| **Support scalable** | Mail Arkalia seul casse dès &gt; ~20 utilisatrices actives exigeantes | FAQ / bornage cercle |
| **MAJ sans USB** fiables | Keystore unique + doc ; Play = moins de casse | Ops |
| **Preuve sociale** | Avis, presse locale, médiateur — absents | Marketing léger **après** objet fini |
| **Modèle abo non justifié** | Pas de serveur → abo = pure extraction | Ne comble **pas** le trou |
| **Scope creep « pour vendre »** (banque, OCR montant, graphs) | Détruit la promesse privacy / Reste — tue le premium long terme | Discipline charte |

### Scénario numérique (illustratif, pas une prévision)

| Scénario | Volume / an | Prix moyen | CA brut indicatif | Coût réel (temps) | Verdict |
|----------|-------------|------------|-------------------|-------------------|---------|
| Cercle famille | 3–8 installs | 0 € | **0 €** | Élevé (cadeau + support) | **Normal aujourd’hui** |
| Soft launch one-shot | 20–40 ventes | 10 € | **200–400 €** | Support + MAJ | Café / hardware — **pas** un business |
| Play niche BE réussi | 200–500 one-shot | 10–13 € | **2–6,5 k€** | Reviews, CI, support | Complément possible — **pas prouvé** |
| « Salaire » indie | milliers | — | — | Discovery + ads ou presse | **Hors trajectoire** sans trahir privacy ou élargir scope |

**Rentabilité réelle** = au minimum le scénario Play niche **répété sur plusieurs années** avec coût de support sous contrôle — **ou** monétisation **indirecte** (carrière / missions) via portfolio. L’APK sideload seul ne finance pas la maintenance Android 2027+.

---

## 8. Matrice décision pricing (6.77 → suite)

```
Aujourd’hui (6.77)
  → Prix affiché public : NON (ou 0 € cadeau)
  → Valeur réelle : craft + cadeau famille + option carrière

Après smoke S26 + backup rituel + notice
  → One-shot foyer 9,90–12,90 € (honneur) OK à tester
  → Abo : NON

Après 60–90 j rétention + 0 mensonge PRIVACY
  → Décision canal Play interne
  → Famille 15–19,90 € seulement si preuves

Portfolio en parallèle (hors CA)
  → Bio/pins ; archives PII ; pas d’ouverture précipitée
```

**Question déjà posée (nuit-03), toujours valide :**  
cadeau irréprochable d’abord, **puis** one-shot — pas l’inverse. Vendre avant « dans la main » brûle le capital confiance privacy (le seul vrai moat).

---

## 9. Scores valorisation (analyste)

| Axe | /10 | Commentaire |
|-----|-----|-------------|
| Justesse du prix one-shot *si objet fini* | **7** | ~10 € est le bon ordre ; 20 €+ non |
| Justesse d’un abo aujourd’hui | **2** | Mauvais fit offline |
| Potentiel don | **3** | Affectif, non scalable |
| Valeur portfolio (si vitrine alignée) | **7–8** | Craft réel ; exécution vitrine faible |
| Maturité « vendable payant » (6.77) | **3–4** | Aligné nuit-00 / nuit-03 |
| Maturité « business rentable » | **1–2** | Canal + volume + preuve usage manquent |
| Risque de surprix / mensonge | **Élevé si claim 9/10 ou 49 €** | Discipline journal = actif |

---

## 10. Synthèse exécutive

Mon Mois Clair à **6.77** vaut surtout : (1) un **cadeau familial** de haute qualité technique, (2) un **actif de carrière** sous-exploité, (3) une **option** de one-shot ~**10 €** foyer **après** smoke et preuves — pas avant.

Ce n’est **pas** valorisable comme une SaaS, ni comme une app « premium 50 € », ni comme un revenu de substitution. La rentabilité réelle exige un **canal**, du **volume**, et une **rétention** que le sideload seul ne fournira pas ; le haut de fourchette one-shot exige la checklist doigt et l’honnêteté privacy, pas plus de features banque.

---

*Fin audit valorisation · analyste pricing privacy-first BE · 2026-10-23 · état 6.77.*
