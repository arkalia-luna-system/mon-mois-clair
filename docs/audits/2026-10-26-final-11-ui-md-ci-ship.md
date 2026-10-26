# final-11 — UI quiet · MD · CI · ship develop=main · install

| | |
|--|--|
| **Date** | 2026-10-26 · **final-11** |
| **Version** | `0.1.0-6.87` · `versionCode` 92 |
| **Tests** | **367 exécutés** / 368 déclarés (1 skipped), 87 XML, 0 failure |
| **Mode** | Exécution demandée (UI + MD + push + CI + main + install) |

---

## 1. UI fixes (petits P1 — pas redesign)

| Fix | Fichier | Note |
|-----|---------|------|
| « Autres infos » → **Détails** | `HomeStickyHeader` | P1-S2 final-01 |
| › sticky Courses/épargne → `titleMedium` **InkMuted** | `HomeStickyChrome` | P1-S3 — ne rivalise pas avec Reste |
| Aide + charte alignées | `HowItWorksCopy` · `CHARTE` · test | Libellé Détails |

**Déjà faits (6.83–6.85, non rouverts) :** quieting sticky mois · cartes montant+FAIT · socle `shadowElevation=0` · sheets 28 + poignée · HubRow › · shortSheet · widget coins 16.

**aapt :** `versionName='0.1.0-6.87'` · `versionCode='92'` (APK release).

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

## 3. CI / git (à compléter après push)

| Champ | Valeur |
|-------|--------|
| Branche travail | `develop` |
| SHA develop | _(après push)_ |
| SHA main | _(après ff-only)_ |
| CI develop | _(après `gh run`)_ |
| Install device | _(après adb)_ |

---

## 4. Reste Athalia (smoke)

- S26 D1–D7 · code papier · restore `.mmc` · QuietLink mail  
- Pas GO cadeau sans ces cochages

---

*final-11 · preuve ship 6.87*
