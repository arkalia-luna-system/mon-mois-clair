# Audits premium — statut

Lots 6.16 → 6.23 **clos côté code**.  
Synthèse vivante : [`AUDIT_PREMIUM.md`](AUDIT_PREMIUM.md) (A01–C02 corrigés + liste téléphone).  
Mesure tests : **157/157** · version `0.1.0-6.23`.

| Vague | État |
|-------|------|
| Origine / suite / passe 3 | ✅ code |
| A01–A04, B01–B02, C01–C02 | ✅ code (voir `AUDIT_PREMIUM.md`) |
| Smoke Galaxy | ✅ partiel — table ci-dessous |

## Smoke SM-S938B — 2026-10-20 (`0.1.0-6.23`)

| # | Check | Résultat |
|---|-------|----------|
| 1 | Notification ≥4 factures → bonne ligne | ⏸ |
| 2 | Échéance trimestrielle : nom seul | ⏸ |
| 3 | Mois vide : un seul teal | ✅ |
| 4 | Recherche « zzzz » | ✅ |
| 5 | Retrait courses > total | ✅ |
| 6 | Splash / cold start | ✅ |
| 7 | Menu outils / Comment / Restaurer | ✅ (fix Outils hors écran) |
| 8 | Envoyer copie → ZIP | ✅ `mon-mois-clair-securite.zip` |
| 9 | Menu années groupées / pastilles / courses | ⏸ à rejouer après install 6.23 complète |
| 10–11 | Phrases échec / Voir le mois / version affichée | ⏸ |
