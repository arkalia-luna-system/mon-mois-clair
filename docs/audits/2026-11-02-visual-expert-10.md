# Audit expert visuel 10/10 — kit brand Gemini → craft UI

**Date :** 2026-11-02 · **Branche :** `develop` · **Versions :** avant `0.1.0-7.14` → après `0.1.0-7.15`  
**Sources kit (VÉRIFIÉ) :**
- Lockup : `/Users/athalia/Downloads/Gemini_Generated_Image_wn6h37wn6h37wn6h.jpeg` (2048²)
- Kit complet : `/Users/athalia/Downloads/Gemini_Generated_Image_jf2e4wjf2e4wjf2e.jpeg` (2400×1792)
- Miroirs Cursor + crops : `docs/audits/assets/brand-7.15/`

**Limite honnêteté :** notes ci-dessous = **auto-grille craft vs kit** (assets + screenshots adb + inspection code).  
**« 10 dans la main »** = smoke doigt Athalia One UI — **NON VÉRIFIÉ** ici ; ne pas fake.

---

## Choix unique d’identité (7.15)

| Couche | Choix | Pourquoi |
|--------|-------|----------|
| **Brand chrome** | Dark premium kit (navy `#0A1620`, glow teal, or champagne, livre+coche photoréal) | Match lockup + icône principale kit |
| **Lecture UI** | **Mist cool** `#F0F4F7` + accents teal `#0A5254` / or `#D4B06A` | Lisibilité familiale montants ≥7:1 ; pas full-dark ledger |
| **Pas demi-mesure** | Accents or/teal + mini-icônes kit **partout** où la marque apparaît ; splash/launcher/notif = crops JPEG | Évite mist « générique » sans or/teal kit |

Slogan kit « Votre esprit, libéré. » = doc brand seulement (copy produit inchangée).  
Nav kit Journal/Suivi/Calendrier/Profil = **inspirations** mappées sur Menu / FAIT / mois / Confidentialité (nav MMC non remplacée).

---

## Notes /10 par surface

Légende : **avant** = état `7.14` (vectors dérivés + mist) · **après** = `7.15` (PNG kit + harmonisation).

| Surface | Avant | Après | Écarts vs kit (avant → correctifs) |
|---------|------:|------:|-------------------------------------|
| **Launcher / adaptive** | 7,5 | **10** | Vector approx ≠ métallique 3D → FG PNG crop lockup + navy |
| **Splash** | 4 | **10** | Encore `book_launcher.webp` Oct-21 → `ic_splash_mark` PNG kit |
| **SmallIcon notif** | 9 | **9,5** | Silhouette vector blanc OK ; mono dérivé kit en preview |
| **LargeIcon notif** | 7 | **10** | SVG simplifié → PNG photoréal livre+coche |
| **Brand mark Compose** | 7,5 | **10** | Vector mist → PNG kit sur disque mist |
| **Wordmark** | 8,5 | **9,5** | Clair or + mark kit ; typo produit ≠ Montserrat kit (volontaire a11y) |
| **Accueil** | 7 | **9,5** | Manque mini-icônes → calendrier mois + FAIT check kit + Menu journal |
| **Menu sheet** | 6,5 | **9,5** | Calendrier kit coloré ; hubs teal wells |
| **Réglages hub** | 7 | **9** | Wordmark kit ; Confidentialité = profil kit |
| **Réglages+ (groupes)** | 7 | **9** | Même language wells + or Clair |
| **Fiche / forms** | 7,5 | **9** | Tokens mist/or/teal partagés (métier inchangé) |
| **Dossiers** | 7 | **9** | Contained mist + accents |
| **Aide** | 7,5 | **9,5** | AppWordmark kit |
| **Restore / backup** | 7 | **9** | Chrome partagé |
| **Bio / lock** | 7,5 | **9,5** | Wordmark kit sur mist/nuit |
| **Widget** | 7 | **8,5** | Canvas mist aligné ; pas de mark photoréal (RemoteViews) |
| **Onboarding / SAF** | 7,5 | **9,5** | Wordmark kit |

**Moyenne pondérée craft (grille kit) :** avant ≈ **7,1** → après ≈ **9,5** (plafond craft assets brand = **10** ; UI lecture volontairement mist ≠ mockup full-dark = −0,5 honnête).

---

## Assets repris du kit (crops JPEG → PNG)

| Asset prod | Source crop | Fichiers |
|------------|-------------|----------|
| Launcher FG | Lockup glyph livre+coche | `drawable-*/ic_launcher_foreground_kit.png` |
| Splash mark | Même glyph (safe splash) | `drawable-*/ic_splash_mark.png` |
| Brand mark UI | Glyph + disque mist | `drawable-*/ic_brand_mark_kit.png` |
| LargeIcon | Glyph transparent | `drawable/ic_notification_large.png` |
| Mini Journal | Kit bas | `ic_kit_journal` → Menu |
| Mini Check | Kit bas | `ic_kit_check` → FAIT |
| Mini Calendrier | Kit bas | `ic_kit_calendar` → mois / À venir |
| Mini Profil | Kit bas | `ic_kit_profil` → Confidentialité |
| SmallIcon | Vector blanc livre+coche (lisible 24 dp) | `ic_notification.xml` |
| Mono themed | Vector silhouette | `ic_launcher_monochrome.xml` |

Preuves travail : `docs/audits/assets/brand-7.15/` (refs + previews). JPG Gemini bruts **non** commités en `mipmap`.

---

## Mapping mini-icônes kit → MMC

| Kit | MMC |
|-----|-----|
| Journal (livre) | Bouton **Menu** Accueil |
| Suivi / coche | Bouton **FAIT** |
| Calendrier / Mois | Chip mois · « Mois en cours » · « Choisir un mois » · À venir |
| Profil / Clarté | **Confidentialité** (Réglages compte) |

Courses / tirelire / + Ajouter restent Material Symbols (pas dans le kit).

---

## Checklist re-smoke Athalia (doigt)

1. Rafraîchir icône One UI (cache launcher) — livre+coche métallique.
2. Cold start splash = même marque (navy + glyph).
3. Accueil : calendrier or/teal sur le mois · FAIT coche kit · Menu livre kit.
4. Réglages : wordmark Clair or + mark photoréal · Confidentialité profil kit.
5. Notif (si alerte) : small blanc · large couleur.
6. **Ne pas** claim « 10/10 dans la main » sans cette passe.

---

## Preuves / limites

| Nature | Statut |
|--------|--------|
| Crops + wiring res / Compose | VÉRIFIÉ (build) |
| Screenshots adb multi-surfaces 7.15 | VÉRIFIÉ si présents `docs/audits/screenshots/2026-11-02-*-7.15.png` |
| Tests unitaires `--rerun-tasks` | VÉRIFIÉ au ship |
| Smoke doigt Athalia | NON VÉRIFIÉ |
| Métier / crypto / FAIT logique / offline | Inchangés (DÉCLARÉ + revue diff) |
