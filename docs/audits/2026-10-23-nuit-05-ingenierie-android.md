# Audit ingénierie Android — Mon Mois Clair

| | |
|--|--|
| **Date** | 2026-10-23 (nuit) |
| **Profil** | Staff Android — Kotlin / Compose / Room |
| **Portée** | Architecture, god-files, tests, dette, perf, widget, notifs, régressions |
| **Mode** | Lecture seule (ce fichier seul créé) |
| **Baseline** | `0.1.0-6.75` · `versionCode` 80 · schéma Room **8** · claim journal **305/305** unit tests (non ré-exécutés ici) |

---

## Verdict

Architecture **saine pour une app familiale locale** : couches `ui` / `domain` / `data` lisibles, **MutatingRepository** comme goulot unique des écritures + side-effects (backup / rappels / widget), domaine pur testé en masse, Room migrations 1→8 + schemas exportés.

Le vrai risque de maintenabilité n’est pas le stack — c’est la **concentration UI** (`EntryFormScreen` ~1894 L, `HomeScreen` ~1544 L) et des **ViewModels fiche/accueil** qui injectent des DAO, reconstruisent trop d’état, et n’ont **aucun test instrumenté** ni **MigrationTestHelper**. Widget et notifs sont correctement découpés, mais le refresh widget + `reconcileAll()` scannent **toutes** les instances à chaque mutation.

**Note maintenabilité : 6,5 / 10**

| Critère | Note | Commentaire |
|---------|------|-------------|
| Séparation couches | 7,5 | Domaine clair ; fuite DAO → ViewModels |
| Modularité UI | 4 | Deux god-files Compose + god-scaffold formulaire |
| Données / mutations | 8,5 | MutatingRepository + migrations exemplaires pour la taille |
| Tests (couverture utile) | 7 | Domaine/backup solides ; UI/VM/migrations/instrumentation vides |
| Perf / côté batterie | 6,5 | OK à échelle famille ; O(n) reconcile + widget full-scan |
| Surfaces OS (widget/notif/boot) | 7,5 | Designs soignés ; smoke doigt encore ouvert |
| Dette opérationnelle | 5,5 | ~130 `._*` AppleDouble ; scission god-files reportée |

---

## Architecture (état)

```
ui/          Compose + Hilt ViewModels (+ DAO injectés sur Home/Edit/Add)
domain/      Règles pures + usecases (MonthCycleEngine, DossierRemaining, …)
data/        Room · MutatingRepository · backup SAF/WM · reminder · widget · receipt
di/          Database · Backup · Biometric · Widget
```

**Forces**

1. **Mutation unique** (`MutatingRepository.mutate`) : transaction Room → backup enqueue → reconcile rappels → widget. Évite les half-writes et les alarmes orphelines sur échec transaction.
2. **Schéma Room 8** avec `exportSchema = true`, chaînes `Migrations.MIGRATION_1_2`…`7_8`, **pas** de `fallbackToDestructiveMigration` — correct pour sideload famille.
3. **Horloge / fuseau** centralisés (`AppClock`, `Europe/Brussels`) — cohérent avec rappels et overdue.
4. **Surfaces OS isolées** : `PaymentNotificationHelper`, `ReminderSchedulerImpl`, `CarnetWidgetRefresher` + texte pur `CarnetWidgetText` (testable).
5. **Nav** `AppNavHost` (~313 L) raisonnable ; hub Réglages découpé en sous-pages (aligné charte §7).

**Faiblesses structurelles**

1. **Pas de couche « Lecture »** : `HomeViewModel` / `EditEntryViewModel` / `AddEntryViewModel` consomment `InstanceDao` / `TemplateDao` / `AppMetaDao` / `ExpenseFeeEventDao` directement. Les usecases existent pour l’écriture, pas pour l’agrégation Accueil → logique de timeline **dans** le VM (formatage Money/DayLabel, overdue, dossier, hasReceipt).
2. **God-files UI** explicitement notés « Pas fait » dans `docs/BENCHMARK_QUALITE_2026.md` — toujours vrai.
3. **EntryFormScaffold** (~900 L de paramètres + corps) = API Compose impossible à faire évoluer sans régression visuelle.
4. **Receivers** (`ReminderReceiver`, `BootReceiver`, `CarnetWidgetProvider`) : `goAsync()` + `CoroutineScope(IO + SupervisorJob)` jetable — pattern Android classique, mais **pas de structured concurrency / testabilité** ; risque de travail coupé si `finish()` trop tôt sous charge (faible à l’échelle actuelle).
5. **Aucun `androidTest`** malgré `testInstrumentationRunner` déclaré — smoke = humain uniquement (`AUDIT_STATUS.md`).

---

## God-files (inventaire)

| Fichier | Lignes | Rôle | Risque |
|---------|-------:|------|--------|
| `ui/entry/EntryFormScreen.kt` | ~1894 | Add + Edit + scaffold + photo/PDF + sections | **P0 refactor** — conflit de merge permanent |
| `ui/home/HomeScreen.kt` | ~1544 | Sticky, liste, sheets, snackbar, highlight notif | **P0/P1** — Accueil = surface critique |
| `ui/entry/EditEntryViewModel.kt` | ~782 | État fiche + OCR + IBAN + frais + épargne | **P1** — god-VM |
| `ui/settings/SettingsPages.kt` | ~611 | Plusieurs écrans Réglages | P2 découpage déjà partiel via routes |
| `ui/entry/AddEntryViewModel.kt` | ~597 | Miroir partiel Edit | P1 — duplication avec Edit |
| `ui/home/HomeViewModel.kt` | ~560 | combine nested + divers/épargne/mail | P1 |
| `ui/settings/SettingsViewModel.kt` | ~498 | Hub + backup + thème + … | P2 |

**Découpage Compose déjà amorcé (positif)** : `HomeCoursesSheet`, `HomeSavingsSheet`, `HomeMenuSheet`, `HomeMonthPickerSheet`, rows privés en bas de `HomeScreen`. Le corps `HomeScreen` + `ExpenseRow` restent trop gros.

**EntryForm** : `AddEntryScreen` / `EditEntryScreen` + `EntryFormScaffold` unique — bonne intention DRY, mauvaise granularité (un seul fichier = photo, EPC, dossier, récurrence, OCR, PDF).

---

## P0 techniques

| Id | Problème | Preuve / impact | Action recommandée |
|----|----------|-----------------|--------------------|
| **P0-1** | God-file `EntryFormScreen` | ~1894 L ; `EntryFormScaffold` API monstre | Extraire sans changer le comportement : `EntryPhotoSection`, `EntryPaymentSection`, `EntryDossierSection`, `EntryRecurrenceSection` + fichier `EntryFormScaffold.kt`. **Pas** de redesign. |
| **P0-2** | Pas de filet instrumentation sur chemins régression famille | 0 `androidTest` ; smoke photo loyer3 / notif / widget encore ouverts (journal 6.73–6.75) | Au minimum 2–3 tests Compose ou UI Automator smoke : ouvrir Accueil, FAIT, ouvrir fiche avec `receiptRelativePath` multi-ligne. |
| **P0-3** | `reconcileAll()` + widget `getAll*()` à **chaque** mutation | `MutatingRepository` → `reminderScheduler.reconcileAll()` + `widgetSink.refresh()` ; refresher filtre ensuite le mois courant | OK tant que N factures ≪ 200. Dès qu’une année complète d’historique grossit : **reconcile delta** (ids touchés) + widget query `WHERE ledgerId = :current`. Sinon risque jank post-FAIT / batterie One UI. |
| **P0-4** | Régression photo multi-chemin | Correctif 6.73 `ReceiptPhotoStore.resolvedPath` ; smoke doigt **non clos** | Traiter comme P0 **produit/qualité** jusqu’à smoke Galaxy (loyer3 webp+PDF). Côté eng : tests unitaires `resolvedPath` / VIEW déjà utiles — vérifier couverture edge (PDF seul, webp+PDF, chemins sales). |

---

## P1 techniques

| Id | Problème | Détail | Action |
|----|----------|--------|--------|
| **P1-1** | God-file `HomeScreen` | Sticky + LazyColumn + highlight + sheets inline | Extraire `HomeStickyHeader`, `HomeTimeline`, `ExpenseRow` → fichiers ; garder state hoist dans `HomeScreen`. |
| **P1-2** | DAO dans ViewModels | `EditEntryViewModel` injecte 4 DAO + usecases | Introduire `ObserveHomeMonthUseCase` / `LoadEditEntryUseCase` (ou repository lecture) ; VM = orchestration UI only. |
| **P1-3** | `HomeViewModel` combine nested | `flatMapLatest` + combine 5 flux + rebuild timeline + `observeAll()` ledgers + **tous** templates à chaque tick | Scoper : `observeLedger(ledgerId)`, templates map id→meta en cache ; éviter rebuild complet si seul `highlight` change (déjà partiellement combiné). |
| **P1-4** | Duplication Add/Edit VM | ~597 + ~782 L, logique photo/PDF/récurrence chevauchante | Extraire `ReceiptDraftController` / `EntryFormStateReducer` partagé (JVM testable). |
| **P1-5** | Migrations non testées | Schemas 1–8 présents ; **aucun** `MigrationTestHelper` | Ajouter tests Room `1→8` et `7→8` (frais) — filet sideload upgrade famille. |
| **P1-6** | `AgreementFuturesSync` sans filet dédié | Logique sync accords futurs critique ; pas de `*Test` nommé | Extraire cas Remettre / solde / réserve mois courant en tests unitaires purs. |
| **P1-7** | `UpdateExpenseTemplateUseCase` / listes upcoming / ShareBackup | Pas de tests dédiés (messages Share seulement) | Couvrir archive template + projection « À venir ». |
| **P1-8** | AppleDouble `._*` (~130 sous `app/`) | Bruit T7 / risques d’outils ; journal mentionne nettoyage partiel | Script `find … -name '._*' -delete` + `COPYFILE_DISABLE=1` (déjà README) en hook pré-commit. |
| **P1-9** | Widget nuit hors process | Doc : `ACTION_CONFIGURATION_CHANGED` seulement si process vivant | Acceptable ; documenter comme limite One UI. Option P2 : `AppWidgetProvider` + alarm légère minuit / `ACTION_TIME_TICK` filtré — **ne pas** sur-ingénierer. |
| **P1-10** | Notif MARK_DONE / UNDO | Logique non triviale dans `ReminderReceiver` (double toggle edge) | Tests Robolectric sur receiver ou extraction `ReminderNotificationActions` pure + tests. |

---

## Refactors sûrs (ordre recommandé)

Critère : **zéro changement de copy / pixels / schéma** ; tests unitaires existants restent verts.

1. **Découpe fichiers Compose (mechanical move)**  
   - `ExpenseRow` + `IncomeRow` + chips → `HomeTimelineRows.kt`  
   - Sheets déjà séparés : ne pas fusionner  
   - `EntryFormScaffold` → fichier dédié ; sections → `entry/sections/*`  
   *Risque : faible si `git mv` + imports ; valider 1 smoke Accueil + 1 fiche.*

2. **Extraire builders purs depuis `HomeViewModel`**  
   - `TimelineBuilder.build(incomes, expenses, templates, …): List<TimelineItem>`  
   - Déjà des formats testés (`MoneyFormat`, `DoneDateFormat`) — le builder devient testable sans Compose.

3. **Lecture widget**  
   - Remplacer `getAllExpenses().filter { ledgerId }` par DAO `observeExpenses` / `getExpenses(ledgerId)` déjà présent pour l’UI.

4. **Reconcile ciblé (après mesure)**  
   - API `ReminderScheduler.reconcile(expenseIds: Set<String>)` + full reconcile au boot / `MY_PACKAGE_REPLACED` seulement.

5. **Ne pas faire maintenant**  
   - Glance migration widget (XML RemoteViews tient la charte papier).  
   - Modularisation Gradle multi-module (surcoût > bénéfice pour 1 app sideload).  
   - Remplacer Hilt / Room.

---

## Trous de tests

### Ce qui est bien couvert (à préserver)

- Domaine : cycle mois, récurrence, overdue, dossiers/reste, EPC, `+++`, épargne, widget **texte**, formats, a11y contrast / title tokens.
- Data : backup codec/restorer/rotator/security, `MutatingRepository`, `ReminderSchedulerImpl` + timing, `ReceiptPhotoStore` (partiel).
- Usecases majeurs : Add / Toggle FAIT / Update-Delete / ConfigureSavings / PendingLetter (Create+List+Convert dans **un** fichier test) / DossierAgreement.

### Trous prioritaires

| Zone | Trous | Priorité |
|------|-------|----------|
| **UI / VM** | 0 test `HomeViewModel`, `EditEntryViewModel`, `EntryForm*`, navigation | P0/P1 |
| **Instrumentation** | 0 `androidTest` | P0 |
| **Room migrations** | Schemas OK, 0 MigrationTest | P1 |
| **AgreementFuturesSync** | Couverture indirecte seulement | P1 |
| **UpdateExpenseTemplateUseCase** | Absent | P1 |
| **ListUpcomingMonthsUseCase** | Absent | P2 |
| **ShareBackupUseCase** | Messages seuls | P2 |
| **Notification actions** | Helper + Receiver non testés | P1 |
| **CarnetWidgetRefresher.render** | Texte testé ; RemoteViews/nuit non | P2 |
| **Perf / reconcile scale** | Aucun test de charge N instances | P2 |

### Note sur le claim 305/305

Le journal / README affirment **305/305**. Cet audit **n’a pas** relancé Gradle (`--rerun-tasks`). Avant tout bump / push : recompter les `TEST-*.xml` (règle workspace). Les `._*Test.kt` AppleDouble ne doivent **pas** être compilés comme sources — vérifier qu’ils sont exclus (sinon risque flaky / double).

---

## Perf

| Sujet | Observation | Gravité |
|-------|-------------|---------|
| Timeline Accueil | Rebuild liste complète à chaque émission Flow (incomes/expenses/templates/ledgers) | Moyenne — N petit |
| `hasReceipt` | `isPdf` = suffixe chemin (pas I/O disque) — OK | Faible |
| Photo fiche | `decodeBitmap` / `BitmapFactory` bornés (`maxSide`, readBounded) — bon réflexe OOM | Faible |
| Widget | Full table scan à chaque mutation même sans widget posé… non : early-return si `ids.isEmpty()` — **bien** | — |
| Widget si posé | Toujours full `getAll*` | Moyenne long terme |
| Rappels | `reconcileAll` cancel/schedule AlarmManager × 3 kinds × N TODO | Moyenne (One UI + exact alarms) |
| Compose | `collectAsStateWithLifecycle` Accueil — correct ; beaucoup de `LaunchedEffect(Unit)` Home (gates) — surveiller double collect | Faible |
| Release | `minify` + `shrinkResources` + ABI ARM only — bon pour sideload | — |

---

## Widget

| Point | État |
|-------|------|
| Architecture | `Provider` → `ensureCurrentMonth` + `Refresher` ; sink injecté depuis mutations | Solide |
| UI | RemoteViews XML + fonds jour/nuit ; Reste 36 sp (6.72+) | Aligné charte |
| Verrou biométrie | Masque montant + filet | Correct |
| Tests | `CarnetWidgetTextTest` seulement | OK couche domaine |
| Limites | Nuit hors process ; pas de deep-link facture (ouvre app seule) | Acceptable V1 |
| Régression | Pads / filet / stroke — smoke doigt encore listé | Suivre `AUDIT_STATUS` |

---

## Notifications / rappels

| Point | État |
|-------|------|
| Canal | `paiements`, HIGH, icône silhouette + teal | 6.72+ |
| Kinds | J-3, J, OVERDUE quotidien | Clair |
| Actions | FAIT sans ouvrir l’app + UNDO 10 s | Bien pensé famille |
| Droits | Check POST_NOTIFICATIONS + `areNotificationsEnabled` + catch SecurityException | Robuste |
| Santé | Écran Alertes + `ReminderHealthChecker` | Aligné charte |
| Exact alarms | `USE_EXACT_ALARM` + cancel si droit retiré | P3-R01 tenu |
| Boot / time / replace | `BootReceiver` exported + directBoot | Nécessaire ; surface attaque limitée (pas de data) |
| Dette test | Receiver / helper | P1 |
| Smoke | Notif ≥4 factures historique OK ; lots 6.73–6.75 à revalider | Produit |

---

## Régressions à surveiller (matrice eng)

| Zone | Régression probable si on touche… | Filet actuel |
|------|-----------------------------------|--------------|
| Photo multi-ligne | `resolvedPath`, sanitize, VIEW ClipData | Unit store + **smoke manuel** |
| Sticky compact ≤800 dp | `DisplayFit`, Home sticky / Plus | Quasi aucun test UI |
| Snackbar FAIT | Home placement / durée | Manuel |
| PageTitle InkMuted | Tokens + `CarnetTitleTokensTest` | Unit OK |
| Widget nuit | `render` couleurs hardcodées vs `A11yTokens` | Dérive possible si tokens changent |
| FAIT depuis notif | `ToggleExpenseDone` + undo race | Unit toggle ; pas receiver |
| Backup après mutate | Ordre MutatingRepository | Tests repo/backup |
| Upgrade DB 7→8 | Frais datés | **Pas** de MigrationTest |
| Menu contact Arkalia | Nav / QuietLink only | Copy tests partiels |

---

## Dette (hors P0/P1 ci-dessus)

- Scission god-files **reportée** (benchmark) alors que la vélocité polish 6.7x augmente le coût de merge.
- `EditEntryState` data class très large → candidats `sealed` sous-états (photo / dossier / savings) quand on touchera la fiche.
- DI minimal (4 modules) — OK ; pas de module `ReminderModule` explicite (bindings probablement `@Binds` ailleurs ou ctor inject) — vérifier cohérence si on ajoute fakes tests.
- `SettingsPages.kt` 611 L : découper par groupe Compte / Confort / Aide quand on y retouche.
- Documentation audits premium riche ; **peu d’audits eng versionnés** — ce fichier amorce `docs/audits/`.

---

## Synthèse priorisée (prochaines 2 semaines eng)

1. **Smoke Galaxy** photo / sticky / notif / widget (bloque claim premium — hors code).  
2. **Découpe mécanique** `EntryFormScreen` + rows Home (P0-1 / P1-1) — 1 PR « move only ».  
3. **MigrationTest 7→8 + 1→8** (P1-5).  
4. **`TimelineBuilder` + tests** (P1-3 amorce).  
5. **DAO widget par `ledgerId`** (P0-3 partiel, sûr).  
6. **Nettoyage `._*`** (P1-8).  
7. Reporter instrumentation Compose à après (2)–(3).

---

## Note maintenabilité : **6,5 / 10**

**Pourquoi pas plus haut :** deux écrans critiques monolithes, fuite de persistance dans les VM, zéro filet UI/migration automatisé, O(n) side-effects système.

**Pourquoi pas plus bas :** discipline mutations/backup/rappels rare à cette taille ; domaine testé ; charte et journal qui empêchent la dérive produit ; surfaces widget/notif pensées « One UI famille » plutôt que collage demo.

---

*Fin audit — aucune modification code hors ce fichier.*
