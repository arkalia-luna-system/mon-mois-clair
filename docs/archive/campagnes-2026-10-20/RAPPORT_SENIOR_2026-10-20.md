> **Figé le 2026-10-20, version 6.26.** Ne décrit pas l’app actuelle (journal **6.36**). État : `docs/archive/AUDIT_ALEX_MAMAN_2026-10-21.md` §8.

# Rapport senior / One UI — 2026-10-20

## Contexte
- Device / version : SM-S938B · 0.1.0-6.26

## Synthèse
- Verdict : utilisable seul (après 6.26)
- Top 5 peurs ou confusions
  1. « À payer 0 € » sur mois vide = je crois que c’est payé → corrigé
  2. Batterie « À régler » : j’ai peur de casser Samsung (texte guide présent)
  3. Empreinte optionnelle : bien dit « Par défaut l’app s’ouvre sans code »
  4. Restore : « remplace tout » bien visible
  5. FAIT le JJ/MM rassure

## Journal du persona
| Moment | Ce que je voulais | Ce que j’ai compris | Blocage |
|--------|-------------------|--------------------|---------|
| Home | Combien il me reste | Reste + À payer | non |
| Menu | Trouver réglages | Outils en haut | non |
| Mois vide | Voir si c’est normal | Mois vide + hint Ajouter | « À payer 0 € » → fix |
| Restore | Pas tout casser | Confirmation annoncée | non |

## Problèmes
| ID | Sévérité | Phrase persona | Fix minimal |
|----|----------|----------------|-------------|
| S1 | mineur | « À payer 0 € me rassure alors qu’il n’y a rien » | masquer la ligne |
| S2 | mineur | « pas OK, c’est cassé ? » | « pas prêts » |

## Corrections faites
| ID | Fichiers | Résumé |
|----|----------|--------|
| S1 | StickyStillToPayLabel + HomeScreen | ligne absente si aucune facture |
| S2 | PermissionsSetupScreen | wording |

## Message pour validation
Oui pour sideload famille après install 6.26 ; garder le guide batterie Samsung.
