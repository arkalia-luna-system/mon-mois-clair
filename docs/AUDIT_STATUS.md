# Audits premium — statut

Les trois audits du 2026-10-20 (origine, suite, passe 3) sont **clos côté code** (étapes 6.16 → 6.22).  
Fichiers historiques : [`docs/archive/`](archive/).  
Dernière mesure tests : **152/152** (`0.1.0-6.22`).

| Vague | Contenu | État |
|-------|---------|------|
| Origine + 6.16–6.18 | Mois d’ajout, Reste, photo, FAIT contour, typo… | ✅ |
| Suite S01–S04, S06 | Photo in-app, snackbar, polices, archive ZIP | ✅ |
| Passe 3 | Ancre, rappels, divers, notif FAIT, Home | ✅ |
| Smoke Galaxy (P3-Q04 / S05) | Checklist au doigt | ⏸ ouvert |

## Smoke encore à faire (téléphone)

1. Notification avec ≥4 factures → bonne ligne  
2. Échéance trimestrielle : corriger le nom → mois futurs intacts  
3. Mois vide : un seul bouton teal  
4. Recherche « zzzz » : phrase + totaux  
5. Retrait divers > total : message honnête  
6. Splash : M teal entier  
7. Menu → Restaurer / Comment ça marche  
8. Envoyer une copie → ZIP avec photos ; restaurer ce ZIP  

Ne pas rouvrir les audits archivés pour du code déjà tenu : noter seulement le smoke ici ou dans `PROGRESS_LOG.md`.
