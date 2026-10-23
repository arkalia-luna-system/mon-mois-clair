# Synthèse priorités — audits nuit 2026-10-23

| | |
|--|--|
| **Date** | 2026-10-23 → lecture demain matin |
| **Sources** | `nuit-01` … `nuit-08` (8 audits, lecture seule code `0.1.0-6.75`) |
| **Branche** | `develop` · **aucun** correctif app dans cette passe |
| **Claim** | « 9/10 dans la main » = **non** · journal 6.75 tenu |

---

## 1. Verdict global

Mon Mois Clair est déjà un **carnet belge crédible** en code : ledger Accueil, FAIT, offline réel, EPC/`+++`, dossiers, privacy manifeste, tokens papier tenus. Ce qui empêche le claim premium / vendable, ce n’est plus « manquer de features » — c’est un **triangle** :

1. **Métier BE** — trois pièges où l’app a l’air sûre alors que le geste banque ou le Reste peuvent être faux (propagation `+++`, 1ʳᵉ mensualité d’accord, EPC sans Purpose).
2. **Smoke doigt** — S26 (cible n°1) jamais fumé ; photo multi-ligne / sticky Plus / snackbar / notif encore ouverts.
3. **Vitrine & trust** — dépôt privé OK ; GitHub public bloqué par PII archives ; profil recruteur cache le craft Kotlin.

Direction visuelle et UX famille sont **au-dessus** des carnets génériques, mais le sticky Accueil et le widget diluent encore le Reste serif. Ingénierie saine (MutatingRepository, 305 unit) avec dette god-files — **reporter** après smoke + P0 métier. Produit : cadeau famille d’abord ; one-shot ~10 € seulement après S26 vert.

---

## 2. Notes par métier

| Audit | Note / lecture | Score honnête |
|-------|----------------|---------------|
| **01 DA visuel** | Charte tenue ; fracture widget sans serif + sticky bavard | **7,5 / 10** |
| **02 UX famille** | Accueil clair ; Plus / snackbar / Réglages « Compte » / onboarding Samsung | Accueil ~**8** · secondaire **6,5–7** |
| **03 Produit / vente** | Différenciation BE+privacy forte ; pas encore indispensable ni vendable | Diff **8,5** · vendable **3–4** |
| **04 Sécu / privacy** | Pas de P0 runtime ; P0 docs avant public ; bio = UI gate | Réseau **9,5** · repos **5** · public **Non** |
| **05 Ingénierie** | Couches OK ; god-files Entry/Home ; 0 androidTest / MigrationTest | Maintenabilité **6,5 / 10** |
| **06 A11y / One UI** | Socle 48 dp / sheets / IME tenu ; risque fontScale + S26 réel | Code solide · main **ouvert** |
| **07 GitHub carrière** | Craft ~8 caché ; vitrine ~4 ; ne pas public demain | Vitrine **~4 / 10** |
| **08 Domaine BE** | Socle excellent ; P0 argent/banque B01–B03 | Confiance métier **~7,5 / 10** |

---

## 3. Top 15 — demain (ordre strict)

Ordre : **P0 métier BE** → **smoke doigt** → **P1 UI/a11y** → **GitHub/career** → **eng** (sans casser la charte).

| # | Priorité | Action | Audit | Type |
|--:|----------|--------|-------|------|
| **1** | P0 BE | **B02** — plafonner 1ʳᵉ mensualité d’accord (`installmentCents` à add + convert) | 08 | **corriger** |
| **2** | P0 BE | **B03** — durcir `EpcQrParser` (Purpose optionnel + scan `+++` dans payload) | 08 | **corriger** |
| **3** | P0 BE | **B01** — décision produit puis politique `+++` (ne pas propager aveuglément hors dossiers/prélèvements) | 08 | **corriger** (+ tip Aide) |
| **4** | Smoke | **S26 / hauteur courte** : sticky (Courses, Plus→épargne), Reste + fontScale ≥ 1,3 | 02 · 06 | **smoke** |
| **5** | Smoke | **Photo multi-ligne** (loyer3 webp+PDF) + « Ouvrir à part » | 02 · 03 · 05 | **smoke** |
| **6** | Smoke | **FAIT + snackbar** (durée / placement / Annuler) + mois passé dialogue | 02 · 06 | **smoke** |
| **7** | Smoke | **Notif** silhouette + canal teal ; optionnellement FAIT depuis notif | 01 · 03 · 05 | **smoke** |
| **8** | Smoke | **Widget** Reste + nuit + verrou bio ; noter fracture serif | 01 · 05 | **smoke** |
| **9** | P1 UI | Widget = **même Reste** que l’app (serif / équivalent RemoteViews) + tokens muted alignés | 01 | **corriger** *(après smoke #8)* |
| **10** | P1 UI | Accueil **quieting sticky** : mois muted ; moins de concurrence visuelle avec le Reste | 01 · 02 | **corriger** |
| **11** | P1 a11y | Plafond effectif hero sous **fontScale** agressif (P0-1 a11y) si smoke #4 rouge | 06 | **corriger** *si besoin* |
| **12** | P1 a11y / copy | FAIT compact **88→96** dp **ou** charte ; libellés **Plus** / **Compte & données** / « après courses » (3–4 mots max) | 02 · 06 | **corriger** / **doc** |
| **13** | Career | Bio + pins + README profil (**Kotlin/Compose**) — **sans** rendre le repo public | 07 | **doc** (GitHub profil) |
| **14** | Privacy docs | Avant tout public futur : scrub archives PII + copy « archive de sécurité » = clair | 04 · 07 | **doc** / **reporter** public |
| **15** | Eng | Découpe mécanique `EntryFormScreen` **ou** MigrationTest 7→8 — **seulement** si #1–8 verts ; sinon reporter | 05 | **reporter** *(défaut)* |

**Ne pas demain :** Play Store, pricing IAP, OCR montant, banque, redesign Accueil complet, `visibility: public` sur MMC, bump version « premium ».

---

## 4. Tableau thème → fichier → action

| Thème | Fichier audit | Action dominante |
|-------|---------------|------------------|
| Direction artistique / papier | [`nuit-01-da-visuel.md`](./2026-10-23-nuit-01-da-visuel.md) | **corriger** widget serif + sticky (après smoke) · polish P1 cartes |
| UX praticité 60+ / One UI | [`nuit-02-ux-famille.md`](./2026-10-23-nuit-02-ux-famille.md) | **smoke** 12 points · **doc**/décision copy express |
| Produit / vente / pricing | [`nuit-03-produit-vente.md`](./2026-10-23-nuit-03-produit-vente.md) | **smoke** P0 · **reporter** prix/Play · **doc** positionnement |
| Sécurité & privacy | [`nuit-04-securite-privacy.md`](./2026-10-23-nuit-04-securite-privacy.md) | **doc** P0 publication · **reporter** chiffrement / SQLCipher · P1 notif bio |
| Ingénierie Android | [`nuit-05-ingenierie-android.md`](./2026-10-23-nuit-05-ingenierie-android.md) | **reporter** god-files · **smoke** photo · MigrationTest = P1 après |
| Accessibilité / S26 | [`nuit-06-a11y-oneui.md`](./2026-10-23-nuit-06-a11y-oneui.md) | **smoke** 10 points · **corriger** fontScale si rouge |
| GitHub / carrière | [`nuit-07-github-carriere.md`](./2026-10-23-nuit-07-github-carriere.md) | **doc** bio/pins/README profil · **reporter** public MMC |
| Domaine factures BE | [`nuit-08-domaine-belgique.md`](./2026-10-23-nuit-08-domaine-belgique.md) | **corriger** B01–B03 · P1 IBAN PDF / tips |

---

## 5. Ce qu’il ne faut PAS casser (charte carnet)

Verrous directionnels — toute « amélioration » ici **casse** le positionnement :

| Verrou | Pourquoi |
|--------|----------|
| Palette crème / encre / **un** teal `#1B4D4E` | Anti-fintech, anti-purple/glass |
| Serif **uniquement** pour le Reste (in-app) | Signature carnet |
| Pas de logo / wordmark sur sticky Accueil | Ledger, pas landing |
| Un hero Accueil (pas camembert, solde banque, 2ᵉ chiffre) | Clarté &lt; 2 s |
| FAIT forêt ≠ teal barre « + Ajouter » | Un primary barre, une action ligne |
| Pastille Auto or **fond** seulement ; brique retard (pas rouge sang) | Contraste + calme |
| Zéro `INTERNET` · pas de compte · pas de pub | Promesse vérifiable |
| OCR **`+++` seulement** — jamais le montant | Reste intact |
| Pas d’IBAN / « total dettes » sur Accueil | Charte dossiers |
| FAIT interdit en aperçu futur ; confirm mois passé | Règles mois |
| Cibles ≥ 48 dp · corps ≥ 18 sp · contraste ≥ 7:1 (cible) | Famille Galaxy |
| Socle pouce Menu / + Ajouter · Courses sur sticky | UX 60+ |
| Dossiers = 0 pixel tant que N=0 | Accueil non pollué |
| MutatingRepository = goulot écritures | Alarmes / backup / widget cohérents |
| Keystore hors git · même keystore famille | Sideload MAJ |
| Repo **privé** tant que PII archives ouvertes | Privacy proches |

---

## 6. Score vendable / premium (honnête)

| Axe | /10 | Lecture |
|-----|-----|---------|
| Code carnet (fonctionnel BE + offline) | **8–8,5** | Au-dessus des pairs génériques |
| Direction papier (DA) | **7,5** | Tenue ; widget + sticky freinent le « terminé » |
| Dans la main (S26 / famille) | **~5–6** | Non prouvé — smoke ouvert |
| Confiance métier argent/banque | **~7,5** | P0 B01–B03 avant claim « app chère » |
| Trust sécurité « premium » | **~6** | Excellent réseau ; repos clair ; bio UI-only |
| Vendable (prix + canal + preuves) | **3–4** | Cadeau oui · produit payant non |
| Vitrine carrière GitHub | **~4** | Craft caché ; fix profil sans public |
| **Premium « 9/10 dans la main »** | **Non** | Aligné journal 6.75 |

**Phrase unique :** *objet papier presque fini en code ; demain = corriger ce qui fait payer faux, fumer le S26, puis seulement polish UI / carrière — sans trahir la charte.*

---

## 7. Top 5 à lire demain matin (ordre)

1. **[`nuit-00` — ce fichier]** — priorités + verrous  
2. **[`nuit-08-domaine-belgique.md`](./2026-10-23-nuit-08-domaine-belgique.md)** — P0 B01–B03  
3. **[`nuit-02-ux-famille.md`](./2026-10-23-nuit-02-ux-famille.md)** — checklist smoke doigt  
4. **[`nuit-06-a11y-oneui.md`](./2026-10-23-nuit-06-a11y-oneui.md)** — smoke S26 / fontScale (10 points)  
5. **[`nuit-03-produit-vente.md`](./2026-10-23-nuit-03-produit-vente.md)** — ce qu’on ne vend / ne code pas demain  

*(Compléments si temps : `nuit-01` pour widget/sticky · `nuit-07` pour bio GitHub 45 min · `nuit-04` avant tout public.)*

---

*Synthèse compilée 2026-10-23 · 8 audits · zéro édition `app/`.*
