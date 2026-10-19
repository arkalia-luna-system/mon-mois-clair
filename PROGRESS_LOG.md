# Journal de bord — Mon Mois Clair

Règle : aucune étape n’est considérée comme terminée tant que ce fichier n’est pas mis à jour.  
Règle push : README / `versionName` / badges = journal + tests Gradle `--rerun-tasks`.

---

## État global

| Champ | Valeur |
|-------|--------|
| Dernière étape terminée | **3.2.1** (polish UX + audit device) |
| Étape en cours | *(aucune)* |
| Prochaine sous-étape | Restore UI / Device Care manuel familial / biométrie optionnelle |

---

## Journal

### 2026-10-19 — Polish 3.2.1 (audit détail)

**Corrigé :**
- `safeDrawingPadding` (plus de contenu sous status/gesture bar Ultra).
- Montants : `parseToCents` **sans Double** (charte).
- FAIT : zone ligne / bouton séparées (plus de double action).
- Suppression : confirmation AlertDialog + bouton danger.
- SAF depuis Home : retour sans rejouer permissions (déjà) ; jour défaut = aujourd’hui.
- Nav mois : pas de mois fantôme (prev/next gated + check DAO).
- Clavier : `imePadding` sur formulaires.

**Tests :** 69/69 OK · re-install device OK (toggle FAIT, édition, formulaire jour=19).

### 2026-10-19 — Validation device (SM-S938B / S25 Ultra)

- Install adb Success · Android 16 · user 0.
- SAF OK · backups 1–3 sur disque · `BackupWorker` SUCCESS.
- Home : reste-à-vivre + liste + FAIT.
- Santé : notifications / alarmes / batterie / SAF tous **OK** ; hint Device Care affiché.
- Détail : `dist/CHECKLIST_S25.md`.

### 2026-10-19 — Étapes 2.4 + 3.2 terminées

**Codé (2.4) :**
- `AddEntryUseCase` / `UpdateInstanceUseCase` / `DeleteInstanceUseCase` (+ option mois suivants).
- UI `AddEntryScreen` / `EditEntryScreen`.

**Codé (3.2) :**
- `HomeScreen` : sticky reste-à-vivre, encore à payer, nav mois, liste chrono, badge FAIT ✓.

**Tests validés :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **69/69 OK**.

**Vigilance :**
- Ne pas choisir le dossier **Download** pour SAF (souvent refusé par Android).
- Device Care Samsung ≠ exemption batterie standard.

---

## Modèle d’entrée

```markdown
### YYYY-MM-DD — Étape X.Y terminée
**Codé :** …
**Tests validés :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → N/N OK
**Vigilance :** …
**Prochaine sous-étape :** …
```
