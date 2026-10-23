# Smoke device — 2026-10-23 (soir)

| | |
|--|--|
| **Device** | Samsung **SM-S938B** (USB) · Android **17** |
| **App** | `be.monmoisclair.app` · **versionName `0.1.0-6.77`** · versionCode **82** |
| **Install** | Déjà installée (alignée `build.gradle.kts`) — pas de réinstall (aucun APK release local sous `app/build/outputs/`) |
| **Méthode** | `adb` · `uiautomator` · taps · `screencap` · lecture PNG |
| **Code app** | **Aucun commit / aucune modif** HomeScreen / EntryForm |
| **Captures** | `docs/audits/smoke/*.png` |

**Verdict :** smoke **exécuté**. L’app s’ouvre et se navigue (Accueil → Menu → Réglages / Dossiers / À venir / calendrier / fiche). **P0 widget** : instance launcher cassée (« Impossible d’ajouter le widget. »). Le badge **FAIT** chevauche encore la carte Accueil (claim 6.77 « FAIT 96 dp » **non validé** visuellement).

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
| 8 | Widget launcher | [`docs/audits/smoke/08_widget.png`](smoke/08_widget.png) |

---

## P0 — bloquant

### W01 — Widget launcher mort
- **Où :** écran d’accueil Android · instance `CarnetWidgetProvider` (widget id **65** confirmé via `dumpsys appwidget`).
- **Vu :** pastille sombre avec texte **« Impossible d'ajouter le widget. »** — aucun Reste / titre / montant (capture `08_widget.png`).
- **Impact :** promesse « widget Reste » (synthèse nuit #8) **fausse** sur ce device One UI ; inutile pour la famille.
- **Piste :** refresh RemoteViews après install/update, layout One UI / taille 3×2, ou ré-ajout widget ; **ne pas** se fier au seul claim code 6.77.

---

## P1 — fort (visibilité / pratique)

### H01 — Badge « FAIT » chevauche la carte
- **Où :** Accueil · carte « Mettre de côté » (`01_accueil.png`).
- **Vu :** pastille « FAIT le 22/10/26 » **par-dessus** le libellé et le montant « − 2 € » ; le bouton « Remettre » reste visible à droite.
- **Impact :** lecture confuse ; claim smoke « FAIT 96 dp » **non tenu** sur SM-S938B @ densité réelle.
- **Attendu :** pastille dans sa propre bande (pas de collision texte/montant).

### F01 — Jour 31 masqué / collé sous sticky Enregistrer
- **Où :** fiche (`07_fiche.png`) · grille « Jour du mois ».
- **Vu :** cercles 1–30 visibles ; le **31** est dans l’arbre a11y (`Jour 31`) mais **hors zone lisible** — footer sticky « Enregistrer / Annuler / Supprimer » recouvre le bas de la grille.
- **Impact :** mois à 31 jours → jour difficile à sélectionner / à voir (surtout gros doigt / fontScale).

### D01 — Compteur Menu « Dossiers (4) » ≠ liste visible
- **Où :** Menu `02_menu.png` (« Dossiers **(4)** ») vs écran Dossiers `04_dossiers.png` (section **En cours** = **2** cartes : birdet, CourrierTestP).
- **Impact :** doute sur le nombre réel ; si 2 sont archivés / autre section, le menu doit le dire ou n’afficher que le compte visible.

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
- Réglages : version **6.77** + claim privacy « pas de compte, pas de cloud » alignés.
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

## Suite recommandée (hors scope ce smoke)

1. **Corriger W01** (widget) puis re-screencap `08_widget.png`.  
2. **Revalider H01** (FAIT) + **F01** (jour 31) sur ce même SM-S938B.  
3. Ensuite seulement : claim « smoke S26 / dans la main » dans la synthèse nuit.
