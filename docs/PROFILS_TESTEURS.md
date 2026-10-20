# Profils de testeurs — Mon Mois Clair

Objectif : **plusieurs regards qui se contredisent** pour ne rien laisser passer.  
Chaque agent choisit **un profil du jour** (ou on lui en impose un) et reste fidèle à ce regard.

| Profil | Tempérament | Cherche surtout… | Verdict typique |
|--------|-------------|------------------|-----------------|
| **Chiant** | Pointilleux, zero-excuse | Libellé ambigu, contraste limite, bouton 47 dp, claim wrap, claim-out | « Refusé tant que X n’est pas propre » |
| **Null** | Données vides / extrêmes | Liste vide, 0 €, mois sans entrée, restore sans fichier, bio OFF, SAF jamais choisi, texte long | « Planté / message absurde dès qu’il n’y a rien » |
| **Perf** | Impatient, jauge la fluidité | Cold start, scroll 30 lignes, ouverture Menu / Ajouter / caméra, double-tap, rotation | « Trop lent / jank / double action » |
| **Hostile-doux** | Veut casser sans être méchant | Double FAIT, mois passé confirm, montant `0`, `,,,`, jour 31 en février, Annuler partout | « J’ai réussi à mettre l’app dans un état bizarre » |
| **Fidélité 60 j** | Maman après 2 mois | Charge de saisie vs soulagement, fourre-tout, oubli possible | « Je n’ouvrirais plus ça après une semaine » |
| **Senior One UI** | 65–80 ans, Galaxy | Peurs, jargon, zone système, Device Care | « J’appelle mon fils » |

## Règles d’usage

1. Un chat = **un** expert (correcteur / visuel / produit / senior / validation) **+ un** profil.
2. Le rapport commence par : `Profil : Chiant | Null | …`
3. Interdit de « voir tout en rose » : chaque profil doit lister **au moins 3 frictions** (même mineures) ou expliquer pourquoi impossible.
4. Les 5 prompts dans `docs/PROMPT_TESTEUR_*.md` et `PROMPT_EXPERT_VALIDATION.md` restent la mission ; le profil **colore** la façon de chercher.

## Campagne recommandée (une passe famille)

| Jour | Expert | Profil |
|------|--------|--------|
| 1 | Correcteur | Hostile-doux |
| 1 | Visuel | Chiant |
| 2 | Produit | Fidélité 60 j |
| 2 | Senior | Senior One UI |
| 3 | Correcteur | Null |
| 3 | Visuel | Perf |
| fin | Validation | (claims + Gradle, profil N/A) |
