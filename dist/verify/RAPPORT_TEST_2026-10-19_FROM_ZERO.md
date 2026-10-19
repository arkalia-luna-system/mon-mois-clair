# Rapport test — 2026-10-19

## Contexte
- Device / émulateur : Galaxy S25 Ultra (`SM-S938B` / `R3CY60BJ3ZM`), Android 16, user 0
- Version app (versionName) : `0.1.0-3.3` (HEAD `9e73b34` + fix CheckRow local)
- Étape PROGRESS concernée : **campagne from zero** post-push 3.3 (personas A→E)
- Build / install :
  - `pm clear --user 0 be.monmoisclair.app` (obligatoire persona A)
  - `./gradlew :app:assembleDebug` + `adb install -r`
  - `./gradlew :app:testDebugUnitTest --rerun-tasks` → **74/74**
  - Preuves : `dist/verify/from_zero/` · procédure `dist/verify/RESET_TESTS_FROM_ZERO.md`
  - JSON `Teste/backup-1..3` **conservés** volontairement pour Restore

## Synthèse
- Verdict : **OK avec réserves**
- Nb problèmes bloquants / majeurs / mineurs : **0 / 1 / 0** (majeur corrigé + re-test OK)

## Parcours testés
| Persona | Scénario | Résultat | Preuve (UI / log / fichier) |
|---------|----------|----------|-----------------------------|
| A Reset | `pm clear` → écran SAF (pas Home) | OK | `from_zero/a0_saf.png` |
| A SAF | Picker Documents/`Teste` → UTILISER CE DOSSIER → AUTORISER | OK | `a2_picker.xml`, `a4_perms.png` |
| A Permissions | Notif/alarmes/batterie/SAF OK + hint Device Care → Continuer | OK | `a5_after_refresh.xml` |
| A Home vide | Reste 0,00 € / encore 0,00 € / « Aucune entrée » | OK | `a6_home.png` |
| B Ajout | Revenu pension 1300 j15 | OK | `b2_after_income.png` |
| B Virgule | Dépense courses **470,50** → reste 829,50 / encore 470,50 | OK | `b4_after_exp.png` |
| B + FAIT | Loyer 500 ; FAIT courses → encore 500 ; undo → 970,50 | OK | `b8_fait.png`, `b9_todo.png` |
| B Nav mois | Suiv. `enabled=false` sur mois courant | OK | `b9_todo.xml` |
| B Récurrente | Case « Revient… » **morte** puis fix → eau 45 € + template | OK après fix | `backup_recurring.json`, `fx6_edit.xml` |
| C Édition | Annuler sans casse ; rename loyer→loyerOK | OK | `c2_cancel.png`, `c4_edited.png` |
| C Suppression | Confirm dialog + delete courses → 800 / 500 | OK | `c7_confirm.png`, `c8_deleted.png` |
| C Template | Option « Appliquer aussi aux mois suivants » sur eau | OK | `fx6_edit.xml` |
| D Santé | 4× OK + Device Care + « Tout est prêt » | OK | `d1_sante.png` |
| D SAF Home | Dossier sauvegarde → Retour (pas re-onboarding perms) | OK | `d3_saf.png`, `d4_home.png` |
| D Backup | Fichiers 1–3 présents ; `BackupWorker` SUCCESS après FAIT | OK | `ls Teste/` + logcat |
| D Restore | Confirm all-data → succès → Home cohérent | OK | `d8.png`, `d9.png` |
| E Jour défaut | Formulaire jour = **19** (aujourd’hui) | OK | `e3_add` / `fx1` |
| E Clavier | `imePadding` + Enregistrer accessible (historique + forms) | OK | formulaires B/C |
| E Double zone | FAIT vs tap label édition séparés | OK | parcours C |
| E Clamp 31 | Non rejoué device (couvert unit tests) | Doute léger | `MonthDayClampTest` |

## Problèmes trouvés
| ID | Sévérité | Zone | Description | Attendu (charte) | Observé |
|----|----------|------|-------------|------------------|---------|
| F1 | Majeur | Add entry / CheckRow | Case « Revient tous les mois » ne réagissait pas aux taps (pattern `toggleable` + `Checkbox(onCheckedChange=null)`) | Saisie récurrente / templates | Checkbox restait `checked=false` ; aucun `expenseTemplates` |

## Corrections faites
| ID | Fichiers | Résumé du fix | Re-test OK ? |
|----|----------|---------------|--------------|
| F1 | `EntryFormScreen.kt` (`CheckRow`) | `clickable` + `Checkbox(onCheckedChange=…)` | Oui — template `eau` créé ; option « mois suivants » visible |

## Non traité (hors scope / à laisser à l’expert validation)
- Picker Restore « Choisir un fichier JSON… » (fichier unitaire) non E2E
- Device Care : hint OK, réglage manuel Paramètres non ouvert
- Clamp jour 31 / mois court : seulement tests unitaires
- Commit / push du fix CheckRow (à faire par expert validation)
- Biométrie (3.4)

## Checklist device (cocher)
- [x] SAF Documents (`Teste/` via picker)
- [x] Backups 1–3
- [x] FAIT ✓
- [x] Add / Edit / Delete + confirm
- [x] Santé rappels
- [x] Insets / lisibilité
- [x] Pas de crash
- [x] **pm clear** + vraie 1ʳᵉ install (persona A)
- [x] Restore dernière sauvegarde
- [x] Entrée récurrente (après fix)

## Message pour l’expert validation
Campagne **from zero** réelle : `pm clear` → SAF `Teste` → permissions OK → Home vide → add/FAIT/edit/delete/santé/SAF retour/restore.  
**Bug majeur trouvé et corrigé** : case « Revient tous les mois » inopérante sur device ; fix `CheckRow` re-testé (template `eau` + option mois suivants).  
Gradle toujours **74/74**. Working tree local contient le fix non poussé.  
À re-vérifier avant push : diff `EntryFormScreen.kt`, rejouer une case « Revient… » à la main, claims inchangés (pas de bump version pour ce correctif UX).  
Picker JSON restore + Device Care manuel restent réserves non bloquantes.
