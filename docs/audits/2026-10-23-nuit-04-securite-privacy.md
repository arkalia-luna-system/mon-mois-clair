# Audit sécurité & privacy — Mon Mois Clair

| | |
|--|--|
| **Date** | 2026-10-23 (nuit-04) |
| **Profil** | Security engineer — apps finance locales Android |
| **Version audité** | `0.1.0-6.75` (`versionCode` 80, branche `develop`) |
| **Périmètre** | Code + manifeste + docs vivants (`PRIVACY.md`, `SECURITY.md`, keystore) + PII docs |
| **Hors périmètre** | Smoke doigt, pentest device rooté, revue Dependabot CVE jar-par-jar |
| **Verdict** | **Pas de P0 runtime bloquant** pour un sideload famille. **Avant GitHub public :** traiter les P0 publication (PII archives + journal) et clarifier le trust premium (backups clair, bio = UI gate). |

---

## 1. Synthèse exécutive

Mon Mois Clair tient un **modèle de menace local-first** cohérent : pas d’`INTERNET`, `allowBackup="false"`, FileProvider non exporté, zip-slip / zip-bomb bornés, presse-papiers sensible (API 33+), biométrie optionnelle + `FLAG_SECURE`, keystore hors git, `PRIVACY.md` / `SECURITY.md` alignés sur le code.

Ce n’est **pas** une app « coffre-fort » : Room en clair, backups SAF JSON+ZIP en clair (choix produit documenté), biométrie **sans** `CryptoObject` / SQLCipher. Pour un carnet familial sideloadé, c’est acceptable **si** l’utilisatrice comprend la frontière (téléphone + dossier Documents). Pour un **trust premium** et un dépôt **public**, les écarts ci-dessous comptent plus que des exploits réseau (il n’y en a pas).

---

## 2. Matrice par surface

### 2.1 Backups clair (SAF / ZIP)

| Point | Constat |
|-------|---------|
| Emplacement | Dossier Documents choisi via SAF (`SafBackupStore`) ; archive partageable `mon-mois-clair-securite.zip` |
| Contenu | JSON schéma Room : montants, libellés, **IBAN**, e-mails créancier, chemins photos, notes — **en clair** (`BackupJsonCodec`) |
| Durcissement restore | `BackupSecurityArchive` : préfixe `receipts/`, rejet `..`, extensions `.webp`/`.pdf`, lecture bornée (anti zip-bomb) — **tests unitaires présents** |
| Cloud Android | `android:allowBackup="false"` — pas de backup auto Google de l’app data |
| Risque résiduel | Synchro Drive / copie USB / partage mail du ZIP = **fuite totale** hors de l’app |

**Trust premium :** le libellé « archive de sécurité » ne doit pas laisser croire à un chiffrement. `PRIVACY.md` dit déjà « en clair » — bon. Renforcer le copy UI / README si claim premium.

### 2.2 Biométrie

| Point | Constat |
|-------|---------|
| Authenticators | `BIOMETRIC_WEAK` **ou** `DEVICE_CREDENTIAL` (`BiometricAuthGatewayImpl`) |
| Crypto | **Aucun** `CryptoObject` / clé Android Keystore liée — verrou **UI** seulement |
| Données | Room `mon_mois_clair.db` non chiffrée ; accessible sur appareil déverrouillé (root / backup OEM / `run-as` debug) |
| Multitâche | `FLAG_SECURE` si biométrie ON (`MainActivity` + `BiometricFlagCache` au 1er frame) |
| Widget | Montants masqués si bio ON (`CarnetWidgetText.compose(locked=…)`) — **bien** |
| Notifications | Libellé + **montant** toujours affichés, bio ou non (`ReminderSchedulerImpl.bodyFor`) — **écart** |

### 2.3 Exports & partage

| Canal | Contenu | Contrôle post-app |
|-------|---------|-------------------|
| ZIP sauvegarde | JSON + photos/PDF reçus | Aucun (autre app / cloud) |
| Résumé texte / PDF mois | Totaux, lignes du mois | Aucun (`ACTION_SEND` + FileProvider `cache/share/`) |
| Mail créancier | Destinataire, montant, référence | Client mail |
| Mail support | Version, modèle appareil, mois affiché → `arkalia.luna.system@gmail.com` | Client mail |
| Photo / courrier VIEW | URI FileProvider `files/receipts/` | Lecteur externe |

FileProvider : `exported="false"`, `grantUriPermissions="true"`, paths limités (`share/`, `camera/`, `receipts/`) — **conforme**.

### 2.4 Presse-papiers

`SensitiveClipboard` : `EXTRA_IS_SENSITIVE` (API 33+), clear après **60 s** (`clearPrimaryClip` API 28+).

Écarts mineurs : fenêtre de lecture par apps presse-papiers avant clear ; clear peut effacer un autre clip collé entre-temps ; API &lt; 28 : pas de clear programmatique. IBAN refusé si checksum KO — **bien**.

### 2.5 Permissions

| Permission | Usage | Note |
|------------|-------|------|
| *(pas d’)* `INTERNET` | — | **Atout privacy n°1** |
| `USE_BIOMETRIC` | Verrou | OK |
| `CAMERA` | Scan QR EPC | `required=false` ; demandée à l’écran scan |
| `POST_NOTIFICATIONS` | Rappels paiement | Runtime API 33+ |
| `USE_EXACT_ALARM` / `SCHEDULE_EXACT_ALARM` | Rappels J−3 / J / retard | Aligné Santé → Alertes |
| `RECEIVE_BOOT_COMPLETED` | Reprogrammation alarmes | Nécessaire |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Santé appareil | Sensible Play / trust — documenter pourquoi |

Pas de `READ_MEDIA_*` / stockage large : photos en `files/receipts/` + SAF — **bon**.

### 2.6 Composants exportés

| Composant | exported | Évaluation |
|-----------|----------|------------|
| `MainActivity` | true | Lanceur uniquement — OK |
| `CarnetWidgetProvider` | true | Requis widgets ; données lues en process app |
| `BootReceiver` | true | Filtre actions système ; `directBootAware` ; debounce `TIME_SET` |
| `ReminderReceiver` | **false** | Actions via PendingIntent `FLAG_IMMUTABLE` — OK |
| FileProvider | **false** | OK |
| InitializationProvider | false (+ WorkManager init retiré) | OK |

Pas de deep link / intent-filter custom dangereux observé.

### 2.7 Logs

- Pas de `Log.d` / `Log.i` / `Log.v` dans `app/src/main`.
- `Log.e` / `Log.w` : tags techniques, parfois `expense.id` (UUID), messages d’échec backup — **pas d’IBAN / montant dans les logs repérés**.
- Release : `isMinifyEnabled = true` (R8).

### 2.8 Keystore & secrets docs

| Contrôle | État |
|----------|------|
| `.gitignore` : `keystore.properties`, `*.jks`, `*.keystore` | OK |
| Index git : pas de `.jks` / `local.properties` | OK (seulement `keystore.properties.example` avec `CHANGE_ME`) |
| `SECURITY.md` + `CONTRIBUTING.md` + README | Alignés |
| `docs/DISTRIBUTION_FAMILLE.md` | Rappelle même keystore / jamais debug |

### 2.9 `PRIVACY.md`

Aligné code : local, pas INTERNET, backups clair, clipboard, bio + `FLAG_SECURE`, contact Arkalia.  
Manques pour trust premium / public : notifications (montants sur lockscreen), widget (montants si bio OFF), Room non chiffrée, e-mail support en clair dans le binaire.

### 2.10 PII docs (Senior / Accords / prénom civil)

| Zone | État |
|------|------|
| Code / strings UI / tests contact | « Athalia » (prénom civil) **exclu** des drafts (`SupportMailComposerTest`, `HowItWorksCopyTest`) — OK 6.75 |
| Docs vivants produit (`CHARTE`, `PRIVACY`, `DISTRIBUTION`, README) | Personas anonymisés / Arkalia — OK |
| `PROGRESS_LOG.md` (vivant, historique) | Mentions **Senior / Accords / prénom civil** encore présentes (entrées antérieures) |
| `docs/archive/*` | **Concentré** : `AUDIT_ALEX_MAMAN_*`, `FEEDBACK_TEST_FAMILLE_*`, `RECHERCHE_OUTILS_*`, checklist « S25 Ultra de test », etc. |
| Liens vivants → archive | `docs/AUDIT_STATUS.md`, `docs/PLAN_PRODUIT_FINI.md` pointent encore vers `AUDIT_ALEX_MAMAN_…` |

Pour un dépôt **privé / famille** : acceptable (archives figées). Pour **GitHub public** : risque réputationnel + vie privée des proches (prénoms, modèles, usages dettes / huissier).

---

## 3. Findings — P0 / P1

### P0 — bloquants avant GitHub **public** (privacy / confiance)

| ID | Finding | Preuve | Remède recommandé |
|----|---------|--------|-------------------|
| **P0-DOC-1** | Archives + journal portent des **noms / contextes familiaux** (Senior, Accords, prénom civil) + détails device | `docs/archive/AUDIT_ALEX_MAMAN_2026-10-21.md`, `FEEDBACK_TEST_FAMILLE_*`, `PROGRESS_LOG.md` historique | Avant public : scrub / remplacer par personas (Senior / Accords) **ou** exclure `docs/archive/` + historiser le journal ; ne pas laisser le filename `AUDIT_ALEX_MAMAN` indexé |
| **P0-DOC-2** | Claim implicite « archive de **sécurité** » vs ZIP **non chiffré** (IBAN + photos) | `BackupSecurityArchive.SHARE_ZIP_NAME`, `PRIVACY.md` | Garder « en clair » partout (UI partage, README) ; éventuellement renommer copy « copie de secours » ; option future : chiffrement passphrase (hors scope 6.75) |

*Aucun P0 d’exploitation réseau / composant exporté critique trouvé dans le code auditée.*

### P1 — trust premium / durcissement

| ID | Finding | Preuve | Remède |
|----|---------|--------|--------|
| **P1-BIO-1** | Bio = gate UI, pas chiffrement au repos | Pas de SQLCipher / `EncryptedFile` / `CryptoObject` | Documenter explicitement dans `PRIVACY.md` / `SECURITY.md` ; option roadmap « verrou fort » |
| **P1-NOTIF-1** | Rappels affichent **libellé + montant** même si bio ON | `ReminderSchedulerImpl.bodyFor` | Si bio ON : titre générique (« Une échéance ») / masquer montant ; ou canal « sensible » |
| **P1-CLIP-1** | Clear presse-papiers 60 s ; pas de clear API &lt; 28 ; clear non sélectif | `SensitiveClipboard` | Documenter ; option clear à `onPause` après copie |
| **P1-PRIV-1** | `PRIVACY.md` silencieux sur notifs / widget / Room clair | `PRIVACY.md` | Ajouter 3 lignes honnêtes |
| **P1-PERM-1** | `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Manifest | Justifier dans PRIVACY / Aide Santé ; éviter demande agressive hors parcours Alertes |
| **P1-MAIL-1** | Adresse Gmail support en dur dans l’APK + tests | `SupportMailComposer.SUPPORT_EMAIL` | Acceptable si public assumé ; sinon alias / formulaire hors app (mais pas d’INTERNET) |
| **P1-LOG-1** | `Log.e` conserve IDs / messages d’échec en release | plusieurs `Log.e` | Strip / no-op release si claim premium strict |
| **P1-BOOT-1** | `BootReceiver` exporté + `LOCKED_BOOT_COMPLETED` | Manifest | OK fonctionnel ; revalider que rien de sensible n’est touché en direct boot (aujourd’hui : reconcile alarmes seulement) |

---

## 4. Ce qui est déjà solide (ne pas casser)

1. **Zéro `INTERNET`** — frontière réseau absente.
2. **`allowBackup="false"`** — pas d’extraction cloud Android de la sandbox.
3. **Zip-slip / bombe** traités + tests (`BackupSecurityArchiveTest`).
4. **FileProvider** non exporté, chemins bornés ; intents partage avec `ClipData` URI.
5. **PendingIntent `FLAG_IMMUTABLE`** partout (alarmes, notifs, widget).
6. **`ReminderReceiver` non exporté**.
7. **Widget respecte le verrou** (pas de montant si bio).
8. **EpcScanHandoff** évite IBAN dans `SavedStateHandle`.
9. **Keystore** hors dépôt ; example sans secret.
10. **Docs 6.75** : identity Arkalia, `PRIVACY.md` créé, personas CHARTE anonymisés.

---

## 5. Checklist « avant public GitHub »

- [ ] Scrub / gated `docs/archive/**` contenant Senior·Accords·prénom civil (ou repo privé archive).
- [ ] Passer `PROGRESS_LOG.md` (ou tronquer l’historique public) pour les mêmes noms.
- [ ] Relire filenames / liens `AUDIT_ALEX_MAMAN_*` depuis docs vivants.
- [ ] `PRIVACY.md` : notifs, widget, Room clair, partage = hors périmètre app.
- [ ] Copy UI « archive de sécurité » → ne pas sur-promettre le chiffrement.
- [ ] Décider : notifs sans montant si bio ON (**P1-NOTIF-1**).
- [ ] Confirmer Security Advisory path (`SECURITY.md`) une fois le repo public.
- [ ] Pas de APK / dumps `dist/verify` / captures dans le push (déjà gitignorés — vérifier le remote).
- [ ] Dependabot : suivre `docs/CI.md` (bruit protobuf/netty ≠ fail app).

---

## 6. Score trust (honnête)

| Critère | Note | Commentaire |
|---------|------|-------------|
| Confidentialité réseau | **9,5/10** | Pas d’INTERNET |
| Surface attaque IPC | **8,5/10** | Exportés limités et justifiés |
| Données au repos | **5/10** | Room + SAF clair ; bio UI-only |
| Exports utilisateur | **6/10** | Choix produit clair, risque social élevé |
| Docs privacy publics | **7/10** | `PRIVACY`/`SECURITY` bons ; archives PII à traiter |
| Prêt GitHub **privé** | **Oui** | |
| Prêt GitHub **public** | **Non** tant que P0-DOC-* ouverts | |
| Claim « premium sécurité » | **Non** sans P1-BIO/NOTIF + smoke | |

---

## 7. Méthode

Relecture statique : `AndroidManifest.xml`, biometric / clipboard / backup / widget / notifications / FileProvider / `DatabaseModule`, `PRIVACY.md`, `SECURITY.md`, `.gitignore`, `keystore.properties.example`, grep PII `Senior|Accords|prénom` (vivants vs `docs/archive`), absence de secrets dans l’index git.

**Audit only** — aucun correctif code dans cette passe.
)
