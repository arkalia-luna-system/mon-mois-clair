# Sécurité

## Signaler une vulnérabilité

Ouvre une **issue privée** (GitHub Security Advisory) ou contacte la mainteneuse du dépôt `arkalia-luna-system/mon-mois-clair`.

Ne publie pas de preuves d’exploitation dans une issue publique.

## Modèle de menace (volontaire)

Mon Mois Clair est une app **locale, hors ligne**, destinée à un usage familial sideloadé :

- Pas de serveur, pas d’analytics, pas d’API tierce.
- **Room** : SQLCipher ; passphrase aléatoire enveloppée Android Keystore (`mmc_db_wrap_v1`). Migration one-shot des DB claires pré-6.81. Si migration **Failed** → ouverture claire temporaire + marqueur + bannière Réglages (pas silencieux).
- **Wrap sans bio** (`mmc_db_wrap_v1`) : **volontaire** — widget et alarmes doivent ouvrir Room hors porte UI ; le verrou bio (`mmc_unlock_v1`) ne wrappe pas la passphrase SQLCipher.
- **Backups SAF / copie WhatsApp** : enveloppe AES-GCM (`MMC1`) dérivée du **code de copie** (8 chiffres, Keystore local). Sans ce code, un fichier partagé est **illisible**. Texte d’envoi rappelle de **ne pas** coller le code dans le même message. Anciennes copies ZIP/JSON claires : **refusées** (`CleartextLegacyGrace.OPT_IN_AVAILABLE = false` depuis 6.99) — seul chemin = `.mmc` / `MMC1B64`.
- **Photos / PDF reçus** : fichiers `receipts/` et copies SAF enveloppés (`MRF1`) avec la même clé wrap Keystore. Cache `view/` TTL **2 min** + **wipe dès ON_STOP** (arrière-plan), pas seulement au verrou.
- **PDF résumé mois** : **clair** volontairement (partage/impression) ; uniquement `cache/share/` éphémère (TTL **2 min** : purge périmés à l’arrière-plan / à l’écriture — **pas** de wipe total ON_STOP, pour laisser le FileProvider servir le sheet système) ; watermark + note d’envoi « pas une copie `.mmc` ».
- Biométrie optionnelle = porte UI + **CryptoObject** (clé `mmc_unlock_v1`, AUTH_BIOMETRIC_STRONG \| DEVICE_CREDENTIAL) quand disponible ; sinon fallback porte UI (DB déjà chiffrée).
- Widget / notifs : montants **et** libellés masqués si biométrie ON.
- Keystore de signature release : **jamais** dans git (`*.jks`, `keystore.properties`).
- Pas de permission `INTERNET` / `ACCESS_NETWORK_STATE` (strip merge libs). `android:allowBackup="false"`. Release minifiée.
- `FLAG_SECURE` seulement quand le verrouillage est activé. Sans verrou, l’aperçu des tâches peut montrer le mois.
- IBAN et communication : presse-papiers marqué sensible (Android 13+) et vidé après **~30 s** (+ wipe immédiat ON_STOP ; API 26–27 = clip vide). Copie IBAN seulement si le contrôle international passe.
- Exportés : activité lanceur, `BootReceiver`, `UserUnlockedReceiver` (alarmes après unlock). FileProvider et le receiver d’alarme ne le sont pas.
- **Direct Boot :** pas `directBootAware` ; DB/vault en CE — écran d’attente si ouverture avant unlock · détail [`docs/DIRECT_BOOT.md`](docs/DIRECT_BOOT.md).
- Le partage quitte l’app : le **fichier** `.mmc` reste chiffré ; le texte d’accompagnement ne contient **pas** le code.
- `androidTest` smoke (package / PDF wipe / cleartext reject) assemblés ; connected device = Athalia (pas Compose UI). Le lint en CI ne bloque pas. Pas de scan CVE en garde.
- Ne pas recopier le numéro de série d’un téléphone dans un markdown.

**Limites restantes :** données déchiffrées en RAM après unlock ; widget/notifs hors process app ; cache `view/` (clair, TTL court) pour ouvrir un PDF système ; PDF résumé clair **pendant** le partage (éphémère) ; root / malware élevé ; pas de recovery cloud si code + téléphone perdus. Grâce cleartext close ≠ coffre bancaire.

## Dependabot Security Updates (bruit rouge)

Les runs GitHub nommés `gradle in /. for protobuf… / netty…` viennent des
**Security Updates** Dependabot (jars transitifs AGP), **pas** de `.github/workflows/ci.yml`.

- Voir la procédure dans [`docs/CI.md`](docs/CI.md) § Phase 3 (Dismiss / désactiver Security Updates).
- Ne pas ajouter de `implementation` netty/protobuf « pour faire plaisir » au graphe app.
- Forces Dependabot locales (protobuf 3.25.9, commons-io 2.22.0, netty 4.1.139, etc.) : **ne pas casser** en ajoutant SQLCipher.

## Secrets / fichiers interdits dans le dépôt

- `local.properties`
- `keystore.properties`, `*.jks`, `*.keystore`
- APK / AAB binaires
- Dumps device (`dist/verify/**` hors `RESET_TESTS_FROM_ZERO.md` + `README.md`)
