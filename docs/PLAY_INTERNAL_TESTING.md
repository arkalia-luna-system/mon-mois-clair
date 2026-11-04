# Play Console — tests internes (famille)

Checklist **ultra claire** pour Athalia.  
Objectif : canal principal = **Play Internal Testing** (MAJ auto) · sideload = secours.

**Ne crée pas le compte ici** — cette page guide uniquement.  
Package : `be.monmoisclair.app` · AAB local : `dist/MonMoisClair-release.aab`  
Dernier build documenté : **`0.1.0-7.17`** / `versionCode` **123** · **~27 Mo** (lire le journal si plus récent).

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
5. Si tu acceptes une clé Google seule → signature ≠ sideload famille → **désinstall/réinstall** = risque perte Room. Garder `.jks` + `keystore.properties` **hors git**.

- [ ] Enrôlement terminé avec **clé existante** (keystore actuel), pas clé Google seule

### 4. Data safety (aucune collecte)

Dans **Contenu de l’appli → Sécurité des données** :

- [ ] L’app **collecte-t-elle des données ?** → **Non**
- [ ] Pas de partage avec des tiers via l’app
- [ ] Chiffrement en transit : N/A (pas de réseau app) — suivre les libellés du formulaire ; rester honnête : données **uniquement sur l’appareil**
- [ ] S’aligner sur [`PRIVACY.md`](../PRIVACY.md) / page HTTPS ci-dessous

### 5. URL politique de confidentialité (HTTPS obligatoire)

Play demande une **URL HTTPS publique**.

**Fichier prêt :** [`docs/privacy.html`](privacy.html) (miroir de `PRIVACY.md`) — présent sur `main`  
(blob `d33635db3daa4269e253abd22421161c104df5ce` · commit `6c55f25`).

**GitHub Pages — état 2026-11-04 :** **KO (pas live)**  
Tentative `gh api …/pages` (branch `main`, path `/docs`) → **422** :  
*« Your current plan does not support GitHub Pages for this repository. »*  
Cause : dépôt **privé** ; le plan actuel de l’org/user ne permet pas Pages sur repo privé.  
URL cible **non joignable** (curl → **404**) :  
`https://arkalia-luna-system.github.io/mon-mois-clair/privacy.html`

**Pour rendre cette URL live**, Athalia doit choisir **une** option :

1. **Rendre le dépôt public** (Settings → Change visibility) puis réactiver Pages (`main` + `/docs`), **ou**
2. **Upgrader** le plan GitHub (Pro / Team) pour Pages sur repo privé, puis Settings → Pages → Deploy from branch → `main` / `/docs`, **ou**
3. Héberger **uniquement** `privacy.html` ailleurs (Cloudflare Pages, Netlify drop, gist public) et coller **cette** URL dans Play.

- [ ] URL HTTPS **live** (ouvrir en navigation privée sans login GitHub)
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
- [ ] Notes de version courtes (ex. `0.1.0-7.17` — lire le journal)
- [ ] Enregistrer → **Examiner la release** → démarrer le déploiement Internal
- [ ] Onglet **Testeurs** → créer une liste e-mail → ajouter les Gmail de la famille
- [ ] Copier le **lien d’invitation** → l’envoyer (WhatsApp OK pour le *lien*, plus pour l’APK)
- [ ] Chaque personne : ouvrir le lien → accepter d’être testeur → installer via Play Store

### 8. Première install / bascule depuis sideload

| Situation | Que faire |
|-----------|-----------|
| Téléphone **neuf** / pas d’app | Installer via le lien Internal → OK |
| Sideload déjà installé · **même** clé de signature Play | Mise à jour Play possible sans wipe |
| Sideload déjà installé · **clé différente** | 1) Backup `.mmc` + code papier 2) Désinstaller 3) Installer Play 4) Restaurer |

- [ ] Sur **ton** Galaxy : vérifier version dans Réglages = `versionName` du journal
- [ ] Smoke court : Accueil / FAIT / une facture / backup visible

---

## Pièges (à relire)

| Piège | Effet |
|-------|--------|
| Upload APK sideload signé ≠ clé Play App Signing | Impossible de « mettre à jour » ; réinstall |
| Oublier backup `.mmc` avant désinstall | Perte mois / factures / photos |
| Publier en **Production** trop tôt | Hors scope « famille Internal » |
| Compte testeur sans Gmail ajouté à la liste | Lien Play refuse l’install |
| Croire que WhatsApp/Drive reste le canal MAJ | Non — désormais **Play Internal** = canal recommandé |
| Activer le workflow CI `publish-play-internal.yml` sans secrets | Échec ; laisser `workflow_dispatch` uniquement |

---

## Après J+0 (rythme)

1. Sur le Mac : `bundleRelease` → AAB dans `dist/`
2. Play Console → Internal → nouvelle release → upload AAB → déployer
3. Famille : notification Play / ouvrir Play Store → Mettre à jour (auto si activé)

Automatisation CI (optionnelle, **désactivée par défaut**) :  
[`.github/workflows/publish-play-internal.yml`](../.github/workflows/publish-play-internal.yml) — secrets documentés dans le fichier ; ne pas brancher sur `push` main tant que les secrets ne sont pas là.

---

## Liens utiles

- [`DISTRIBUTION_FAMILLE.md`](DISTRIBUTION_FAMILLE.md) — sideload = **secours**
- [`PRIVACY.md`](../PRIVACY.md) — texte source confidentialité
- [`privacy.html`](privacy.html) — page HTTPS pour Play
- [`NOTICE_FAMILLE_2MIN.md`](NOTICE_FAMILLE_2MIN.md) — brief oral code papier
