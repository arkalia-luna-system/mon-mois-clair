# Journal de bord — Mon Mois Clair

Règle : aucune étape n’est considérée comme terminée tant que ce fichier n’est pas mis à jour.  
Règle push : README / `versionName` / badges = journal + tests Gradle `--rerun-tasks`.

---

## État global

| Champ | Valeur |
|-------|--------|
| Dernière étape terminée | **3.3 Restore UI** |
| Étape en cours | *(aucune)* |
| Prochaine sous-étape | Biométrie optionnelle (réglages) · Device Care manuel une fois sur chaque Samsung familial |

---

## Journal

### 2026-10-19 — Validation expert (post rapport testeur)

**Confirmé :** 74/74 Gradle `--rerun-tasks` ; claims README/PROGRESS/`versionName` alignés `0.1.0-3.3` ; restore UI (confirm all-data, succès, SAF préservé) ; contraste `labelLarge` OK.  
**Réserves acceptées (non bloquantes) :** picker JSON fichier non E2E ; Device Care manuel.  
**Ops device :** `pm clear` + doc `dist/verify/RESET_TESTS_FROM_ZERO.md` pour prochains tests from zero.  
**Rapport :** `dist/verify/RAPPORT_TEST_2026-10-19.md` · **Push OK.**

### 2026-10-19 — Testeur-correcteur (post 3.3)

**Corrigé :**
- Contraste Primary : `labelLarge` sans `Ink` figé (`MonMoisClairTheme`).
- Restore : dialogue succès + OK ; confirm « toutes les données / tous les mois » ; `ensureCurrentMonthExists` après succès.
- README aligné `0.1.0-3.3` / **74/74** / feature Restore.

**Tests :** 74/74 OK · device E2E restore + FAIT + Santé + BackupWorker SUCCESS.  
**Rapport :** `dist/verify/RAPPORT_TEST_2026-10-19.md`

### 2026-10-19 — Étape 3.3 terminée (Restore UI)

**Codé :**
- Écran `RestoreBackupScreen` + `RestoreBackupViewModel` (dernière sauvegarde SAF + fichier JSON).
- Confirmation destructive avant restauration.
- Bouton Home « Restaurer une sauvegarde ».
- `RestoreMessages` (messages testables).

**Tests validés :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **74/74 OK**.

**Device (SM-S938B) :** restore E2E (dernière sauvegarde) + Home cohérent ; APK `0.1.0-3.3`.

**Vigilance :** restauration remplace toute la DB locale ; l’URI SAF du téléphone est conservée.

**Prochaine sous-étape :** biométrie optionnelle.

---

### 2026-10-19 — Validation device complète (SM-S938B / S25 Ultra)

Vérification visuelle + adb (screenshots `dist/verify/`) :

| Check | Résultat |
|-------|----------|
| Home reste-à-vivre / encore à payer | OK (maths cohérentes) |
| Toggle FAIT ✓ / Marquer à payer | OK |
| Édition ligne (tap zone libellé) | OK — form Modifier / Annuler / Supprimer |
| Ajout entrée (ex. gaz 33 € j8) | OK — backup mis à jour |
| Santé des rappels | OK ×4 (notif, alarmes, batterie, SAF) + hint Device Care |
| Backups rotation 1–3 | OK dans `/storage/emulated/0/Teste/` |
| Permissions notif + exact alarm | granted |
| Crash FATAL | aucun |
| Nav mois Suiv. (mois courant) | désactivé — conforme (pas de mois futur) |

Détail : `dist/CHECKLIST_S25.md`.

---

### 2026-10-19 — Étapes 2.4 + 3.2 terminées

**Codé (2.4) :** Add / Update / Delete (+ mois suivants).  
**Codé (3.2) :** Home sticky + liste + FAIT.  
**Tests :** 69/69 à ce stade.

---

## Modèle d’entrée

```markdown
### YYYY-MM-DD — Étape X.Y terminée
**Codé :** …
**Tests validés :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → N/N OK
**Vigilance :** …
**Prochaine sous-étape :** …
```
