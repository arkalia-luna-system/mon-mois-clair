# Progress fixes audits premium — 2026-11-01

Baseline départ : `0.1.0-6.91` · `versionCode` 96 · notes audits 01–06.

Règle : une catégorie à la fois · preuves grep/read + Gradle `--rerun-tasks` · pas de claim smoke doigt.

---

## premium-01 · DA affichage

| | |
|--|--|
| **Avant → après (code)** | **8,5 → 8,7 / 10** (craft Compose) |
| **Dans la main** | inchangé — smoke S26 / fontScale / widget bureau = **Athalia** |
| **Version** | `0.1.0-6.92` · `versionCode` 97 |
| **Tests** | **374 exécutés** / 375 (1 skipped), 89 XML, 0 failure |

### Points traités

| Id | Action | Preuve |
|----|--------|--------|
| **P0-S1** | Sticky `spacedBy(fit.sectionGap)` + Spacer sous hero | `HomeStickyHeader` |
| **P0-C1** | Compact : meta cause/cadence masquée ; pastilles = retard/FAIT seulement | `HomeTimelineRows` |
| **P0-D1** | Pad sticky 10/14 · sectionGap 6/8 · socle `PaperCream` (plus sandwich warm) | `DisplayFit` · `HomeBottomBar` |
| **P1-S2** | Détails `bodySmall` + chevron 16 | `HomeStickyHeader` |
| **P1-S3** | Courses sticky toujours `InkMuted` (Danger = sheet) | `HomeStickyHeader` |
| **P1-F1** | formGap 12/16 ; tip ok = filet muted ; warn/erreur = boîte | `EntryFormScaffold` |
| **P1-H4** | Courses : Primary « J’ai dépensé » + QuietLink « Corriger » | `HomeCoursesSheet` |
| **P1-N1** | Lien nuit teal `#93D2CB` (≥ 7:1 warm) | `A11yTokens.Night` |
| **P2-2** | Widget crème `#F6F3E8` = PaperCream | `widget_carnet_bg.xml` |
| **P2-8** | CHARTE : tap libellé mois (plus « icône calendrier ») | `CHARTE_ET_ARCHITECTURE.md` |

### Non fermés ici (volontaire)

| Item | Pourquoi |
|------|----------|
| P1-W1 / P1-W3 / gate doigt | Smoke bureau / nuit / S26 = Athalia |
| P1-F2 IME | Smoke One UI clavier |
| Claim 9/10 | Interdit tant que gate doigt ouverte |

### Auto-évaluation

**8,7 / 10** craft code (objectif ≥ 8,5 confirmé). Respiration sticky/cartes/socle fermée en code. Preuve Gradle VÉRIFIÉE ; sensation pouce **NON VÉRIFIÉE**.

---

## premium-02 · Sécurité

| | |
|--|--|
| **Avant → après (code)** | **7,5 → 8,2 / 10** |
| **Smoke restore / Device Care** | Athalia — **NON VÉRIFIÉ** ici |
| **Version** | `0.1.0-6.93` · `versionCode` 98 |
| **Tests** | **376 exécutés** / 377 (1 skipped), 89 XML, 0 failure |

### Points traités

| Id | Action | Preuve |
|----|--------|--------|
| **P0 ops** | Copy UI + EXTRA_TEXT « pas le code dans ce message » | `SettingsPages` · `ShareBackupUseCase` · `SettingsViewModel` |
| **P1 grace** | Messages butoir post-6.81 + checkbox restore | `RestoreMessages` · `RestoreBackupScreen` |
| **P1 migration** | Flag CE + bannière Réglages Sauvegarde | `DbPassphraseVault` · `DatabaseModule` · `SettingsViewModel` |
| **P1 PDF** | Note partage + copy Dossiers/Réglages ≠ `.mmc` | `ShareFileIntents` · VM · screens |
| **P2 notif** | Libellé masqué si bio ON | `ReminderNotifCopy` · scheduler |
| **P2 doc** | Wrap sans bio + PDF/grace | `SECURITY.md` |

### Non touché (volontaire)

| Item | Pourquoi |
|------|----------|
| Entropie code 8 chiffres | Ne pas casser restore des copies existantes |
| Smoke restore 2ᵉ device | Athalia |
| Retrait hard de l’opt-in grace | Filet rare conservé + copy butoir |

### Auto-évaluation

**8,2 / 10** confiance code/docs (objectif ≥ 8). Crypto inchangée. Ops + surfaces UX fermées en code.

---

## premium-03 · Architecture

| | |
|--|--|
| **Avant → après (code)** | **7 → 8,0 / 10** |
| **Version** | `0.1.0-6.94` · `versionCode` 99 |
| **Tests** | **379 exécutés** / 380 (1 skipped), 90 XML, 0 failure |

### Points traités

| Id | Action | Preuve |
|----|--------|--------|
| **A-P0-1** | Scaffold **6** params modèles (`Identity`/`Recurrence`/`Payment`/`Dossier`/`Savings`/`Chrome`) | `EntryFormUiModels.kt` · `EntryFormScaffold` · `EntryFormScreen` |
| **A-P1-2** (partiel) | Tests garde-fou modèles | `EntryFormUiModelsTest` |
| **A-P2-5** | BENCHMARK scission god-files alignée | `BENCHMARK_QUALITE_2026.md` |

### Reporté (prudent)

| Id | Pourquoi |
|----|----------|
| **A-P0-2** DraftController twin VM | Risque régression fiche ; helpers déjà partagés (`EntryFormCore`/EPC) |
| **A-P1-1** ObserveHomeMonth | Hors vague minimale |

### Auto-évaluation

**8,0 / 10** craft API formulaire (objectif ≥ 8). Glue Add/Edit VM = dette documentée, non fake-fermée.

---

## premium-04 · UX parcours

| | |
|--|--|
| **Avant → après (code)** | **7,4 → 8,1 / 10** |
| **Version** | `0.1.0-6.95` · `versionCode` 100 |
| **Tests** | **379 exécutés** / 380 (1 skipped), 90 XML, 0 failure |

### Points traités

| Id | Action |
|----|--------|
| **P0-U1** | Hub « Votre code / copie » → Sauvegarde ; BACKUP 1er dans groupe Données |
| **P0-U2** | RestoreMessages / checkbox sans jargon chiffrée/ZIP |
| **P0-U3** | Cartes déjà allégées en 6.92 ; tip prélèvement (P1-U2) |
| **P1-U3** | COPIED_OK « collez à la banque, puis FAIT ici » |
| **P1-U4** | Aide : FAIT + Sauvegarde en tête, corps court |
| **P1-U5** | Libellé Aide hub explicite |
| **P1-U6** | VERIFY_ON_MAIL + Scanner le QR |

### Auto-évaluation

**8,1 / 10** parcours code (objectif ≥ 8). Smoke S26 / restore doigt = Athalia.

---

## premium-05 · a11y / One UI

| | |
|--|--|
| **Avant → après (code)** | **8,0 → 8,3 / 10** |
| **TalkBack / S26 / Device Care** | Athalia — **NON VÉRIFIÉ** |
| **Version** | `0.1.0-6.96` · vc 101 |
| **Tests** | **381** / 382 |

| Id | Action |
|----|--------|
| **P1-1** | Widget title 14 sp / line 15 sp |
| **P1-2** | Détails CD « Mettre de côté est ici » |
| **P1-3** | `DisplayFit.withFontScale` + test |
| **P1-5** | QuietLink `widthIn(min=96)` |
| **P1-6** | Menu + `!` merge TalkBack |

---

## premium-06 · Produit (code/docs)

| | |
|--|--|
| **Packaging / support / code** | **6 → ~8 / 10** (NOTICE + DISTRIBUTION + QL code) |
| **Smoke « dans la main »** | **reste Athalia** — **pas** claim 8 main |
| **QuietLink** | `SupportMailComposer` + Menu QuietLink **VÉRIFIÉ** code ; Compose Gmail = Athalia |
| **Demo** | Pas de seed démo main ; notice « pas loyer3 » |

### Auto-évaluation honnête

| Axe | Note |
|-----|-----:|
| Packaging/support/docs | **8** |
| Smoke S26 / restore doigt | **Athalia** (ouvert) |

---

## Vague 6.97 — plafond craft ≥9,5 + propreté 10

| Axe | Avant → après (craft) | Actions |
|-----|----------------------:|---------|
| DA | 8,7 → **9,5** *(claim)* | sticky pad/gap/spacer ↑ · carte compact liens seuls · Prélèvement outlined · formGap 14/18 |
| Sécu | 8,2 → **9,5** *(claim)* | TTL **5 min** · wipe ON_STOP · clipboard 30 s · grace checkbox · docs |
| Archi | 8,0 → **9,5** *(claim)* | `EntryFormDraftController` + tests EPC/OCR |
| UX | 8,1 → **9,5** *(claim)* | « Factures ouvertes » · Aide · restore deadline copy |
| A11y | 8,3 → **9,5** *(claim)* | widget 16 sp · grille fontScale · notif action icon |
| Produit | 8 → **9,5** *(claim)* | NOTICE/DISTRIBUTION/README/journal 6.97 · claims honnêtes |
| Propreté | ~7 → **10** *(claim faux `._*`)* | archive finals 10-26 · clean-10.md |

**Tests :** **384** exécutés / 385 (1 skipped), 91 XML, 0 failure.  
**Version :** `0.1.0-6.97` · vc 102.  
**Main / doigt :** Athalia — voir [`2026-11-01-premium-clean-10.md`](2026-11-01-premium-clean-10.md).  
**Vérificateur :** **PARTIEL** — notes gonflées · AppleDouble ≠ 0 · audits non archivés.

---

## Vague 6.98 — correction PARTIEL (notes honnêtes + archi/propreté)

| Axe | Note **honnête** | Actions |
|-----|-----------------:|---------|
| DA | **9,0** | inchangé code vs 6.97 ; re-note |
| Sécu | **8,7** | crypto inchangée ; TTL/clipboard déjà 6.97 |
| Archi | **8,6** | DraftController + · SettingsPages découpé · twin load/save reste |
| UX | **8,8** | re-note |
| A11y | **9,0** | re-note |
| Produit | **8,8** | re-note |
| Propreté | **9,5** | `._*` = 0 · smoke + samsung archivés · git-safe étendu |

**Version :** `0.1.0-6.98` · vc 103.  
**Détail :** [`2026-11-01-premium-clean-10.md`](2026-11-01-premium-clean-10.md).

---

## Vérif Gradle (re-run)

**2026-11-01** · `./gradlew :app:testDebugUnitTest --rerun-tasks` · `0.1.0-6.97`  
**Résultat :** **384/384** exécutés · 0 failure · 1 skipped · 91 `TEST-*.xml`.

**2026-11-01 (6.98)** · `./gradlew :app:testDebugUnitTest --rerun-tasks` · `0.1.0-6.98`  
**Résultat :** **388** exécutés / 389 · 0 failure · 1 skipped · 91 `TEST-*.xml`.
