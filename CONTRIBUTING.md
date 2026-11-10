# Contribuer

Dépôt **public** (vitrine craft Android). Usage produit = familial / Play Internal. Les PR externes ne sont en général **pas** attendues ; une contribution ciblée après lecture de la charte peut être discutée.

## Si vous touchez le code quand même

1. Branche de travail = **`develop`** (pas `main` au quotidien).
2. Lire [`CHARTE_ET_ARCHITECTURE.md`](CHARTE_ET_ARCHITECTURE.md) et [`PROGRESS_LOG.md`](PROGRESS_LOG.md) avant de coder.
3. Avant push : `./gradlew :app:testDebugUnitTest --rerun-tasks` — aligner README / `versionName` / journal.
4. MAJ utilisateur : voir [`docs/WHATS_NEW.md`](docs/WHATS_NEW.md) (`catalog.json` + `versionCode`).
4. Pas de keystore, pas de secrets, pas de claim « premium parfait » sans smoke doigt.

Contact produit : Menu → **Arkalia Système** (voir [`PRIVACY.md`](PRIVACY.md)).
