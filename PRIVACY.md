# Confidentialité — Mon Mois Clair

App **100 % locale**. Pas de compte, pas de serveur Mon Mois Clair, pas d’analytics.

## Réseau

- **Aucune** permission `INTERNET` dans le manifeste.
- Aucun appel HTTP/API, aucune synchro cloud, aucune pub.
- Les intents « partager » / « mail » / « ouvrir PDF » passent par d’**autres apps** choisies sur le téléphone (WhatsApp, Gmail, Drive…). Une fois hors de Mon Mois Clair, c’est leur politique qui s’applique.

## Données sur l’appareil

| Où | Quoi |
|----|------|
| **Room** (SQLite `mon_mois_clair.db`) | Mois, factures, libellés, montants, notes / `+++`, IBAN et e-mails créancier (dossiers), chemins photos — **en clair** sur le téléphone |
| Dossier Documents (SAF) | Backups JSON + archives ZIP (photos) — **en clair** (choix produit, pas un coffre-fort chiffré) |
| Photos / PDF reçus | `files/receipts/` via FileProvider (non exporté) |
| Presse-papiers | Communication / IBAN copiés : marqués sensibles (Android 13+) et **vidés après ~1 min** |

`android:allowBackup="false"` : pas de sauvegarde cloud Android automatique de l’app.

**Honnêteté :** Room et les backups SAF ne sont **pas** chiffrés (pas de SQLCipher). Sur un téléphone déverrouillé (ou avec accès root / backup OEM), les données du carnet sont lisibles. Le verrou biométrique **masque l’UI** ; il ne chiffre pas la base.

## Widget

- Affiche le **Reste** du mois (et une ligne suivante) sur l’écran d’accueil.
- Si le verrou biométrique est **activé** : montants **masqués** sur le widget.
- Sinon : les montants sont visibles hors de l’app (comme un carnet ouvert sur le bureau).

## Notifications

- Rappels J−3 / jour / retard : libellé + **montant** (utile pour payer).
- Même si le verrou biométrique est ON : le texte de la notif n’est **pas** chiffré (limitation Android / choix produit).
- Action « FAIT ✓ » depuis la notif possible sans ouvrir l’écran complet.

## Biométrie & multitâche

- Empreinte / Face optionnelle (Réglages) — **porte UI** seulement (pas de `CryptoObject` / clé liée aux données).
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
