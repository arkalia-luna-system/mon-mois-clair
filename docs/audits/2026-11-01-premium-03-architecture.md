# Audit architecture Android — Mon Mois Clair (premium-03)

| | |
|--|--|
| **Date** | 2026-11-01 |
| **Profil** | Architecte Android senior — structure, god-files, doublons Add/Edit, tests, dette, modules |
| **Portée** | `app/src/main` + `app/src/test` + `app/src/androidTest` · `settings.gradle.kts` · charte § stack · **pas** UI visuelle / sécurité crypto / smoke doigt |
| **Mode** | **AUDIT ONLY** — ce fichier seul créé ; **pas de commit** ; Gradle **non** relancé ici |
| **Baseline code** | branche `develop` · `0.1.0-6.91` · `versionCode` 96 · HEAD local `189a32c` |
| **Précédents** | Archive figée [`2026-10-23-nuit-05-ingenierie-android.md`](../archive/audits-2026-10-23/2026-10-23-nuit-05-ingenierie-android.md) (6.75) · hygiène [`2026-10-26-final-03-code-hygiene.md`](2026-10-26-final-03-code-hygiene.md) (6.82) |

**Verdict court :** couches `ui` / `domain` / `data` + goulot `MutatingRepository` restent le bon socle pour une app familiale locale. Depuis 6.82, la scission Compose a **réduit** les monolithes (`EntryFormScreen` 1794→**770**, `HomeScreen` 1552→**619**) et a extrait un cœur testable (`EntryFormCore` + helpers EPC/photo). La dette durable n’est plus « fichiers de 1800 L », c’est **(1)** l’API `EntryFormScaffold` à **82** paramètres, **(2)** le miroir Add/Edit (~21 méthodes homonymes, 18 champs d’état partagés), **(3)** lectures DAO directes dans les ViewModels, **(4)** zéro test ViewModel. Module unique `:app` = **choix encore juste** à ~24 kLOC main. Note architecture craft premium : **7 / 10**.

---

## 0. Preuves d’inventaire (LOC / fichiers)

Commandes exécutées sur le workspace `/Volumes/T7/Mon Mois Clair` (2026-11-01). Nature : **commande** (comptage disque) — pas une validation fonctionnelle.

| Métrique | Valeur | Statut |
|----------|-------:|--------|
| Module Gradle | `include(":app")` seul | **VÉRIFIÉ** |
| `versionName` / `versionCode` | `0.1.0-6.91` / 96 | **VÉRIFIÉ** (`app/build.gradle.kts`) |
| `.kt` main (hors `._*`) | **211** fichiers · **~23 588** LOC | **VÉRIFIÉ** |
| `.kt` test unit (`*Test.kt`) | **89** fichiers · **~8 538** LOC (avec androidTest partiel dans le total test) | **VÉRIFIÉ** |
| `androidTest` `*Test.kt` | **3** | **VÉRIFIÉ** |
| Répartition LOC main | ui **14 393** · data **4 951** · domain **3 658** · di **240** · root **~346** | **VÉRIFIÉ** |
| Package `ui/entry` | **~4 525** LOC (plus gros cluster UI) | **VÉRIFIÉ** |
| Package `ui/home` | **~3 227** LOC | **VÉRIFIÉ** |
| Claim tests README | **374/375** unit (`1 @Ignore`) | **DÉCLARÉ** (inspection README) — **non exécuté** ici |
| Plugins detekt / ktlint / spotless | **Absents** | **VÉRIFIÉ** |
| AppleDouble `._*` sous `app/src` | **93** | **VÉRIFIÉ** (dette ops T7, hors runtime) |

### Top fichiers main ≥ 400 LOC

| Fichier | LOC | Rôle |
|---------|----:|------|
| `ui/entry/EntryFormScreen.kt` | **770** | `AddEntryScreen` + `EditEntryScreen` (~195 + ~487 L) |
| `ui/entry/EditEntryViewModel.kt` | **739** | État fiche + OCR + IBAN + frais + save/delete |
| `ui/settings/SettingsPages.kt` | **641** | Plusieurs sous-pages Réglages |
| `ui/home/HomeScreen.kt` | **619** | Orchestration Accueil |
| `ui/home/HomeViewModel.kt` | **576** | Timeline + divers/épargne + DAO observe |
| `ui/entry/AddEntryViewModel.kt` | **563** | Miroir partiel Edit + pending letter |
| `ui/entry/EntryFormScaffold.kt` | **543** | Formulaire commun · **82** params nommés |
| `ui/settings/SettingsViewModel.kt` | **508** | Hub backup/thème/bio |
| `ui/dossiers/DossiersScreen.kt` | **436** | Liste dossiers |

**Comparaison hygiène 6.82 (DÉCLARÉ vs VÉRIFIÉ 6.91) :**

| Fichier | 6.82 (audit) | 6.91 (wc -l) | Δ |
|---------|-------------:|-------------:|--:|
| `EntryFormScreen.kt` | 1794 | 770 | **−57 %** |
| `HomeScreen.kt` | 1552 | 619 | **−60 %** |
| `EditEntryViewModel.kt` | 790 | 739 | −6 % |
| `AddEntryViewModel.kt` | 605 | 563 | −7 % |
| `EntryFormScaffold` (extrait) | — / ~900 alors dans Screen | 543 + **82** params | scission faite, API encore « god » |

---

## 1. Structure des couches

```
ui/          Compose + Hilt ViewModels (+ DAO injectés Accueil / fiche)
domain/      Règles + usecases (23 fichiers usecase) + modèles fins
data/        Room · MutatingRepository · backup · reminder · widget · receipt · crypto
di/          Database · Backup · Biometric · Widget
```

**Statut :** **VÉRIFIÉ** (inspection arborescence `app/src/main/java/be/monmoisclair/app/`).

### Forces

1. **Mutation unique** — `MutatingRepository.mutate` : transaction Room → backup → reconcile rappels → widget. Ordre documenté et side-effects conditionnels. Preuve : `data/repository/MutatingRepository.kt` (commentaire + corps). **VÉRIFIÉ** (inspection).
2. **Domaine riche et testé** — `MonthCycleEngine`, dossiers, EPC, formats, 44 fichiers test sous `domain/`. **VÉRIFIÉ** (comptage).
3. **Nav raisonnable** — `AppNavHost.kt` **313** L ; hub Réglages découpé (aligné charte Accueil anti fourre-tout). **VÉRIFIÉ**.
4. **Surfaces OS isolées** — reminder / widget / receipt / backup en packages `data/*`. **VÉRIFIÉ**.
5. **Scission Compose amorcée et poursuivie** (PROGRESS_LOG 6.x) : sheets Home, `HomeTimelineRows`, `HomeStickyHeader`, sections Entry (`Payment` / `Dossier` / `Recurrence`), `EntryFormCore`. Journal = **DÉCLARÉ** ; fichiers présents = **VÉRIFIÉ**.

### Faiblesses structurelles

1. **Pas de couche lecture** — `HomeViewModel` injecte `InstanceDao` / `TemplateDao` / `MonthLedgerDao` / `PendingLetterDao` et construit la timeline dans le VM (Money/DayLabel/overdue/dossier). Idem `EditEntryViewModel` / `AddEntryViewModel` (DAO + parfois `mutate` hors usecase pur pour pièces jointes). **VÉRIFIÉ** (constructeurs lus).
2. **Fuites Android dans `domain/`** — `Context` / `Intent` / `Uri` / `Log` dans usecases onboarding, share backup, thème, bio + helpers `ShareFileIntents`, caches. Acceptable en mono-module, **bloque** un futur `:domain` JVM pur. **VÉRIFIÉ** (`grep import android.`).
3. **Un seul repository** (`MutatingRepository`) — excellent pour les écritures ; les lectures restent DAO-first. Pas de `*QueryRepository` / use case « ObserveHomeMonth ». **VÉRIFIÉ**.
4. **Benchmark** liste encore « scission god-files » en **Pas fait** — **désaccord partiel** avec le code : scission **faite** sur les écrans, **pas** sur l’API Scaffold ni les twin VM. Doc = **DÉCLARÉ** ; code = **VÉRIFIÉ**.

---

## 2. God-files restants (seuil craft)

Seuil retenu pour « god » durable : **≥ 500 L** **ou** API Compose **≥ ~40** paramètres **ou** VM sans test avec **> 15** responsabilités mutantes.

| Id | Élément | Preuve | Gravité craft |
|----|---------|--------|---------------|
| **A-GOD-1** | `EntryFormScaffold` · **82** params | Signature L118–203 | **P0** — régression visuelle / merge à chaque champ |
| **A-GOD-2** | `EditEntryViewModel` **739** L · état **42** champs | `wc` + data class | **P0/P1** — cœur métier fiche |
| **A-GOD-3** | `EntryFormScreen` **770** L (Add+Edit) | `wc` + 2 `@Composable` top-level | **P1** — mieux qu’en 6.82 ; encore fichier chaud |
| **A-GOD-4** | `HomeScreen` **619** + `HomeViewModel` **576** | `wc` | **P1** — Accueil critique ; déjà découpé |
| **A-GOD-5** | `SettingsPages` **641** + `SettingsViewModel` **508** | `wc` | **P2** |
| **A-GOD-6** | `DossiersScreen` **436** | `wc` | **P2** (surveiller) |

**Plus « god-file 1800 L » :** **VÉRIFIÉ** — aucun `.kt` main ≥ 800 L.

---

## 3. Doublons Add / Edit

### 3.1 Ce qui est déjà partagé (positif)

| Helper | LOC | Usage |
|--------|----:|-------|
| `EntryFormCore` | 172 | validation save / money-day / interval / pending letter / fees |
| `EntryFormEpcApply` | 49 | parse + patch (Add & Edit délèguent) |
| `EntryFormReceiptDraft` | 55 | brouillon photo/PDF |
| `EntryFormPaymentNoteCopy` | 23 | copie note |
| `EntryFormMessages` | 9 | messages |
| `EntryFormCapture` | 102 | capture UI |
| Sections + `EntryFormScaffold` | — | UI commune |
| `RecurrenceChoice.kt` | 4 | enum extrait (plus couplé dans Add VM) |

Tests associés : `EntryFormCoreTest` (141 L) · `EntryFormEpcApplyTest` (58 L). **VÉRIFIÉ**.

### 3.2 Miroir restant

| Zone | Preuve | Gravité |
|------|--------|---------|
| **21** méthodes homonymes Add↔Edit | `applyEpc*`, `attach*`, `set*`, `save`, OCR, `copyPaymentNote`, … | **Élevée** (boilerplate + drift) |
| **18** champs d’état partagés | `label`, `amountText`, `iban`, `ocrBusy`, … | **Élevée** |
| Corps `applyEpc` / `applyEpcRaw` | quasi identiques (Add force `isExpense = true`) | Moyenne — logique dans helper, glue dupliquée |
| `EditEntryState` **42** vs `EntryFormState` **22** | Edit = sur-ensemble (frais, mail, savings, receipt paths…) | Normal produit ; manque un **DraftState** commun |
| Save/delete Edit vs Add/pending | chemins divergents légitimes | OK si cœur validation reste `EntryFormCore` |

**Constat :** le chantier « helpers partagés » (PROGRESS_LOG vague B) a **abaissé** le risque métier ; le miroir **ViewModel glue** reste la dette n°1 pour un craft premium durable. **VÉRIFIÉ** (inspection + comptage méthodes).

---

## 4. Tests (architecture filets)

| Couche | Fichiers `*Test*` | Lecture |
|--------|------------------:|---------|
| `domain/` | 44 | Fort — cycle, dossiers, usecases écriture |
| `data/` | 41 | Fort — backup, Room migration, reminders |
| `ui/` | 29 | Formats, thème, helpers entry ; **pas** de `*ViewModel*Test*` |
| `androidTest` | 3 | Contexte + restore cleartext + PDF share |

**VÉRIFIÉ** (inventaire fichiers). Exécution Gradle : **non exécuté** — claim **374/375** = **DÉCLARÉ** (README).

### Trous architecture

| Id | Trou | Impact |
|----|------|--------|
| **A-TEST-1** | Aucun test `HomeViewModel` / `AddEntryViewModel` / `EditEntryViewModel` / `SettingsViewModel` | Régressions timeline / save glue invisibles |
| **A-TEST-2** | Pas de test Compose UI / paparazzi / screenshot | God-scaffold sans filet visuel |
| **A-TEST-3** | `androidTest` mince vs surfaces OS (widget, notif, boot) | Smoke = humain |
| **A-TEST-4** | Monolithe test `DossierAgreementUseCaseTest` **696** L | OK filet ; coût maintenance |

---

## 5. Modules Gradle

| Question | Réponse | Statut |
|----------|---------|--------|
| Multi-module ? | Non — `:app` seul | **VÉRIFIÉ** |
| Justifié à ~24 kLOC ? | **Oui** pour sideload famille : Hilt/Room/Compose déjà concentrés ; split `:domain`/`:data` **prématuré** tant que `domain` importe `android.*` | Jugement archi |
| Quand splitter ? | Après extraction JVM-pure (`MonthCycleEngine`, formats, `EntryFormCore`-like) + interfaces data ; pas avant réduction miroir Add/Edit | Recommandation |

Forcer `:feature:home` / `:feature:entry` **maintenant** ajouterait friction CI/Hilt sans enlever les 82 params Scaffold.

---

## 6. Dette architecture (synthèse)

| Id | Dette | Nature | Priorité |
|----|-------|--------|----------|
| **A-P0-1** | `EntryFormScaffold` 82 params → introduire `EntryFormUiModel` / sections state | Structure API | **P0** |
| **A-P0-2** | Glue Add/Edit VM (21 méthodes) → `EntryFormDraftController` + états sealed Add\|Edit | Duplication | **P0** |
| **A-P1-1** | DAO directs VM → use cases / observers lecture (`ObserveHomeMonth`, `LoadEditEntry`) | Couches | **P1** |
| **A-P1-2** | Tests unitaires VM (fake DAO + `EntryFormCore` déjà là) | Filets | **P1** |
| **A-P1-3** | `HomeViewModel` combine nested + mapping timeline → extractor pur testable | Accueil | **P1** |
| **A-P2-1** | Découper `SettingsPages` / alléger `SettingsViewModel` | Hygiène | **P2** |
| **A-P2-2** | Purger fuites `android.*` hors usecases OS (share/bio/thème) pour futur module | Modularité | **P2** |
| **A-P2-3** | detekt/ktlint + budget LOC CI (seuil 500 / params 40) | Garde-fou | **P2** |
| **A-P2-4** | AppleDouble `._*` T7 (~93 sous `app/src`) | Ops | **P2** |
| **A-P2-5** | Aligner `BENCHMARK_QUALITE_2026.md` (« scission Pas fait ») sur l’état 6.91 | Doc drift | **P2** |

`docs/BENCHMARK_QUALITE_2026.md` non relu ligne à ligne au-delà du grep « scission god-files » — claim **DÉCLARÉ**.

---

## 7. Faiblesses → améliorations (craft premium durable)

Sans proposer de patch appliqué — cibles seulement.

1. **API formulaire** — remplacer la liste plate de 82 params par un modèle UI immuable + callbacks groupés (`Identity`, `Payment`, `Dossier`, `Chrome`). Garde le Scaffold ; baisse le coût de chaque champ.
2. **Un brouillon, deux modes** — controller partagé (déjà amorcé via `EntryFormCore` / EPC / ReceiptDraft) ; Add/Edit ne gardent que load/save/delete et champs exclusifs (frais, mail, pending letter).
3. **Lecture = use case** — sortir le `combine` Home et le `load` Edit vers des fonctions pures / usecases ; VM = orchestration + `StateFlow`.
4. **Filet VM** — 1 test par chemin critique : save Add happy-path, Edit fee+recurrence, Home toggle past-month confirm (fakes DAO).
5. **Rester mono-module** jusqu’à domain JVM-clean ; documenter la frontière dans la charte.
6. **Budget qualité** — CI : fail si nouveau fichier UI > 600 L ou composable > 40 params (après introduction du modèle).

---

## 8. Note / 10

| Critère | Note | Commentaire |
|---------|-----:|-------------|
| Séparation couches | **7,5** | Domaine clair ; fuite DAO → VM toujours vraie |
| Modularité UI | **6,5** | Gros progrès 6.82→6.91 ; Scaffold params = plafond |
| Doublons Add/Edit | **6,0** | Cœur extrait ; glue VM encore miroir |
| Données / mutations | **8,5** | `MutatingRepository` exemplaire pour la taille |
| Tests (couverture utile) | **7,0** | Domaine/data solides ; VM/Compose faibles ; androidTest né |
| Modules Gradle | **7,5** | `:app` seul = bon choix actuel |
| Dette / drift doc | **6,0** | Benchmark « Pas fait » partiellement faux ; `._*` |
| **Synthèse craft premium** | **7 / 10** | |

**Pourquoi pas 8+ :** API Scaffold + twin VM + lectures DAO sans filet VM.  
**Pourquoi pas ≤ 6 :** scission réelle des god-screens, helpers testés, mutations uniques, baseline tests unitaires dense.

Évolution vs nuit-05 (6.75, note **6,5**) : **+0,5** grâce à la découpe Compose / `EntryFormCore`, pas grâce à un redesign de couches.

---

## 9. Périmètre non audité

- Sécurité crypto / Keystore / SQLCipher (autres audits).
- Rendu UI / a11y contraste / smoke Samsung doigt.
- Perf runtime (`reconcileAll`, widget full-scan) — mention historique nuit-05 seulement (**NON VÉRIFIÉ** ici).
- Exécution `./gradlew :app:testDebugUnitTest` — **non exécuté**.

---

## Clôture auditeur

- **Domaine couvert :** architecture Android (structure, god-files, doublons Add/Edit, tests filets, dette, modules) — baseline **0.1.0-6.91**.
- **Limites :** comportement runtime et compteur de tests verts = **NON VÉRIFIÉ** (pas de Gradle ni smoke) ; claims journal/README = **DÉCLARÉ** quand non recoupés par `wc`/lecture source.
- **Modification :** création seule de `docs/audits/2026-11-01-premium-03-architecture.md` (oui explicite Athalia). **Aucune modification de code.**
