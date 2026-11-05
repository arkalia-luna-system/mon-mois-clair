> **ARCHIVÉ** 2026-11-05 (7.33) — craft clos · ne plus lire pour l’état vivant. Vivants : `visual-expert-10` · smoke-checklist · CHARTE / JOURNAL / README.

# Brand unisson 7.14 — livre + coche · teal glow · or champagne

**Date :** 2026-11-02 · **Étape :** `0.1.0-7.14` · **Branche :** `develop`  
**Autorisé :** Demande Athalia — logo cible + kit brand → launcher / notif / UI unisson.

---

## Sources (chemins exacts)

| Rôle | Chemin | Preuve |
|------|--------|--------|
| **Logo lockup principal** | `/Users/athalia/Downloads/Gemini_Generated_Image_wn6h37wn6h37wn6h.jpeg` | VÉRIFIÉ — 2048×2048 · 2,3 Mo · mtime 2026-11-02 13:01 |
| **Kit brand complet** | `/Users/athalia/Downloads/Gemini_Generated_Image_jf2e4wjf2e4wjf2e.jpeg` | VÉRIFIÉ — 2400×1792 · 2,2 Mo · mtime 2026-11-02 13:05 |
| Copie Cursor (kit) | `/Users/athalia/.cursor/projects/Volumes-T7-Mon-Mois-Clair/assets/Gemini_Generated_Image_jf2e4wjf2e4wjf2e-f824db56-a825-421c-91e7-8185b53b0666.jpg` | DÉCLARÉ — miroir session |
| Refs docs (compressées) | `docs/audits/assets/brand-7.14/` | VÉRIFIÉ — crops + SVG dérivés |

**Règle production :** ne **pas** committer les JPG Gemini bruts comme launcher. Dériver **vectors XML** + PNG notif léger.

---

## Décisions brand

| Décision | Choix | Pourquoi |
|----------|-------|----------|
| Motif | **Livre ouvert + coche** (plus monogramme « M ») | Kit + lockup Athalia ; coche = clarté / FAIT |
| Launcher | Fond **dark navy `#0A1620`** + glyph teal/or + glow soft | Match icône kit One UI squircle |
| Safe zone | Glyph inset ≈ cercle 66 dp (padding Google) | Évite clip mask / parallax |
| SmallIcon notif | Silhouette **blanc** livre+coche | Status bar monochrome obligatoire (pas or fin) |
| LargeIcon notif | Couleur simplifiée (teal + champagne, peu de bruit) | Bannière collapsed Samsung |
| UI app | **Mist cool** inchangé (canvas `#F0F4F7`) + accents teal `#0A5254` / or `#C9A36A` | Lisibilité familiale · pas full dark |
| Hero Accueil | Reste tonal mist (7.13) — **pas** hero dark brandé | Cohérence lecture montants ; marque via wordmark / mark |
| Wordmark | « Mon Mois » encre · « **Clair** » or champagne | Kit typo |
| Nav kit (Journal / Profil…) | **Référence seulement** | Nav produit MMC = Menu / Courses / Tirelire / FAIT — pas remplacer |
| Slogan kit « Votre esprit, libéré » | Doc brand uniquement | Copy produit existante (carnet / hors ligne) inchangée |
| Or pastille Accord | `#C9A36A` (ex `#D99B26`) | Champagne kit · WCAG encre ≥ 7:1 (≈7,45) |

---

## Assets livrés

| Asset | Fichier | Nature |
|-------|---------|--------|
| Adaptive FG | `res/drawable/ic_launcher_foreground.xml` | Vector livre+coche + glow |
| Adaptive BG | `res/values/colors.xml` → `#0A1620` | Dark navy |
| Monochrome | `res/drawable/ic_launcher_monochrome.xml` | Silhouette blanche |
| Notif small | `res/drawable/ic_notification.xml` | Blanc alpha |
| Notif large | `res/drawable/ic_notification_large.png` | Couleur 256 |
| Brand UI | `res/drawable/ic_brand_mark.xml` | Disque mist + livre/coche |
| SVG sources | `docs/audits/assets/brand-7.14/*.svg` | Travail / preview |

---

## Unisson UI

- `AppWordmark` / `BrandMark` → nouveau mark · Clair doré
- `A11yTokens.AccentAuto` / CHARTE pastille Auto → champagne
- Accueil / Réglages / lock / onboarding : wordmark partagé (déjà branché)
- Hubs wells teal (7.13) conservés — accent or via pastilles Accord + wordmark
- Métier FAIT / crypto / offline / touch 48 / pas purple : **inchangés**

---

## Améliorations vs JPG Gemini (production)

1. Moins de grain / glow excessif → halo dial-down vector
2. Traits plus épais pour 18–48 px (notif / themed icon)
3. Safe zone adaptive (glyph réduit vs mockup plein squircle)
4. SmallIcon sans détail or

---

## Re-smoke One UI (Athalia)

Après install sans wipe : **rafraîchir icône** (parfois cache launcher Samsung) — long-press → info app / redémarrage launcher si ancienne « M » visible.

Checklist : launcher livre+coche · Accueil mist + mark · Réglages wordmark Clair or · notif silhouette blanche.

**Captures adb (sans wipe) :**  
`docs/audits/screenshots/2026-11-02-accueil-7.14.png` ·  
`docs/audits/screenshots/2026-11-02-reglages-7.14.png` ·  
`docs/audits/screenshots/2026-11-02-app-info-icon-7.14.png` (icône One UI).

---

## Limites

- Inspection assets + contraste calculé : **VÉRIFIÉ** en build.
- Smoke doigt Athalia / notif réelle OVERDUE : **NON VÉRIFIÉ** ici.
- Kit mini-icônes nav Gemini : **non portées** (hors modèle nav MMC).
