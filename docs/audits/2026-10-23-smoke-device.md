# Smoke device — 2026-10-23 (soir) + suivi P0 widget 6.79

| | |
|--|--|
| **Device** | Samsung **SM-S938B** (USB) · Android **17** |
| **App (smoke initial)** | `be.monmoisclair.app` · **versionName `0.1.0-6.77`** · versionCode **82** |
| **App (fix P0)** | **`0.1.0-6.79`** · versionCode **84** — police système RemoteViews |
| **Méthode** | `adb` · `uiautomator` · taps · `screencap` · lecture PNG |
| **Captures** | `docs/audits/smoke/*.png` (08 = état **avant** fix ; à re-capturer après install 6.79) |

**Verdict (soir) :** smoke **exécuté**. L’app s’ouvre et se navigue. **P0 widget** : instance launcher cassée (« Impossible d’ajouter le widget. »).

**Suivi 6.79 :** cause host = `View` interdit en RemoteViews (+ `@font` 6.76). Fix + install release **6.79** · widget id **66** affiche **Reste 2 106 €** (capture `08_widget.png` post-fix). Tests **318/318**.

---

## Captures

| # | Écran | Fichier |
|--:|-------|---------|
| 1 | Accueil (Octobre 2026) | [`docs/audits/smoke/01_accueil.png`](smoke/01_accueil.png) |
| 2 | Menu (bottom sheet) | [`docs/audits/smoke/02_menu.png`](smoke/02_menu.png) |
| 3 | Réglages | [`docs/audits/smoke/03_reglages.png`](smoke/03_reglages.png) |
| 4 | Dossiers | [`docs/audits/smoke/04_dossiers.png`](smoke/04_dossiers.png) |
| 5 | À venir | [`docs/audits/smoke/05_a_venir.png`](smoke/05_a_venir.png) |
| 6 | Choisir un mois (calendrier) | [`docs/audits/smoke/06_calendrier.png`](smoke/06_calendrier.png) |
| 7 | Fiche édition (`loyer3`) | [`docs/audits/smoke/07_fiche.png`](smoke/07_fiche.png) |
| 8 | Widget launcher (**OK 6.79** — Reste) | [`docs/audits/smoke/08_widget.png`](smoke/08_widget.png) (PNG gitignoré) |

---

## P0 — bloquant

### W01 — Widget launcher mort → **CORRIGÉ + validé device (6.79)**
- **Où :** écran d’accueil Android · `CarnetWidgetProvider` (id **65** cassé → **66** après fix).
- **Vu (soir) :** pastille **« Impossible d'ajouter le widget. »**.
- **Cause (log `AppWidgetHostView`) :** `Class not allowed to be inflated android.view.View` sur le filet (`widget_rule`). Aggravant 6.76 : `@font/source_serif4_semibold`. Manifest / `exported` OK.
- **Fix 6.79 :** filet → `ImageView` · police `serif` système · `minResize*` · coquille sync · tests contrat.
- **Preuve device :** install `0.1.0-6.79` (vc 84) · uiautomator `Reste` + `En retard · virement…` · screencap `08_widget.png` (2 106 €).

---

## P1 — fort (visibilité / pratique)

### H01 — Badge « FAIT » chevauche la carte → **FIX CODE 6.80**
- **Où :** Accueil · carte « Mettre de côté » (`01_accueil.png`).
- **Vu (soir) :** pastille « FAIT le 22/10/26 » **par-dessus** le libellé et le montant « − 2 € ».
- **Fix 6.80 :** `ExpenseRow` — pastille / statut dans une **bande sous** la rangée titre·montant·Remettre (plus de collision).
- **Reste :** revalider visu doigt S938B.

### F01 — Jour 31 masqué / collé sous sticky Enregistrer → **FIX CODE 6.80**
- **Où :** fiche (`07_fiche.png`) · grille « Jour du mois ».
- **Vu (soir) :** **31** hors zone lisible sous sticky Enregistrer / Annuler / Supprimer.
- **Fix 6.80 :** spacer post-grille **3×** `minTouchHeight` + clearance (sticky 3 actions).
- **Reste :** revalider visu doigt.

### D01 — Compteur Menu « Dossiers (4) » ≠ liste visible → **FIX CODE 6.80**
- **Où :** Menu (« Dossiers **(4)** ») vs Dossiers **En cours** = **2**.
- **Cause :** Menu comptait tous les accords actifs (soldés inclus).
- **Fix 6.80 :** chiffre Menu = dossiers **non soldés** (`ListDossiersUseCase`) ; soldés seuls → « Dossiers » sans faux (N).

---

## P2 — polish / copy / données démo

### C01 — Noms de test en prod locale
- Accueil / fiche : **`loyer3`** ; Dossiers : **`birdet`**, **`CourrierTestP`**.
- OK pour debug, **pas** pour démo famille — nettoyer ou renommer avant distribution.

### C02 — Copy À venir un peu « doc »
- Sous-titre : « Prochains mois (lecture seule). Plus loin : Accueil → calendrier. »
- Correct mais technique ; pour famille, préférer une phrase courte sans flèche de parcours.

### C03 — Fiche : Annuler serré entre deux gros CTA
- « Enregistrer » (plein) → « Annuler » (lien) → « Supprimer » (outline danger) : hiérarchie OK, mais **Annuler** est visuellement écrasé (`07_fiche.png`).

### C04 — Grands vides avant « Retour »
- Réglages / Dossiers / À venir : beaucoup d’espace crème puis bouton Retour sticky — lisible, un peu « page vide » sur grand écran S938B.

### OK notés (pas de ticket)
- Menu bottom sheet clair (Réglages / Dossiers / À venir / Choisir un mois) + lien Arkalia.
- Réglages : version **6.77** (soir) + claim privacy « pas de compte, pas de cloud » alignés.
- Calendrier mois : années 2024–2028, Oct sélectionné, copy futur « sans FAIT » OK.
- À venir : 6 mois listés, chacun « 10 échéances » (cohérent si récurrences stables — à revalider si un mois diverge).

---

## Parcours effectué

1. `adb devices` → `R3CY60BJ3ZM` device  
2. Launch `be.monmoisclair.app` (déjà `0.1.0-6.77`)  
3. Accueil → Menu → Réglages → Retour  
4. Menu → Dossiers → Retour  
5. Menu → À venir → Retour  
6. Header « Choisir un mois » → calendrier → fermer  
7. Tap carte `loyer3` → fiche édition → Annuler / Back  
8. `KEYCODE_HOME` → pages launcher → widget Mon Mois Clair (état erreur)

---

## Suite recommandée

1. ~~W01 widget~~ **fait 6.79**.  
2. **Revalider H01** (FAIT) + **F01** (jour 31) sur ce même SM-S938B.  
3. Ensuite seulement : claim « smoke S26 / dans la main » dans la synthèse nuit.
