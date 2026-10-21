# Quinté d’experts + profils — Mon Mois Clair

Chaque prompt = **un agent / un chat** distinct.  
**Seul** l’expert validation commit/push (si demandé).

Campagnes optionnelles (pas le journal produit). État actuel : `PROGRESS_LOG.md` · plan : [`../PLAN_PRODUIT_FINI.md`](../PLAN_PRODUIT_FINI.md).

| # | Fichier | Mission de base |
|---|---------|-----------------|
| 1 | [`PROMPT_TESTEUR_CORRECTEUR.md`](PROMPT_TESTEUR_CORRECTEUR.md) | Bugs, régressions, Gradle, parcours complets |
| 2 | [`PROMPT_TESTEUR_VISUEL.md`](PROMPT_TESTEUR_VISUEL.md) | Contraste, marges, zone pouce, clavier, cartes |
| 3 | [`PROMPT_TESTEUR_PRODUIT_MANQUES.md`](PROMPT_TESTEUR_PRODUIT_MANQUES.md) | Manques, inutiles, abandon 60 j |
| 4 | [`PROMPT_TESTEUR_SENIOR_ONEUI.md`](PROMPT_TESTEUR_SENIOR_ONEUI.md) | Confusion Samsung / peur / jargon |
| 5 | [`PROMPT_EXPERT_VALIDATION.md`](PROMPT_EXPERT_VALIDATION.md) | Claims vs code, score tests, push |

**Profils (regards)** : [`PROFILS_TESTEURS.md`](PROFILS_TESTEURS.md) — Chiant · Null · Perf · Hostile-doux · Fidélité 60 j · Senior.

Impose toujours : `Profil du jour : …` en tête du prompt collé.

## Campagne 6.9+ (parcours commun, peu importe le profil)

1. Sticky : Reste + À payer lisibles ; factures **avant** Courses/divers  
2. FAIT ✓ / Remettre → **FAIT le JJ/MM** ; pastilles Auto (or fond + encre) / EN RETARD (brique)  
3. Barre pouce Menu · + Ajouter ; Menu calendrier ; « Alertes » sur le bouton seulement si notifs/alarmes KO  
4. Ajouter : grille jours 1–31 ; montant clavier décimal ; défaut ce mois-ci seul  
5. Divers avec **clavier ouvert** : +/− encore atteignables (`imePadding`)  
6. Aide : Reste du mois passé ; mois indépendants  
7. Noter bugs **et** choses inutiles / sous-travaillées  

Reset neuve : `dist/verify/RESET_TESTS_FROM_ZERO.md`.

## Enchaînement

```text
Feature
  → Correcteur (+ profil Hostile ou Null)
  → Visuel (+ Chiant ou Perf)
  → Produit (+ Fidélité) et/ou Senior
  → Validation + push (demande humaine)
```

## Rapports

Sous `dist/verify/` (temporaires) : toujours la ligne `Profil : …` en haut.
