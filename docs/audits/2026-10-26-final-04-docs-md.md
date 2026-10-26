# Audit docs MD — trop longs / redondants / obsolètes

| | |
|--|--|
| **Date** | 2026-10-26 |
| **Portée** | **AUDIT ONLY** — inventaire Markdown, claims vs vérité produit, liste garder / fusionner / archiver / supprimer. **Aucun code app.** |
| **Vérité de référence** | Journal **6.82** · `0.1.0-6.82` · `versionCode` **87** · **332/332** unit · Room **8** · crypto **6.81** livrée · polish copy/carnet/widget **6.82** · smoke S25 **partiel** · S26 / doigt crypto **ouverts** · **pas** « 9/10 dans la main » · **pas** « coffre parfait » |
| **Sources croisées** | `PROGRESS_LOG.md` (état global + entrée 6.82) · `README.md` · `PRIVACY.md` / `SECURITY.md` · `docs/README.md` · `docs/archive/README.md` · inventaire `**/*.md` (49 fichiers utiles + AppleDouble `._*` gitignorés) |

---

## 0. Verdict en 30 secondes

La **source de vérité** (journal + table README + PRIVACY/SECURITY) est globalement honnête pour **6.82**. Les problèmes docs sont ailleurs :

1. **Claims faux / décalés** surtout dans les **badges README** (encore 6.81 / 329) et dans des **audits figés lus comme vivants** (chiffrement « en clair », compteurs tests, versions pinées).
2. **Doublons d’audits** (nuit 01–08 + synthèse + valorisation + cadeau + chiffrement) : ~**2 200+ lignes** de constats déjà absorbés ou périmés par 6.78–6.82.
3. **PROGRESS_LOG** (~1 570 lignes / ~100 Ko) et **archives déjà figées** (~1,9 Mo dossier) : utiles en historique, pas à rouvrir pour décider.
4. **Prompts testeurs** : versions **6.37 / 6.42** en dur alors que le README testeurs dit « lire le journal ».

**Pas de suppression brutale recommandée** des archives PII sans scrub : plutôt **déplacer** vers `docs/archive/audits-2026-10-23/` + bandeau « figé · ne plus lire pour l’état code ».

---

## 1. Inventaire (taille & rôle)

Seuils : **court** &lt; 80 l · **moyen** 80–200 · **long** &gt; 200 · **très long** &gt; 400.

### 1.1 Racine (vivants)

| Fichier | Lignes | Rôle | Santé vs 6.82 |
|---------|-------:|------|---------------|
| `PROGRESS_LOG.md` | **1571** | Journal + état global | **OK** contenu ; **trop long** pour lecture courante |
| `CHARTE_ET_ARCHITECTURE.md` | 267 | Règles produit / UI / étapes | **Garder** (crypto mentionnée) |
| `README.md` | 91 | Vitrine | Table **OK** · **badges FAUX** (voir §2) |
| `PRIVACY.md` | 59 | Privacy post-6.81 | **Aligné** |
| `SECURITY.md` | 44 | Sécurité post-6.81 | **Aligné** |
| `CONTRIBUTING.md` | 12 | Contrib courte | **Garder** |

### 1.2 `docs/` vivant

| Fichier | Lignes | Rôle | Santé vs 6.82 |
|---------|-------:|------|---------------|
| `docs/README.md` | 36 | Index | **Garder** ; ajouter lien audits 10-26 + règle « audits = figés sauf index » |
| `docs/PLAN_PRODUIT_FINI.md` | 31 | État court | **Garder** ; « prochain geste » **stale** (ignore crypto 6.81–6.82 déjà faits) |
| `docs/AUDIT_STATUS.md` | 86 | Smoke Galaxy | **Garder** ; s’arrête à **6.80** (manque 6.81/6.82) |
| `docs/BENCHMARK_QUALITE_2026.md` | 80 | Barème pairs 2026 | **Garder** ; en-tête encore **6.80** |
| `docs/CI.md` | 80 | CI + Dependabot | **Garder** (6.80+) |
| `docs/DISTRIBUTION_FAMILLE.md` | 67 | Sideload famille | **Garder** ; ex. version **6.77** dans le texte |
| `docs/licenses/FONTS.md` | 9 | Licences | **Garder** |

### 1.3 `docs/audits/` (constats — beaucoup figés)

| Fichier | Lignes | Sujet | Santé vs 6.82 |
|---------|-------:|-------|---------------|
| `2026-10-23-nuit-00-SYNTHESE-PRIORITES.md` | 77 | Matrice Top15 post-6.78 | **Index utile** ; § imperfectibles **obsolète** (Room/backups « en clair », notifs toujours visibles) |
| `nuit-01` … `nuit-08` | 124–301 | DA / UX / vente / privacy / eng / a11y / carrière / BE | **Doublons** de la synthèse ; versions 6.75–6.78 ; **archiver en bloc** |
| `2026-10-23-smoke-device.md` | 105 | Smoke S25 adb | **Garder vivant** (référencé README / journal) |
| `2026-10-23-valorisation.md` | 225 | Pricing | Figé 6.77 ; claim « Room en clair » **périmé** ; redondant nuit-03 |
| `2026-10-23-evaluation-cadeau-maman.md` | 195 | Devis construction | Figé ~6.80 / ~318 tests ; **untracked** ; redondant valorisation |
| `2026-10-26-securite-chiffrement.md` | 211 | Plan chiffrement pré-6.81 | **Obsolète comme état** (décrit Room plain / pas SQLCipher) — **archive historique du plan** ; vérité = PRIVACY + journal 6.81 |

Dossier local `docs/audits/smoke/` ≈ **4,3 Mo** PNG/XML (**gitignoré**, PII) — pas des MD, mais bruit disque à traiter (voir §5 supprimer local).

### 1.4 `docs/archive/` (déjà figé — ne plus éditer)

| Entrée | Lignes (approx.) | Note |
|--------|-----------------:|------|
| `AUDIT_ALEX_MAMAN_2026-10-21.md` | 402 | Long + PII personas — **garder archive privée** |
| `FEEDBACK_TEST_FAMILLE_…` | 287 | Idem |
| `AUDIT_PREMIUM.md` | 275 | Punch list 6.16–6.28 |
| `PLAN_PRODUIT_FINI.md` | 213 | Lots A→F clos |
| `RECHERCHE_OUTILS_…` | 189 | Sources matin |
| `POLISH_PASS.md` | 77 | Journal polish agent (doublon journal) |
| `CHECKLIST_S25_…` | 64 | Device 5.4.1 |
| `campagnes-2026-10-20/*` | 11–55 | Rapports 6.26 |
| `README.md` | 16 | Index archive — **garder** |

### 1.5 `docs/testeurs/`

| Fichier | Note vs 6.82 |
|---------|----------------|
| `README_TESTEURS.md` | **OK** (« lire le journal ») |
| `CAMPAGNE_PRODUIT_FINI.md` | Matrice utile ; exemples **6.37 / 6.52** stale |
| `PROMPT_*` (×5) | Versions **6.37 / 6.42** en dur — **risque faux claim** si un agent les suit |
| `PROFILS_TESTEURS.md` | **Garder** |

### 1.6 Divers

| Chemin | Note |
|--------|------|
| `dist/verify/README.md` + `RESET_TESTS_FROM_ZERO.md` | Utiles sideload/verify — **garder** |
| `**/._*.md` (AppleDouble T7) | **0 ligne utile**, gitignorés — **supprimer du disque** (pas git) |

---

## 2. Claims faux ou trompeurs vs 6.82

Référence journal : **332/332** · `0.1.0-6.82` · crypto livrée · pas « 9/10 dans la main ».

| # | Où | Claim actuel | Vérité 6.82 | Gravité |
|--:|----|--------------|-------------|---------|
| C1 | `README.md` badges | `tests-329/329` · `version-0.1.0-6.81` | **332/332** · **6.82** (table du même fichier déjà correcte) | **P0 doc** — mensonge vitrine |
| C2 | `2026-10-26-securite-chiffrement.md` | Room plain, pas SQLCipher, backups clair, bio sans CryptoObject, « aucun code dans cette passe » | **6.81 a livré** SQLCipher + MMC1 + MRF1 + CryptoObject ; notifs masquées si bio ON | **P0 si lu comme vivant** — archiver + bandeau |
| C3 | `nuit-00` §4 imperfectibles | « Room / backups / notifs en clair » · « montant visible même si bio ON » | Room/backups chiffrés ; notifs masquées si bio ON | **P1** — amendement ou archive |
| C4 | `valorisation` / `nuit-04` / parties `nuit-03` | « Pas coffre · Room + backups en clair » comme **état** | Vrai **à la date** 6.75–6.77 ; **faux aujourd’hui** | **P1** — bandeau « figé pré-6.81 » |
| C5 | `BENCHMARK_QUALITE_2026.md` | « état code : **6.80** » | **6.82** | **P2** en-tête |
| C6 | `AUDIT_STATUS.md` | Dernière vague listée **6.80** | Manque lignes **6.81 / 6.82** (code OK, smoke doigt crypto ouvert) | **P2** |
| C7 | `PLAN_PRODUIT_FINI.md` | Prochain geste = smoke S26 only | Toujours vrai pour doigt ; omet que crypto/polish code **faits** | **P2** |
| C8 | Prompts testeurs | `doit être 0.1.0-6.42` / vérifier `6.37` | Doit être **journal = 6.82** | **P1** si campagne relancée |
| C9 | `evaluation-cadeau-maman.md` | ~318 tests · 6.80 | **332** · **6.82** | **P2** (doc figée OK si bandeau) |
| C10 | `DISTRIBUTION_FAMILLE.md` | Exemple `0.1.0-6.77` | Cosmétique si « ex. » ; préférer « lire journal » | **P3** |

**Non-claims tenus (bien) :** journal, table README, PRIVACY/SECURITY, CONTRIBUTING, refus explicite « 9/10 » / « coffre parfait » dans les entrées 6.81–6.82.

---

## 3. Redondances / doublons

| Cluster | Fichiers | Problème | Action proposée |
|---------|----------|----------|-----------------|
| **A — Audits nuit** | `nuit-00` + `nuit-01`…`08` | 8 rapports + synthèse ; Top15 déjà **FAIT** en code 6.78–6.80 | **Garder** `nuit-00` (avec bandeau) · **archiver** 01–08 |
| **B — Privacy / crypto** | `nuit-04` + `2026-10-26-securite-chiffrement` + PRIVACY/SECURITY | Trois états empilés ; le 26/10 décrit le **pré**-crypto | **Archiver** les deux audits · **vivant** = PRIVACY/SECURITY + journal 6.81 |
| **C — Prix / cadeau / vente** | `nuit-03` + `valorisation` + `evaluation-cadeau` | Trois angles pricing/devis ; chiffres d’époque | **Fusionner conceptuellement** : 1 page « valeur » en archive (ou garder les 3 **archivés** sans les citer dans docs/README) |
| **D — Smoke** | `AUDIT_STATUS` + `smoke-device` + checklist archive + campagnes 20/10 | Historique empilé OK si rôles clairs | **Garder** les deux vivants · archives inchangées |
| **E — Plan** | `PLAN` court + `archive/PLAN` | Déjà bien séparé | **Garder** les deux |
| **F — Polish / journal** | `archive/POLISH_PASS` + `PROGRESS_LOG` | Même type de récit | Archive déjà · **ne pas** fusionner dans le journal vivant |
| **G — Testeurs** | `CAMPAGNE` + `PROMPT_*` + `archive/campagnes-*` | Campagne vivante vs rapports figés | **Garder** ; dé-pinner versions prompts |
| **H — Journal** | `PROGRESS_LOG` entier | Pré-6.68 = histoire | **Fusionner/archiver** : extraire pré-6.68 → `docs/archive/PROGRESS_LOG_pre-6.68.md` (option) · garder état global + depuis ~6.68 |

---

## 4. Liste d’actions (garder / fusionner / archiver / supprimer)

Légende : **maintenant** = correction claims sans gros chantier · **plus tard** = ménage structurel.

### 4.1 GARDER (vivants — éditer seulement pour aligner 6.82)

| Fichier | Pourquoi | Fix claim min. |
|---------|----------|----------------|
| `PROGRESS_LOG.md` | Vérité | Aucun (déjà 6.82) |
| `README.md` | Vitrine | **Badges → 332/332 · 0.1.0-6.82** |
| `CHARTE_ET_ARCHITECTURE.md` | Règles | — |
| `PRIVACY.md` / `SECURITY.md` | Post-crypto | — |
| `CONTRIBUTING.md` | Gate push | — |
| `docs/README.md` | Index | Pointer smoke + dire audits datés = historique |
| `docs/PLAN_PRODUIT_FINI.md` | État court | Une ligne « crypto 6.81 + polish 6.82 faits · prochain = smoke doigt » |
| `docs/AUDIT_STATUS.md` | Smoke | Ajouter vague 6.81/6.82 (code OK · doigt ouvert) |
| `docs/BENCHMARK_QUALITE_2026.md` | Barème | Bump en-tête → 6.82 ; « Pas fait » : rescan Dependabot + smoke |
| `docs/CI.md` / `DISTRIBUTION_FAMILLE.md` / `licenses/FONTS.md` | Ops | Ex. version → « journal » |
| `docs/audits/2026-10-23-smoke-device.md` | Preuve S25 | Garder ; évent. note « post-6.80 fixes H01/F01/D01 » déjà dedans |
| `docs/testeurs/README_TESTEURS.md` + `PROFILS_*` + `PROMPT_EXPERT_VALIDATION.md` | Campagne | — |
| `dist/verify/*` | Verify famille | — |
| `docs/archive/README.md` + contenu archive actuel | Historique | Ne pas rouvrir |

### 4.2 FUSIONNER (réduire sans perdre l’info)

| Action | Détail |
|--------|--------|
| **F1** Prompts testeurs | Remplacer toutes les versions pinées par « `versionName` = `PROGRESS_LOG` état global » (1 phrase commune) — *équivalent fusion anti-doublon* |
| **F2** Cluster prix | Option A : un seul `docs/archive/VALEUR_ET_DEVIS_2026-10-23.md` (extraits verdicts valorisation + cadeau + nuit-03) puis archiver les 3. Option B : archiver les 3 **sans** fusion (moins de travail, un peu plus de bruit) |
| **F3** Journal | Option : scinder pré-6.68 → archive ; `PROGRESS_LOG` ne garde que état global + entrées ≥ 6.68 (~réduit ~70 % lignes) |

### 4.3 ARCHIVER (bandeau « figé · ne plus lire pour l’état code/crypto »)

Cible suggérée : `docs/archive/audits-2026-10-23/` (et `…-2026-10-26/` pour le plan crypto).

| Fichier | Motif |
|---------|-------|
| `nuit-01` … `nuit-08` | Absorés par `nuit-00` + code 6.78–6.80 |
| `nuit-00` | **Soit** rester index vivant avec bandeau amendé **soit** archiver après copie d’une checklist doigt courte dans `AUDIT_STATUS` / PLAN |
| `2026-10-23-valorisation.md` | Pricing figé pré-crypto |
| `2026-10-23-evaluation-cadeau-maman.md` | Devis figé ; **committer puis archiver** (actuellement `??`) |
| `2026-10-26-securite-chiffrement.md` | Plan pré-6.81 — **historique précieux**, dangereux si lu comme audit courant |
| `nuit-04` | Privacy pré-6.81 |

Déjà archivé (rien à faire sauf ne pas « ressusciter ») : `AUDIT_ALEX_*`, `AUDIT_PREMIUM`, `FEEDBACK_*`, `PLAN` long, `POLISH_PASS`, `RECHERCHE_*`, `CHECKLIST_S25_*`, `campagnes-2026-10-20/`.

### 4.4 SUPPRIMER

| Cible | Type | Commentaire |
|-------|------|-------------|
| `**/._*` (AppleDouble) | Disque | Déjà gitignorés ; `find … -name '._*' -delete` sur T7 — **pas** un commit |
| `docs/audits/smoke/*.png|*.xml` | Disque local PII | Déjà gitignorés ; purger machine si plus besoin (synthèse MD suffit) |
| **Aucun MD tracked** en suppression sèche aujourd’hui | Git | Préférer **archiver** (PII + preuves métier) plutôt que `git rm` sans scrub |

---

## 5. Priorité d’exécution (doc only — hors périmètre de *cette* passe)

Ordre recommandé **quand** l’humaine demandera le ménage (pas fait ici) :

1. **P0** — Badges README (C1).
2. **P0** — Bandeau + déplacement `2026-10-26-securite-chiffrement.md` → archive (C2).
3. **P1** — Dé-pinner prompts testeurs (C8 / F1).
4. **P1** — Archiver bloc nuit-01…08 + valorisation + cadeau + nuit-04 ; amender ou archiver `nuit-00` (C3–C4).
5. **P2** — AUDIT_STATUS + BENCHMARK + PLAN court (C5–C7).
6. **P2/P3** — Option scission `PROGRESS_LOG` ; purge AppleDouble + smoke PNG locaux.
7. Mettre à jour `docs/README.md` + `docs/archive/README.md` pour refléter le nouvel index.

---

## 6. Compteurs

| Métrique | Valeur |
|----------|--------|
| MD utiles (hors `._*`) | **49** |
| Audits datés dans `docs/audits/` | **13** (+ ce fichier = **14**) |
| Dont clairement **périmés comme état vivant** post-6.81 | ≥ **4** (chiffrement 26/10, nuit-04, valorisation « en clair », nuit-00 §4) |
| Claims **faux** sur README vivant | **2 badges** |
| Archives déjà en `docs/archive/` | **14** fichiers MD (+ README campagnes) |
| Lignes journal | **1571** (candidat n°1 « trop long ») |
| Bruit disque non-MD lié docs | smoke ≈ **4,3 Mo** · AppleDouble nombreux |

---

## 7. Hors scope (rappel)

- Pas de modification app / Gradle / tests dans cette passe.
- Pas de commit automatique.
- Pas de claim « docs parfaitement alignées » tant que C1–C2 ne sont pas traités.
- Smoke doigt 6.81/6.82 et S26 restent **ouverts** côté produit (journal) — ce n’est pas un défaut de ce rapport MD.

---

*Fin audit final-04 docs MD · 2026-10-26 · référence produit **6.82** / **332/332**.*
