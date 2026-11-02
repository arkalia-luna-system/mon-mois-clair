# Audit expert — Fiche Add / Edit (forms)

| Champ | Valeur |
|-------|--------|
| Date | 2026-11-02 |
| Branche | `develop` @ `6d877b9` · **`0.1.0-7.06`** (`versionCode` 111) |
| Domaine | EntryForm Add/Edit — identité · jour · récurrence · Paiement BE · photos/PDF · retard/accord (surface) |
| Déclencheur | Fouillis, longueur, clarté **60+** |
| Nature | Inspection code seule · **pas** smoke doigt Athalia · **pas** tests Gradle relancés |
| Note globale | **5,5 / 10** (parcours 60+ : long, dense une fois ouvert, structure FORM01 honnête mais clearance et mur d’actions) |

---

## 1. Carte du parcours (ordre réel)

Ordre dans `EntryFormScaffold` (Add + Edit partagent le même socle) :

| # | Bloc | Repliable ? | Add | Edit |
|---|------|-------------|-----|------|
| 1 | Titre page | — | « Nouvelle facture ou revenu » | libellé ou « Modifier… » |
| 2 | Dépense (−) / Revenu (+) | chips | oui | non (`onExpense` no-op ; chips masqués via `showRecurring`) |
| 3 | Nom · Montant (€) | champs | oui | oui (+ lock épargne) |
| 4 | Bandeau accord (reste / rythme) | texte | — | si `isAgreement` |
| 5 | **Jour du mois** (grille 1–31, 48 dp) | toujours visible | oui | oui |
| 6 | **Spacer ~172 dp** (`3×48 + 28`) | vide | oui | oui |
| 7 | **Fréquence** (+ 12 mois si 3 mois / annuel Add) | **toujours ouverte** | 4 chips | chips si template/dépense ; sinon texte info |
| 8 | **Paiement & preuve** | **fermé** par défaut (sauf pièce pending) | dépense | dépense |
| 9 | **Retard ou accord** | fermé (état VM) | dépense | dépense hors épargne |
| 10 | « Aussi les mois suivants » | case | non | si template |
| 11 | Sticky Enregistrer · Annuler · (Supprimer) | fixe bas | 2 | 3 |

Fichiers pivots : `EntryFormScreen.kt` · `EntryFormScaffold.kt` · `EntryFormPaymentSection.kt` · `EntryFormRecurrenceSection.kt` · `EntryFormDossierSection.kt` · `DayOfMonthGrid.kt`.

---

## 2. Notes /10 (axes)

| Axe | /10 | Lecture courte |
|-----|----:|----------------|
| **Structure / rythme FORM01** | **7,0** | Qui → combien → jour → Fréquence → Paiement → Accord ; 1 Primary ; QuietLink Annuler — tenu depuis 6.59–6.62 |
| **Longueur scroll 60+** | **4,0** | Grille 31 + **spacer mort ~172 dp** + Fréquence toujours ouverte + 2ᵉ spacer bas = premier parcours « interminable » |
| **Fouillis (ouvert)** | **4,5** | Paiement déplié = QR + +++ + tip + Copier + OCR + Photo + PDF + états pending + extras |
| **Clarté libellés** | **6,5** | Beaucoup de FR simple ; hint fermé « QR, +++ , photo, PDF » et tip « Aussi les mois suivants » loin de la case |
| **Paiement BE (+++ / QR)** | **7,5** | Copy structurée + VERIFY_ON_MAIL solides ; pas de pont banque (charte) |
| **Photos / PDF** | **5,5** | Fonctionnellement riche ; QuietLinks conditionnels nombreux (« ce mois » / « courrier n » / reprendre) |
| **Récurrence** | **6,0** | Chips clairs ; Add expose 4 choix d’emblée ; ancre 12 mois = 2ᵉ grille ; Edit tip ≠ checkbox |
| **Add vs Edit cohérents** | **6,5** | Même scaffold ; asymétries utiles (type, applyFollowing, extras) mais charge mentale différente |

**Moyenne indicative (non pondérée) : ~5,9 → verdict produit 60+ : 5,5 / 10.**  
Ne pas confondre avec craft global premium (~8,9) : ici c’est **la fiche dans la main**, pas l’archi.

---

## 3. Constats — preuves & priorités

### P0 — Spacer « clearance sticky » **entre** grille et Fréquence

| | |
|--|--|
| **Constat** | Après `DayOfMonthGrid`, un `Spacer(minTouchHeight * 3 + bottomSystemClearance)` ≈ **172 dp** de crème vide, **puis** Fréquence / Paiement / Accord. Un second spacer bas existe déjà pour le sticky. |
| **Statut** | **VÉRIFIÉ** |
| **Nature** | inspection |
| **Preuve** | `EntryFormScaffold.kt` L368–393 et L505–510 ; `A11yTokens.minTouchHeight = 48.dp`, `bottomSystemClearance = 28.dp`. Journal 6.62 : « Spacer sous grille + marge bas sticky » — aujourd’hui les sections **après** la grille rendent le 1ᵉʳ spacer un **trou** au milieu du scroll. |
| **Impact 60+** | Impression de formulaire cassé / « ça n’en finit pas » avant même Paiement ; Fréquence découvrable seulement après un grand geste de scroll. |

### P0 — Premier écran = mur de jours (31 cibles 48 dp)

| | |
|--|--|
| **Constat** | « Jour du mois » occupe ~5 rangées × ~52–54 dp + titre ≈ **un viewport entier** avant Fréquence (et après le trou P0). |
| **Statut** | **VÉRIFIÉ** (taille codée) · comportement doigt **NON VÉRIFIÉ** |
| **Nature** | inspection |
| **Preuve** | `DayOfMonthGrid.kt` L65–112 (`size(A11yTokens.minTouchHeight)` × 31). |
| **Impact 60+** | Nom + montant OK, puis **carte calendrier** sans résumé « Jour 12 ▾ » — charge visuelle haute pour un seul choix. |

### P1 — « Paiement & preuve » fermé = bon ; **ouvert = cockpit**

| | |
|--|--|
| **Constat** | Fermé par défaut (sauf pending) — FORM01 OK. Ouvert : Prélèvement, Scanner QR, champ +++ , tip utilities, Copier, Lire OCR, Photo, Joindre PDF, bannières pending, Voir/Reprendre/Enlever, liste « Autres courriers ». |
| **Statut** | **VÉRIFIÉ** |
| **Nature** | inspection |
| **Preuve** | `EntryFormPaymentSection.kt` L76–355 ; hint fermé L97 « QR, +++ , photo, PDF ». |
| **Impact 60+** | Une facture simple (nom + montant + jour) force le dépli pour +++ / photo ; une fois ouvert, **trop de gestes au même niveau** Secondary / QuietLink. |

### P1 — Hint « QR, +++ » opaque ; tip +++ toujours visible

| | |
|--|--|
| **Constat** | Hint section fermée jargonisé. Tip « Eau, énergie, télécom : souvent une nouvelle +++ chaque mois. » dès ouverture (sauf accord) — utile BE mais dense pour une première saisie. |
| **Statut** | **VÉRIFIÉ** |
| **Nature** | inspection |
| **Preuve** | `EntryFormPaymentSection.kt` L95–98, L131–137. |
| **Impact 60+** | « +++ » sans exemple dans le hint fermé ; placeholder champ OK (`+++090/…`) mais section fermée ne le montre pas. |

### P1 — Photos / PDF : jungle de QuietLinks conditionnels

| | |
|--|--|
| **Constat** | Libellés varient : « Photo de la facture papier » / « Ajouter un courrier (photo) » ; « Enlever la photo » vs « … de ce mois » ; extras « Voir le courrier n » ; PDF externe vs dialog bitmap. |
| **Statut** | **VÉRIFIÉ** |
| **Nature** | inspection |
| **Preuve** | `EntryFormPaymentSection.kt` L163–354 ; dialogs `EntryFormScreen.kt` Edit L534–633. |
| **Impact 60+** | Risque d’enlever la mauvaise pièce ; « ce mois » vs modèle peu expliqué hors case « Aussi les mois suivants » (plus bas). |

### P1 — Récurrence : toujours déployée + tip loin de la case Edit

| | |
|--|--|
| **Constat** | Add : 4 chips Fréquence **non repliables**. Trimestriel/annuel → **12 chips mois** (« Prochaine échéance »). Edit template : tip « Mois déjà créés : cochez « Aussi les mois suivants ». » juste sous Fréquence, mais la **case** est **après** Paiement + Accord. |
| **Statut** | **VÉRIFIÉ** |
| **Nature** | inspection |
| **Preuve** | `EntryFormRecurrenceSection.kt` L37–84 ; `EntryFormScaffold.kt` L452–463 ; `AddEntryState.needsNextDueMonth`. |
| **Impact 60+** | Double grille (jours + mois) ; conseil Edit orphelin de son contrôle. |

### P2 — Copy +++ / messages BE (point fort relatif)

| | |
|--|--|
| **Constat** | `BelgianStructuredCommunication` : copie 12 chiffres si valide ; `COPIED_OK` / `VERIFY_ON_MAIL` clairs. Aligné charte « app bancaire sur le même téléphone ». |
| **Statut** | **VÉRIFIÉ** |
| **Nature** | inspection |
| **Preuve** | `BelgianStructuredCommunication.kt` L24–48 ; `EntryFormPaymentNoteCopy.kt`. |
| **Impact** | Bon filet ; ne sauve pas la densité de la section. |

### P2 — Dossier « Retard ou accord » (surface fiche seulement)

| | |
|--|--|
| **Constat** | Hint fermé déjà corrigé (« pas la pastille En retard ») ; ouvert = cause + reste + IBAN + mail + frais. Densité déjà notée audit retard-accord — **pas re-audité en profondeur ici**. |
| **Statut** | **DÉCLARÉ** (renvoi) + extrait **VÉRIFIÉ** |
| **Nature** | inspection |
| **Preuve** | `EntryFormDossierSection.kt` L56–60 ; `docs/audits/2026-11-02-retard-accord.md`. |

### P2 — Twin Add/Edit encore lourds (hors UX doigt)

| | |
|--|--|
| **Constat** | `EntryFormScreen.kt` ~793 L ; Add VM ~574 L ; Edit VM ~756 L — dette lecture, pas note 60+ directe. |
| **Statut** | **VÉRIFIÉ** |
| **Nature** | commande `wc -l` |
| **Preuve** | totaux package entry form ~4,4k L hors tests. |

---

## 4. Ce qui est déjà bon (ne pas casser)

1. **Paiement fermé par défaut** + auto-open si photo/PDF pending (6.62).
2. **1 Primary** Enregistrer ; Annuler = QuietLink ; erreurs en liveRegion.
3. **Discard** : déplier accord sans saisie ≠ dirty (Add).
4. **Chips fréquence** libellés FR simples (`Une seule fois` / `Tous les mois`…).
5. **Copy +++** avec vérification modulo 97 + message « Vérifiez sur le courrier… ».
6. **Ordre métier** FORM01 (Accord après Paiement) respecté.

---

## 5. Plan fix suggéré (P0 → P1) — **pas appliqué**

| Priorité | Action | Effet 60+ |
|----------|--------|-----------|
| **P0** | Retirer (ou réduire fortement) le spacer **intermédiaire** ; garder **un seul** clearance bas sticky | Formulaire « accroché » : jour → Fréquence sans trou |
| **P0** | Option : jour = champ/chip « Jour 12 » + disclosure grille (ou grille compacte) | Premier écran = qui/combien/jour sans mur |
| **P1** | Paiement ouvert : regrouper preuves (Photo \| PDF) vs actions secondaires (Copier / OCR) ; hint fermé en français (« Référence, QR, photo ») | Moins de cockpit |
| **P1** | Rapprocher « Aussi les mois suivants » de Fréquence (ou tip à côté de la case) | Tip actionnable |
| **P1** | Fréquence Add : repliable si « Une seule fois » (défaut), ou tip unique court | Moins de chips d’emblée |
| **P2** | Unifier libellés enlever photo / courrier ; smoke doigt Add simple vs Edit photo+applyFollowing | Clarté pièces |

**Hors scope volontaire :** deep-link banque · redesign pastilles · refonte Dossiers · rewrite twin VM.

---

## 6. Smoke Athalia (plus tard)

1. Add dépense minimale (nom + montant + jour 31) — noter trou scroll + sticky.
2. Déplier Paiement — compter boutons visibles sans photo.
3. Photo + OCR + Copier +++ (valide / invalide).
4. Add « Tous les 3 mois » — grille 12 mois.
5. Edit template : changer montant, tip « mois suivants », cocher/décocher applyFollowing + enlever photo « ce mois ».

---

## 7. Limites de cet audit

| Domaine | Couvert ? |
|---------|-----------|
| EntryForm Add/Edit UI (longueur, fouillis, clarté, BE, photos, récurrence) | **Oui** |
| Comportement runtime / doigt S26 | **NON VÉRIFIÉ** |
| Tests unitaires rejoués | **non exécuté** |
| Scan EPC / OCR précision | **NON VÉRIFIÉ** (wiring seulement) |
| Retard/Accord métier profond | Renvoi audit du jour — **pas re-audité** |
| Accueil Copier / Voir photo hors fiche | **Hors domaine** |

**Domaine couvert :** formulaires fiche EntryForm (Add/Edit) — fouillis, longueur, clarté 60+, paiement BE, photos/PDF, récurrence.  
**aucune modification faite** (fichier d’audit rédigé sur OUI Athalia ; **aucun** correctif code).
