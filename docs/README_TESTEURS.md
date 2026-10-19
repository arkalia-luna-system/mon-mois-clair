# Duo / quatuor d’experts — Mon Mois Clair

Chaque prompt = **un agent / un chat** distinct.  
Toujours finir par l’**expert validation** (claims + Gradle + push) — aucun testeur ne push.

| # | Fichier | Trouve surtout… | Quand |
|---|---------|-----------------|--------|
| 1 | [`PROMPT_TESTEUR_CORRECTEUR.md`](PROMPT_TESTEUR_CORRECTEUR.md) | Bugs fonctionnels, régressions, claims faux, tests Gradle | Après chaque feature / fix |
| 2 | [`PROMPT_TESTEUR_VISUEL.md`](PROMPT_TESTEUR_VISUEL.md) | Marges, contrastes, wrap, zones tactiles, clavier, dialogs | Après grosse UI / thème / insets |
| 3 | [`PROMPT_TESTEUR_PRODUIT_MANQUES.md`](PROMPT_TESTEUR_PRODUIT_MANQUES.md) | Trous d’info, peurs données, chemins peu découvrables | Avant sideload « version famille » |
| 4 | [`PROMPT_TESTEUR_SENIOR_ONEUI.md`](PROMPT_TESTEUR_SENIOR_ONEUI.md) | Confusion, jargon, peurs Samsung / Device Care | Avant envoi APK à un proche peu tech |

## Enchaînement conseillé

```text
Feature codée
    → 1 Correcteur (preuve que ça marche)
    → 2 Visuel   (si écrans touchés)
    → 3 ou 4     (si tu vas « sortir » l’APK)
    → Expert validation + push
```

## Rapports attendus

| Prompt | Rapport |
|--------|---------|
| Correcteur | `dist/verify/RAPPORT_TEST_YYYY-MM-DD.md` |
| Visuel | `dist/verify/RAPPORT_VISUEL_YYYY-MM-DD.md` |
| Produit | `dist/verify/RAPPORT_PRODUIT_YYYY-MM-DD.md` |
| Senior | `dist/verify/RAPPORT_SENIOR_YYYY-MM-DD.md` |

Reset from zero (persona A) : `dist/verify/RESET_TESTS_FROM_ZERO.md` — surtout pour le correcteur.
