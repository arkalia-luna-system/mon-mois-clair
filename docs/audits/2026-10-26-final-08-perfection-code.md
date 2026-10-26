# Audit final-08 — perfection code (structure / filets / sécu résiduelle)

| | |
|--|--|
| **Date** | 2026-10-26 · **final-08** |
| **Produit** | `0.1.0-6.84` · `versionCode` 89 · **346/346** unit |
| **Branche** | `develop` |
| **Hors scope** | Smoke doigt S26 · redesign DA · Play Store · force-push `main` |

**Verdict :** progrès **réel** sur hygiène, filets, miroir Add/Edit, découpe god-files, sécu résiduelle safe. **Pas** un score « 9/10 dans la main » ni GO cadeau — S26 / restore papier restent Athalia.

---

## 1. Avant / après (LOC)

| Fichier | Avant (6.83 / final-03) | Après (6.84) |
|---------|------------------------:|-------------:|
| `EntryFormScreen.kt` | **1794** | **770** (Add/Edit + dialogs) |
| `EntryFormScaffold.kt` | (dans Screen) | **1027** (nouveau) |
| `EntryFormCapture.kt` | (dans Screen) | **102** (nouveau) |
| `HomeScreen.kt` | **1538** / 1552 audit | **1098** |
| `HomeTimelineRows.kt` | — | **367** |
| `HomeStickyChrome.kt` | — | **85** |
| `HomeBottomBar.kt` | — | **90** |
| `AddEntryViewModel.kt` | **605** | **593** |
| `EditEntryViewModel.kt` | **790** | **782** |

Cible « ≪ 800–1000 L » : **EntryFormScreen** OK ; **HomeScreen** ~1098 (progrès net) ; **EntryFormScaffold** encore ~1027 (sections paiement/dossier encore inline — prochaine vague mécanique).

---

## 2. Vague A — hygiène + filets

| Item | État |
|------|------|
| `todoAmounts` / PacePhrase | Déjà propre (param `doneAmounts` utilisé) |
| Imports morts V4 | Déjà faits en 6.83 |
| AppleDouble `app/`/`docs/` | Purgés (0) |
| `ListUpcomingMonthsUseCaseTest` | **4** tests (revenus, annuel, dossier soldé, cap horizon) |
| `ShareBackupUseCaseTest` | dossier manquant + miroir pack→MMC1 |
| `ShareSecretVaultTest` / `DbPassphraseVaultTest` | wrap/unwrap Keystore (fallback software Robolectric) |
| Migrations Room 1→8 | **Documenté** dans `RoomMigrationTest` : harness = 7→8 seulement ; 1→6 sans MigrationTestHelper sous Robolectric |

---

## 3. Vague B — anti-doublon Add/Edit

Extraits partagés (même package `ui/entry`) :

- `RecurrenceChoice.kt`
- `EntryFormReceiptDraft.kt` (bitmap/PDF pending)
- `EntryFormEpcApply.kt` + test
- `EntryFormPaymentNoteCopy.kt`
- `EntryFormMessages.kt`

Les **noms** de méthodes restent homonymes sur les deux VM (wrappers fins) — le **corps** EPC / pièces / messages / copie +++ n’est plus dupliqué. `save()` reste spécifique Add vs Edit (attendu).

---

## 4. Vague C — god-files

Fichiers découpés (zéro redesign UX) :

**Home :** `HomeTimelineRows`, `HomeStickyChrome`, `HomeBottomBar`  
**Fiche :** `EntryFormCapture`, `EntryFormScaffold` (Add/Edit restent dans `EntryFormScreen`)

Widget RemoteViews : **non touché**.

---

## 5. Vague D — sécu résiduelle (safe)

| Surface | Changement |
|---------|------------|
| ZIP/JSON clairs à l’import | Toujours acceptés (grace famille) + **`cleartextGrace`** → warning fort `RestoreMessages.CLEARTEXT_GRACE_WARNING` |
| Cache `view/` | TTL **1 h → 15 min** ; `purgeViewCache()` au **lock** biométrique (`MainViewModel.onAppBackgrounded`) |
| PDF résumé | Toujours **clair** ; watermark `SENSITIVE_MARK` ; PRIVACY / SECURITY honnêtes |

Pas de recovery cloud inventé. SQLCipher / MMC1 / MRF1 **non cassés**.

---

## 6. Tests

```
./gradlew :app:testDebugUnitTest --rerun-tasks
→ 346/346 (84 XML, 0 failure)
```

Delta vs 6.83 (332) : Upcoming + ShareBackup + vaults + EpcApply + Restore cleartext + PDF mark ≈ **+14**.

---

## 7. Ce qui reste objectivement imparfait

1. `HomeScreen` ~1098 L et sticky header encore monolithique.
2. `EntryFormScaffold` ~1027 L (paiement BE + dossier inline).
3. Setters Add/Edit encore miroir de surface (wrappers) ; pas de reducer unique `EntryFormState`.
4. Migrations Room **1→6** sans filet Robolectric dédié.
5. `ReminderReceiver` / `androidTest` Compose toujours absents.
6. Grace cleartext **encore** importable (choix produit — warning seulement).
7. PDF résumé non chiffré (scope volontairement limité).
8. Smoke S26 + restore doigt : **Athalia** — hors cette passe.

**Note structure (honnête) :** ~**7 / 10** (était ~6).  
**Note sécu résiduelle (honnête) :** ~**7,5 / 10** (grace + PDF clair + cache view restent des résidus assumés).  
**Pas** « parfait » ni « 9/10 dans la main ».

---

*Fin final-08 · exécution code 6.84.*
