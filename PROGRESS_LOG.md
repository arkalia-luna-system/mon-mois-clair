# Journal de bord — Mon Mois Clair

Règle : aucune étape n’est considérée comme terminée tant que ce fichier n’est pas mis à jour.

---

## État global

| Champ | Valeur |
|-------|--------|
| Dernière étape terminée | **3.1** |
| Étape en cours | **2.4** (use cases Add/Update/Delete — **pas** clôturée : tests + UI manquants) |
| Prochaine sous-étape | Valider 3.1 sur S25, puis **3.2** liste **ou** finir **2.4** (tests) |

---

## Journal

### 2026-10-19 — Choix de direction

Priorité **3.1** (plutôt que 2.4) : sans onboarding SAF + permissions, backup et rappels restent **invérifiables** sur Galaxy S25 / One UI. Le droit à l’erreur (2.4) suit juste après.

### 2026-10-19 — Étape 3.1 terminée

**Codé :**
- Thème accessibilité (crème papier, texte ≥ 18 sp, boutons ≥ 48 dp, serif marque)
- Navigation : SafSetup → Permissions → Home placeholder → Santé
- `PersistSafFolderUseCase` (URI persistée + 1er backup)
- `CompleteOnboardingUseCase` / `ObserveOnboardingStateUseCase`
- Écrans : choix dossier Documents, notif / alarmes exactes / batterie, hint Samsung Device Care
- `ReminderHealthScreen` consultable depuis Home
- Home placeholder (liste mensuelle = prochaine UI)

**Tests validés :** `./gradlew :app:testDebugUnitTest --rerun-tasks` → **63/63 OK**
- `OnboardingUseCasesTest` (4) + régression

**Corrections audit pré-push :**
- `PersistSafFolderUseCase` : ne plus traiter `SkippedNotConfigured` comme succès après persist.
- Permissions / Santé : refresh `ON_RESUME` après retour des réglages système.
- Bouton Continuer désactivé tant que le rapport santé n’est pas chargé.

**Vigilance sécurité / fiabilité :**
- Impossible d’atteindre Home sans dossier SAF réel (`hasPersistedTree`).
- Continuer avec permissions incomplètes possible mais avec avertissement explicite.
- À valider **sur le S25** : sideload APK → choisir Documents → exemption batterie → Device Care.

**Prochaine sous-étape :** installer l’APK sur le S25 pour valider 1.2+2.3 en conditions réelles, puis **3.2** (liste mensuelle) + **2.4** (édition/suppression).

---

## Modèle d’entrée (à dupliquer à chaque fin d’étape)

```markdown
### YYYY-MM-DD — Étape X.Y terminée

**Codé :**
- …

**Tests validés :**
- …

**Vigilance sécurité / fiabilité :**
- …

**Prochaine sous-étape :** …
```
