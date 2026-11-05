# In-App Updates (Flexible) — Mon Mois Clair

**Statut code :** livré depuis **7.20** (`PlayInAppUpdater` + cold start `MainActivity`).  
**Pas Production** — piste **Internal** familiale uniquement.

## Comment ça marche

| Étape | Qui fait quoi |
|-------|----------------|
| 1 | CI / Console publie un AAB Internal avec `versionCode` **plus grand** |
| 2 | Le téléphone a l’app **installée via Play** (testeur opt-in) |
| 3 | Ouverture de Mon Mois Clair → après unlock, **une fois par process** |
| 4 | Play Core (IPC Store) détecte la MAJ Flexible → UI / téléchargement **dans** le flux app |
| 5 | Quand le paquet est `DOWNLOADED` → `completeUpdate()` (redémarrage soft Play) |

L’utilisatrice **n’a pas besoin d’ouvrir l’appli Play Store manuellement** pour ce chemin.  
Mon Mois Clair **n’a pas** la permission `INTERNET` : le Store parle au Store.

Code : `app/src/main/java/be/monmoisclair/app/update/PlayInAppUpdater.kt`  
Déclencheur : `MainActivity` (`maybeCheckFlexibleOnColdStart` + `onResumeCompleteIfReady`).

## Limites honnêtes

| Cas | Effet |
|-----|--------|
| Install **sideload** (APK Drive / USB) | IAU = **no-op** |
| **Option B** App Signing (clé Play ≠ sideload) | Impossible de « mettre à jour » par-dessus le sideload → wipe + `.mmc` + install Play |
| Compte **pas** testeur / pas opt-in Internal | Pas de release visible → pas d’IAU |
| Flexible | Peut afficher un **accord** Play Core (pas 100 % silencieux) |
| Secours | Play Store → Mettre à jour / MAJ auto système |

## Ce que ce n’est pas

- Pas un bouton « Télécharger » dans l’app (pas de socket app).
- Pas Immediate (bloquant plein écran) — volontairement **Flexible**.
- Pas une promesse « sans jamais ouvrir Play » si IAU échoue : le Store reste le secours.

Checklist famille : [`PLAY_INTERNAL_TESTING.md`](PLAY_INTERNAL_TESTING.md) §9.
