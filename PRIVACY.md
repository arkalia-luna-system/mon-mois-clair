# Confidentialité — Mon Mois Clair

App **100 % locale**. Pas de compte, pas de serveur Mon Mois Clair, pas d’analytics.

## Réseau

- **Aucune** permission `INTERNET` dans le manifeste.
- Aucun appel HTTP/API, aucune synchro cloud, aucune pub.
- Les intents « partager » / « mail » / « ouvrir PDF » passent par d’**autres apps** choisies sur le téléphone (WhatsApp, Gmail, Drive…). Une fois hors de Mon Mois Clair, c’est leur politique qui s’applique.

## Données

| Où | Quoi |
|----|------|
| Room (SQLite) sur l’appareil | Mois, factures, photos référencées, dossiers / accords |
| Dossier Documents (SAF) | Backups JSON + archives ZIP (photos) — **en clair** (choix produit) |
| Presse-papiers | Communication / IBAN copiés : marqués sensibles (Android 13+) et **vidés après ~1 min** |

`android:allowBackup="false"` : pas de sauvegarde cloud Android automatique de l’app.

## Biométrie & multitâche

- Empreinte / Face optionnelle (Réglages).
- Si le verrouillage est activé : `FLAG_SECURE` (aperçu multitâche masqué).
- Sans verrou : l’aperçu des tâches peut montrer le mois ouvert.

## Contact

Bug, idée ou question : Accueil → **Menu** → **« Un souci ? Écrire à Arkalia Système »**  
(mail local via Gmail / Mail — joindre une capture dans le client mail).

## Voir aussi

- [`SECURITY.md`](SECURITY.md) — modèle de menace, keystore, Dependabot
- [`CHARTE_ET_ARCHITECTURE.md`](CHARTE_ET_ARCHITECTURE.md) — règles produit
- [`docs/DISTRIBUTION_FAMILLE.md`](docs/DISTRIBUTION_FAMILLE.md) — APK sideload (hors Play Store)
