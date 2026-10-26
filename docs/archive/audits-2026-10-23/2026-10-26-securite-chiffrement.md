> **ARCHIVÉ / FIGÉ** (2026-10-26) — ne plus lire pour l’état code. Vérité = `PROGRESS_LOG.md` + PRIVACY/SECURITY + audits `final-0*`.

# Audit chiffrement au repos — Mon Mois Clair

| | |
|--|--|
| **Date** | 2026-10-26 |
| **Branche / version** | `develop` · `0.1.0-6.80` (`versionCode` 85) |
| **Périmètre** | Room, backups SAF / ZIP partage, pièces jointes, biométrie, presse-papiers, widget, notifications, claims `PRIVACY.md` / `SECURITY.md` |
| **Hors périmètre** | Pentest root, CVE Dependabot jar-par-jar, chiffrement réseau (pas d’`INTERNET`) |
| **Code livré dans cette passe** | **Aucun** — plan seulement (voir §8). Un rewrite SQLCipher / ZIP chiffré en une passe serait fragile pour maman 60+ et les 318 tests. |
| **Contact** | Accueil → Menu → « Un souci ? Écrire à Arkalia Système » |

**Verdict court :** l’utilisatrice a raison. Room + backups SAF + ZIP WhatsApp sont **en clair**. La biométrie est une **porte UI** (pas de `CryptoObject` / Keystore lié aux données). WhatsApp (et tout `ACTION_SEND`) ne chiffre **pas** le fichier pour le destinataire : le ZIP reste lisible hors de l’app. `PRIVACY.md` / `SECURITY.md` sont **déjà honnêtes** sur ce point — pas de sur-promesse docs vs code.

Complète (sans la remplacer) l’audit privacy [`2026-10-23-nuit-04-securite-privacy.md`](2026-10-23-nuit-04-securite-privacy.md) en se concentrant sur **chiffrement au repos** et un **plan priorisé**.

---

## 1. État actuel (tableau)

| Surface | Emplacement / code | En clair ? | Protégé ? | Notes |
|---------|-------------------|------------|-----------|-------|
| **Room SQLite** | `mon_mois_clair.db` via `DatabaseModule` (`Room.databaseBuilder`, **pas** de `SupportFactory` / SQLCipher) | **Oui** | Sandbox app + `allowBackup="false"` seulement | Montants, libellés, IBAN, e-mails créancier, notes `+++`, chemins photos |
| **SQLCipher** | — | N/A | **Absent** (grep deps / code : 0 hit) | — |
| **EncryptedSharedPreferences / EncryptedFile** | — | N/A | **Absent** (`security-crypto` non déclaré) | Prefs actuelles = miroirs UI non secrets |
| **SharedPreferences** | `biometric_flag_cache`, `theme_preference_cache`, debounce boot | Booléen / thème | Non chiffré ; **faible enjeu** (pas d’IBAN) | Source de vérité bio = Room `app_meta` |
| **Backup JSON SAF** | Documents (URI tree) · `SafBackupStore` / `BackupJsonCodec` · rotation `backup-1/2/3.json` | **Oui** | Choix dossier utilisateur ; sync Drive/OEM possible hors app | Schéma Room complet + checksum SHA-256 (intégrité, **pas** confidentialité) |
| **Archive ZIP partage** | `mon-mois-clair-securite.zip` · `ShareBackupUseCase` + `BackupSecurityArchive` | **Oui** | Anti zip-slip / zip-bomb à la **restore** seulement | JSON + `receipts/*.webp|*.pdf` ; WhatsApp / mail / Drive = hors contrôle |
| **Photos / PDF** | `files/receipts/` (+ copie SAF Documents) · `ReceiptPhotoStore` | **Oui** | FileProvider `exported=false`, paths limités | Double copie locale + Documents |
| **Biométrie** | `BiometricAuthGatewayImpl` · `BIOMETRIC_WEAK` \| `DEVICE_CREDENTIAL` | N/A | **UI gate** + `FLAG_SECURE` si ON | **Pas** de `authenticate(CryptoObject)` ; DB reste lisible sur téléphone déverrouillé |
| **Presse-papiers** | `SensitiveClipboard` | Texte IBAN / comm. | Marqué sensible (API 33+) ; clear ~60 s (API 28+) | Fenêtre de lecture avant clear |
| **Widget** | `CarnetWidgetRefresher` / `CarnetWidgetText` | Reste + prochaine ligne | **Masqué si bio ON** | Si bio OFF = carnet ouvert sur le bureau |
| **Notifications** | `ReminderSchedulerImpl.bodyFor` → `PaymentNotificationHelper` | Libellé + **montant** | **Toujours visibles** (bio ou non) | Action FAIT sans écran complet |
| **Cloud Android** | Manifeste `allowBackup="false"` | — | Pas de backup auto Google de l’app data | Ne protège pas Documents SAF |
| **Réseau** | Pas d’`INTERNET` | — | Atout n°1 privacy | Partage = autre app |

---

## 2. Claims docs vs réalité

| Claim (`PRIVACY.md` / `SECURITY.md`) | Réalité code (6.80) | Écart |
|--------------------------------------|---------------------|-------|
| Room en clair, pas SQLCipher | `DatabaseModule` : builder plain SQLite | **Aucun** — aligné |
| Backups SAF en clair (choix produit) | JSON + ZIP sans passphrase | **Aucun** |
| Bio = porte UI, pas chiffrement | Pas de `CryptoObject` ; succès → callback UI | **Aucun** |
| Widget masqué si bio ON | `locked = BIOMETRIC_ENABLED` → montant `GONE` | **Aucun** |
| Notifs : montant même si bio ON | `bodyFor` inclut `EuroFormat.formatCents` | **Aucun** |
| Clipboard sensible + clear 1 min | `EXTRA_IS_SENSITIVE` + `clearPrimaryClip` | **Aucun** |
| Nom ZIP « sécurité » | `SHARE_ZIP_NAME = mon-mois-clair-securite.zip` ; copy UI « copie de sécurité » | **Cosmétique** : le mot « sécurité » peut laisser croire à un chiffrement — déjà signalé nuit-04 (P0-DOC-2) ; docs disent « en clair » |

**Conclusion docs :** pas de mensonge. Le risque est **compréhension utilisatrice** (surtout partage WhatsApp), pas un claim faux dans les MD vivants.

---

## 3. Risques concrets — usage famille

Scénario cible : sideload familial, maman 60+, téléphone Samsung, parfois aide d’un proche, parfois envoi d’une « copie de sécurité » par WhatsApp.

| Scénario | Ce qui fuit | Gravité famille |
|----------|-------------|-----------------|
| **Téléphone déverrouillé** (prêté, oublié sur table, enfant) | UI ouverte **ou** Room/photos lisibles via outils debug / root / backup OEM si bio OFF ou contournée au niveau OS | **Élevée** si bio OFF ; **moyenne** si bio ON (UI bloquée, données au repos encore en clair) |
| **ZIP envoyé WhatsApp / mail / Drive** | Tout le carnet + photos/PDF + IBAN — destinataire **et** toute synchro cloud du téléphone | **Critique** — WhatsApp ne chiffre pas le *fichier* pour le destinataire ; E2E chat ≠ fichier illisible |
| **Dossier Documents SAF** synchronisé (Drive, OneDrive, câble PC) | Mêmes JSON/photos en clair hors sandbox | **Élevée** |
| **Restore d’un ZIP reçu** | Si le ZIP vient d’un canal compromis : données injectées (intégrité checksum OK ≠ provenance sûre) ; zip-slip déjà borné | **Moyenne** (intégrité oui, authenticité non) |
| **Écran verrouillé, notif visible** | Libellé + montant sur lockscreen | **Faible à moyenne** (habitude Android finance) |
| **Widget bio OFF** | Reste du mois sur l’accueil | **Faible** (attendu produit) |

Ce n’est **pas** un risque serveur / API (il n’y en a pas). La frontière de confiance actuelle = **téléphone + dossier Documents + apps de partage**.

---

## 4. Options techniques réalistes (Android offline)

Contraintes charte : **100 % local**, pas de compte cloud, pas de sync serveur, UX maman 60+, sideload.

| Option | Principe | Protège | Ne protège pas | Effort / risques |
|--------|----------|---------|----------------|------------------|
| **A. SQLCipher (Room + `SupportFactory`)** | Passphrase ou clé Keystore ; DB chiffrée au repos | Fichier `.db` (vol téléphone éteint / dump sandbox) | ZIP déjà partagé ; notifs ; widget si bio OFF | **Élevé** : migration DB existante, unlock avant open, tests Room, perte clé = perte données |
| **B. AES-GCM fichier + Android Keystore** | Clé dans Keystore (`setUserAuthenticationRequired` lié bio/PIN **appareil**) ; enveloppe DB ou fichiers | Idem A si bien branché | Idem ; perte biométrie appareil peut bloquer la clé selon flags | Moyen–élevé ; UX liée au **code écran**, pas à un PIN app |
| **C. PIN / code app + dérivation (Argon2/scrypt) → clé** | Secret connu de maman ; Keystore wrap optionnel | DB + backups si même clé | Oubli PIN = recovery difficile **sans** cloud | Moyen ; UX critique 60+ (récupération locale : indices ? 2ᵉ appareil ?) |
| **D. EncryptedFile / AES-GCM sur pièces jointes** | `files/receipts/` chiffrés | Photos/PDF locaux | JSON backup tant que non chiffré | Moyen ; double écriture SAF complique |
| **E. ZIP / JSON backup chiffrés (passphrase)** | Archive `*.mmc` ou ZIP+AES ; restore demande le code | Fuite WhatsApp / Drive **si** passphrase non collée à côté | DB locale si non chiffrée ; mauvaise UX si code oublié | Moyen ; **meilleur ROI anti-WhatsApp** |
| **F. EncryptedSharedPreferences seul** | Prefs Jetpack Security | Secrets futurs (wrap clé) | Room / ZIP / photos **aujourd’hui** (prefs = thème / flag bio) | Faible code, **gain sécurité quasi nul** en l’état |
| **G. Statu quo + copy UX / rename ZIP** | Clarifier « en clair » partout | Compréhension | Aucune donnée | **Minimal** — déjà largement fait dans PRIVACY |

**Non-objectifs (charte) :** compte cloud de recovery, sync E2E serveur Mon Mois Clair, attestation Play Integrity obligatoire.

---

## 5. Recommandation priorisée (Mon Mois Clair)

Ordre proposé pour **maximiser la protection famille** sans casser le modèle local-first ni exiger un compte :

1. **P0 — Room SQLCipher (ou équivalent Keystore)**  
   Clé : **Android Keystore** avec authentification utilisateur (bio **ou** code PIN **de l’appareil** — déjà le modèle `BiometricPrompt` actuel), **pas** un second compte. Au premier lancement post-maj : migration one-shot clair → chiffré, puis suppression sécurisée de l’ancien fichier.  
   *Pourquoi en premier :* le carnet vit surtout sur le téléphone ; c’est la base de confiance « téléphone perdu / prêté ».

2. **P1 — Backups / ZIP chiffrés (passphrase ou même clé dérivée)**  
   Objectif explicite : **un ZIP WhatsApp sans le code = illisible**. UX : « Code de copie » défini une fois (long, affichable en gros) ; avertissement avant partage ; restore demande le code. Conserver option « copie non chiffrée » **déconseillée** seulement si on veut compat descendante — sinon tout chiffrer après cutover.  
   *Pourquoi ensuite :* risque concret déjà vécu (partage famille) ; indépendant du cloud.

3. **P2 — Pièces jointes `EncryptedFile` / même enveloppe que la DB**  
   Aligner `files/receipts/` et copies SAF sur la même politique. Éviter photos en clair à côté d’une DB chiffrée.

4. **Transversal UX (dès P0)**  
   - Écran Réglages : « Verrou & confidentialité » — distinguer clairement **Verrou écran (masquer l’app)** vs **Chiffrement du carnet** (quand P0 livré).  
   - Avant partage ZIP : phrase fixe du type « Sans code, cette copie se lit comme un carnet ouvert ».  
   - Renommer progressivement `mon-mois-clair-securite.zip` → `mon-mois-clair-copie.zip` (ou suffixe `.mmc`).

5. **Ne pas faire en premier**  
   EncryptedSharedPreferences « pour la forme » (F) sans stocker la clé DB. Bio `CryptoObject` **seul** sans chiffrer Room (illusion de coffre). Gros rewrite one-shot DB+ZIP+photos.

**Alignement charte :** reste offline ; contact Arkalia OK pour support ; pas de serveur de recovery — donc **écrire noir sur blanc** : « code oublié + téléphone mort sans copie = données perdues ».

---

## 6. Plan d’implémentation par étapes

### P0 — Base Room chiffrée (~4–6 jours utiles)

| Étape | Contenu |
|-------|---------|
| P0.1 | Dépendance SQLCipher (ou `SQLite` chiffré maintenu) + factory Room ; feature flag / versionName étape dédiée |
| P0.2 | Provisionnement clé : Keystore AES + unlock via `BiometricPrompt` **avec** `CryptoObject` (passage de UI-gate → crypto-gate) **ou** dérivation PIN app si on refuse de lier au code écran |
| P0.3 | Migration : détecter DB claire → exporter tables / `ATTACH` / réécrire fichier chiffré → delete ancien ; tests sur DB fixture schéma 8 |
| P0.4 | UX : écran « Activer la protection du carnet » (opt-in ou obligatoire à la maj — **décision produit**) ; cold start = prompt avant 1ʳᵉ query |
| P0.5 | Tests : unitaires clé/migration ; garder les 318+ verts ; pas de fuite passphrase dans logs |
| P0.6 | Docs : `PRIVACY.md` / `SECURITY.md` / copy Réglages mis à jour **après** tests verts |

**Risque majeur :** utilisatrice qui refuse bio/code après maj → app inutilisable si chiffrement obligatoire. Prévoir mode « rester en clair » **temporaire** documenté, ou chiffrement obligatoire seulement pour nouvelles installs.

### P1 — Backups / ZIP (~3–5 jours)

| Étape | Contenu |
|-------|---------|
| P1.1 | Format versionné (header magic + salt + AES-GCM) ; JSON + receipts dans enveloppe unique |
| P1.2 | Définir / confirmer « code de copie » (lié à la clé P0 ou indépendant — **préférer une seule notion** pour maman) |
| P1.3 | `ShareBackupUseCase` : toujours chiffrer ; copy UI avant chooser |
| P1.4 | Restore : demander code ; échec = phrase fixe (pas de jargon crypto) ; compat lecture anciennes copies **claires** (une version de grâce) |
| P1.5 | Tests zip bornés existants + vecteurs chiffrement / mauvais mot de passe |

### P2 — Pièces jointes (~2–4 jours)

| Étape | Contenu |
|-------|---------|
| P2.1 | Écriture `EncryptedFile` (ou blob dans DB chiffrée — plus simple restore, DB plus grosse) |
| P2.2 | Migration fichiers `receipts/` existants |
| P2.3 | VIEW / FileProvider : déchiffrement temporaire en cache chiffré ou stream — attention fuites cache `share/` |

### UX transverse (parallèle, ~1–2 jours)

- Glossaire utilisateur : Verrou / Protection / Copie chiffrée.  
- Checklist restore : code + dossier Documents + photos.  
- Widget / notifs : inchangés fonctionnellement ; documenter le résiduel (§7).

---

## 7. Ce qui reste exposé **même après** chiffrement P0–P2

| Surface | Pourquoi |
|---------|----------|
| **Notifications** | Le système affiche titre/corps en clair ; pas de chiffrement OS des textes de notif. Mitigation possible : masquer le montant si bio/chiffrement ON (choix produit vs utilité « combien payer »). |
| **Widget** | RemoteViews hors process app ; si on affiche le Reste, il est visible. Mitigation actuelle (masquer si bio) reste pertinente. |
| **Écran allumé après unlock** | Données déchiffrées en RAM / UI — normal. |
| **Partage volontaire déchiffré** | Si on offre « envoyer sans code », le risque WhatsApp revient. |
| **Appareil root / malware avec droits élevés** | Keystore + SQLCipher ralentissent ; ne sont pas une preuve formelle contre un attaquant puissant sur device compromis. |
| **OCR / presse-papiers** | Fenêtre temporelle après copie IBAN. |
| **Aperçu multitâche** | Déjà `FLAG_SECURE` si verrou ON. |

---

## 8. Décision d’implémentation (cette passe)

| Candidat « premier pas » | Pourquoi **pas** maintenant |
|--------------------------|-----------------------------|
| EncryptedSharedPreferences | Aucun secret métier en prefs ; gain ≈ 0 vs menace ZIP/Room |
| SQLCipher one-shot | Migration + unlock + tests Room = rewrite fragile ; risque casse 318 tests / données maman |
| ZIP chiffré seul | Utile, mais sans clé/UX code déjà en place → improvisation PIN + restore = dette |

**Livré :** ce document uniquement.  
**Pas encore codé** : ni SQLCipher, ni `EncryptedFile`, ni ZIP chiffré, ni bump version, ni push.

Prochain commit code conseillé (quand l’utilisatrice le demande) : **spike P0.1–P0.2 sur branche dédiée ou étape journal** avec tests de migration sur fixture, **sans** activer par défaut sur `develop` tant que le parcours maman n’est pas validé.

---

## 9. Effort estimé (jours calendaires agent + revue)

| Lot | Jours (ordre de grandeur) | Dépendances |
|-----|---------------------------|-------------|
| P0 DB + Keystore/CryptoObject + migration + UX unlock | **4–6** | Décision : opt-in vs obligatoire |
| P1 backups/ZIP chiffrés + restore + copy | **3–5** | Idéalement même secret que P0 |
| P2 pièces jointes | **2–4** | P0 (même clé) |
| Copy UX / rename ZIP / PRIVACY (sans crypto) | **0,5–1** | Peut précéder le code |
| **Total coffre utile famille** | **≈ 10–16 jours** | Hors Dependabot / polish non liés |
| Smoke device (S26 bio, restore ZIP, partage) | **+1–2** | Matériel réel |

---

## 10. Références code (constat 6.80)

- DB : `app/src/main/java/be/monmoisclair/app/di/DatabaseModule.kt` — `"mon_mois_clair.db"`, migrations 1→8, pas de factory chiffrée.  
- Bio : `…/data/biometric/BiometricAuthGatewayImpl.kt` — pas de `CryptoObject`.  
- ZIP : `…/data/backup/BackupSecurityArchive.kt` (`SHARE_ZIP_NAME`), `…/domain/usecase/ShareBackupUseCase.kt`.  
- Photos : `…/data/receipt/ReceiptPhotoStore.kt` — `files/receipts/` + SAF.  
- Widget : `…/data/widget/CarnetWidgetRefresher.kt`.  
- Notifs : `…/data/reminder/ReminderSchedulerImpl.kt` (`bodyFor`).  
- Docs vivants : `PRIVACY.md`, `SECURITY.md` (alignés).

---

*Fin de l’audit — 2026-10-26. Implémentation crypto : en attente de go explicite sur P0 (pas de rewrite forcé).*
