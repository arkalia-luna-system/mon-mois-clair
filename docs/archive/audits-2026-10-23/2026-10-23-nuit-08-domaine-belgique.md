> **ARCHIVÉ / FIGÉ** (2026-10-26) — ne plus lire pour l’état code. Vérité = `PROGRESS_LOG.md` + PRIVACY/SECURITY + audits `final-0*`.

# Audit métier — factures belges (EPC · +++ · prélèvement · accords · hors mois)

| | |
|--|--|
| **Date** | 2026-10-23 (nuit) |
| **Profil** | Expert métier factures belges (OGM/VCS, QR EPC/Girocode, prélèvement SEPA, accords/retards, échéances hors mois, dossiers courrier) |
| **État code** | `0.1.0-6.75` · schéma Room **8** · **305/305** unit · claim premium « dans la main » **non** |
| **Sources** | Code (`EpcQrParser`, `BelgianStructuredCommunication`, `ToggleExpenseDone`, `MonthCycleEngine`, `AgreementFuturesSync`, `DossierRemaining`, `PrepareMonthSummary`, dossiers / pending letters, rappels) · `CHARTE_ET_ARCHITECTURE.md` · `HowItWorksCopy` · archives Accords/Senior |
| **Portée** | **AUDIT ONLY** — aucun commit code app. Améliorations **charte-compatibles** seulement (pas de banque, pas d’OCR montant, pas de total dettes sur Accueil). |

---

## 0. Verdict en une phrase

Le socle belge est **crédible** (mod 97, copie 12 chiffres, FAIT bloqué en aperçu, accords avec reste, hors-mois, PDF résumé). Ce qui casse la confiance « app chère » n’est plus l’absence de features : ce sont des **pièges de vraie vie** où l’app a l’air sûre alors que le geste banque / le montant du mois peuvent être faux.

---

## 1. Ce qui tient déjà (ne pas rouvrir)

| Geste belge | Preuve code | Pourquoi ça rassure |
|-------------|-------------|---------------------|
| Communication structurée mod 97 | `BelgianStructuredCommunication` + tests | Copie → **12 chiffres** si valide ; sinon « Vérifiez sur le courrier. » |
| OCR `+++` seulement | `findInOcrText` + contexte OGM/VCS | Ne fabrique pas un montant → Reste intact |
| QR « facture belge » (pas le mot EPC à l’écran) | `EpcQrParser` + messages UI | Nom / montant / remittance / IBAN lus |
| FAIT mois passé avec confirm | `HomeViewModel` + dialogue | Oubli du 28 = OK ; pas de FAIT furtif |
| FAIT interdit en aperçu futur | `ToggleExpenseDoneResult.FuturePreviewBlocked` | Calendrier 24+24 = lecture seule tant que ce n’est pas le mois |
| Prélèvement | pastille + corps notif distincts | « Vérifiez le prélèvement » ≠ « Pensez à payer » |
| Accords | `openingRemaining` + `AgreementFuturesSync` + pastille **Accord** sans € sur Accueil | Charte tenue ; soldé coupe les futures |
| Hors mois / annuel | `ListOffMonthPeriodics` + Aide « Sur une année » | Assurance 1×/an visible hors Reste du mois |
| Courrier sans plan | `pending_letters` + conversion dossier | Retard réel (mail puis virement) sans forcer 12 mensualités |
| Résumé PDF | même texte que Prepare · 18/22 pt | Médiation / créancier sans formulaire CPAS |
| Jour 31 → clamp | `MonthDayClamp` | Février ne casse pas l’échéance |

---

## 2. Bugs / edge métier (confiance)

Légende sévérité : **P0** = demain (argent / banque / Reste trompeur) · **P1** = bientôt · **P2** = polish métier.

### B01 — Communication `+++` **propagée** aux mois suivants — **P0**

**Vu.** Lot A : `paymentNote` du modèle suit **toujours** les instances TODO déjà créées, et `MonthCycleEngine` recopie le modèle à chaque nouveau mois — même **sans** « Aussi les mois suivants » (photo, elle, exige la case).

**Réalité belge.** Loyer / huissier / plan d’étude : la ref peut être stable. **Eau, énergie, télécom, mutuelle, taxes** : chaque facture a un **nouvel** OGM. Recopier le `+++` d’octobre en novembre = coller une communication **morte** (rejet banque) ou, pire, une ref d’une autre période.

**Effet « app chère ».** L’accueil propose « Copier » avec assurance. L’utilisatrice croit que c’est prêt pour Bancontact / l’app banque. Ce n’est pas un bug UI : c’est un **faux positif métier**.

**Piste charte-compatible (pas codée ici).**
- Ne pas changer le Reste ni ajouter un dashboard.
- Option A : à la matérialisation d’un mois **nouveau**, laisser `paymentNote` vide si le modèle a une `+++` structurée (garder la photo / le libellé) + tip fiche « Chaque facture a souvent une nouvelle communication ».
- Option B : garder la propagation **seulement** pour dossiers (`openingRemaining != null`) et prélèvements ; factures ordinaires = saisie / QR / OCR chaque mois.
- Aide : une phrase honnête sous « Facture papier » (loyer ≠ Engie).

### B02 — Dernière mensualité d’accord : 1ʳᵉ ligne **sans** plafond reste — **P0**

**Vu.** `DossierRemaining.installmentCents` plafonne bien forfait ↔ reste dans `MonthCycleEngine`, « À venir », hors-mois. Mais **création** :
- `AddEntryUseCase` : instance = `amountCents` saisi, **pas** `installmentCents(opening, …)` ;
- `ConvertPendingLetterToDossierUseCase` : idem (`monthlyAmountCents` brut).

**Scénario.** Reste dû 30 €, versement prévu 50 € → Accueil affiche **50 €** à payer ce mois, Reste après factures trop bas de 20 €, jusqu’à édition manuelle. Le mois **suivant** (si généré via cycle) serait correct — le mois où elle convertit le courrier, non.

**Piste.** À l’insert initial (add + convert), `amountCents = installmentCents(forfait, opening, paid=0, fees)`. Aligné tests déjà présents sur le cycle.

### B03 — Parseur EPC fragile si ligne « Purpose » absente — **P0**

**Vu.** `EpcQrParser` lit remittance aux index **9 puis 10** (après BIC/nom/IBAN/montant). Le sample VIVAQUA a `CHAR` en purpose → OK.

**Réalité.** Beaucoup de QR belges **omettent** Purpose (ou n’ont pas de ligne vide). Alors la communication structurée glisse à l’index 8 : le parseur peut renvoyer une **remittance vide** ou le libellé libre (« Facture eau ») à la place du `+++`.

**Effet.** Scan « réussi » (nom + montant + IBAN) mais **sans** communication → elle retape / OCR, ou enregistre une fausse ref. Confiance QR = cœur différenciateur BE.

**Piste.** Après montant : sauter purpose vide / 4 lettres ; chercher d’abord une ligne `+++…+++` / `***…***` / 12 chiffres mod 97 dans le reste du payload ; tests avec QR **sans** purpose et avec remittance non structurée.

### B04 — IBAN du QR **jeté** hors dossier — voulu, mais piège — **P1**

**Vu.** `applyEpc` remplit `iban` en state. Persistance IBAN seulement si accord / section dossier (`syncCreditor` / `openingRemaining != null`). Facture ordinaire : IBAN lu puis **oublié** à l’enregistrement (charte Lot C = IBAN sur dossier).

**Réalité.** Engie / Vivaqua : virement manuel = IBAN + `+++` chaque mois. Sans dossier, elle doit rouvrir la photo. Le QR a pourtant fourni l’IBAN.

**Piste charte-compatible.** Ne pas mettre l’IBAN sur Accueil. Soit tip après scan ordinaire « IBAN non gardé — ouvrez Retard ou accord si vous en avez besoin » ; soit (plus fort, arbitrage produit) permettre IBAN optionnel sur template **sans** `openingRemaining` — hors Accueil, fiche seule. **Ne pas** rouvrir sans décision charte.

### B05 — Résumé PDF / texte : dossiers sans IBAN — **P1**

**Vu.** `PrepareMonthSummaryUseCase` : qui / pour quoi / versement / reste / rythme / **Référence** ; pas d’IBAN ; phrase « photos dans l’application ».

**Réalité médiation / mail créancier.** On demande souvent IBAN + communication. L’app a l’IBAN en base dossier ; le PDF qu’elle envoie ne l’a pas → elle croit l’avoir « tout mis ».

**Piste.** Si `tpl.iban` non vide : ligne `IBAN : …` (compact, checksum OK seulement). Toujours pas de « total dettes ». Charte OK (résumé dédié, pas Accueil).

### B06 — Aperçu annuel / futur : montant figé du modèle — **P1**

**Vu.** Calendrier / À venir matérialisent ou projettent le **forfait modèle**. Assurance +3 % l’an prochain : aperçu menteur jusqu’à édition.

**Piste.** Pas de FAIT en futur (déjà). Tip sticky aperçu ou fiche lecture seule : « Montant d’après la dernière saisie — à vérifier sur le nouveau courrier. » Pas de promesse d’indexation.

### B07 — Remettre après solde d’accord — **P1** (comportement à surveiller)

**Vu.** `AgreementFuturesSync.afterToggle` : Remettre (TODO) peut **recréer** des futures ; FAIT qui solde les efface.

**Risque.** Remettre « par erreur » un dernier FAIT → les mois suivants réapparaissent avec versements. Correct mathématiquement ; surprenant si elle pensait « dossier fini ».

**Piste.** Snackbar Remettre sur ligne d’accord soldé : « Le dossier n’est plus soldé — les mois suivants peuvent revenir. » (confirm déjà pour mois passé ; message métier en plus.)

### B08 — Prélèvement + FAIT = confirmation humaine — **P2** (OK si dit)

**Vu.** Pastille Prélèvement + notifs « vérifiez ». FAIT reste manuel. C’est le bon modèle (pas d’open banking).

**Risque.** Optimiste qui coche FAIT **avant** que le débit apparaisse, puis contestation banque → Remettre. Aide le dit en partie.

**Piste.** Une ligne Aide / tip fiche prélèvement : « Cochez FAIT quand vous voyez le débit, pas le jour prévu. »

### B09 — Communication libre / RF Creditor — **P2**

**Vu.** `prepareCopy` : hors forme structurée valide → copie brut + parfois « Vérifiez sur le courrier. » RF Creditor (`RF…`) non traité à part (OK pour coller tel quel).

**Piste.** Si préfixe `RF` + longueur plausible : message « Référence européenne copiée — vérifiez dans votre banque. » Pas de faux mod 97.

### B10 — Courrier sans plan sans photo — **P2**

**Vu.** Pending letter peut n’avoir que qui / pour quoi. Conversion crée dossier mensuel ancré **mois courant**.

**Risque.** Courrier de taxe annuelle « ranger sans plan » puis convert → mensualité, pas 1×/an. Friction si elle ne change pas la fréquence après.

**Piste.** Sur écran convert : rappel fréquence (mensuel par défaut) + lien mental « sinon échéance hors mois ». Charte : pas de 4ᵉ pastille Accueil.

### B11 — Factures du mois dans le résumé sans `+++` — **P2**

**Vu.** Section « Factures du mois » = libellé + montant + FAIT/à payer. Communication seulement sous **Dossiers**.

**Piste.** Optionnelle : si `paymentNote` non vide sur une ligne ordinaire, une sous-ligne `Référence :` (médiation loyer). Garde-fou longueur PDF.

---

## 3. Cohérence des flux (FAIT · futur · dossiers · PDF)

| Flux | Cohérence | Écart métier |
|------|-----------|--------------|
| **FAIT / Remettre** | Solide : courant + passé (confirm) ; futur bloqué ; snackbar Annuler ; accords sync | B07 message solde |
| **Mois futur / annuel** | Aperçu lecture seule ; horizon 24 mois = assurance N+1 ; À venir ≤ 6 | B06 montant ; B01 `+++` déjà dans aperçu si ledger créé |
| **Dossiers** | Reste = départ + frais − FAIT ; Accueil sans € dette ; Mes dossiers ; frais datés ; Écrire mailto | B02 1ʳᵉ ligne ; B05 IBAN PDF |
| **Courriers** | Pending → convert ; multi-pièces sur accord ; PDF + photo | B10 fréquence convert |
| **Résumé PDF** | Même texte ; gros corps ; pas chemin fichier ; pas titre « total dettes » | B05 · B11 |
| **Hors mois** | Réglages toujours ; soldés exclus | OK |
| **Prélèvement** | Copy notif + pastille | B08 timing FAIT |

---

## 4. Améliorations fonctionnelles (charte-compatibles)

À **ne pas** faire (rappel) : OCR montant · IBAN sur sticky · badge « Reste 450 € » Accueil · onglet Dettes · date « fin prévue » certaine · synchro banque.

| Id | Amélioration | Pourquoi | Garde-fou |
|----|--------------|----------|-----------|
| F1 | Plafonner 1ʳᵉ mensualité à `installmentCents` (add + convert) | Reste / À payer justes dès J0 | Aucun nouveau champ Accueil |
| F2 | Durcir `EpcQrParser` (purpose optionnel + scan `+++` dans payload) | QR = promesse BE | Message inchangé « facture belge » |
| F3 | Politique `+++` : ne pas propager aveuglément (voir B01) | Évite virement sur mauvaise ref | Tip fiche / Aide ; dossiers peuvent garder stabilité |
| F4 | IBAN dans résumé si présent sur dossier | Médiation complète | Pas sur Accueil |
| F5 | Tip « vérifiez la communication ce mois » si ligne récurrente + `+++` non vide | Habitude belge | QuietLink / InkMuted, pas bannière permanente |
| F6 | Convert courrier : choix fréquence dès la conversion (1 / 3 / 12) | Aligné taxes / huissier | Toujours derrière Mes dossiers |
| F7 | Après Remettre sur accord qui était soldé : snackbar explicite | Évite « l’app a tout ressorti » | Confirm mois passé déjà là |

---

## 5. P0 demain (liste courte)

Ordre suggéré pour une passe code **après** cet audit (hors scope ce soir) :

1. **B02** — `installmentCents` à la création / conversion d’accord (bug Reste mesurable + test unitaire miroir du cycle).
2. **B03** — Parseur EPC sans purpose + fixtures QR réels (Vivaqua-like **et** QR « maigre »).
3. **B01** — Décision produit écrite puis correctif propagation `+++` (le plus sensible : ne pas casser le loyer stable des dossiers).

Hors P0 métier mais déjà plan produit : smoke doigt S26 (photo / sticky / snackbar / notif) — sans ça le claim « app chère » reste code-only.

---

## 6. Hors scope volontaire (ne pas dériver)

- Open banking / confirmation prélèvement automatique.
- Validation exhaustive IBAN à chaque scan (la copie suffit ; le parse peut rester permissif si B03 est corrigé).
- Stockage IBAN sur **toutes** les factures sans arbitrage charte (B04).
- Formulaire CPAS / conseils juridiques dans le PDF.
- Changer le sens de FAIT pour les prélèvements (cocher = « prévu » vs « vu au compte ») sans copy claire.

---

## 7. Synthèse scores (honnête)

| Dimension | Note /10 | Commentaire |
|-----------|----------|-------------|
| Geste banque BE (copie / mod 97 / OCR `+++`) | **8,5** | Excellent socle ; propagation `+++` = dette de confiance |
| QR EPC | **7** | Marche sur fixtures ; fragile hors purpose |
| FAIT / mois / futur | **9** | Règles claires, alignées charte |
| Accords / courriers | **8** | Très bon pour Accords ; trou 1ʳᵉ mensualité partielle |
| Hors mois / annuel | **8** | Inventaire + aperçu OK ; montants futurs figés |
| Résumé PDF médiation | **7,5** | Utile ; IBAN manquant |
| Confiance « app chère » globale métier | **~7,5** | Au-dessus des carnets génériques ; P0 B01–B03 avant claim premium |

**Phrase pour demain :** corriger ce qui fait **payer faux** ou **croire le Reste** avant tout polish visuel.
