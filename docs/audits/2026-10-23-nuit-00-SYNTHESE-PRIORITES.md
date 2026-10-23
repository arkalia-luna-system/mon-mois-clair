# Synthèse priorités — audits nuit 2026-10-23

| | |
|--|--|
| **Date** | 2026-10-23 → lecture demain matin |
| **Sources** | `nuit-01` … `nuit-08` (8 audits) + **passe code 6.77** (nuit agent) |
| **Branche** | `develop` · `0.1.0-6.77` · **315/315** unit |
| **Claim** | « 9/10 dans la main » = **non** · smoke S26 encore ouvert |

---

## 1. Verdict global (après 6.77)

P0 métier BE (B01–B03) **FAIT**. Sticky / a11y / widget tokens / copy / PRIVACY / eng min (Migration 7→8 + extract chrome fiche) **FAIT**.  
P1 BE restants codés (B04 tip IBAN, B05 IBAN PDF, B06 tip aperçu, B07 snackbar Remettre, B08 tip prélèvement).  
Scrub PII docs vivants (PROGRESS_LOG / DISTRIBUTION / audits) **FAIT** ; archives figées **REPORTÉ** avant public.  
Carrière GitHub profil **REPORTÉ** (hors repo).  
Smoke doigt S26 = seul juge « dans la main ».

---

## 2. Top 15 — statut croisé (6.77)

| # | Priorité | Action | Statut 6.77 | Preuve |
|--:|----------|--------|-------------|--------|
| **1** | P0 BE | **B02** 1ʳᵉ mensualité plafonnée | **FAIT** | `AddEntryUseCase` / `ConvertPendingLetter…` + tests |
| **2** | P0 BE | **B03** `EpcQrParser` robuste | **FAIT** | `resolveRemittance` + fixtures sans Purpose |
| **3** | P0 BE | **B01** politique `+++` | **FAIT** | `PaymentNotePolicy` · cycle · update · Aide |
| **4** | Smoke | S26 sticky / fontScale / Plus | **CODE FAIT** | Autres infos · plafond hero · **smoke REPORTÉ** |
| **5** | Smoke | Photo multi-ligne | **CODE FAIT** | `resolvedPath` 6.73 · **smoke REPORTÉ** |
| **6** | Smoke | FAIT + snackbar | **CODE FAIT** | FAIT 96 dp · durée Long · **smoke REPORTÉ** |
| **7** | Smoke | Notif silhouette + teal | **CODE FAIT** | 6.72 tenu · **smoke REPORTÉ** |
| **8** | Smoke | Widget Reste + nuit + bio | **CODE FAIT** | Serif + tokens InkMuted/PaperBorder · **smoke REPORTÉ** |
| **9** | P1 UI | Widget = même Reste serif | **FAIT** | `@font/source_serif4_semibold` + muted `#4A453C` / rule `#E2DDD2` |
| **10** | P1 UI | Quieting sticky | **FAIT** | Mois muted · sticky crème · Autres infos |
| **11** | P1 a11y | Plafond hero fontScale | **FAIT** | `rememberDisplayFit` pré-compense sp |
| **12** | P1 a11y/copy | FAIT 96 · Plus · Compte · après | **FAIT** | 96 dp · Autres infos · Données & sauvegarde · il reste · Lire la communication |
| **13** | Career | Bio + pins GitHub | **REPORTÉ** | Hors repo (profil `arkalia-luna-system`) |
| **14** | Privacy docs | Scrub PII + PRIVACY | **FAIT** (vivants) | PRIVACY/SECURITY · journal · audits ; **archives REPORTÉ** |
| **15** | Eng | Découpe EntryForm / MigrationTest | **FAIT** (gain min) | `EntryFormChrome` + `RoomMigrationTest` 7→8 ; god-files Home **REPORTÉ** |

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

*Synthèse + matrice 6.77 · agent nuit · push `develop` si gate OK.*
