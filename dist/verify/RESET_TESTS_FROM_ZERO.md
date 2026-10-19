# Reset « from zero » — tests propres

Pourquoi : si l’app garde onboarding, Room et permissions, le prochain test **ne rejoue pas** une vraie 1ʳᵉ installation.  
À faire **avant** chaque campagne « persona A / première install », ou après une session de validation.

## 1. Vider l’app (recommandé à chaque campagne)

Téléphone branché, USB debug :

```bash
adb devices
adb shell pm clear --user 0 be.monmoisclair.app
```

Effet :
- base Room vide
- onboarding à refaire (SAF + permissions)
- pas de mois / entrées

**Ne supprime pas** les fichiers JSON déjà dans Documents (ex. `Teste/mon-mois-clair-backup-*.json`) — utile pour tester Restore après réinstall.

## 2. Option « disque aussi à zéro »

Si tu veux plus aucune sauvegarde de test :

```bash
# Adapter le dossier (ex. Teste ou MonMoisClair)
adb shell rm -f /storage/emulated/0/Teste/mon-mois-clair-backup*.json
adb shell rm -f /storage/emulated/0/Teste/mon-mois-clair-backup.tmp
```

## 3. Réinstaller la build à tester

```bash
export COPYFILE_DISABLE=1
./gradlew :app:assembleDebug
cp ~/.cache/mon-mois-clair/gradle-build/app/outputs/apk/debug/app-debug.apk dist/MonMoisClair-debug.apk
adb install -r dist/MonMoisClair-debug.apk
adb shell am start --user 0 -n be.monmoisclair.app/.MainActivity
```

## 4. Secure Folder

Si tu as installé aussi dans Secure Folder (user 150), clear **userément** :

```bash
adb shell pm clear --user 150 be.monmoisclair.app
```

## Règle pour le testeur-correcteur

Persona **A Première installation** = toujours commencer par `pm clear` (sauf si le rapport dit explicitement « régression sur données existantes »).
