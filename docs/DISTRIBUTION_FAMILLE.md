# Distribution famille — Play Internal (recommandé) + sideload secours

**Canal recommandé :** [Google Play — tests internes](PLAY_INTERNAL_TESTING.md)  
→ mises à jour **automatiques** · pas d’APK à renvoyer · lien d’invitation une fois.

**Canal secours :** APK sideload (ci-dessous) si Play indisponible, téléphone hors compte Google, ou dépannage Arkalia.

Les destinataires **n’ont pas besoin** d’USB.  
Checklist complète Play : [`PLAY_INTERNAL_TESTING.md`](PLAY_INTERNAL_TESTING.md).

---

## A. Play Internal (chemin normal)

1. Athalia : compte Play Console + app `be.monmoisclair.app` + piste **Internal** (voir checklist).
2. Upload `dist/MonMoisClair-release.aab` (build : `./gradlew :app:bundleRelease`).
3. Ajouter les e-mails famille → envoyer le **lien d’invitation**.
4. Chez eux : accepter → installer / mettre à jour via **Play Store**.

**Attention signature :** un sideload déjà installé n’est **pas** toujours la même signature que Play.  
Avant toute réinstall forcée → **backup `.mmc`** + code papier (NOTICE famille).

---

## B. Sideload APK (secours)

### 1. Arkalia (Mac) — build APK

```bash
COPYFILE_DISABLE=1 ./gradlew :app:assembleRelease
# APK : dist/MonMoisClair-release.apk  (~42 Mo, ARM + ML Kit / SQLCipher)
```

- Toujours l’APK **release** signé (jamais debug).
- **Même keystore** qu’avant = mise à jour sideload sans perdre les données Room.
- Vérifier : `aapt dump badging dist/MonMoisClair-release.apk | head -1`  
  (ex. `versionName='0.1.0-7.17'` — lire le journal).

### 2. Envoyer (sans USB) — uniquement si Play Internal ne convient pas

| Canal | Comment |
|-------|---------|
| **Google Drive** | Déposer l’APK → partager le lien |
| **WhatsApp** | Document `.apk` (parfois bloqué → préférer Drive) |
| **Mail** | Pièce jointe si &lt; limite ; sinon Drive |

### 3. Chez eux (Galaxy) — installer / mettre à jour

1. Ouvrir le message / le lien Drive.
2. **Télécharger** si besoin (Mes fichiers / Téléchargements).
3. **Ouvrir** `MonMoisClair-release.apk`.
4. Autoriser l’installation depuis cette source si demandé.
5. **Installer** ou **Mettre à jour**.
6. Réglages dans l’app → vérifier `versionName` = journal.
7. Vérifier le mois (Reste, factures).

### Pièges sideload

| Piège | Effet |
|-------|--------|
| APK **debug** | Signature différente → échec ou données à part |
| Ancien APK (schéma Room plus bas) | Peut refuser une base déjà migrée |
| Mail / WhatsApp qui refuse ~42 Mo / `.apk` | Drive |
| Keystore différent (ou bascule Play autre clé) | MAJ impossible sans désinstall (backup `.mmc` d’abord) |

---

## Dans l’app

Pas de téléchargement automatique **dans** l’app (pas d’`INTERNET`).  
Les MAJ Play passent par le **Play Store**, pas par Mon Mois Clair.  
Souci : Accueil → Menu → **« Un souci ? Écrire à Arkalia Système »**.

---

## Annexe — USB (poste Arkalia)

```bash
adb push dist/MonMoisClair-release.apk /sdcard/Download/MonMoisClair-release.apk
# Ou :
adb install -r dist/MonMoisClair-release.apk
```

Sur One UI, l’intent `file://` vers l’installateur est souvent bloqué.
