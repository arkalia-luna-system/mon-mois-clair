# Synthèse tâches finales — Mon Mois Clair

| | |
|--|--|
| **Date** | 2026-10-26 · **final-00** |
| **Rôle** | Fusion dédupliquée des audits **final-01 → final-06** |
| **Produit** | `0.1.0-6.83` · `versionCode` 88 · **332/332** (exécution final-07) |
| **Mode** | Exécution backlog — voir [`final-07-verification-execution.md`](2026-10-26-final-07-verification-execution.md) |

Sources : [`final-01-affichage`](2026-10-26-final-01-affichage.md) · [`final-02-securite`](2026-10-26-final-02-securite.md) · [`final-03-code-hygiene`](2026-10-26-final-03-code-hygiene.md) · [`final-04-docs-md`](2026-10-26-final-04-docs-md.md) · [`final-05-fichiers-captures`](2026-10-26-final-05-fichiers-captures.md) · [`final-06-livrable-famille`](2026-10-26-final-06-livrable-famille.md).

---

## 1. Verdict global LIVRABLE

**GO conditionnel (code)** — backlog V0 docs/APK + V1–V4 exécutés ; Ultra `0.1.0-6.83` installé.

**NO-GO cadeau sans Athalia** tant que smoke **S26** + **restore code papier** ne sont pas cochés au doigt.

---

## 2. Liste UNIQUE des tâches (par vagues)

### Vague 0 — bloquant cadeau

| # | Tâche | P | E | Src | État |
|---|-------|:-:|:-:|-----|------|
| V0-1 | **Smoke S26** (D1–D7 min.) | P0 | M | 01, 02, 06 | **À faire Athalia** |
| V0-2 | **Restore + code de copie** (papier · `.mmc` · 2ᵉ device) | P0 | M | 02, 06 | **À faire Athalia** |
| V0-3 | **Badges README** → tests/version journal | P0 | S | 03, 04, 06 | **Fait** (6.83 / 332) |
| V0-4 | **Brief code / notice 2 min** | P0 | S | 02, 06 | **Fait** (`NOTICE_FAMILLE_2MIN` + Aide) |
| V0-5 | **APK release** + aapt = journal · install Ultra | P0 | S | 02, 06 | **Fait** (S26 install cadeau : Athalia) |

### Vague 1 — clean local

| # | Tâche | P | E | Src | État |
|---|-------|:-:|:-:|-----|------|
| V1-1 | Purger PNG smoke | P1 | S | 04, 05 | **Fait** |
| V1-2 | APK dist rebuildé (ancien purgé puis `assembleRelease`) | P1 | S | 05 | **Fait** |
| V1-3 | Purger AppleDouble `._*` | P1 | S | 03, 04, 05 | **Fait** |

### Vague 2 — docs

| # | Tâche | P | E | Src | État |
|---|-------|:-:|:-:|-----|------|
| V2-1 | Archiver nuit-01…08 + valorisation + évaluation | P1 | M | 04 | **Fait** |
| V2-2 | Archiver `securite-chiffrement` pré-6.81 + bandeau | P0 | S | 02, 04 | **Fait** |
| V2-3 | Dé-pinner prompts · bump AUDIT/BENCHMARK/PLAN | P1 | S | 04 | **Fait** |

### Vague 3 — polish UI

| # | Tâche | P | E | Src | État |
|---|-------|:-:|:-:|-----|------|
| V3-1 | Quieting sticky | P0 | M | 01 | **Fait** |
| V3-2 | Cartes premier regard | P0 | M | 01 | **Fait** |
| V3-3 | Socle barre sans shadow | P0 | S | 01 | **Fait** |
| V3-4 | Sheets 28 + poignée · HubRow · shortSheet | P1 | M | 01 | **Fait** |
| V3-5 | Fiche air · widget micro sans `@font/` | P1 | M | 01 | **Fait** (smoke nuit widget : Athalia) |

### Vague 4 — hygiène

| # | Tâche | P | E | Src | État |
|---|-------|:-:|:-:|-----|------|
| V4-1 | 15 imports morts | P2 | S | 03 | **Fait** |
| V4-2 | Param `todoAmounts` retiré | P2 | S | 03 | **Fait** |
| V4-3 | Filets Upcoming / ShareBackup | P1 | M | 03 | **Reporté** (après cadeau) |
| V4-4 | Découpe god-files | P2 | L | 01, 03, 06 | **Reporté** (après cadeau) |

### Déjà fait (ne pas rouvrir)

| Item | Preuve | Src |
|------|--------|-----|
| Crypto SQLCipher + MMC1 + MRF1 + CryptoObject | 6.81–6.82 | 02 |
| Polish copy / Annuler QuietLink / widget serif | 6.82 | 01, 06 |
| Palette / anti-purple / FAIT forêt | Charte | 01 |
| `.gitignore` APK / PNG / `._*` | OK | 05 |
| 332/332 unit | Journal 6.83 | 03, 06 |

---

## 4. Checklist « demain matin » (Athalia)

```
☐ 1. Lire PROGRESS_LOG + Réglages = 0.1.0-6.83
☐ 2. aapt dump badging = 6.83 (pas debug)
☐ 3. Install S26 (ou téléphone maman) via Drive
☐ 4. Smoke S26 D1–D7
☐ 5. Code de copie → papier
☐ 6. Partager .mmc sans code ; restore 2ᵉ device
☐ 7. Mauvais code → message clair
☐ 8. QuietLink Menu → mail Arkalia
☐ 9. Device cadeau sans noms démo
☐ 10. Notice orale 2 min
☐ 11. Décider GO / GO conditionnel / NO-GO
```

---

*Synthèse final-00 · mise à jour exécution 6.83 · voir final-07.*
