# Synthèse audits premium — Mon Mois Clair (6.91)

| | |
|--|--|
| **Date** | 2026-11-01 · premium-00 |
| **Baseline** | `develop` · `0.1.0-6.91` · `versionCode` 96 |
| **Sources** | premium-01 → 06 (même jour) |
| **Mode** | Synthèse cross-audits · pas de smoke rejoué ici |

---

## Verdict global « vraiment premium »

| | Note | GO / NO-GO |
|--|-----:|:-----------|
| **Craft code** (moyenne indicative 01–05) | **~7,7 / 10** | — |
| **Vraiment premium dans la main** (gate produit) | **6 / 10** | **NO-GO** |
| **Livrable famille** (matrice final-06 + 6.91) | — | **NO-GO** (conditionnel seulement si Athalia à côté 48 h) |

| Audit | Note | Lecture |
|-------|-----:|---------|
| 01 DA affichage | 8,5 | Craft carnet tenu ; respiration + doigt ouverts |
| 02 Sécurité | 7,5 | Socle crypto aligné ; surfaces hors process + ops famille |
| 03 Architecture | 7 | Couches OK ; Scaffold 82 params + twin VM |
| 04 UX parcours | 7,4 | FAIT fort ; sauvegarde / jargon / profondeur |
| 05 a11y · One UI | 8,0 | Contrats code OK ; S26 / TalkBack / Device Care ouverts |
| 06 Produit | **6** | Preuves doigt + packaging = frein |

> **Pas de claim 9/10.** Pas de claim « premium vendable ». 374 tests + Ultra installé **ne ferment pas** l’objet dans la main.

**Phrase unique :** craft prêt à devenir cadeau ; preuves smoke / restore / packaging ne le sont pas → **NO-GO premium**.

---

## Top faiblesses dédupliquées (cross-audits)

### P0 — bloquants cadeau / « premium dans la main »

| Id | Faiblesse | Audits |
|----|-----------|--------|
| **P0-SMOKE** | Smoke **S26** D1–D7 (sticky · Menu · FAIT · jour 31 · widget · QuietLink · fontScale) **non fait** | 01 · 04 · 05 · 06 |
| **P0-RESTORE** | Restore E2E code **papier** + `.mmc` (bon / mauvais code) **ouvert** | 02 · 04 · 06 |
| **P0-QL** | QuietLink mail Arkalia **non revalidé** sur 6.91 | 06 |
| **P0-OPS** | Code de copie + `.mmc` **même message** = fuite totale (humain) | 02 · 04 · notice |
| **P0-DEMO** | Device cadeau avec noms démo possibles | 06 |
| **P0-UI-AIR** | Sticky / cartes / socle encore **trop denses** (1ʳᵉ facture mangée compact) | 01 · 04 |
| **P0-COPY-SAVE** | Sauvegarde **trop profonde** + copy restore encore jargon (« chiffrée », ZIP/JSON) | 04 |
| **P0-DC** | Rescan **Device Care** post-6.91 non confirmé | 05 · 06 |

### P1 — gain premium net (après smoke)

| Id | Faiblesse | Audits |
|----|-----------|--------|
| **P1-PACK** | NOTICE pin **6.83** · DISTRIBUTION **~14 Mo** vs APK **~42 Mo** | 06 *(corrigé docs dans vague 0)* |
| **P1-DRIVE** | Install Drive **sans USB** non prouvée jour J | 06 |
| **P1-PDF** | PDF résumé / cache `view/` = clair éphémère — UX éducation | 02 |
| **P1-MIG** | Migration SQLCipher Failed → Room claire (résidu) | 02 |
| **P1-UX-PAY** | Pont Copier → banque → FAIT faible ; tip prélèvement invisible | 04 |
| **P1-AIDE** | Aide dense / 3 taps ; section Sauvegarde trop technique | 04 |
| **P1-WSP** | Widget titre/ligne 11/13 sp ; QuietLink étroits vs FAIT | 05 · 01 |
| **P1-VM** | Zéro test ViewModel ; lectures DAO dans VM | 03 |

### P2 — polish / dette durable

| Id | Faiblesse | Audits |
|----|-----------|--------|
| **P2-SCAF** | `EntryFormScaffold` **82** params · miroir Add/Edit (~21 méthodes) | 03 |
| **P2-SEC** | Entropie code 8 chiffres ; libellé notif si bio ON ; FAIT notif vs verrou | 02 |
| **P2-DA** | Micro-dérives widget crème / cyan nuit / pastilles chroma | 01 |
| **P2-DOC** | CHARTE « icône calendrier » · BENCHMARK « scission Pas fait » | 01 · 03 |
| **P2-OPS** | AppleDouble `._*` T7 · detekt absent | 03 |

---

## Plan d’améliorations — vagues ordonnées

### Vague 0 — docs packaging (ce commit)

- NOTICE **6.83 → 6.91**
- DISTRIBUTION **~14 Mo → ~42 Mo** (+ exemples version)
- Synthèse + audits 01–06 versionnés

### Vague 1 — cadeau / smoke d’abord (bloquant GO)

1. `assembleRelease` · version Réglages = **6.91**
2. Smoke **S26** D1–D7 (+ fontScale ≥ 1,3 si possible)
3. Restore E2E + code **papier** (bon / mauvais)
4. QuietLink → Gmail Compose
5. Device cadeau **sans** démo
6. Rescan Device Care
7. Brief oral notice 2 min (jamais code + `.mmc` ensemble)
8. Une install **Drive** sans USB

**Stop :** pas d’autre chantier UI/archi tant que vague 1 ouverte, sauf bugs bloquants smoke.

### Vague 2 — UI (respiration Accueil)

1. Air sticky (P0-S1) — moins de pile sous le Reste
2. Cartes allégées (jour · nom · montant · FAIT ; meta / Copier 2ᵉ rang)
3. Sandwich crème / warm socle — micro-pass densités compact
4. Reval H01/F01 + bannière notif `largeIcon` au doigt

### Vague 3 — UX copy / parcours

1. Copy restore sans jargon crypto
2. Raccourci « Votre code / copie » (sans Restore dans Menu Accueil)
3. Snackbar post-Copier : « Collez à la banque, puis FAIT »
4. Tip muted prélèvement ; Aide : Sauvegarde + FAIT en tête, corps court

### Vague 4 — sécurité confiance

1. Éducation PDF clair ≠ `.mmc`
2. Durcir / surveiller chemin migration Failed → claire
3. Grace cleartext : butoir une fois famille ≥ 6.81
4. Optionnel : masquer libellé notif si bio ON

### Vague 5 — architecture (après GO famille)

1. `EntryFormUiModel` (finir les 82 params)
2. `EntryFormDraftController` (casser le miroir Add/Edit)
3. Use cases lecture + **tests VM** critiques
4. **Pas** de multi-module avant domain JVM-clean

---

## Ce qu’il NE FAUT PAS faire

| Interdit | Pourquoi |
|----------|----------|
| **Refacto massif** (modules, rewrite fiche, god-file chase) **avant** smoke S26 / restore | Retarde le GO cadeau ; risque régression sans preuve doigt |
| **Play Store** / tests internes comme « fix » immédiat | Hors produit actuel ; n’ouvre pas le GO famille |
| Claim **9/10** / « premium vendable » / badge marketing | Audits 01–06 l’interdisent tant que vague 1 ouverte |
| Remettre **Aide / Restore** dans le Menu Accueil | Contre-charte ; charge ledger |
| Rallumer purple / logo sticky / ombres cartes | Fermé en code 6.83–6.87 |
| Slim ABI « pour le chiffre » sans plan test multi-device | Cosmétique packaging ≠ preuve smoke |

---

## Checklist Athalia (≤ 12)

1. ☐ Smoke **S26** D1–D7 sur build **6.91**
2. ☐ Restore `.mmc` + code **papier** (bon + mauvais)
3. ☐ QuietLink mail → Compose OK
4. ☐ Device cadeau **sans** noms démo
5. ☐ Rescan Device Care (0 FATAL récents)
6. ☐ Install **Drive** sans USB
7. ☐ Brief oral notice 2 min (code ≠ même fil que `.mmc`)
8. ☐ NOTICE / DISTRIBUTION = version + taille **vraies** (docs)
9. ☐ Micro-pass UI sticky / cartes compact (après 1–7)
10. ☐ Copy restore sans jargon « chiffrée »
11. ☐ Reval notif `largeIcon` + H01/F01 doigt
12. ☐ **Puis seulement** : Scaffold / twin VM + tests VM

Tant que **1–3** ouverts → **NO-GO** cadeau sans Athalia **et** **NO-GO** premium.

---

## Liens

| # | Fichier | Note |
|---|---------|-----:|
| 01 | [`2026-11-01-premium-01-da-affichage.md`](2026-11-01-premium-01-da-affichage.md) | 8,5 |
| 02 | [`2026-11-01-premium-02-securite.md`](2026-11-01-premium-02-securite.md) | 7,5 |
| 03 | [`2026-11-01-premium-03-architecture.md`](2026-11-01-premium-03-architecture.md) | 7 |
| 04 | [`2026-11-01-premium-04-ux-parcours.md`](2026-11-01-premium-04-ux-parcours.md) | 7,4 |
| 05 | [`2026-11-01-premium-05-a11y-oneui.md`](2026-11-01-premium-05-a11y-oneui.md) | 8,0 |
| 06 | [`2026-11-01-premium-06-produit.md`](2026-11-01-premium-06-produit.md) | 6 · NO-GO |

*Synthèse premium-00 · `0.1.0-6.91` · NO-GO « vraiment premium » 6/10.*

---

## Suivi fixes (append)

### Vague premium-01 (6.92)

| Catégorie | Avant → après | Tests | Version |
|-----------|--------------:|------:|---------|
| DA affichage | 8,5 → **8,7** (code) | 374/375 | `0.1.0-6.92` |

Détail : [`2026-11-01-premium-fix-progress.md`](2026-11-01-premium-fix-progress.md). Smoke doigt toujours Athalia.

### Vague premium-02 (6.93)

| Catégorie | Avant → après | Tests | Version |
|-----------|--------------:|------:|---------|
| Sécurité | 7,5 → **8,2** (code/docs) | 376/377 | `0.1.0-6.93` |

Restore doigt / Device Care toujours Athalia.

### Vague premium-03 (6.94)

| Catégorie | Avant → après | Tests | Version |
|-----------|--------------:|------:|---------|
| Architecture | 7 → **8,0** (Scaffold models) | 379/380 | `0.1.0-6.94` |

Twin VM DraftController encore ouvert (prudent).
