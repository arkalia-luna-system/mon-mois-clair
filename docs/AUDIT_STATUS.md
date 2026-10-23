# Audits premium — statut smoke

**Version / tests vivants :** lire [`PROGRESS_LOG.md`](../PROGRESS_LOG.md) et le README (ne pas figer un `versionName` ici).  
Lots visuels 6.16 → 6.28 **clos côté code**. Tables ci-dessous = historique smoke Galaxy (figé).  
Dossiers / accords (figé) : [`archive/AUDIT_ALEX_MAMAN_2026-10-21.md`](archive/AUDIT_ALEX_MAMAN_2026-10-21.md) §8.  
Punch list visuelle : [`archive/AUDIT_PREMIUM.md`](archive/AUDIT_PREMIUM.md) (figée).

**Contact support (6.75) :** Menu Accueil QuietLink « Un souci ? Écrire à Arkalia Système » — **pas** dans Réglages / Aide.

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
| 6.43–6.55 | ✅ code. Smoke S25 du 2026-10-22 : accueil nuit, widget, OCR, PDF. Photo loyer3 : revalider après 6.54/6.55. S26 : pas encore |
| 6.78 | ✅ code + smoke S25 **partiel** adb — [`audits/2026-10-23-smoke-device.md`](audits/2026-10-23-smoke-device.md). S26 / notif / widget doigt : ouverts |

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
| 24 | Contact « Un souci… » (était Réglages 6.39 ; **Menu** depuis 6.72) | ✅ code 6.72 · smoke à revalider |
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
| 4 | Widget page 4 | ✅ Posé. « Reste » + montant + ligne en retard. Le tap ouvre le carnet. Fond encre (téléphone sombre) |
| 5 | « Lire +++ sur la photo » | ✅ Sur une facture du 31 : la photo s’ouvre, l’OCR répond qu’il n’y a pas de `+++` valide. Rien enregistré |
| 6 | PDF à côté de la photo | ✅ « Joindre un PDF » puis « PDF prêt. Il s’ajoute à côté de la photo ». Courrier retiré, Annuler. Aucun PDF dans `receipts/` (seulement des webp) |
| 7 | Photo de loyer3 | Le formulaire propose la photo, l’image ne s’ouvre pas. Aucun montant touché |
| 8 | S26, fondu FAIT, bascule Clair à la main | Pas faits |

## Pas encore fumés au doigt

| Lot | À vérifier sur Galaxy |
|-----|------------------------|
| 6.43–6.45 | Fondu FAIT (les formes de l’accueil nuit ont été vues, pas le fondu) |
| 6.46 | Basculer Clair à la main (le mode « comme le téléphone » était déjà sombre) |
| 6.49 | PDF dès Ajouter (le PDF à l’édition a été vu, puis retiré) |
| S26 | Pas en main |

Ne pas rouvrir l’audit premium pour du code déjà tenu : noter seulement le smoke ici ou dans `PROGRESS_LOG.md`.
