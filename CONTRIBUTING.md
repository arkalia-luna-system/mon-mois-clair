# Contribuer

Dépôt **privé**, usage **familial**. Les PR externes ne sont en général **pas** attendues.

## Si vous touchez le code quand même

1. Branche de travail = **`develop`** (pas `main` au quotidien).
2. Lire [`CHARTE_ET_ARCHITECTURE.md`](CHARTE_ET_ARCHITECTURE.md) et [`PROGRESS_LOG.md`](PROGRESS_LOG.md) avant de coder.
3. Avant push : `./gradlew :app:testDebugUnitTest --rerun-tasks` — aligner README / `versionName` / journal.
4. Pas de keystore, pas de secrets, pas de claim « premium parfait » sans smoke doigt.

Contact produit : Menu → **Arkalia Système** (voir [`PRIVACY.md`](PRIVACY.md)).
