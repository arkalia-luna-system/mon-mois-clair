# Journal de bord — Mon Mois Clair

Règle : aucune étape n’est considérée comme terminée tant que ce fichier n’est pas mis à jour.  
Règle push : README / `versionName` / badges = **ce journal** + **tests Gradle `--rerun-tasks`** (pas d’anticipation).

---

## État global

| Champ | Valeur |
|-------|--------|
| Dernière étape terminée | **2.3** |
| Étape en cours | *(aucune)* |
| Prochaine sous-étape | **3.1** — UI Compose (Home / Month / Reste-à-vivre) |

---

## Journal

### 2026-10-19 — Étapes 1.1 → 1.3

Room + SAF backup + restore atomique.

### 2026-10-19 — Étape 2.1 terminée

`MonthCycleEngine`, `InstanceIds`, `MonthSummaryCalculator` ; idempotence ; `sideEffectsWhen = Created`.

### 2026-10-19 — Étape 2.2 terminée

`ToggleExpenseDoneUseCase` (mois courant + précédent) ; `sideEffectsWhen = Toggled`.

### 2026-10-19 — Étape 2.3 terminée

**Codé :**
- `ReminderTiming` (J−3 10:00 / Jour J 09:00, `Europe/Brussels`, skip passé).
- `ReminderRequestCodes` déterministes ; `ReminderPayload` ; `ReminderKind`.
- `ExactAlarmGateway` / `ExactAlarmGatewayImpl` (`setExactAndAllowWhileIdle`).
- `ReminderSchedulerImpl` (schedule TODO, cancel DONE / flags off / passé).
- `ReminderReceiver` (`goAsync` + notification) ; `BootReceiver` (reconcile au boot).
- `PaymentNotificationHelper` (canal notifications).
- `ReminderHealthChecker` (notifs, exact alarm, batterie, SAF, hint Samsung Device Care).
- Manifest : permissions + receivers ; Hilt bind `ReminderSchedulerImpl`.

**Corrections audit pré-push :**
- `MutatingRepository.sideEffectsWhen` (pas de faux backup sur no-op).
- `PendingIntent` non-null à la planification.
- Tests `ReminderSchedulerImpl` : `runBlocking<Unit>` (JUnit void).
- Receiver Boot enregistré.

**Tests validés :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **59/59 OK**
- `ReminderTimingTest` (5)
- `ReminderSchedulerImplTest` (6)
- + régression 1.x / 2.1 / 2.2 / MutatingRepository sideEffects (48)

**Vigilance :**
- Écran UI « Santé des rappels » = étape 3 (API `ReminderHealthChecker` prête).
- Device Care Samsung ≠ exemption batterie → hint permanent prévu en UI.

**Prochaine sous-étape :** 3.1 — UI Compose Home / Month.

---

## Modèle d’entrée

```markdown
### YYYY-MM-DD — Étape X.Y terminée

**Codé :** …
**Tests validés :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → N/N OK
**Vigilance :** …
**Prochaine sous-étape :** …
```
