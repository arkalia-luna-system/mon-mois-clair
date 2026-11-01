> **ARCHIVÉ** 2026-11-01 — historique figé. Vivant = `docs/audits/2026-11-01-premium-*` + journal.

# final-11 — UI quiet · MD · CI · ship develop=main · install

| | |
|--|--|
| **Date** | 2026-10-26 · **final-11** |
| **Version** | `0.1.0-6.89` · `versionCode` 94 |
| **Tests** | **367 exécutés** / 368 (1 skipped), 87 XML, 0 failure |
| **Gradle** | **8.14.4** (AGP 8.7.3) |

---

## 1. UI (6.87)

| Fix | Note |
|-----|------|
| « Autres infos » → **Détails** | P1-S2 |
| › sticky `titleMedium` InkMuted | P1-S3 |
| Aide + charte | alignées |

---

## 2. CI / Dependabot

| Item | Action |
|------|--------|
| NewApi URLEncoder | `encode(…, "UTF-8")` — 6.88 |
| #3 #4 commons-compress | force **1.26.0** |
| #20 #25 bcpkix | force **1.86** (+ bcprov 1.86) |
| #54 kotlin-gradle-plugin | **reporté** — besoin AGP 9 + Hilt ≥2.59 |
| PR #11 robolectric | **4.17** appliqué |
| PR #12 core-ktx 1.19.1 | close — exige AGP 9.1 |
| PR #13 compose 2.4.21 seul | close — casse sans stack complète |
| PR #14/#15 hilt 1.4.0 | close — exige AGP 9.1 |
| Gradle avis | **8.11.1 → 8.14.4** |

---

## 3. Git / install

| Champ | Valeur |
|-------|--------|
| SHA develop | `69bafef` |
| SHA main | `69bafef` (ff-only) |
| CI develop | **success** · [run 40334976512](https://github.com/arkalia-luna-system/mon-mois-clair/actions/runs/40334976512) |
| Install device | **OK** SM-S938B · `0.1.0-6.89` / vc 94 (sans wipe) |

---

## 4. Reste Athalia

S26 D1–D7 · code papier · restore `.mmc` · QuietLink mail — pas GO cadeau sans.

*final-11 · 6.87–6.89*
