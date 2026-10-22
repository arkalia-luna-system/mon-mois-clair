# Distribution famille — APK sans USB

Pour Alex / maman : **pas besoin** de brancher le téléphone sur le Mac.

## Ce qu’Athalia fait (Mac)

```bash
COPYFILE_DISABLE=1 ./gradlew :app:assembleRelease
# APK : dist/MonMoisClair-release.apk  (~14 Mo, ARM)
```

Envoyer le fichier par **WhatsApp**, **Google Drive** ou **Mail** (si la pièce jointe passe).

- Toujours l’APK **release** signé (jamais debug).
- Même keystore qu’avant = mise à jour sans perdre les données.

## Ce qu’ils font (Galaxy)

1. Ouvrir le fichier reçu.
2. Si demandé : autoriser l’installation d’apps de cette source (une fois).
3. **Installer** / **Mettre à jour**.
4. Ouvrir Mon Mois Clair → vérifier le mois.

## Pièges

| Piège | Effet |
|-------|--------|
| APK debug | Signature différente → « application non installée » ou données à part |
| Ancien APK (schéma Room plus bas) | Peut refuser d’ouvrir une base déjà migrée |
| Mail qui refuse la pièce | Utiliser Drive / WhatsApp |

## Dans l’app

Pas de téléchargement automatique (l’app n’a pas Internet).  
Pour un souci : Réglages → **« Un souci avec l’app ? »** → mail à Athalia (joindre une capture dans Gmail).

## Plus tard (option)

Play Store **tests internes** = mises à jour auto. Hors produit actuel.
