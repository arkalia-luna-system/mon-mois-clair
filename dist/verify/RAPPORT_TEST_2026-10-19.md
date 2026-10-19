# Rapport test — 2026-10-19

## Contexte
- Device / émulateur : Galaxy S25 Ultra (`SM-S938B` / `R3CY60BJ3ZM`), Android 16, user 0
- Version app (versionName) : `0.1.0-3.5`
- Étape PROGRESS concernée : **3.4 biométrie** + **3.5 hub Réglages** (données conservées, pas de `pm clear`)
- Build / install : `assembleDebug` + `adb install -r` · preuves `dist/verify/p35/`
- Tests Gradle : **84/84** (`--rerun-tasks`, 21 × `TEST-*.xml`, 0 fail)

## Synthèse
- Verdict : **OK avec réserves**
- Nb problèmes bloquants / majeurs / mineurs : **0 / 0 / 1** (wrap « Suiv. › » corrigé)
- Audit code : **fait** (gateway, preference/MutatingRepository, MainUiState décision E, Settings hub, overlay lock)
- Audit visuel : **fait** (Home, Réglages, prompt bio système, Restore depuis hub)

## Parcours testés
| Persona | Scénario | Code OK ? | Visuel OK ? | Preuve |
|---------|----------|-----------|-------------|--------|
| F Feature | Hub Réglages : Confidentialité / Rappels / Device Care / Sauvegarde | Oui | Oui | `p35/03_settings_top.png`, `04_settings_foot.xml` |
| F Feature | Home allégé : Ajouter · Santé · Réglages (plus SAF/Restore) | Oui | Oui | `p35/02_home_foot.png` |
| F Feature | Toggle bio → prompt Samsung titre/sous-titre FR | Oui | Oui | `p35/12_bio_prompt.xml` (pkg `biometrics.app.setting`) |
| F Feature | Annuler prompt → reste désactivé (cancel) | Oui | Oui | `12` → retour app sans enable forcé |
| F Feature | Lock screen copy (titre / Déverrouiller) — vu quand flag ON | Oui | Oui | dumps prompt + lock overlay |
| F Feature | Décision E : `needsUnlock` seulement si Ready | Oui | N/A | `MainUiStateTest` |
| F Feature | Restore depuis Réglages → Retour hub | Oui | Oui | `p35/05_restore.png` |
| G Régression | Marges bas Home/Réglages vs barre système | Oui | Oui | clearance Home 512 px / Retour 456 px |
| G Régression | Contraste Primary (crème sur vert) | Oui | Oui | `02_home_foot.png` |
| B Léger | Liste + montants home (données existantes) | Oui | Oui | 755 € / 45 € |
| — | Claims README 3.4/82 vs code 3.5/84 | Corrigé doc | N/A | README badges 3.5 / 84 |

## Problèmes trouvés
| ID | Sévérité | Zone | Description | Attendu (charte) | Observé |
|----|----------|------|-------------|------------------|---------|
| H1 | Mineur | Home nav mois | « Suiv. › » wrap sur 2 lignes (colonne trop étroite) | Lisibilité seniors | `Suiv.` h≈394 px avant fix |
| H2 | Doc | README | Badges encore 82/3.4 alors que `versionName` 3.5 + 84 tests | Claims = réalité | Décalage (corrigé) |

## Corrections faites
| ID | Fichiers | Résumé du fix | Re-test code | Re-test UI |
|----|----------|---------------|--------------|------------|
| H1 | `HomeScreen.kt` | `weight(1f)` sur mois + `softWrap=false` Préc/Suiv | 84/84 | Oui — Suiv. h=192 / w=351 (`18_home`) |
| H2 | `README.md` | Badges **84/84** · `0.1.0-3.5` · hub | N/A | N/A |

## Non traité (hors scope / expert validation)
- Déverrouillage empreinte **bout-en-bout** (succès doigt) : prompt OK ; succès complet à confirmer humain une fois
- Device Care « Mise en veille » : copy + intents OK, geste manuel One UI non ouvert
- Picker Restore fichier JSON unitaire
- Commit / push

## Checklist (seulement ce que TU as fait ce tour)
- [x] Lecture diff / charte
- [x] Tests unitaires **84/84**
- [x] Scénarios feature du diff (bio + hub)
- [x] Checklist visuelle (marges, contraste, dialogs/prompt)
- [x] Biométrie / lock (prompt + copy ; unlock doigt = réserve humain)
- [x] Pas de crash

## Message pour l’expert validation
3.4+3.5 prêts côté code + device hub : Home 3 boutons, Réglages complet, Device Care section, Restore depuis hub, prompt bio Samsung correct.  
Décision E respectée (`needsUnlock` seulement si Ready). Overlay session NavHost présent (évite perte pile Réglages).  
Corrigé : wrap « Suiv. › » + claims README **84/84** / `0.1.0-3.5`.  
À re-vérifier avant push : score Gradle `--rerun-tasks` = 84 ; unlock empreinte manuel une fois ; working tree (`HomeScreen`, README, PROGRESS, rapport).  
Pas de push de mon côté.
