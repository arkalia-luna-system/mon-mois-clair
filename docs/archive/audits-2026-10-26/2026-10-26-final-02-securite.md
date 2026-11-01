> **ARCHIVÉ** 2026-11-01 — historique figé. Vivant = `docs/audits/2026-11-01-premium-*` + journal.

# Audit final sécurité — Mon Mois Clair (post-crypto 6.81 / polish 6.82)

| | |
|--|--|
| **Date** | 2026-10-26 |
| **Branche / version** | `develop` · `0.1.0-6.82` (`versionCode` 87) |
| **Périmètre** | Crypto réelle vs claims `PRIVACY.md` / `SECURITY.md` ; trous restore / ZIP·MMC / photos / bio / notifs / widget ; checklist avant distribution famille |
| **Hors périmètre** | Pentest root, rewrite code, CVE Dependabot jar-par-jar, Play Store |
| **Code livré dans cette passe** | **Aucun** — audit only |
| **Prédécesseurs** | [`2026-10-23-nuit-04-securite-privacy.md`](2026-10-23-nuit-04-securite-privacy.md) (pré-crypto) · [`2026-10-26-securite-chiffrement.md`](2026-10-26-securite-chiffrement.md) (plan à `0.1.0-6.80`, Room encore clair) |

**Verdict court :** depuis **6.81**, les claims crypto de `PRIVACY.md` / `SECURITY.md` sont **globalement alignés** avec le code (SQLCipher + Keystore, enveloppe `MMC1` / code de copie, photos `MRF1`, bio `CryptoObject` quand strong dispo, montants widget/notifs masqués si bio ON). Ce n’est **toujours pas** un coffre parfait — et les docs le disent. Les trous restants sont surtout **UX / grace / surfaces hors app** (PDF résumé, cache `view/`, code collé à côté du `.mmc`, migration Failed → DB claire temporaire, libellé notif visible). **Pour une distribution famille sideload**, le modèle est **acceptable** après smoke doigt code-de-copie + restore, sans promesse « banque / vault ».

---

## 1. Synthèse exécutive

| Avant (≤ 6.80) | Après (6.81 → 6.82) |
|----------------|---------------------|
| Room SQLite en clair | SQLCipher (`net.zetetic:sqlcipher-android:4.6.1` + `SupportOpenHelperFactory`) |
| ZIP WhatsApp lisible | `.mmc` = ZIP emballé AES-GCM (`MMC1`) ; texte d’accompagnement **sans** le code |
| SAF JSON en clair | `MMC1B64:` + PBKDF2 (120 000) du code 8 chiffres |
| Photos / PDF en clair | `MRF1` + clé wrap Keystore ; SAF aussi scellé |
| Bio = porte UI seule | Porte UI + `CryptoObject` (`mmc_unlock_v1`) si STRONG / credential OK ; sinon fallback UI (DB déjà chiffrée) |
| Notifs : montant toujours visible | Montant → « Montant masqué » si bio ON (aligné widget) |

**Honnêteté docs :** `PRIVACY.md` § « Honnêteté » + `SECURITY.md` § « Limites restantes » restent justes. Pas de sur-promesse détectée sur le chiffrement au repos **des écritures neuves**.

**Écart vs l’audit plan du matin (6.80) :** cet audit **remplace** le constat « Room + ZIP en clair » pour l’état **6.82**. Le plan P0/P1/P2 de `2026-10-26-securite-chiffrement.md` est **livré** ; cet audit final mesure les **résidus**.

---

## 2. Claims PRIVACY / SECURITY vs code

| Claim | Preuve code (repères) | Écart |
|-------|----------------------|-------|
| Pas d’`INTERNET` | `AndroidManifest.xml` : aucune permission réseau | **Aucun** |
| `allowBackup="false"` | Manifeste application | **Aucun** |
| Room chiffré SQLCipher ; clé enveloppée Keystore | `DatabaseModule` + `DbPassphraseVault` (`mmc_db_wrap_v1`) + `EncryptedDbMigrator` | **Aucun** sur device après migration OK ; **nuance** tests JVM / fallback Failed (§3.1) |
| Backups SAF enveloppe AES-GCM + code | `SafBackupStore.writeRotatedBackup` → `BackupEnvelope.sealToUtf8` | **Aucun** (écritures neuves) |
| Partage `.mmc` illisible sans code ; texte sans code | `ShareBackupUseCase` + `ShareFileIntents.encryptedCopy` | **Aucun** |
| Anciennes copies claires restaurables (grace) | `BackupRestorer.openIncomingBytes` / `decodeMaybeSealedUtf8` → plaintext si pas MMC | **Aucun** — **trou volontaire** documenté |
| Photos `files/receipts/` MRF1 ; aperçu = cache | `LocalFileCipher` + `ReceiptPhotoStore.viewUri` (`cache/view/`, purge > 1 h) | **Aucun** ; résidu cache clair (§3.3) |
| Bio : porte UI + CryptoObject si possible | `BiometricAuthGatewayImpl` (`preferCrypto`, fallback plain) | **Aucun** ; CryptoObject **ne déverrouille pas** la passphrase SQLCipher (§3.4) |
| Base chiffrée même sans verrou UI | Clé wrap **sans** `userAuthenticationRequired` → process ouvre Room pour widget/rappels | **Aligné** (choix produit) |
| Widget masqué si bio ON | `CarnetWidgetText.compose(locked=…)` | **Aucun** |
| Notifs : montant masqué si bio ON | `ReminderSchedulerImpl.bodyFor` + `BIOMETRIC_ENABLED` | **Aucun** ; **libellé** toujours dans le titre |
| `FLAG_SECURE` si verrou ON | `MainActivity` + `BiometricFlagCache` | **Aucun** |
| Clipboard sensible + clear ~1 min | `SensitiveClipboard` | **Aucun** |
| FileProvider non exporté | Manifeste + `file_paths.xml` (share / view / camera / receipts) | **Aucun** structurel |
| « Pas un coffre parfait » / root / RAM | Docs + réalité (données en clair après open) | **Aucun** |

**Conclusion claims :** docs vivants **honnêtes** pour 6.82. Risque principal = **compréhension famille** (code de copie, grace claires, PDF résumé hors enveloppe), pas un mensonge MD ↔ code.

---

## 3. Surfaces à risque — trous résiduels

### 3.1 Room / SQLCipher

| Point | Constat | Gravité famille |
|-------|---------|-----------------|
| Chiffrement au repos | Passphrase 32 o aléatoire, hex → factory ; wrap AES-GCM Keystore | **Faible** si migration OK |
| Migration Failed | `DatabaseModule` : ouverture **claire** temporaire si export échoue et pas de marqueur | **Moyenne** (rare ; données conservées mais non chiffrées) |
| Tests JVM | `loadLibrary("sqlcipher")` échoue → Room clair (Robolectric) | N/A produit |
| Process app vivant | Unwrap wrap key **sans** bio → DB utilisable pour widget / alarmes | **Attendu** ; vol téléphone **déverrouillé** + malware process = toujours dangereux |
| Marqueur `db_encrypted.flag` | Fichier local « honneur » ; crypto réelle = SQLCipher + clé | Faible (pas un secret) |

### 3.2 Restore / ZIP / `.mmc` / SAF

| Point | Constat | Gravité famille |
|-------|---------|-----------------|
| Écritures neuves SAF | `MMC1B64:` | OK |
| Partage | ZIP temporaire → seal → `mon-mois-clair-copie.mmc` ; ZIP tmp effacé | OK |
| Restore autre téléphone | Saisie code ; mauvais code → `BadShareCode` (phrase fixe) | OK |
| Grace pré-6.81 | JSON / ZIP **clair** encore acceptés | **Élevée** si une vieille copie traîne sur Drive / WhatsApp |
| Entropie code 8 chiffres | ~10⁸ + PBKDF2 120k — OK contre curieux ; **pas** contre crack offline motivé du `.mmc` seul | **Faible–moyenne** |
| Code + fichier dans le même chat | UX : l’app n’envoie pas le code ; l’utilisatrice **peut** le coller à côté | **Critique si fait** |
| Intégrité restore | SHA-256 payload ; zip-slip / zip-bomb bornés | Authenticité canal **non** prouvée |
| Constant `SHARE_ZIP_NAME` legacy | Encore dans `BackupSecurityArchive` / grace ; flux partage = `.mmc` | Cosmétique |

### 3.3 Photos / PDF

| Point | Constat | Gravité famille |
|-------|---------|-----------------|
| Local + SAF | Écriture `MRF1` ; lecture tolère clair (migration) | OK |
| `viewUri` | Déchiffre vers `cache/view/` pour lecteur système ; purge > 1 h | **Faible–moyenne** (fenêtre + apps avec URI) |
| FileProvider `receipts/` | Path déclaré ; blobs au repos = MRF1 (ciphertext) | Faible si grant d’URI fichier brut |
| PDF / résumé mois | `ACTION_SEND` PDF **en clair** (hors enveloppe backup) | **Moyenne** si partagé comme « copie » |

### 3.4 Biométrie

| Point | Constat | Gravité famille |
|-------|---------|-----------------|
| CryptoObject | Preuve d’auth strong ; **ne wrappe pas** l’ouverture Room | Aligné docs (« porte » + DB déjà chiffrée) |
| Fallback WEAK / plain | Face convenience / OEM sans strong → UI gate | **Faible** (DB reste SQLCipher) |
| Sans bio | Aperçu multitâche possible ; widget/notifs montants visibles | **Produit** — à dire à maman |
| FAIT depuis notif | Sans ouvrir l’écran complet (`PRIVACY`) | Attendu ; surface PendingIntent |

### 3.5 Notifications & widget

| Surface | Bio ON | Bio OFF |
|---------|--------|---------|
| Widget | Pas de montant ; « Touchez pour ouvrir » | Reste + prochaine ligne **visibles** (carnet sur le bureau) |
| Notifs | Montant masqué ; **libellé** dans le titre (« Aujourd’hui : Loyer ») | Libellé + montant |
| Texte système | Non chiffré (limite Android) — claim OK | Idem |

### 3.6 Autres (inchangés / sains)

- Pas d’`INTERNET` ; partage = autre app.
- Receivers : `ReminderReceiver` non exporté ; widget + boot exportés (requis).
- R8 minify release.
- Keystore signature hors git (`SECURITY.md`).

---

## 4. Scénarios famille (post-6.82)

| Scénario | Fuite ? | Commentaire |
|----------|---------|-------------|
| Téléphone éteint / sandbox dump sans clé Keystore | Room / receipts **illisibles** | Gain majeur vs 6.80 |
| WhatsApp : `.mmc` **sans** code | Illisible | Gain majeur |
| WhatsApp : `.mmc` + code dans le même message | **Fuite totale** pour le destinataire | Éducation utilisatrice |
| Ancien `mon-mois-clair-securite.zip` encore sur Drive | **Clair** | Grace ; supprimer / remplacer |
| Documents SAF sync cloud | Blobs `MMC1B64` / `MRF1` — illisibles **sans** code appareil (SAF) ou wrap Keystore (photos) | Photos SAF = clé **appareil**, pas le code de copie → autre téléphone ne lit pas les MRF1 Documents seuls |
| Lockscreen + bio ON | Libellé facture visible, pas le montant | Acceptable |
| Bio OFF + widget | Montants sur l’accueil | Attendu |
| Perte téléphone + oubli code + pas d’autre copie | **Perte données** | Documenté ; pas de recovery cloud |

---

## 5. Tâches finales avant distribution famille

Ordre conseillé (pas de code dans cet audit) :

### Bloquantes (faire avant envoi APK)

1. **Smoke doigt 6.82** (PROGRESS) : 1ʳᵉ ouverture → migration / Room OK ; affichage **code de copie** ; partage `.mmc` WhatsApp/Drive ; restore sur **2ᵉ appareil** avec code ; mauvais code → message clair.
2. **APK release** signé (`assembleRelease`) ; vérifier `versionName='0.1.0-6.82'` (`aapt dump badging`) — jamais debug.
3. **Brief oral / fiche 5 lignes** pour maman / famille :
   - noter le code **une fois** hors du téléphone (papier) ;
   - **ne jamais** envoyer le code dans le même message que le fichier ;
   - activer le **verrou** (empreinte / code) si le téléphone circule ;
   - les vieilles ZIP « sécurité » d’avant = à jeter / remplacer.
4. **Tester une install update** (même keystore) depuis une build pré-6.81 si un téléphone famille a encore l’ancienne : migration SQLCipher + receipts.

### Fortement recommandées

5. Smoke **S26** / checklist produit (FAIT, photo, widget, notif, QuietLink) — hors crypto mais bloque le « cadeau utilisable ».
6. Mettre à jour mentalement [`docs/DISTRIBUTION_FAMILLE.md`](../DISTRIBUTION_FAMILLE.md) (exemples encore à **6.77**) au prochain polish doc — pas bloquant sécurité.
7. Rescan / dismiss Dependabot (bruit AGP) selon `docs/CI.md` — hygiène, pas runtime APK.

### Non bloquantes / plus tard

8. Réduire grace claires (date butoir) une fois toute la famille en ≥ 6.81.
9. Renforcer entropie code (plus de chiffres / alphabet) si usage hors cercle très fermé.
10. Purge plus agressive / scope `view/` ; éventuel masquage libellé notif (trade-off utilité).
11. Ne **pas** promettre coffre / Play « bank-grade » ; coller à `PRIVACY.md`.

---

## 6. Matrice de confiance (distribution famille)

| Critère | Note indicative | Motif |
|---------|-----------------|-------|
| Privacy réseau | **Excellent** | Zéro `INTERNET` |
| Confidentialité au repos (device OK) | **Bon** | SQLCipher + MRF1 + Keystore |
| Confidentialité copies partagées | **Bon si discipline code** | `.mmc` ; grace + erreur humaine = points faibles |
| Alignement docs ↔ code | **Bon** | Claims crypto tenus |
| Prêt cadeau maman 60+ | **Conditionnel** | Smoke code/restore + brief code **requis** |
| Pentest / root | **Hors scope** | Limites assumées |

**Go / no-go famille :** **GO conditionnel** après items §5.1–5.4 verts. Pas de P0 crypto runtime bloquant identifié sur le code 6.82 pour un sideload familial **discipliné**.

---

## 7. Fichiers / modules de référence (lecture seule)

| Zone | Chemins |
|------|---------|
| DB | `di/DatabaseModule.kt`, `crypto/DbPassphraseVault.kt`, `crypto/EncryptedDbMigrator.kt` |
| Keystore | `crypto/KeystoreAesKey.kt`, `crypto/AesGcmBox.kt` |
| Backup / share | `crypto/BackupEnvelope.kt`, `crypto/ShareSecretVault.kt`, `backup/SafBackupStore.kt`, `backup/BackupRestorer.kt`, `domain/usecase/ShareBackupUseCase.kt` |
| Photos | `crypto/LocalFileCipher.kt`, `data/receipt/ReceiptPhotoStore.kt` |
| Bio / UI | `data/biometric/BiometricAuthGatewayImpl.kt`, `MainActivity.kt` |
| Widget / notifs | `domain/CarnetWidgetText.kt`, `data/reminder/ReminderSchedulerImpl.kt` |
| Claims | `PRIVACY.md`, `SECURITY.md` |

---

*Fin de l’audit final-02 sécurité — 2026-10-26. Aucun code modifié. Implémentation crypto déjà sur `0.1.0-6.82`.*
