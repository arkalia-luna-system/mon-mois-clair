# Quinté testeurs — Mon Mois Clair (device-first)

Chaque prompt = **un agent / un chat**.  
**Seul** Validation commit/push (si l’humaine le demande).

| Doc | Rôle |
|-----|------|
| [`CAMPAGNE_PRODUIT_FINI.md`](CAMPAGNE_PRODUIT_FINI.md) | **Matrice complète** + protocole adb — lire en premier |
| [`PROFILS_TESTEURS.md`](PROFILS_TESTEURS.md) | Tempéraments + personae |
| Prompts 1–5 ci-dessous | Mission de chaque expert |

Version cible journal : **`0.1.0-6.37`** · schéma **7** · tests **233/233**.

## En-tête obligatoire (coller en haut de chaque chat)

```text
Profil du jour : Chiant | Null | Perf | Hostile-doux | Fidélité 60 j | Senior One UI
Persona : Maman | Alex | Installateur | Médiateur | Base vide | Multi-dossiers
Device : OBLIGATOIRE — téléphone branché, release 6.37 installée
Interdit : valider sans geste adb + preuve ; minimum 8 frictions
```

## Les 5 experts

| # | Fichier | Sur le téléphone, tu… |
|---|---------|------------------------|
| 1 | [`PROMPT_TESTEUR_CORRECTEUR.md`](PROMPT_TESTEUR_CORRECTEUR.md) | Casses fonctionnelles, double-tap, montants, dossiers, Gradle |
| 2 | [`PROMPT_TESTEUR_VISUEL.md`](PROMPT_TESTEUR_VISUEL.md) | 48 dp, contraste, compact, texte coupé, jank |
| 3 | [`PROMPT_TESTEUR_PRODUIT_MANQUES.md`](PROMPT_TESTEUR_PRODUIT_MANQUES.md) | Abandon, zéro pixel, résumé, praticité familiale |
| 4 | [`PROMPT_TESTEUR_SENIOR_ONEUI.md`](PROMPT_TESTEUR_SENIOR_ONEUI.md) | Peurs Samsung, jargon, doigts larges |
| 5 | [`PROMPT_EXPERT_VALIDATION.md`](PROMPT_EXPERT_VALIDATION.md) | Claims vs preuves, score tests, push |

## Enchaînement famille (recommandé)

```text
1a Correcteur Hostile × Multi-dossiers
1b Correcteur Null × Base vide   (pm clear SEULEMENT si OK humain)
2a Visuel Chiant × Maman
2b Visuel Perf × Alex
3a Produit Fidélité × Maman
3b Produit Fidélité × Médiateur
4  Senior × Maman
fin Validation → push si demandé
```

## Install avant campagne

```bash
COPYFILE_DISABLE=1 ./gradlew :app:assembleRelease
cp -f …/outputs/apk/release/app-release.apk dist/MonMoisClair-release.apk
adb install -r dist/MonMoisClair-release.apk
adb shell dumpsys package be.monmoisclair.app | grep versionName
```

Reset neuve (destructif) : `dist/verify/RESET_TESTS_FROM_ZERO.md`.

## Rapports

`dist/verify/campagne-YYYY-MM-DD/RAPPORT_….md` + png/xml.  
Sans `Profil` + `Persona` + cases matrice + ≥8 frictions → rapport **refusé** par Validation.
