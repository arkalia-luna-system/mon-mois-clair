# Audit produit — cadeau famille → objet premium (~6.91)

| | |
|--|--|
| **Date** | 2026-11-01 · premium-06 |
| **Projet** | `/Volumes/T7/Mon Mois Clair` — **VÉRIFIÉ** (`pwd` = workspace transmis) |
| **Rôle** | Product owner — écart *livrable famille* vs *vraiment premium* |
| **État code** | `0.1.0-6.91` · `versionCode` 96 · journal Samsung Direct Boot / notif |
| **Portée** | **AUDIT ONLY** · un domaine (produit) · zéro fix app |
| **Précédent** | [`2026-10-26-final-06-livrable-famille.md`](2026-10-26-final-06-livrable-famille.md) · [`2026-11-01-samsung-defaillances-notif-icon.md`](2026-11-01-samsung-defaillances-notif-icon.md) · [`2026-11-01-premium-01-da-affichage.md`](2026-11-01-premium-01-da-affichage.md) |
| **Sources** | `README` · `PROGRESS_LOG` · `NOTICE_FAMILLE_2MIN` · `DISTRIBUTION_FAMILLE` · `CHARTE_ET_ARCHITECTURE` · audits récents · `dist/` |

---

## Verdict exécutif

**Note globale « vraiment premium dans la main » : 6 / 10.**  
Craft carnet (code + crypto + polish + ship Ultra) ~ **8,5 / 10** ; preuve doigt + packaging confiance ~ **5 / 10**.

| Décision | Verdict |
|----------|---------|
| **GO livrable famille** (matrice final-06) | **NO-GO** (ou GO conditionnel seulement si Athalia à côté 48 h) |
| **GO premium** (objet fini, sans Arkalia, digne d’un carnet payant) | **NO-GO** |

Raison courte : le journal et les plans **déclarent encore ouverts** S26 D1–D7, restore `.mmc` + code papier, QuietLink mail, rescan Device Care — et refusent explicitement le claim « 9/10 dans la main ». Un cadeau livré maintenant = risque émotionnel > gain prestige.

> **Pas de claim 9/10.** Pas de claim « premium vendable ». 374 tests verts + Ultra installé **ne ferment pas** le produit dans la main.

---

## 1. Définitions croisées

### Livrable famille (réf. final-06)

Install release · code de copie noté papier · restore prouvé · smoke **S26** · copy 60+ · QuietLink Arkalia · notice 2 min.  
**≠** Play Store · ≠ claim premium.

### Vraiment premium (barre PO de cet audit)

Tout le livrable famille **plus** : preuves smoke à jour sur version courante · support revalidé · polish sans dette visuelle ouverte · confiance (pas d’historique crash / Device Care « défaillances » non traité) · packaging exact (taille, notice, canal Drive) · zéro données démo · sensation « ça marche seule ».

---

## 2. Constats par axe

### A. Preuve smoke

| Constat | Statut | Nature | Preuve |
|---------|--------|--------|--------|
| Version Gradle `0.1.0-6.91` / vc **96** | **VÉRIFIÉ** | inspection | `app/build.gradle.kts` · badge README |
| README / journal alignés 6.91 · 374 tests · S26 / restore ouverts | **DÉCLARÉ** | inspection | `README.md` · `PROGRESS_LOG.md` état global |
| Smoke Ultra 6.91 : install OK, 0 FATAL post-install | **DÉCLARÉ** | inspection (historique) | `docs/audits/2026-11-01-samsung-defaillances-notif-icon.md` §D |
| Smoke **S26** D1–D7 | **NON VÉRIFIÉ** (ouvert) | inspection docs | journal « prochaine sous-étape Athalia » · CHARTE usage principal S26 · NOTICE L23–26 |
| Restore + code papier bout-en-bout | **NON VÉRIFIÉ** (ouvert) | inspection docs | final-06 · NOTICE « À faire encore » · PROGRESS |
| Captures smoke PNG versionnées | **VÉRIFIÉ** absentes (gitignorées) | inspection | `docs/audits/smoke/` |
| Tests unitaires réexécutés cet appel | **NON VÉRIFIÉ** | — | hors périmètre PO (claim journal 374/375) |
| Charte : cible n°1 = **S26** | **VÉRIFIÉ** | inspection | `CHARTE_ET_ARCHITECTURE.md` |

**Écart famille→premium :** Ultra partiel + sim 360×780 ≠ preuve usage principal. Premium exige S26 vert **et** restore prouvé sur **cette** build.

### B. Support

| Constat | Statut | Nature | Preuve |
|---------|--------|--------|--------|
| QuietLink Menu → Arkalia Système (contrat produit) | **DÉCLARÉ** | inspection | `PRIVACY.md` Contact · NOTICE · DISTRIBUTION |
| QuietLink / mailto revalidés sur **6.91** | **NON VÉRIFIÉ** | — | journal liste encore « QuietLink mail » à faire Athalia |
| Notice orale 2 min existe | **VÉRIFIÉ** | inspection | `docs/NOTICE_FAMILLE_2MIN.md` |
| Notice pinne encore **`0.1.0-6.83`** | **VÉRIFIÉ** | inspection | NOTICE L3 — désaccord avec version **6.91** |

**Écart :** support *codé* ≠ support *prouvé* sur le téléphone cadeau. Premium = maman sait où cliquer **sans** Arkalia + mail testé le jour J.

### C. Polish

| Constat | Statut | Nature | Preuve |
|---------|--------|--------|--------|
| Lots quieting / copy 6.82–6.87 **déclarés** faits côté code | **DÉCLARÉ** | inspection | journal 6.82–6.87 · premium-01 DA craft ~8,5 |
| P1 H01/F01 fix code 6.80 — reval doigt ouverte | **DÉCLARÉ** | inspection | `2026-10-23-smoke-device.md` H01/F01 |
| Notif `largeIcon` 6.91 | **DÉCLARÉ** | inspection | audit samsung 2026-11-01 §B — smoke bannière doigt **non** relancé ici |
| Noms démo `loyer3` / etc. encore documentés | **DÉCLARÉ** | inspection | smoke-device — risque device cadeau |

**Écart :** polish craft élevé ; premium exige revalidation doigt post-6.80/6.91 + device cadeau **propre**.

### D. Confiance

| Constat | Statut | Nature | Preuve |
|---------|--------|--------|--------|
| Crashes 6.90 (IV) + 6.91 (Direct Boot ENOENT) corrigés **déclarés** | **DÉCLARÉ** | inspection | journal 6.90–6.91 · audit samsung |
| Device Care « défaillances » / batterie — rescan Athalia **ouvert** | **DÉCLARÉ** | inspection | audit samsung §A « à faire Athalia » |
| Crypto MMC1 / SQLCipher alignés PRIVACY | **DÉCLARÉ** | inspection | `2026-10-26-final-02-securite.md` · PRIVACY |
| Honnêteté « pas coffre parfait » | **VÉRIFIÉ** | inspection | `PRIVACY.md` |
| Dependabot #54 pin 2.0.21 (CVE classpath) | **DÉCLARÉ** | inspection | audit 6.91 §C · `docs/CI.md` |
| Badge README + refus « 9/10 dans la main » | **VÉRIFIÉ** | inspection | `README.md` · PROGRESS état global |

**Écart :** confiance technique en hausse, mais historique crash + Device Care non rescanné = pas encore « premium serein » pour un proche 60+.

### E. Packaging

| Constat | Statut | Nature | Preuve |
|---------|--------|--------|--------|
| APK release présent `dist/MonMoisClair-release.apk` | **VÉRIFIÉ** | commande | `ls -lh` → **~42 Mo** |
| Doc distribution « ~14 Mo » | **VÉRIFIÉ** | inspection | `DISTRIBUTION_FAMILLE.md` L10, L42 |
| **Désaccord taille** doc vs fichier | **VÉRIFIÉ** | inspection + commande | **14 Mo** déclaré vs **~42 Mo** réel |
| Contenu APK multi-ABI (arm64 + armeabi-v7a) + ML Kit OCR + SQLCipher | **VÉRIFIÉ** | inspection zip | `lib/*/libmlkit_*` · `libsqlcipher.so` — explique la masse |
| `badging` version APK | **VÉRIFIÉ** | build-tools | `versionName='0.1.0-6.91'` · `versionCode='96'` |
| Canal sideload Drive (pas Play) | **VÉRIFIÉ** | inspection | DISTRIBUTION · README « Hors scope Play Store » |
| Play / MAJ auto | Hors scope volontaire | inspection | charte / README |

**Écart packaging fort :** une notice « ~14 Mo » face à un APK ~42 Mo casse la confiance (Mail/WA, attentes). Premium = chiffres vrais + rituel Drive testé sans USB.

---

## 3. Top améliorations à fort impact

### P0 — bloquants cadeau / premium

| Id | Action | Impact |
|----|--------|--------|
| **P0-S26** | Smoke **S26** D1–D7 (sticky · Menu · FAIT · jour 31 · widget · QuietLink) | Gate charte usage principal — **ouvert** |
| **P0-RESTORE** | Restore E2E : code **papier** · `.mmc` sans code · bon code · mauvais code · base intacte | Sérénité post-casse téléphone |
| **P0-QL** | QuietLink → Gmail Compose (version + modèle) sur build **6.91** | Support sans Arkalia à côté |
| **P0-DEMO** | Device cadeau sans noms démo (`loyer3`…) | Première impression |

### P1 — packaging & confiance

| Id | Action | Impact |
|----|--------|--------|
| **P1-DC** | Rescan Device Care après 6.91 + confirmer 0 FATAL récents | Confiance Samsung |
| **P1-NOTICE** | Aligner NOTICE **6.83 → 6.91** | Brief oral = version réelle |
| **P1-DIST** | Aligner DISTRIBUTION **~14 Mo → ~42 Mo** (ou APK ABI-split / slim) | Packaging honnête |
| **P1-DRIVE** | Une install **Drive sans USB** sur téléphone famille | Preuve canal réel |
| **P1-REVAL** | Reval doigt H01/F01 + bannière notif `largeIcon` | Polish « fini » |

### P2 — hors jour J

| Id | Note |
|----|------|
| **P2-1** | connected androidTest (bloqué UTP — journal) |
| **P2-2** | God-files Home / Dependabot AGP9 — dette eng |
| **P2-3** | Play tests internes — option packaging, hors produit actuel |

---

## 4. Matrice Go / No-Go (PO)

| | Livrable famille | Vraiment premium |
|--|------------------|------------------|
| **GO** | A–F final-06 verts **dont S26 + restore + QuietLink** | Idem + Device Care clair + packaging exact + 0 démo + notice à jour |
| **GO conditionnel** | S26 min D1–D7 **ou** proxy+sticky S26 ; restore fait **par Arkalia** jour J ; code papier ; Arkalia 48 h | Rarement pertinent : « premium conditionnel » = oxymore |
| **Aujourd’hui (6.91)** | **NO-GO** / conditionnel seulement avec Athalia | **NO-GO** |

Phrase PO :

> Craft **prêt à devenir** cadeau et carnet premium. **Preuves doigt et packaging** ne le sont pas. Ne pas confondre 374 tests verts + Ultra installé avec « objet premium dans la main de maman ».

---

## 5. Notes /10 (grille PO)

| Axe | Note | Commentaire |
|-----|------|-------------|
| Preuve smoke | **5** | Ultra 6.91 déclaré OK ; **S26 / restore ouverts** |
| Support | **6** | Canal codé + notice ; reval 6.91 + notice version **périmée (6.83)** |
| Polish | **8** | Code très avancé (~craft 8,5 DA) ; revals doigt ouvertes |
| Confiance | **6,5** | Crypto + fixes crash ; Device Care / CVE pin ouverts |
| Packaging | **5** | Sideload doc OK ; **taille APK 42 Mo vs 14 Mo** + pin notice faux |
| **Global premium** | **6 / 10** | Au-dessus d’un WIP ; sous un carnet « payant calme » |

*(Craft code seul, hors main : ~8,5 — ne pas l’afficher comme note produit.)*

---

## 6. Checklist jour J (rappel — non cochée)

Reprise condensée de final-06 + delta 6.91 :

1. ☐ `assembleRelease` + version Réglages = **6.91**
2. ☐ Drive sans USB sur device famille
3. ☐ Smoke **S26** D1–D7
4. ☐ Code de copie noté **papier** + restore E2E
5. ☐ QuietLink mail 6.91
6. ☐ Device sans démo
7. ☐ Rescan Device Care
8. ☐ NOTICE + DISTRIBUTION alignés (version + Mo)

Tant que 3–5 restent ouverts → **NO-GO** cadeau sans Athalia **et** **NO-GO** premium.

---

## Domaine couvert / limites

- **Domaine :** produit (écart famille → premium) uniquement.
- **Non audité :** sécu approfondie, hygiène code, CI détail, UI pixel (voir premium-01 / final-02…).
- **Limites :** pas de Gradle `--rerun-tasks` dans cet appel ; pas de smoke S26 / restore / QuietLink live. Beaucoup de preuves **DÉCLARÉES** (docs / audits antérieurs). Taille APK et badging **VÉRIFIÉS** sur `dist/`.
- **aucune modification faite** au code app (ce rapport MD seul).

*Audit premium-06 produit · `0.1.0-6.91` · AUDIT ONLY · NO-GO premium 6/10.*
