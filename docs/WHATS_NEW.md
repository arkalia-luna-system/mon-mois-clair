# Nouveautés automatiques (pour Athalia)

**Pour une MAJ :** montez `versionCode` + `versionName` dans `app/build.gradle.kts`, puis ajoutez une entrée pour **cette** version dans `app/src/main/assets/whats_new/catalog.json`.

Règles de contenu :
- 2–4 puces FR simples, utiles, sans jargon.
- Pas de doublon avec une version précédente.
- Pas de détail interne (`tests`, `Hilt`, `gates`, `CE`, build, bump, etc.).
- Si la version est purement technique : gardez l’entrée mais mettez `"userVisible": false` et `bullets: []`.

**Ce que voient les utilisatrices :** à la première ouverture après la mise à jour, l’app regarde seulement l’entrée du `versionCode` installé. Elle montre l’écran « Nouveautés » une seule fois pour cette version, avec `J’ai compris` ou `Passer`. Les anciennes versions ne sont pas rejouées. S’il manque une entrée visible, une phrase générique s’affiche quand même. La CI vérifie le JSON à chaque build (`verifyWhatsNewCatalog`).
