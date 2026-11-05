> **ARCHIVÉ** 2026-11-05 (7.33) — craft clos · ne plus lire pour l’état vivant. Vivants : `visual-expert-10` · smoke-checklist · CHARTE / JOURNAL / README.

# Passage final craft — 2026-11-02 · 7.16

**Branche :** `develop` (= `main` après ship) · **Mission :** Athalia — dernier passage multi-domaines.  
**Limite honnêteté :** craft / code / docs / chemins critiques **code** = claimables.  
**Smoke doigt** (S26, restore code papier, notif réelle, « 10/10 dans la main ») = **NON VÉRIFIÉ** — checklist Athalia seule restante.

---

## Grille domaines

| Domaine | Note | Preuve | Reste Athalia |
|---------|------|--------|---------------|
| **Visuel** (kit 7.15, écrans, logo, notif, cohérence) | **Craft fini** (~9,5/10 grille kit) | PNG kit launcher/splash/mark/largeIcon/minis · mist+or/teal · screenshots `docs/audits/screenshots/2026-11-02-*-7.15.png` · audit [`visual-expert-10.md`](2026-11-02-visual-expert-10.md) · 7.16 retire assets morts (`book_launcher.webp`, vectors FG/mark non branchés) | Re-smoke One UI : rafraîchir icône · cold splash · Accueil minis · Réglages wordmark · notif si alerte |
| **Code** (hygiène, compile, tests, bugs P0) | **Fini** | `./gradlew :app:testDebugUnitTest --rerun-tasks` → **413** exécutés / 414 (1 skipped), 93 XML, 0 failure · pas INTERNET · FAIT futur bloqué · QuietLink Dossiers conditionnel | Aucun P0 craft ; juger UX au doigt |
| **Docs** (MD vivants, archives, claims) | **Alignés** | README / badges / `versionName` / journal = `0.1.0-7.16` · index [`docs/README.md`](../README.md) · vivants : passage-final · fin-journee-qa · visual-expert-10 · brand-logo | Mettre à jour AUDIT_STATUS **après** smoke doigt |
| **Fonctions critiques** | **Code OK** | Voir table chemins ci-dessous + [`fin-journee-qa.md`](2026-11-02-fin-journee-qa.md) | Smoke doigt FAIT / Courses·épargne / résumé share / restore / hub / Aide |
| **Ship** | **OK craft** | `develop` = `main` · APK release installée SM-S938B sans wipe · `versionName=0.1.0-7.16` / `versionCode=122` | Confirmer launcher cache One UI après install |

---

## Chemins critiques (code — VÉRIFIÉ inspection + tests)

| Chemin | Verdict craft | Notes |
|--------|---------------|-------|
| **FAIT** | OK | `requestToggleDone` · undo · blocage aperçu futur · dialogue mois passé |
| **Accueil Courses / épargne** | OK | Sheets sticky · `ExpenseOverdue` ignore « Mettre de côté » |
| **Résumé** | OK | Texte + PDF Réglages & Dossiers · note ≠ sauvegarde · QuietLink Dossiers si `dossierCount > 0` |
| **Restore copy** | OK | Cleartext refusée → message plain FR · pas jargon `.mmc` / `6.81` UI |
| **Settings hub** | OK | Essentiel → Code/copie · Données · Confort = Apparence+Alertes · Aide |
| **Aide** | OK | Résumé : Réglages d’abord · Dossiers si accord/courrier |

---

## Correctif 7.16 (seul écart P1 craft restant)

| Action | Détail |
|--------|--------|
| Supprimé | `drawable/book_launcher.webp` (splash legacy Oct-21, 0 ref) |
| Supprimé | `drawable/ic_brand_mark.xml` (remplacé par `ic_brand_mark_kit` PNG) |
| Supprimé | `drawable/ic_launcher_foreground.xml` (remplacé par `ic_launcher_foreground_kit` PNG) |

Métier / crypto / offline / a11y touch **inchangés**.

---

## Preuves chiffrées

| Élément | Valeur |
|---------|--------|
| Version | `0.1.0-7.16` |
| versionCode | 122 |
| Tests | **413** exécutés / 414 (1 `@Ignore`), 93 XML, 0 failure |
| Device | SM-S938B · `adb install -r` sans wipe |
| Charte | offline · FAIT · crypto locale respectés (DÉCLARÉ + revue diff hygiène) |

---

## Checklist Athalia (doigt UNIQUEMENT)

1. Rafraîchir icône One UI (cache launcher) — livre+coche métallique kit.
2. Cold start splash = navy + glyph kit.
3. Accueil : calendrier kit · FAIT coche kit · Menu journal kit · Courses / tirelire.
4. Cocher / Remettre FAIT (mois courant) · aperçu futur refuse.
5. Réglages hub → Code/copie · Données résumé · Confort · Aide « Résumé du mois ».
6. Partager résumé texte/PDF (ne pas envoyer) — note « pas une copie ».
7. Restore : tenter vieille copie claire → message refus FR (sans jargon).
8. Notif (si échéance) : small blanc · large couleur.
9. **S26** tenu en main (si dispo) — **ouvert**, non claimé ici.
10. Restore code papier 2ᵉ device — **ouvert**, non claimé ici.

**Interdit** de cocher « produit fini dans la main » sans 1–8 au minimum.
