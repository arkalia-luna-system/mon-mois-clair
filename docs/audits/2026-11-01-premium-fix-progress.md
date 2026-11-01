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
