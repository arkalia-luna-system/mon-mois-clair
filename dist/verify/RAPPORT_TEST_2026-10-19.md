# Rapport test — 2026-10-19

## Contexte
- Device / émulateur : Galaxy S25 Ultra (`SM-S938B`, serial `R3CY60BJ3ZM`), Android 16, user 0
- Version app (versionName) : `0.1.0-3.3`
- Étape PROGRESS concernée : **3.3 Restore UI** (+ régression 3.2 / 2.4 / Santé)
- Build / install : `./gradlew :app:assembleDebug` + `adb install -r dist/MonMoisClair-debug.apk`  
  Tests : `./gradlew :app:testDebugUnitTest --rerun-tasks` → **74/74** (17 fichiers `TEST-*.xml`)

## Synthèse
- Verdict : **OK avec réserves**
- Nb problèmes bloquants / majeurs / mineurs : **0 / 1 / 2** (tous corrigés dans ce tour sauf réserves listées)

## Parcours testés
| Persona | Scénario | Résultat | Preuve (UI / log / fichier) |
|---------|----------|----------|-----------------------------|
| A Première install | Non rejoué (clear data) — déjà validé checklist | OK (historique) | `dist/CHECKLIST_S25.md` |
| B Usage mensuel | Reste-à-vivre 517 € / encore 533 € ; liste chrono gaz/loyer/pension/course | OK | `r2_home0.png`, `r5_scroll.xml` |
| B FAIT ✓ | Toggle gaz → encore à payer 500 € ; ligne reste ; badge FAIT | OK | `r5_after_fait.png` + `BackupWorker` SUCCESS |
| B Nav mois | Suiv. désactivé sur mois courant | OK | `r2_home0.xml` (`Suiv.` enabled=false) |
| C Droit à l’erreur | Annuler dialogue restore ; Retour | OK | `r2_confirm.xml` → `r2_restore_cancel.xml` |
| D Samsung / Santé | Notif / alarmes / batterie / SAF tous OK + hint Device Care | OK | `r2_sante.png` |
| D Backup 1–3 | Présents sous `/storage/emulated/0/Teste/` ; rotation après mutation | OK | `ls Teste/` + logcat BackupWorker |
| D Restore E2E | Dernière sauvegarde → confirm → succès → Home cohérent | OK | `r2_success.png`, `r2_home_after.png` |
| E Cas limites | Montants virgule / jour 31 : non rejoués ce tour (tests unitaires existants) | Doute léger | `MoneyFormatTest` / `MonthDayClampTest` |
| E Clavier / double tap | Non rejoués ce tour (validés 3.2.1) | OK (historique) | entrée PROGRESS 3.2.1 |

## Problèmes trouvés
| ID | Sévérité | Zone | Description | Attendu (charte) | Observé |
|----|----------|------|-------------|------------------|---------|
| P1 | Majeur | Thème / a11y | `labelLarge` forçait `Ink` → texte quasi illisible sur boutons Primary (vert foncé) | Contraste fort, texte ≥ 18 sp lisible | Texte sombre sur `ButtonFill` |
| P2 | Mineur | Restore UI | Succès → navigation immédiate sans feedback | Utilisateur comprend le résultat | Message jamais vu |
| P3 | Mineur | Restore UI | Dialogue disait « mois en cours » alors que `clearAllTables` efface tout | Droit à l’erreur / vérité | Sous-évaluation du risque |
| P4 | Mineur (doc) | README | Claims `69/69` et `0.1.0-3.2.1` alors que code/journal en 3.3 / 74 | Pas de fausse promesse | README en retard |

## Corrections faites
| ID | Fichiers | Résumé du fix | Re-test OK ? |
|----|----------|---------------|--------------|
| P1 | `MonMoisClairTheme.kt` | Retirer couleur figée de `labelLarge` (héritage `LocalContentColor`) | Oui — `r2_restore.png` texte clair sur vert |
| P2 | `RestoreBackupScreen.kt` | Dialogue « Restauration réussie » + OK avant retour Home | Oui — `r2_success.png` |
| P3 | `RestoreBackupScreen.kt` | Texte confirm : toutes les données / tous les mois ; SAF local conservé | Oui — `r2_confirm.xml` |
| P2+ | `RestoreBackupViewModel.kt` | Après succès : `ensureCurrentMonthExists()` | Oui (mois courant présent post-restore) |
| P2+ | `RestoreBackupScreen.kt` | « Choisir un fichier JSON » en Secondary (pas Danger) | Oui |
| P4 | `README.md` | Alignement version `0.1.0-3.3`, badges **74/74**, feature Restore | N/A (doc) |

## Non traité (hors scope / à laisser à l’expert validation)
- Biométrie optionnelle (étape 3.4)
- Device Care Samsung : confirmation **manuelle** une fois (hint affiché, pas vérifié dans Paramètres système)
- Parcours « Choisir un fichier JSON… » (picker SAF) non automatisé ce tour — seule la restauration « dernière sauvegarde » est prouvée E2E
- Clear-data / première installation non rejoués aujourd’hui
- Commit / push (réservé à l’expert validation)

## Checklist device (cocher)
- [x] SAF Documents
- [x] Backups 1–3
- [x] FAIT ✓
- [x] Add / Edit / Delete + confirm *(Add/Edit/Delete : historique 3.2.1 + preuves `dist/verify/` ; non rejoués exhaustivement ce tour)*
- [x] Santé rappels
- [x] Insets / lisibilité *(contraste Primary corrigé)*
- [x] Pas de crash
- [x] Restore UI E2E (dernière sauvegarde)

## Message pour l’expert validation
Restore UI 3.3 est **fonctionnelle sur device** : confirm destructive correct, succès explicite, Home à 517/533 après restore, FAIT + `BackupWorker` SUCCESS, Santé OK ×4.  
Correctifs a11y (contraste boutons) et doc README alignés sur **74/74** / `0.1.0-3.3`.  
À re-vérifier avant push : (1) claims README ↔ `versionName` ↔ PROGRESS ↔ score Gradle `--rerun-tasks` ; (2) parcours picker fichier JSON manuel ; (3) fichiers non commités (`ui/restore/`, theme, README, ce rapport) ; (4) ne pas annoncer biométrie / Device Care manuel comme terminés.  
Pas de FATAL observé. Prêt pour audit validation — **ne pas merger sur ma seule parole**.
