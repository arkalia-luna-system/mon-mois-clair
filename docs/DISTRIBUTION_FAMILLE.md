# Distribution famille — APK sans USB

Les destinataires de l’APK **n’ont pas besoin** de brancher le téléphone sur le Mac.  
**Arkalia** construit chez elle → envoie le fichier → mise à jour **chez eux**.

## 1. Arkalia (Mac) — build

```bash
COPYFILE_DISABLE=1 ./gradlew :app:assembleRelease
# APK : dist/MonMoisClair-release.apk  (~42 Mo, multi-ABI + ML Kit / SQLCipher)
```

- Toujours l’APK **release** signé (jamais debug).
- **Même keystore** qu’avant = mise à jour sans perdre les données Room.
- Vérifier la version dans le fichier : `aapt dump badging dist/MonMoisClair-release.apk | head -1`
  (ex. `versionName='0.1.0-6.98'` — lire le journal).

## 2. Envoyer (sans USB)

| Canal | Comment |
|-------|---------|
| **Google Drive** | Déposer l’APK → partager le lien (ou le fichier) avec la famille |
| **WhatsApp** | Envoyer le document `.apk` (parfois renommé / bloqué selon version WA → préférer Drive) |
| **Mail** | Pièce jointe si &lt; limite du fournisseur ; sinon Drive |

## 3. Chez eux (Galaxy) — installer / mettre à jour

1. Ouvrir le message / le lien Drive.
2. **Télécharger** le fichier si besoin (Mes fichiers / Téléchargements).
3. **Ouvrir** `MonMoisClair-release.apk`.
4. Si demandé : autoriser l’installation d’apps de cette source (une seule fois).
5. Toucher **Installer** ou **Mettre à jour**.
6. Ouvrir Mon Mois Clair → Menu → **Réglages** → vérifier que la version affichée est la nouvelle (ex. **6.98** — lire le journal).
7. Vérifier le mois en cours (Reste, factures).

## Pièges

| Piège | Effet |
|-------|--------|
| APK **debug** | Signature différente → « application non installée » ou données à part |
| Ancien APK (schéma Room plus bas) | Peut refuser d’ouvrir une base déjà migrée |
| Mail qui refuse la pièce (~42 Mo) | Utiliser Drive / WhatsApp |
| WhatsApp qui bloque `.apk` | Envoyer via Drive |
| Keystore différent | Mise à jour impossible sans désinstaller (perte données) |

## Dans l’app

Pas de téléchargement automatique (l’app n’a **pas** Internet).  
Pour un souci : Accueil → Menu → **« Un souci ? Écrire à Arkalia Système »** → mail (joindre une capture dans Gmail).

## Annexe — USB (option, poste Arkalia)

Même fichier que pour la famille, pour tester sur ton tel :

```bash
adb push dist/MonMoisClair-release.apk /sdcard/Download/MonMoisClair-release.apk
# Au doigt : Mes fichiers → Téléchargements → MonMoisClair-release.apk
# Ou shell :
adb push dist/MonMoisClair-release.apk /data/local/tmp/MonMoisClair-release.apk
adb shell pm install -r /data/local/tmp/MonMoisClair-release.apk
```

Sur One UI, l’intent `file://` vers l’installateur est souvent bloqué.

## Plus tard (option)

Play Store **tests internes** = mises à jour auto. Hors produit actuel.
