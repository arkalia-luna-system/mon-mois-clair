> **Index figé 10-23** — détails nuit-01…08 archivés dans `docs/archive/audits-2026-10-23/`. État code = journal **6.82** + `final-0*`. § imperfectibles Room/backups « en clair » = **obsolète** (crypto 6.81).

# Synthèse priorités — audits nuit 2026-10-23

| | |
|--|--|
| **Date** | 2026-10-23 → lecture demain matin |
| **Sources** | `nuit-01` … `nuit-08` (8 audits) + smoke S25 + **passe code 6.78** |
| **Branche** | `develop` · `0.1.0-6.78` · **315/315** unit |
| **Claim** | « 9/10 dans la main » = **non** · smoke S26 encore ouvert |

---

## 1. Verdict global (après 6.78)

P0 métier BE (B01–B03) **FAIT** (re-croisé sources). Sticky / a11y / widget tokens / copy / PRIVACY / eng min **FAIT**.  
P1 BE (B04–B08) **FAIT**. Dependabot transitifs (protobuf / commons-io / netty) **forcés** patchés.  
Smoke S25 Ultra **partiel** (`2026-10-23-smoke-device.md`) → micro P1 cartes / À venir / jour 31 **FAIT**.  
Scrub PII vivants **FAIT** ; captures smoke gitignorées ; archives **REPORTÉ**.  
Carrière GitHub / S26 doigt **REPORTÉ**.

---

## 2. Top 15 — statut croisé (6.78) — **CODE**

| # | Priorité | Action | Statut | Preuve code |
|--:|----------|--------|--------|-------------|
| **1** | P0 BE | **B02** 1ʳᵉ mensualité plafonnée | **FAIT** | `AddEntryUseCase` / `ConvertPendingLetter…` + `installmentCents` |
| **2** | P0 BE | **B03** `EpcQrParser` robuste | **FAIT** | `resolveRemittance` + fixtures sans Purpose |
| **3** | P0 BE | **B01** politique `+++` | **FAIT** | `PaymentNotePolicy` · cycle · update · Aide |
| **4** | Smoke | S26 sticky / fontScale / Plus | **CODE FAIT** | Autres infos · plafond hero · **S26 REPORTÉ** |
| **5** | Smoke | Photo multi-ligne | **CODE FAIT** | `resolvedPath` · **smoke doigt REPORTÉ** |
| **6** | Smoke | FAIT + snackbar | **CODE FAIT** | FAIT 96 dp · Long · dump S25 OK |
| **7** | Smoke | Notif silhouette + teal | **CODE FAIT** | tenu · **notif doigt REPORTÉ** |
| **8** | Smoke | Widget Reste + nuit + bio | **CODE FAIT** | Serif + tokens · **widget doigt REPORTÉ** |
| **9** | P1 UI | Widget Reste lisible One UI | **FAIT 6.79** | police **système** `serif` (plus `@font` — cassait le host) + muted/rule |
| **10** | P1 UI | Quieting sticky + cartes | **FAIT** | Sticky crème · cartes **filet seul** (6.78) |
| **11** | P1 a11y | Plafond hero fontScale | **FAIT** | `rememberDisplayFit` pré-compense sp |
| **12** | P1 a11y/copy | FAIT 96 · Plus · Compte · après | **FAIT** | + À venir « N factures » (6.78) |
| **13** | Career | Bio + pins GitHub | **REPORTÉ** | Hors repo |
| **14** | Privacy docs | Scrub PII + PRIVACY | **FAIT** (vivants) | smoke PNG gitignorés ; archives REPORTÉ |
| **15** | Eng | Découpe EntryForm / MigrationTest | **FAIT** (gain min) | `EntryFormChrome` + `RoomMigrationTest` 7→8 |

---

## 3. Checklist doigt (matin)

À cocher sur S26 (ou 360×780 + texte ≥ 1,3) — **pas** claimés verts ici :

- [ ] Sticky : Reste lisible ; Courses ; Autres infos → épargne ; pas de logo
- [ ] fontScale Grand : 1ʳᵉ facture visible ; FAIT atteignable
- [ ] FAIT → snackbar Long + Annuler ; Remettre accord → message dossier
- [ ] Photo multi-ligne / Ouvrir à part
- [ ] Widget serif + nuit + verrou bio
- [ ] Notif teal + silhouette
- [ ] Menu QuietLink Arkalia

---

## 4. Imperfectibles honnêtes

- Smoke S26 réel = seul juge « dans la main »
- Archives `docs/archive/*` : PII personas — garder privé / scrub avant public
- God-file `HomeScreen` : dette eng volontairement reportée (EntryForm chrome extrait)
- Bio = UI gate ; Room / backups / notifs en clair (PRIVACY le dit)
- Carrière GitHub : craft caché tant que repo privé + bio non mise à jour
- Notifs : montant visible même si bio ON (choix produit documenté ; masquage = P2)

**Note code estimée (matin) :** carnet fonctionnel ~**9/10** · confiance métier ~**9** · DA ~**8,5** · dans la main ~**5–6** (smoke ouvert) · vendable ~**3–4**.

---

## 5. Verrous charte (inchangés)

Palette crème / un teal · serif = Reste seulement · pas logo sticky · pas Alertes/Aide/Restore Menu Accueil · OCR `+++` seulement · zéro INTERNET · MutatingRepository.

---

*Synthèse + matrice 6.78 · gate tests + push `develop` / `main`.*
