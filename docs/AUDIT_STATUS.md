# Audits premium — statut

Lots visuels 6.16 → 6.28 **clos côté code**.  
Le journal du carnet, lui, est à **6.31** (`0.1.0-6.31`, 183 tests annoncés à la clôture de B). Cet en-tête ne décrit pas le schéma 6 du lot C, qui n’est pas clos. Produit dossiers : [`AUDIT_ALEX_MAMAN_2026-10-21.md`](AUDIT_ALEX_MAMAN_2026-10-21.md) partie 7.  
Synthèse visuelle : [`AUDIT_PREMIUM.md`](AUDIT_PREMIUM.md) (figée plus tôt, en-tête 6.28).

| Vague | État |
|-------|------|
| Origine / suite / passe 3 | ✅ code |
| A01–A12, B01–B05, C01–C05, D01–D02, E01–E08 | ✅ code |
| 6.27 sticky / Menu / fréquence / photo fiche | ✅ code + Galaxy |
| 6.28 Voir la photo (bouton) | ✅ code + Galaxy |
| Smoke Galaxy | ✅ partiel — table ci-dessous |

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

Ne pas rouvrir l’audit pour du code déjà tenu : noter seulement le smoke ici ou dans `PROGRESS_LOG.md`.
