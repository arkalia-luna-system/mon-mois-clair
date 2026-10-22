> **Figé le 2026-10-20, version 6.26.** Ne décrit pas l’app actuelle (journal **6.36**). État : `docs/archive/AUDIT_ALEX_MAMAN_2026-10-21.md` §8.

# Rapport test — 2026-10-20

## Contexte
- Device / émulateur : Galaxy S25 Ultra (SM-S938B)
- Version app (versionName) : 0.1.0-6.26 (installée après fix ; campagne lue sur 6.25)
- Étape PROGRESS concernée : 6.26 (campagne prompts Correcteur)
- Build / install : assembleRelease → adb install -r
- Tests Gradle : 162/162

## Synthèse
- Verdict : OK avec réserves (smoke notif / trimestriel encore ouverts)
- Nb problèmes bloquants / majeurs / mineurs : 0 / 0 / 2 (corrigés)
- Audit code : fait
- Audit visuel : partiel (Home, Menu, Alertes, Réglages, Restore, Ajouter, recherche)

## Parcours testés
| Persona | Scénario | Code OK ? | Visuel OK ? | Preuve |
|---------|----------|-----------|-------------|--------|
| B | Home sticky Reste / À payer | oui | oui | uiautomator |
| B | Menu Outils avant Mois + années 2026/2025 | oui | oui | dump menu |
| B | Alertes pastilles Prêt | oui | oui | dump alertes |
| B | Réglages version « Mon Mois Clair » + 6.25/6.26 | oui | oui | dump settings |
| B | Restore phrases sans jargon | oui | oui | dump restore |
| E | Recherche zzzz + Effacer | oui | oui | dump search |
| E | Mois vide « À payer 0 € » | non→fix | non→fix | Nov 2025 |

## Problèmes trouvés
| ID | Sévérité | Zone | Description | Attendu (charte) | Observé |
|----|----------|------|-------------|------------------|---------|
| P1 | mineur | Home sticky | Mois vide : « À payer 0 € » vert | Pas de fausse réussite | Corrigé 6.26 |
| P2 | mineur | Onboarding permissions | « pas OK » | Aligné « Prêt » | Corrigé « pas prêts » |

## Corrections faites
| ID | Fichiers | Résumé du fix | Re-test code | Re-test UI |
|----|----------|---------------|--------------|------------|
| P1 | StickyStillToPayLabel.kt, HomeScreen.kt, test | Masque la ligne sans facture | 162/162 | à rejouer Nov vide après install 6.26 |
| P2 | PermissionsSetupScreen.kt | « pas prêts » | compile OK | phrasing |

## Non traité (hors scope / expert validation)
- Smoke notification multi-factures
- Trimestriel édition nom seulement
- Batterie « À régler » (état téléphone réel)

## Checklist
- [x] Lecture diff / charte
- [x] Tests unitaires 162/162
- [x] Scénarios feature du diff
- [x] Checklist visuelle (marges, contraste, dialogs) partielle
- [ ] Biométrie / lock (non rejoué ce tour)
- [x] Pas de crash

## Message pour l’expert validation
Correcteur Hostile-doux : Menu/Alertes/Restore/Recherche OK sur 6.25. Deux frictions Senior corrigées en 6.26. Push demandé par l’humaine. Claims version = 6.26 / 162 tests.
