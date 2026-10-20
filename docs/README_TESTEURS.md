# Quinté d’experts — Mon Mois Clair

Chaque prompt = **un agent / un chat** distinct.  
**Seul** l’expert validation commit/push (si demandé) — aucun des 4 testeurs ne push.

| # | Fichier | Trouve surtout… | Quand |
|---|---------|-----------------|--------|
| 1 | [`PROMPT_TESTEUR_CORRECTEUR.md`](PROMPT_TESTEUR_CORRECTEUR.md) | Bugs fonctionnels, régressions, claims faux, tests Gradle | Après chaque feature / fix |
| 2 | [`PROMPT_TESTEUR_VISUEL.md`](PROMPT_TESTEUR_VISUEL.md) | Marges, contrastes, zone pouce, cartes, clavier | Après grosse UI / thème |
| 3 | [`PROMPT_TESTEUR_PRODUIT_MANQUES.md`](PROMPT_TESTEUR_PRODUIT_MANQUES.md) | Trous d’info, peurs données, chemins peu découvrables, inutiles | Avant sideload famille |
| 4 | [`PROMPT_TESTEUR_SENIOR_ONEUI.md`](PROMPT_TESTEUR_SENIOR_ONEUI.md) | Confusion, jargon, peurs Samsung / Device Care | Avant APK proche peu tech |
| 5 | [`PROMPT_EXPERT_VALIDATION.md`](PROMPT_EXPERT_VALIDATION.md) | Claims vs code, score tests, push develop | **Toujours en dernier** |

## Campagne fidélité 6.8 (étude 2026) — parcours commun

Tous les testeurs doivent **vraiment** exercer (pas survoler) :

1. **Deux questions** : lire « Reste » + « À payer » sans scroller ; cocher FAIT → voir « À payer » baisser + **FAIT le JJ/MM**
2. **Barre pouce** : Menu (calendrier 12 mois + Santé + Réglages) · + Ajouter — rien d’autre critique en haut
3. **Mois indépendants** : pas de report magique ; entrée ponctuelle si besoin
4. **Montant variable** : éditer une facture récurrente → défaut « ce mois-ci seul »
5. **Papier** : scan EPC / Copier / photo (si lot 6) ; sauvegarde envoyer / restaurer
6. **Inutile / sous-travaillé** : noter tout ce qui fatigue, redonde, ou promet trop

Reset from zero (persona neuve) : `dist/verify/RESET_TESTS_FROM_ZERO.md`.

## Enchaînement conseillé

```text
Feature codée
    → 1 Correcteur (preuve que ça marche)
    → 2 Visuel   (si écrans / thème touchés)
    → 3 Produit  (manques / inutiles / fidélité 60 j)
    → 4 Senior   (si APK famille)
    → 5 Validation + push (sur demande)
```

## Rapports attendus

Écrits sous `dist/verify/` le temps de la campagne, puis **supprimables** une fois fixes + tests verts.

| Prompt | Rapport temporaire |
|--------|-------------------|
| Correcteur | `RAPPORT_TEST_YYYY-MM-DD.md` |
| Visuel | `RAPPORT_VISUEL_YYYY-MM-DD.md` |
| Produit | `RAPPORT_PRODUIT_YYYY-MM-DD.md` |
| Senior | `RAPPORT_SENIOR_YYYY-MM-DD.md` |
| Validation | résumé dans le chat (+ PROGRESS si clôture) |
