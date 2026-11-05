# Play Console — tests internes (famille)

Checklist **ultra claire** pour Athalia.  
Objectif : canal principal = **Play Internal Testing** (MAJ auto) · sideload = secours.

**Ne crée pas le compte ici** — cette page guide uniquement.  
Package : `be.monmoisclair.app` · AAB local : `dist/MonMoisClair-release.aab`  
Dernier build documenté : **`0.1.0-7.22`** / `versionCode` **128** · targetSdk **36** · **~27 Mo** (lire le journal si plus récent).

**MAJ auto = Play Store** (piste Internal Active + testeurs opt-in).  
In-App Updates **Flexible** au cold start (Play Core, IPC Store — **pas** d’`INTERNET` app). Sideload → no-op ; MAJ auto Play reste utile en secours.

---

## Avant de commencer (sur le Mac)

1. Build AAB signé :
   ```bash
   export COPYFILE_DISABLE=1
   ./gradlew :app:bundleRelease
   cp -f ~/.cache/mon-mois-clair/gradle-build/app/outputs/bundle/release/app-release.aab \
     dist/MonMoisClair-release.aab
   ls -lh dist/MonMoisClair-release.aab
   ```
2. Note version : lire `PROGRESS_LOG.md` (état global) — `versionName` / `versionCode` dans `app/build.gradle.kts`.
3. **Backup `.mmc`** sur chaque téléphone qui a déjà l’APK sideload (voir § Signature).

---

## Jour J — dans l’ordre

### 1. Compte Play Console (~25 € une fois)

- [ ] Aller sur [play.google.com/console](https://play.google.com/console)
- [ ] Créer un compte développeur Google Play (carte / paiement unique ~25 USD/EUR selon région)
- [ ] Accepter les conditions développeur
- [ ] **Ne pas** publier en production demain — seulement **tests internes**

### 2. Créer l’application

- [ ] **Créer une app** → nom affiché : **Mon Mois Clair**
- [ ] Langue par défaut : **Français**
- [ ] Type : Application · gratuit
- [ ] Déclarations : pas de pub dans l’app, etc. (cocher selon le formulaire ; l’app n’a **pas** d’INTERNET)
- [ ] Confirmer que l’**ID d’application** sera `be.monmoisclair.app` (fixé au **premier** AAB uploadé — doit matcher le `applicationId` Gradle)

### 3. Play App Signing (critique famille)

**Clics Play Console (ne pas laisser Google créer une clé seule) :**

1. **Configuration → Intégrité de l’appli** (ou assistant 1ʳᵉ release) → **Play App Signing**.
2. Choisir **Exporter et importer une clé depuis un dépôt Java** / **Utiliser une clé existante** — **pas** « Laisser Google générer une clé d’application ».
3. Exporter depuis ton keystore release actuel (le `.jks` local / `keystore.properties`) le certificat demandé, puis **uploader** ce fichier dans la Console.
4. La **clé d’upload** = ton keystore actuel ; les AAB futurs restent signés avec ce même `.jks`.
5. Si tu acceptes une clé Google seule (**Option B**) → signature Play ≠ sideload famille → **désinstall/réinstall** = backup `.mmc` d’abord. Garder `.jks` + `keystore.properties` **hors git** (clé d’upload).

- [ ] Enrôlement App Signing terminé (Option A = même clé sideload, ou **Option B** = clé Google — wipe + `.mmc` au passage sideload → Play)

### 4. Data safety (aucune collecte)

Dans **Contenu de l’appli → Sécurité des données** :

- [ ] L’app **collecte-t-elle des données ?** → **Non**
- [ ] Pas de partage avec des tiers via l’app
- [ ] Chiffrement en transit : N/A (pas de réseau app) — suivre les libellés du formulaire ; rester honnête : données **uniquement sur l’appareil**
- [ ] S’aligner sur [`PRIVACY.md`](../PRIVACY.md) / page HTTPS ci-dessous

### 5. URL politique de confidentialité (HTTPS obligatoire)

Play demande une **URL HTTPS publique**.

**URL live (coller dans Play) :**  
https://arkalia-luna-system.github.io/mon-mois-clair-privacy/privacy.html

- Repo public dédié : [`arkalia-luna-system/mon-mois-clair-privacy`](https://github.com/arkalia-luna-system/mon-mois-clair-privacy) (uniquement `privacy.html` + README)  
- Pages : branch `main` / racine `/` · vérifié **HTTP 200** (2026-11-04)  
- Source miroir dans ce dépôt : [`docs/privacy.html`](privacy.html) (blob `d33635db3daa4269e253abd22421161c104df5ce` · commit `6c55f25`)  
- Privacy HTTPS reste sur le repo dédié `mon-mois-clair-privacy` (Pages du dépôt app = optionnel, non requis)

- [x] URL HTTPS **live** (ouvrir en navigation privée sans login GitHub)
- [ ] Coller l’URL réelle dans la fiche Play (Politique de confidentialité)

### 6. Fiche minimale (store listing)

Même pour Internal, Google demande souvent une fiche de base :

- [ ] Titre court · description courte (reste / factures / 100 % hors ligne)
- [ ] Icône 512×512 (depuis le kit brand / mipmap haute rés.)
- [ ] Captures d’écran téléphone (2–4 suffisent pour Internal)
- [ ] Catégorie : Finance ou Productivité (au choix, cohérent « carnet »)
- [ ] Coordonnées contact (mail Arkalia)

### 7. Piste Internal testing

- [ ] **Tester → Tests internes** (Internal testing)
- [ ] Créer une release → **Upload manuel** de `dist/MonMoisClair-release.aab`
- [ ] Notes de version courtes (ex. `0.1.0-7.18` — lire le journal)
- [ ] Enregistrer → **Examiner la release** → démarrer le déploiement Internal
- [ ] Vérifier piste **Active** (pas brouillon)
- [ ] Onglet **Testeurs** → créer une liste e-mail → ajouter les Gmail de la famille
- [ ] Copier le **lien d’invitation** → l’envoyer (WhatsApp OK pour le *lien*, plus pour l’APK)
- [ ] Chaque personne : ouvrir le lien → accepter d’être testeur → installer via Play Store

### 8. Première install / bascule depuis sideload

| Situation | Que faire |
|-----------|-----------|
| Téléphone **neuf** / pas d’app | Installer via le lien Internal → OK |
| Sideload déjà installé · **même** clé de signature Play | Mise à jour Play possible sans wipe |
| Sideload déjà installé · **clé différente** (souvent **Option B**) | 1) Backup `.mmc` + code papier 2) Désinstaller 3) Installer Play 4) Restaurer |

- [ ] Sur **ton** Galaxy : vérifier version dans Réglages = `versionName` du journal
- [ ] Smoke court : Accueil / FAIT / une facture / backup visible

### 9. Recevoir une **new version** (Galaxy / famille)

1. Compte Google du téléphone = e-mail de la liste testeurs + **opt-in** lien Internal accepté.
2. Play Console : release Internal **Active** avec le nouveau `versionCode` (ex. **125**).
3. Sur le téléphone : ouvrir **Play Store** → Mon Mois Clair → **Mettre à jour** (ou MAJ auto Play activée).
4. Dans l’app : Réglages → `versionName` = journal (ex. `0.1.0-7.22`).

Pas besoin d’ouvrir Mon Mois Clair pour « forcer » une MAJ : c’est **Play** qui livre.  
Si le téléphone a encore un sideload **7.16 / 7.17** avec une autre clé → wipe + restore `.mmc` (tableau §8).

---

## Pièges (à relire)

| Piège | Effet |
|-------|--------|
| Upload APK sideload signé ≠ clé Play App Signing | Impossible de « mettre à jour » ; réinstall |
| Oublier backup `.mmc` avant désinstall | Perte mois / factures / photos |
| Publier en **Production** trop tôt | Hors scope « famille Internal » |
| Compte testeur sans Gmail ajouté à la liste | Lien Play refuse l’install |
| Croire que WhatsApp/Drive reste le canal MAJ | Non — désormais **Play Internal** = canal recommandé |
| Merger `main` **sans** bump `versionCode` | Play refuse l’upload (version déjà utilisée) |
| Secrets GitHub absents | Workflow skippe l’upload (run vert + notice) — pas de déploiement |
| Track **production** | **Interdit** — le workflow n’expose que `internal` |

---

## Après J+0 (rythme)

**Canal recommandé (après secrets CI) :** bump sur `develop` → merge/ff `main` → Actions upload Internal auto.

**Secours manuel :**

1. Sur le Mac : `bundleRelease` → AAB dans `dist/`
2. Play Console → Internal → nouvelle release → upload AAB → déployer
3. Famille : notification Play / ouvrir Play Store → Mettre à jour (auto si activé)

---

## CI auto-deploy (push `main` → Internal)

Workflow : [`.github/workflows/publish-play-internal.yml`](../.github/workflows/publish-play-internal.yml)

| | |
|--|--|
| Déclencheurs | **push sur `main`** · `workflow_dispatch` (saisir `PUBLISH`) |
| Piste | **`internal` uniquement** — jamais Production |
| `releaseStatus` | `completed` (disponible aux testeurs) |
| Sans secrets | job d’upload **skippé** (run vert) — pas de CI rouge |

### Secrets GitHub — noms exacts

Dans le dépôt → **Settings → Secrets and variables → Actions → New repository secret** :

| Nom du secret | Contenu |
|---------------|---------|
| `PLAY_SERVICE_ACCOUNT_JSON` | JSON **brut** du compte de service (tout le fichier, y compris `{`…`}`) |
| `RELEASE_KEYSTORE_BASE64` | Keystore release (`.jks`) encodé en **une ligne** base64 |
| `RELEASE_KEYSTORE_PASSWORD` | Mot de passe du keystore (`storePassword`) |
| `RELEASE_KEY_ALIAS` | Alias de la clé (ex. `monmoisclair`) |
| `RELEASE_KEY_PASSWORD` | Mot de passe de la clé (`keyPassword`) |

Ne jamais committer ces valeurs. `keystore.properties` / `*.jks` restent gitignorés.

### Compte de service Play — clics (une fois)

1. **Google Cloud Console** → [console.cloud.google.com](https://console.cloud.google.com)  
   - Créer / choisir un projet (ex. `mon-mois-clair-play`).  
   - **IAM et administration → Comptes de service → Créer**  
     - Nom : `play-internal-upload`  
     - Rôle : laisser minimal ici (l’accès Play se gère dans Play Console).  
   - Onglet **Clés → Ajouter une clé → Créer → JSON** → télécharger le fichier.  
   - Coller le contenu JSON dans le secret GitHub `PLAY_SERVICE_ACCOUNT_JSON`.

2. **Lier l’API Play**  
   - Cloud → **API et services → Bibliothèque** → chercher **Google Play Android Developer API** → **Activer**.

3. **Play Console** → [play.google.com/console](https://play.google.com/console)  
   - **Utilisateurs et autorisations → Inviter de nouveaux utilisateurs**  
   - Coller l’**e-mail** du compte de service (`…@….iam.gserviceaccount.com`).  
   - Droits : **Version vers les pistes de test** / *Release to testing tracks* (Internal).  
     **Pas** besoin de « Release to production ».  
   - Appliquer à l’app **Mon Mois Clair** (`be.monmoisclair.app`) → **Inviter**.

4. Attendre quelques minutes (propagation IAM), puis les secrets keystore (ci-dessous).

### Encoder le keystore en base64 (Mac)

Depuis la racine du projet (chemin réel du `.jks` = celui de `keystore.properties`) :

```bash
# Une seule ligne, sans retour — coller dans RELEASE_KEYSTORE_BASE64
base64 -i release/monmoisclair-release.jks | tr -d '\n' | pbcopy
echo "Base64 copié dans le presse-papiers ($(pbpaste | wc -c) octets)"
```

Les 3 autres secrets keystore = les valeurs de ton `keystore.properties` local (`storePassword`, `keyAlias`, `keyPassword`).

### Bump version — **sur `develop` avant merge `main`**

Play refuse un `versionCode` déjà uploadé. **Pas d’auto-bump CI** (trop risqué pour un dépôt familial).

1. Sur `develop` : incrémenter `versionCode` (+1) et `versionName` dans `app/build.gradle.kts`.  
2. Mettre à jour `PROGRESS_LOG.md` / README si tu closes une étape.  
3. Tests verts → commit → push `develop`.  
4. **Ensuite seulement** : merge/ff `main` → le workflow publie Internal.

### Première activation

1. Créer les **5 secrets** (action Athalia).  
2. Vérifier qu’une release Internal a déjà existé à la main (app pas « draft » pure — sinon `completed` peut échouer → passer temporairement le workflow en `status: draft` ou finir la fiche Console).  
3. Petit bump `versionCode` sur `develop` → merge `main` → onglet **Actions** → run **Publish Play Internal**.  
4. Play Console → Tests internes → nouvelle version Active → famille : MAJ Play Store.

Si `completed` échoue alors que l’app est encore brouillon Console : documenter / basculer `status: draft` dans le workflow et publier la release à la main une fois.

---

## Liens utiles


- [`DISTRIBUTION_FAMILLE.md`](DISTRIBUTION_FAMILLE.md) — sideload = **secours**
- [`PRIVACY.md`](../PRIVACY.md) — texte source confidentialité
- [`privacy.html`](privacy.html) — page HTTPS pour Play
- [`NOTICE_FAMILLE_2MIN.md`](NOTICE_FAMILLE_2MIN.md) — brief oral code papier
