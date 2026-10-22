# Audits premium — statut smoke

Lots visuels 6.16 → 6.28 **clos côté code**.  
Journal produit fermé : **6.52** (`0.1.0-6.52`, schéma Room **8**, unitaires **280/280**).  
Phrase rythme : domaine + UI fiche / Mes dossiers (pas de smoke device dédié pour cette phrase seule).  
Dossiers / accords (figé) : [`archive/AUDIT_ALEX_MAMAN_2026-10-21.md`](archive/AUDIT_ALEX_MAMAN_2026-10-21.md) §8.  
Punch list visuelle détaillée : [`archive/AUDIT_PREMIUM.md`](archive/AUDIT_PREMIUM.md) (figée).

| Vague | État |
|-------|------|
| Origine / suite / passe 3 | ✅ code |
| A01–A12, B01–B05, C01–C05, D01–D02, E01–E08 | ✅ code |
| 6.27 sticky / Menu / fréquence / photo fiche | ✅ code + Galaxy |
| 6.28 Voir la photo (bouton) | ✅ code + Galaxy |
| Smoke Galaxy | ✅ partiel — table ci-dessous |
| Lot C « Écrire » (mail) | ✅ smoke 2026-10-21 (Gmail Compose) |
| 6.33 install + EuroFormat + Menu | ✅ smoke retour 2026-10-21 |
| 6.36 + hauteur Accord / FAIT | ✅ smoke Ultra 2026-10-21 |
| Hauteur courte type S26 | ✅ sim. 360×780 dp + texte 1,3 (2026-10-21) |
| 6.43–6.52 | ✅ code. Smoke **partiel** S25 le 2026-10-22 (accueil nuit, étape 6.51, partage du PDF résumé). OCR, widget posé, PDF facture, S26 : pas encore |

## Smoke SM-S938B — 2026-10-21

| # | Check | Résultat |
|---|-------|----------|
| 1 | Notification ≥4 factures → bonne ligne | ✅ manuel (humain) |
| 2 | Échéance trimestrielle : nom seul | ✅ manuel (humain) + fréquence éditable 6.27 |
| 3 | Mois vide : un seul teal | ✅ |
| 4 | Recherche « zzzz » | ✅ |
| 5 | Retrait courses > total | ✅ |
| 6 | Splash / cold start | ✅ |
| 7 | Menu = Réglages + mois (plus de doublons) | ✅ 6.27 |
| 8 | Envoyer copie → archive | ✅ |
| 9 | Années groupées + pastilles En retard / Prélèvement | ✅ |
| 10 | Biométrie | ✅ manuel (humain) |
| 11 | Sticky « Après les FAIT » / Après courses | ✅ 6.27 · libellé Senior 2026-10-21 |
| 12 | Voir la photo sur la ligne | ✅ 6.28 |
| 13 | Install **6.33** + Reste avec espace fine | ✅ retour pause |
| 14 | Menu sans Mes dossiers si 0 accord | ✅ |
| 15 | Réglages affiche version 6.33 | ✅ |
| 16 | « Écrire » ouvre un vrai brouillon Mail | ✅ fiche accord + e-mail → Gmail Compose |
| 17 | Ligne Accord : FAIT reste sur la carte, cible ≥ 48 dp | ✅ Ultra |
| 18 | Install **6.36** (+ polish release après-midi) | ✅ |
| 19 | Hauteur courte ~780 dp + police 1,3 : Accord / FAIT ≥ 48 dp | ✅ |
| 20 | Menu (Réglages / Mes dossiers) sous chrome compact | ✅ |
| 21 | Fiche ajout : bloc « C’est un retard ou un accord ? » replié (▾) | ✅ |

## Smoke SM-S938B — 2026-10-22 (6.39 → 6.42)

| # | Check | Résultat |
|---|-------|----------|
| 22 | Menu → À venir (3 mois lecture seule) | ✅ 6.39 |
| 23 | Mes dossiers : En cours / Soldés + « Vers … » | ✅ 6.39 |
| 24 | Réglages → « Un souci avec l’app ? » | ✅ 6.39 |
| 25 | Sticky Mis de côté + sheet Courses | ✅ 6.39 |
| 26 | Après courses négatif = brique ; Courses = lien | ✅ 6.40 |
| 27 | Flèche › / Menu → mois futur : aperçu lecture seule | ✅ 6.41 |
| 28 | Re-sync template sur mois futur déjà ouvert ; Soldés repliés | ✅ 6.42 |

## Smoke SM-S938B — 2026-10-22 (release 6.51, mise à jour depuis 6.44)

| # | Check | Résultat |
|---|-------|----------|
| 1 | Accueil octobre, nuit (téléphone sombre, Apparence = comme le téléphone) | ✅ Reste, lignes, FAIT forêt, pastille « En retard » |
| 2 | Réglages → pied de page | ✅ « étape 6.51 » |
| 3 | « PDF du résumé » | ✅ feuille `mon-mois-clair-resume.pdf` dans le partage. Rien envoyé |
| 4 | Widget | Fournisseur installé. Pas posé sur l’accueil |
| 5 | OCR `+++`, PDF à côté d’une photo, S26 | Pas faits dans ce passage |

## Pas encore fumés au doigt

| Lot | À vérifier sur Galaxy |
|-----|------------------------|
| 6.43–6.45 | Fondu FAIT et ombre sticky (les formes de l’accueil nuit ont été vues, pas le fondu) |
| 6.46 | Basculer Clair / Sombre à la main (le mode « comme le téléphone » était déjà sombre) |
| 6.47 | « Lire +++ sur la photo » |
| 6.49 | Poser le widget + PDF dès Ajouter |
| 6.50 | Ouvrir le PDF résumé dans Mail ; widget en nuit sur l’accueil |

Ne pas rouvrir l’audit premium pour du code déjà tenu : noter seulement le smoke ici ou dans `PROGRESS_LOG.md`.
