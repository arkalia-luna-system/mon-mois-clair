# Synthèse priorités — audits nuit 2026-10-23

| | |
|--|--|
| **Date** | 2026-10-23 → lecture demain matin |
| **Sources** | `nuit-01` … `nuit-08` (8 audits) + **passe code 6.76** (nuit agent) |
| **Branche** | `develop` · `0.1.0-6.76` · **314/314** unit |
| **Claim** | « 9/10 dans la main » = **non** · smoke S26 encore ouvert |

---

## 1. Verdict global (après 6.76)

P0 métier BE (B01–B03) **corrigés en code** avec tests. Sticky / a11y / widget / copy / PRIVACY alignés smoke-ready.  
Ce qui reste pour le claim premium : **smoke doigt S26** (pas simulable honnêtement ici) + carrière GitHub profil (hors repo) + scrub archives avant public.

---

## 2. Top 15 — statut croisé (matin)

| # | Priorité | Action | Statut 6.76 | Preuve |
|--:|----------|--------|-------------|--------|
| **1** | P0 BE | **B02** 1ʳᵉ mensualité plafonnée | **FAIT** | `AddEntryUseCase` / `ConvertPendingLetter…` + tests |
| **2** | P0 BE | **B03** `EpcQrParser` robuste | **FAIT** | `resolveRemittance` + fixtures sans Purpose |
| **3** | P0 BE | **B01** politique `+++` | **FAIT** | `PaymentNotePolicy` · cycle · update · Aide |
| **4** | Smoke | S26 sticky / fontScale / Plus | **PARTIEL** | Code prêt (Autres infos, plafond hero) · **smoke REPORTÉ** |
| **5** | Smoke | Photo multi-ligne | **PARTIEL** | Code 6.73 tenu · smoke REPORTÉ |
| **6** | Smoke | FAIT + snackbar | **PARTIEL** | FAIT 96 dp · durée Long · smoke REPORTÉ |
| **7** | Smoke | Notif silhouette + teal | **PARTIEL** | Code 6.72 tenu · smoke REPORTÉ |
| **8** | Smoke | Widget Reste + nuit + bio | **PARTIEL** | Serif + tokens · smoke REPORTÉ |
| **9** | P1 UI | Widget = même Reste serif | **FAIT** | `@font/source_serif4_semibold` + muted/rule alignés |
| **10** | P1 UI | Quieting sticky | **FAIT** | Mois muted · sticky crème · Autres infos |
| **11** | P1 a11y | Plafond hero fontScale | **FAIT** | `rememberDisplayFit` pré-compense sp |
| **12** | P1 a11y/copy | FAIT 96 · Plus · Compte · après | **FAIT** | 96 dp · Autres infos · Données & sauvegarde · il reste |
| **13** | Career | Bio + pins GitHub | **REPORTÉ** | Hors repo (profil `arkalia-luna-system`) |
| **14** | Privacy docs | Scrub PII + PRIVACY | **PARTIEL** | PRIVACY complété · archives figées **REPORTÉ** avant public |
| **15** | Eng | Découpe EntryForm / MigrationTest | **REPORTÉ** | Défaut synthèse — après smoke #1–8 |

---

## 3. Checklist doigt (matin)

À cocher sur S26 (ou 360×780 + texte ≥ 1,3) — **pas** claimés verts ici :

- [ ] Sticky : Reste lisible ; Courses ; Autres infos → épargne ; pas de logo
- [ ] fontScale Grand : 1ʳᵉ facture visible ; FAIT atteignable
- [ ] FAIT → snackbar Long + Annuler
- [ ] Photo multi-ligne / Ouvrir à part
- [ ] Widget serif + nuit + verrou bio
- [ ] Notif teal + silhouette
- [ ] Menu QuietLink Arkalia

---

## 4. Imperfectibles honnêtes

- Smoke S26 réel = seul juge « dans la main »
- Archives `docs/archive/*` + historique journal : PII personas — garder privé / scrub avant public
- God-files Entry/Home : dette eng volontairement reportée
- Bio = UI gate ; Room / backups / notifs en clair (PRIVACY le dit)
- Carrière GitHub : craft caché tant que repo privé + bio non mise à jour

**Note code estimée (matin) :** carnet fonctionnel ~**9/10** · confiance métier ~**8,5** · DA ~**8** · dans la main ~**5–6** (smoke ouvert) · vendable ~**3–4**.

---

## 5. Verrous charte (inchangés)

Palette crème / un teal · serif = Reste seulement · pas logo sticky · pas Alertes/Aide/Restore Menu Accueil · OCR `+++` seulement · zéro INTERNET · MutatingRepository.

---

*Synthèse + matrice 6.76 · agent nuit · push `develop` si gate OK.*
