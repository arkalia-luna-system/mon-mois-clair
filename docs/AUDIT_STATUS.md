# Audits premium — statut smoke

Lots visuels 6.16 → 6.28 **clos côté code**.  
Journal produit : **6.33** (`0.1.0-6.33`, schéma Room **6**, **208/208** tests).  
Dossiers / accords : [`AUDIT_ALEX_MAMAN_2026-10-21.md`](AUDIT_ALEX_MAMAN_2026-10-21.md) partie 7.  
Punch list visuelle détaillée : [`archive/AUDIT_PREMIUM.md`](archive/AUDIT_PREMIUM.md) (figée).

| Vague | État |
|-------|------|
| Origine / suite / passe 3 | ✅ code |
| A01–A12, B01–B05, C01–C05, D01–D02, E01–E08 | ✅ code |
| 6.27 sticky / Menu / fréquence / photo fiche | ✅ code + Galaxy |
| 6.28 Voir la photo (bouton) | ✅ code + Galaxy |
| Smoke Galaxy | ✅ partiel — table ci-dessous |
| Lot C « Écrire » (mail) | ⏳ code OK · geste réel à faire dès qu’un accord a un e-mail |
| 6.33 install + EuroFormat + Menu | ✅ smoke retour 2026-10-21 |

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
| 11 | Sticky Sans les futures / Après courses | ✅ 6.27 |
| 12 | Voir la photo sur la ligne | ✅ 6.28 |
| 13 | Install **6.33** + Reste avec espace fine | ✅ retour pause |
| 14 | Menu sans Mes dossiers si 0 accord | ✅ |
| 15 | Réglages affiche version 6.33 | ✅ |
| 16 | « Écrire » ouvre un vrai brouillon Mail | ⏳ (pas d’accord+e-mail sous la main) |

Ne pas rouvrir l’audit premium pour du code déjà tenu : noter seulement le smoke ici ou dans `PROGRESS_LOG.md`.
