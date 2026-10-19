# Sécurité

## Signaler une vulnérabilité

Ouvre une **issue privée** (GitHub Security Advisory) ou contacte la mainteneuse du dépôt `arkalia-luna-system/mon-mois-clair`.

Ne publie pas de preuves d’exploitation dans une issue publique.

## Modèle de menace (volontaire)

Mon Mois Clair est une app **locale, hors ligne**, destinée à un usage familial sideloadé :

- Pas de serveur, pas d’analytics, pas d’API tierce.
- Les backups JSON dans Documents (SAF) sont **en clair** (décision produit) — le téléphone + le dossier Documents sont la frontière de confiance.
- Biométrie optionnelle + `FLAG_SECURE` si activée (aperçu multitâche masqué).
- Keystore de signature release : **jamais** dans git (`*.jks`, `keystore.properties`).

## Secrets / fichiers interdits dans le dépôt

- `local.properties`
- `keystore.properties`, `*.jks`, `*.keystore`
- APK / AAB binaires
- Dumps device (`dist/verify/**` hors `RESET_TESTS_FROM_ZERO.md`)
