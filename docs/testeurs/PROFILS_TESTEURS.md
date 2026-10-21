# Profils de testeurs — Mon Mois Clair

Objectif : **plusieurs regards qui se contredisent** pour ne rien laisser passer.  
Campagne complète (scénarios A–F) : [`CAMPAGNE_PRODUIT_FINI.md`](CAMPAGNE_PRODUIT_FINI.md).

Chaque agent choisit **un tempérament** + **un persona produit**, et reste fidèle à ce regard.

## Tempéraments (comment chercher)

| Profil | Tempérament | Cherche surtout… | Verdict typique |
|--------|-------------|------------------|-----------------|
| **Chiant** | Pointilleux, zero-excuse | Libellé ambigu, contraste limite, bouton 47 dp, claim wrap | « Refusé tant que X n’est pas propre » |
| **Null** | Données vides / extrêmes | Liste vide, 0 €, mois sans entrée, restore sans fichier, bio OFF, `,,,` | « Planté / message absurde dès qu’il n’y a rien » |
| **Perf** | Impatient, fluidité | Cold start, scroll 30 lignes, Menu / Ajouter / caméra, double-tap | « Trop lent / jank / double action » |
| **Hostile-doux** | Casse sans être méchant | Double FAIT, mois passé, jour 31, Annuler partout, conversion pending 2× | « État bizarre / double écriture » |
| **Fidélité 60 j** | Abandon après 2 semaines | Charge saisie vs soulagement, fourre-tout, oubli possible | « Je n’ouvrirais plus » |
| **Senior One UI** | 65–80 ans, Galaxy | Peurs, jargon, zone système, Device Care | « J’appelle mon fils » |

## Personae produit (qui utilise l’app)

| Persona | Écran mental | Doit réussir… | Échec si… |
|---------|--------------|---------------|-----------|
| **Maman** | Carnet du mois | FAIT, Reste, courses, Menu sans dossiers | Mot huissier / dette € / onglet nouveau sans dossier |
| **Alex** | Même UI + retards | Accord, IBAN, Écrire, reste réel, soldé | « 3 sur 12 », total dettes en hero |
| **Installateur** | Sideload famille | SAF Documents, backup, bio OFF claire | Jargon, Download comme conseil |
| **Médiateur** | Mois impossible | Résumé partageable, mis de côté à part | PDF CPAS, promesse de date |
| **Base vide** | 1ʳᵉ ouverture | Home vide → Ajouter ; zéro pixel dossier | Entrées Menu fantômes |
| **Multi-dossiers** | ≥2 accords + pending | Bandeau, Mes dossiers, conversion | Courrier compté dans À payer |

## Règles d’usage

1. Un chat = **un** expert (correcteur / visuel / produit / senior / validation) **+ un** tempérament **+ un** persona.
2. Le rapport commence par :
   ```text
   Profil : Chiant | Null | …
   Persona : Maman | Alex | …
   ```
3. Interdit de « voir tout en rose » : **≥ 3 frictions** (même mineures) ou expliquer pourquoi impossible.
4. Exercer la **matrice** de `CAMPAGNE_PRODUIT_FINI.md` pour le persona choisi (pas seulement le dernier diff).
5. Les prompts `PROMPT_*.md` restent la mission ; profil + persona **colorent** la chasse.

## Campagne recommandée (passe famille)

| Jour | Expert | Profil | Persona |
|------|--------|--------|---------|
| 1 | Correcteur | Hostile-doux | Multi-dossiers |
| 1 | Correcteur | Null | Base vide |
| 2 | Visuel | Chiant | Maman |
| 2 | Visuel | Perf | Alex |
| 3 | Produit | Fidélité 60 j | Maman |
| 3 | Produit | Fidélité 60 j | Médiateur |
| 4 | Senior | Senior One UI | Maman |
| fin | Validation | — | claims + Gradle |
