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
- Pas de permission `INTERNET`. `android:allowBackup="false"`. Release minifiée.
- `FLAG_SECURE` seulement quand le verrouillage est activé. Sans verrou, l’aperçu des tâches peut montrer le mois.
- IBAN et communication : presse-papiers marqué sensible (Android 13+) et vidé après une minute. Copie IBAN seulement si le contrôle international passe.
- Exportés : activité lanceur, et `BootReceiver` pour les diffusions système. FileProvider et le receiver d’alarme ne le sont pas.
- Le partage (résumé, copie ZIP) quitte l’app. Le texte n’est plus protégé ici.
- Pas de tests d’écran instrumentés. Le lint en CI ne bloque pas. Pas de scan CVE en garde.
- Ne pas recopier le numéro de série d’un téléphone dans un markdown.

## Secrets / fichiers interdits dans le dépôt

- `local.properties`
- `keystore.properties`, `*.jks`, `*.keystore`
- APK / AAB binaires
- Dumps device (`dist/verify/**` hors `RESET_TESTS_FROM_ZERO.md`)
