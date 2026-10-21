# Campagne produit fini — toute l’app, tous les regards

Version cible : **`0.1.0-6.36`** (schéma Room **7**, lots A–F).  
À lancer **avant** un sideload famille, ou après une grosse passe polish.  
Les prompts experts restent dans ce dossier ; cette campagne dit **quoi** exercer, pas seulement « regarde le diff ».

## 1. Personae (profils utilisateurs réels)

| Persona | Qui | Objectif de vie | Ce qu’il ne doit **pas** voir |
|---------|-----|-----------------|------------------------------|
| **Maman** | S26, carnet simple | Loyer, factures, FAIT, courses | Mot huissier, Mes dossiers (0 accord), Mettre de côté si jamais activé, euros de dette sur l’accueil |
| **Alex** | Ultra, mêmes écrans | Retards + accords + mail + virement | Compteur « 3 sur 12 », total dettes en hero, pastille Huissier |
| **Fils/fille installateur** | Sideload + explique en 2 min | SAF Documents, backup, bio optionnelle | Jargon SAF / WorkManager |
| **Médiateur / mois cassé** | Déficit, papier à envoyer | Résumé texte partageable honnête | Faux PDF CPAS, date de fin sans « si » |
| **Base vide** | 1ʳᵉ install / `pm clear` | Onboarding → Home vide → + Ajouter | Menus fantômes (dossiers, résumé) |
| **Multi-dossiers** | ≥2 accords + 1 courrier sans plan | Menu Mes dossiers, bandeau, conversion | Confusion courrier ≠ plan |

Chaque agent prend **un** persona (+ un profil tempérament de `PROFILS_TESTEURS.md`).

## 2. Matrice minimale (ne pas survoler)

Cocher dans le rapport. Un « ✅ » = geste **fait** sur device (ou code+preuve si pas de téléphone, noté clairement).

### Carnet (Maman + Base vide)

| # | Scénario | Attendu |
|---|----------|---------|
| C1 | Home : Reste + À payer sans scroll | Lisibles ; factures avant courses |
| C2 | FAIT / Remettre | Badge date ; Annuler snackbar longtemps |
| C3 | + Ajouter facture mensuelle | Grille jours, virgule, ce mois-ci seul par défaut |
| C4 | Divers clavier ouvert | +/− atteignables |
| C5 | Menu | Réglages + mois ; **pas** Mes dossiers si 0 accord et 0 courrier |
| C6 | Bloc « retard ou accord » | Replié ; ouvrir puis quitter sans reste → **rien** créé |

### Dossiers / accords (Alex)

| # | Scénario | Attendu |
|---|----------|---------|
| D1 | Créer accord (cause + reste) | Pastille Accord ; pas le reste € sur Home |
| D2 | FAIT 30 € sur forfait 50 € | Reste baisse de **30**, pas 50 |
| D3 | Soldé | Plus de ligne mois suivant ; historique FAIT reste |
| D4 | Copier communication structurée | Presse-papiers = **12 chiffres** si modulo 97 OK |
| D5 | Copier IBAN | Sans espaces, majuscules |
| D6 | Écrire | Gmail/Mail avec brouillon ; rien stocké dans l’app |
| D7 | Frais + 2ᵉ photo | Cumul ; Voir / Enlever courriers |
| D8 | Photo modèle → mois suivant | Visible sans reprendre (case « aussi mois suivants » à l’édition) |

### Courriers sans plan (lot D)

| # | Scénario | Attendu |
|---|----------|---------|
| P1 | Ranger sans plan | Bandeau accueil « Voir » ; pas dans À payer |
| P2 | Convertir → accord | Disparaît des pending ; apparaît comme dossier |
| P3 | Menu | Mes dossiers (N) si pending ou accords |

### Mettre de côté (lot E)

| # | Scénario | Attendu |
|---|----------|---------|
| E1 | Activer montant + jour | Ligne facture ordinaire dans le mois |
| E2 | Montant trop haut | Alerte **déficit avant** enregistrement |
| E3 | FAIT | « déjà viré — pas disponible ici » sur fiche |
| E4 | Retirer | Plus de ligne ; accueil comme avant si plus d’activation |

### Résumé (lot F)

| # | Scénario | Attendu |
|---|----------|---------|
| F1 | Préparer un résumé | Texte : revenus, factures, dossiers 4 colonnes, mis de côté **à part** |
| F2 | Partage | Phrase créancier/conseiller ; pas d’identité inventée |
| F3 | Bouton visible | Mes dossiers ; aussi Réglages si déficit ou dossier |

### Fiabilité / Samsung

| # | Scénario | Attendu |
|---|----------|---------|
| S1 | Backup / Envoyer copie | Archive ; restore dit si photos revenues |
| S2 | Restore schéma ancien | Schéma 3 encore OK ; 7 ramène pending |
| S3 | Notif multi-factures | Bonne ligne / action FAIT + Annuler |
| S4 | Hauteur courte ~780 dp ou police large | Accord sur ligne du nom (compact) ; FAIT ≥ 48 dp |
| S5 | Bio OFF / HW KO | Pas de blocage d’accès |

## 3. Enchaînement recommandé (3–4 sessions)

| Session | Expert (prompt) | Persona + profil | Focus matrice |
|---------|-----------------|------------------|---------------|
| 1a | Correcteur | Multi-dossiers + Hostile-doux | D1–D8, P1–P3, C2, double-tap |
| 1b | Correcteur | Base vide + Null | C1–C6, onboarding, Menu sans dossiers |
| 2a | Visuel | Maman + Chiant | S4, Home, pastilles, 48 dp |
| 2b | Visuel | Alex + Perf | Scroll liste longue, Menu, caméra |
| 3a | Produit | Fidélité 60 j + Maman | Abandon, charge saisie, zéro pixel |
| 3b | Produit | Médiateur + Fidélité | F1–F3, E1–E4, honnêteté déficit |
| 4 | Senior One UI | Maman senior | Peurs, jargon, Device Care, restore |
| fin | Validation | — | Claims 6.36, Gradle `--rerun-tasks`, rapports P0/P1 |

Reset neuve si besoin : [`../../dist/verify/RESET_TESTS_FROM_ZERO.md`](../../dist/verify/RESET_TESTS_FROM_ZERO.md).

## 4. Interdits campagne

- Inventer une feature (banque, cloud, graphiques, huissier, snowball).
- Valider « tout OK » sans cocher la matrice des personae choisis.
- Installer un **debug.apk** sur le téléphone famille (signature) → **release** signée.
- Un seul Gradle à la fois ; `COPYFILE_DISABLE=1`.

## 5. Livrable global

Chaque session → un rapport sous `dist/verify/` avec en-tête :

```text
Profil : …
Persona : Maman | Alex | …
Version : 0.1.0-6.36
Matrice : C1 ✅ C2 ❌ …
```

Validation lit **tous** les rapports avant GO sideload.
