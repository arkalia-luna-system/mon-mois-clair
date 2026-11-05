# In-App Updates (Immediate) — Mon Mois Clair

**Statut code :** **Immediate** depuis **7.27** (`PlayInAppUpdater` + cold start `MainActivity`).  
**Pas Production** — piste **Internal** familiale uniquement.

## En une phrase (Athalia)

**Oui** : à l’ouverture, ça se met à jour **tout seul via Play** (écran de mise à jour plein écran, puis l’app relance).  
**Exception** : si l’app a été installée **hors Play** (sideload APK Drive / USB), ça **ne marche pas** (silence) — il faut passer par Play Internal.

## Comment ça marche

| Étape | Qui fait quoi |
|-------|----------------|
| 1 | CI / Console publie un AAB Internal avec `versionCode` **plus grand** |
| 2 | Le téléphone a l’app **installée via Play** (testeur opt-in) |
| 3 | Ouverture de Mon Mois Clair → après unlock, **une fois par process** |
| 4 | Play Core détecte la MAJ → **écran Immediate** (téléchargement + install) |
| 5 | Play **relance** l’app sur la nouvelle version |

L’utilisatrice **n’ouvre pas** l’appli Play Store à la main.  
Mon Mois Clair **n’a pas** la permission `INTERNET` : le Store parle au Store.

Code : `app/src/main/java/be/monmoisclair/app/update/PlayInAppUpdater.kt`  
Déclencheur : `MainActivity` (`maybeCheckImmediateOnColdStart` + `onResumeContinueIfNeeded`).

## Limites honnêtes

| Cas | Effet |
|-----|--------|
| Install **Play Internal** + testeur | **Oui** — écran MAJ à l’ouverture |
| Install **sideload** (APK Drive / USB) | IAU = **no-op** |
| **Option B** App Signing (clé Play ≠ sideload) | Impossible de « mettre à jour » par-dessus le sideload → wipe + `.mmc` + install Play |
| Compte **pas** testeur / pas opt-in Internal | Pas de release visible → pas d’IAU |
| Immediate | Écran Play **plein** (pas un bandeau Flexible en fond) |
| Secours | Play Store → Mettre à jour / MAJ auto système |

## Ce que ce n’est pas

- Pas un bouton « Télécharger » dans l’app (pas de socket app).
- Pas Flexible (bandeau / accord optionnel en arrière-plan) — volontairement **Immediate**.
- Pas une promesse « sans jamais ouvrir Play » si IAU échoue : le Store reste le secours.

Checklist famille : [`PLAY_INTERNAL_TESTING.md`](PLAY_INTERNAL_TESTING.md) §9.
