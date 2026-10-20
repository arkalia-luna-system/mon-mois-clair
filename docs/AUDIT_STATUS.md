# Audits premium — statut

Lots 6.16 → 6.26 **clos côté code**.  
Synthèse : [`AUDIT_PREMIUM.md`](AUDIT_PREMIUM.md).  
Mesure tests : **162/162** · version `0.1.0-6.26`.  
Smoke partiel : notif multi-factures + trimestriel encore ⏸ ; le reste listé ci-dessous tient sur Galaxy.

| Vague | État |
|-------|------|
| Origine / suite / passe 3 | ✅ code |
| A01–A12, B01–B05, C01–C05, D01–D02, E01–E08 | ✅ code |
| Smoke Galaxy | ✅ partiel — table ci-dessous |

## Smoke SM-S938B — 2026-10-20

| # | Check | Résultat |
|---|-------|----------|
| 1 | Notification ≥4 factures → bonne ligne | ⏸ |
| 2 | Échéance trimestrielle : nom seul | ⏸ |
| 3 | Mois vide : un seul teal | ✅ |
| 4 | Recherche « zzzz » | ✅ |
| 5 | Retrait courses > total | ✅ |
| 6 | Splash / cold start | ✅ |
| 7 | Menu outils / Comment / Restaurer | ✅ |
| 8 | Envoyer copie → ZIP | ✅ |
| 9–14 | Relecture A01–E08 sur install 6.24 | ⏸ |

Ne pas rouvrir l’audit pour du code déjà tenu : noter seulement le smoke ici ou dans `PROGRESS_LOG.md`.
