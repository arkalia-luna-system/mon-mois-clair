# Confidentialité — Mon Mois Clair

App **100 % locale**. Pas de compte, pas de serveur Mon Mois Clair, pas d’analytics.

## Réseau

- **Aucune** permission `INTERNET` dans le manifeste.
- Aucun appel HTTP/API, aucune synchro cloud, aucune pub.
- Les intents « partager » / « mail » / « ouvrir PDF » passent par d’**autres apps** choisies sur le téléphone (WhatsApp, Gmail, Drive…). Une fois hors de Mon Mois Clair, c’est leur politique qui s’applique.

## Données sur l’appareil

| Où | Quoi |
|----|------|
| **Room** (SQLite `mon_mois_clair.db`) | Mois, factures, libellés, montants, notes / `+++`, IBAN, chemins photos — **chiffré au repos** (SQLCipher ; clé enveloppée Android Keystore) |
| Dossier Documents (SAF) | Backups JSON enveloppe AES-GCM (code de copie) + photos/PDF locaux enveloppés — **pas en clair** depuis 6.81 |
| Photos / PDF reçus | `files/receipts/` chiffrés (MRF1) ; aperçu = déchiffrement temporaire dans le cache |
| Presse-papiers | Communication / IBAN copiés : marqués sensibles (Android 13+) et **vidés après ~1 min** |

`android:allowBackup="false"` : pas de sauvegarde cloud Android automatique de l’app.

**Honnêteté :** ce n’est **pas** un coffre parfait. Après déverrouillage, les données sont en RAM / à l’écran. Appareil root / malware puissant peut encore contourner. Code de copie oublié + téléphone mort **sans** autre copie = données perdues (pas de serveur de recovery).

Anciennes copies ZIP/JSON **en clair** (pré-6.81) restent restaurables une version de grâce.

## Widget

- Affiche le **Reste** du mois (et une ligne suivante) sur l’écran d’accueil.
- Si le verrou biométrique est **activé** : montants **masqués** sur le widget.
- Sinon : les montants sont visibles hors de l’app (comme un carnet ouvert sur le bureau).

## Notifications

- Rappels J−3 / jour / retard : libellé + montant (utile pour payer).
- Si le verrou biométrique est **ON** : montant remplacé par « Montant masqué » (aligné widget). Le texte système n’est pas chiffré (limitation Android).
- Action « FAIT ✓ » depuis la notif possible sans ouvrir l’écran complet.

## Biométrie & multitâche

- Empreinte / Face / code écran optionnels (Réglages) — **porte UI** + **CryptoObject** (clé Keystore `mmc_unlock_v1`) quand le téléphone le permet (biométrie forte ou code).
- La base reste chiffrée SQLCipher même sans verrou UI ; le verrou lie l’ouverture de session à l’empreinte/code.
- Si le verrouillage est activé : `FLAG_SECURE` (aperçu multitâche masqué).
- Sans verrou : l’aperçu des tâches peut montrer le mois ouvert.

## Batterie (alertes)

- Sur Galaxy / One UI, les rappels fiables peuvent demander d’**ignorer l’optimisation batterie** pour Mon Mois Clair (parcours Réglages → Alertes).
- Ce n’est **pas** un accès réseau : uniquement pour que les alarmes locales sonnent malgré la mise en veille agressive Samsung.

## Contact

Bug, idée ou question : Accueil → **Menu** → **« Un souci ? Écrire à Arkalia Système »**  
(mail local via Gmail / Mail — joindre une capture dans le client mail).

## Voir aussi

- [`SECURITY.md`](SECURITY.md) — modèle de menace, keystore, Dependabot
- [`CHARTE_ET_ARCHITECTURE.md`](CHARTE_ET_ARCHITECTURE.md) — règles produit
- [`docs/DISTRIBUTION_FAMILLE.md`](docs/DISTRIBUTION_FAMILLE.md) — APK sideload (hors Play Store)
