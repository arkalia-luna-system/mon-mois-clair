# final-11 — UI quiet · MD · CI · ship develop=main · install

| | |
|--|--|
| **Date** | 2026-10-26 · **final-11** |
| **Version** | `0.1.0-6.88` · `versionCode` 93 (UI 6.87 + hotfix CI/deps) |
| **Tests** | **367 exécutés** / 368 déclarés (1 skipped), 87 XML, 0 failure |
| **Mode** | Exécution demandée (UI + MD + push + CI + main + install) |

---

## 1. UI fixes (petits P1 — pas redesign) — 6.87

| Fix | Fichier | Note |
|-----|---------|------|
| « Autres infos » → **Détails** | `HomeStickyHeader` | P1-S2 final-01 |
| › sticky Courses/épargne → `titleMedium` **InkMuted** | `HomeStickyChrome` | P1-S3 — ne rivalise pas avec Reste |
| Aide + charte alignées | `HowItWorksCopy` · `CHARTE` · test | Libellé Détails |

**Déjà faits (6.83–6.85, non rouverts) :** quieting sticky mois · cartes montant+FAIT · socle `shadowElevation=0` · sheets 28 + poignée · HubRow › · shortSheet · widget coins 16.

---

## 1bis. Hotfix CI / Dependabot — 6.88

| Fix | Détail |
|-----|--------|
| **NewApi** | `URLEncoder.encode(…, "UTF-8")` — plus de Charset API 33 (`CreditorMailComposer`) · lint NewApi **0** |
| **Force** | `commons-compress:1.26.0` · `bcpkix-jdk18on:1.86` (settings + root build, pattern 6.80) |
| **Kotlin plugin** | CVE ≥2.4.20-Beta1 **non** bumpé (AGP 8.7.3 + KSP 2.0.21) — documenté `docs/CI.md` |

**aapt :** `versionName='0.1.0-6.88'` · `versionCode='93'` (APK release après assemble).

---

## 2. MD actions

| Action | Détail |
|--------|--------|
| **Archivé** | `nuit-00` → `docs/archive/audits-2026-10-23/` |
| **Purge** | AppleDouble `._*` (disque) |
| **Index** | `docs/README` · `docs/archive/README` |
| **Campagne** | Ex. version pinée → « journal » |
| **Gardés** | README · CHARTE · PRIVACY · SECURITY · PROGRESS_LOG · NOTICE · final-00…11 · smoke |

Pas de claims « 9/10 dans la main ».

---

## 3. CI / git

| Champ | Valeur |
|-------|--------|
| Branche travail | `develop` |
| SHA develop | _(après push 6.88)_ |
| SHA main | _(après ff-only quand CI vert)_ |
| CI develop | _(après `gh run`)_ |
| Install device | _(après adb)_ |

---

## 4. Reste Athalia (smoke)

- S26 D1–D7 · code papier · restore `.mmc` · QuietLink mail  
- Pas GO cadeau sans ces cochages

---

*final-11 · preuve ship 6.87 + hotfix 6.88*
