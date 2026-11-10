# Nouveautés automatiques (pour Athalia)

**Pour une MAJ :** montez `versionCode` + `versionName` dans `app/build.gradle.kts`, puis ajoutez une entrée `versionCode` + puces dans `app/src/main/assets/whats_new/catalog.json` (3–4 phrases simples).

**Ce que voient les utilisateurs :** à la première ouverture après la mise à jour, un écran « Nouveautés » (une fois) avec ces puces ; s’il manque une entrée, une phrase générique s’affiche quand même. La CI vérifie le JSON à chaque build (`verifyWhatsNewCatalog`).
