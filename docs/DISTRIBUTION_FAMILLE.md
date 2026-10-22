# Distribution famille — APK sans USB

Pour Alex / maman : **pas besoin** de brancher le téléphone sur le Mac.

## Ce qu’Athalia fait (Mac)

```bash
COPYFILE_DISABLE=1 ./gradlew :app:assembleRelease
# APK : dist/MonMoisClair-release.apk  (~14 Mo, ARM)
```

### Sans USB (famille)

Envoyer le fichier par **WhatsApp**, **Google Drive** ou **Mail** (si la pièce jointe passe).

### Avec USB (Athalia — même parcours « fichier », pas `adb install`)

Dépose l’APK dans **Téléchargements** du téléphone, puis ouvre l’installateur système (comme s’ils l’avaient reçu) :

```bash
adb push dist/MonMoisClair-release.apk /sdcard/Download/MonMoisClair-release.apk
adb shell am start -a android.intent.action.VIEW \
  -d "file:///sdcard/Download/MonMoisClair-release.apk" \
  -t "application/vnd.android.package-archive"
```

Sur One UI récents, si `file://` est refusé : ouvrir **Mes fichiers → Téléchargements → MonMoisClair-release.apk** à la main.

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
