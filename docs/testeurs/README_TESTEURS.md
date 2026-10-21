# Quinté d’experts + profils — Mon Mois Clair

Chaque prompt = **un agent / un chat** distinct.  
**Seul** l’expert validation commit/push (si demandé).

**Campagne produit fini (toute l’app)** : [`CAMPAGNE_PRODUIT_FINI.md`](CAMPAGNE_PRODUIT_FINI.md) — matrice C/D/P/E/F/S + personae Maman / Alex / …  
**Profils** : [`PROFILS_TESTEURS.md`](PROFILS_TESTEURS.md).  
État produit : `PROGRESS_LOG.md` · plan : [`../PLAN_PRODUIT_FINI.md`](../PLAN_PRODUIT_FINI.md) · smokes : [`../AUDIT_STATUS.md`](../AUDIT_STATUS.md).

| # | Fichier | Mission de base |
|---|---------|-----------------|
| 1 | [`PROMPT_TESTEUR_CORRECTEUR.md`](PROMPT_TESTEUR_CORRECTEUR.md) | Bugs, régressions, Gradle, **matrice parcours** |
| 2 | [`PROMPT_TESTEUR_VISUEL.md`](PROMPT_TESTEUR_VISUEL.md) | Contraste, 48 dp, chrome compact, dossiers |
| 3 | [`PROMPT_TESTEUR_PRODUIT_MANQUES.md`](PROMPT_TESTEUR_PRODUIT_MANQUES.md) | Manques, abandon 60 j, zéro pixel maman |
| 4 | [`PROMPT_TESTEUR_SENIOR_ONEUI.md`](PROMPT_TESTEUR_SENIOR_ONEUI.md) | Confusion Samsung / peur / jargon |
| 5 | [`PROMPT_EXPERT_VALIDATION.md`](PROMPT_EXPERT_VALIDATION.md) | Claims vs code, score tests, push |

Impose toujours en tête du prompt collé :

```text
Profil du jour : …
Persona : Maman | Alex | Installateur | Médiateur | Base vide | Multi-dossiers
```

## Surfaces à ne plus oublier (depuis 6.30–6.36)

| Surface | Où | Smoke déjà noté ? |
|---------|-----|-------------------|
| Accueil + FAIT + pastille Accord | Home | ✅ Ultra + sim. S26 |
| Bloc retard/accord replié | Fiche ajout/édition | ✅ sim. |
| IBAN / Écrire / frais / multi-photos | Fiche accord | ✅ Écrire Gmail |
| Mes dossiers + pending | Menu si N>0 | partiel |
| Ranger sans plan → conversion | Ajout / bandeau | ⏳ à rejouer campagne |
| Mettre de côté | Réglages | ⏳ |
| Résumé texte | Mes dossiers / Réglages | ⏳ |
| Backup / restore schéma 3→7 | Réglages | ⏳ doigt |
| Copier 12 chiffres → coller | Communication | ⏳ doigt |

## Enchaînement

```text
Campagne produit fini (matrice)
  → Correcteur ×2 (Hostile Multi-dossiers + Null Base vide)
  → Visuel ×2 (Chiant Maman + Perf Alex)
  → Produit ×2 (Fidélité Maman + Médiateur)
  → Senior (Maman)
  → Validation + push (demande humaine)
```

Reset neuve : `dist/verify/RESET_TESTS_FROM_ZERO.md`.  
APK famille = **release** signée (`assembleRelease` → `adb install -r`), pas debug.

## Rapports

Sous `dist/verify/` : toujours `Profil :` + `Persona :` + cases matrice cochées.
