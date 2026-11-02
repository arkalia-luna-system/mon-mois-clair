# Audit expert-06 — a11y polish (densités · contrastes · One UI · widget · notif)

| Champ | Valeur |
|-------|--------|
| Date | 2026-11-02 |
| Rôle | Auditeur Arkalia — **AUDIT ONLY** (pas de correction appliquée) |
| Domaine | Accessibilité / polish premium — densités sticky, contrastes AAA, One UI S25/S26, widget RemoteViews, icône notification |
| Workspace | `/Volumes/T7/Mon Mois Clair` — **aligné** au projet transmis |
| Branche | `develop` @ `6d877b9` |
| Version | `0.1.0-7.06` · `versionCode` 111 |
| Méthode | Inspection code + calculs WCAG locaux + croisement charte / journal / audits 6.91→7.06 ; **pas** de TalkBack device ; **pas** de `./gradlew` dans cet appel |
| Réfs | [`2026-11-02-accueil-sticky.md`](2026-11-02-accueil-sticky.md) · archive `premium-05-a11y-oneui` · [`premium-clean-10.md`](2026-11-01-premium-clean-10.md) · `CHARTE_ET_ARCHITECTURE.md` §4bis–6 |

**Note /10 (craft a11y–One UI, code seul) : 8,7 / 10**

Socle tenu et **remonté** vs premium-05 (6.91 → **8,0**) : sticky 7.06 plus basse, QuietLink ≥96, badge Menu fusionné, widget 14/16 sp, grille fontScale, action notif icônée, tests `withFontScale`.  
Pas **9+** : smoke **S26** + TalkBack réel + rescan Device Care + pose widget / bannière notif Samsung encore **NON VÉRIFIÉS** (Athalia). Pas claim « 10 main ».

---

## 0. Périmètre / hors périmètre

**Couvert :** densités Accueil (`DisplayFit` / sticky 7.06) · cibles ≥48 / FAIT ≥96 · contrastes palette · fontScale · widget RemoteViews · icônes notif · filets One UI (clearance / sheets) — polish premium P0/P1/P2.

**Non audité ici :** restore `.mmc` doigt · Dependabot · résumé PDF métier · DA pure hors contraste · copy Aide hors a11y.

---

## 1. Densités Accueil (7.06)

### Constat — sticky resserrée + rangée outil

`DisplayFit` : padding vertical sticky **8 / 10 dp**, `sectionGap` **4 / 6**, hero **34 / 40 sp** ; plus de Spacer hero. Courses + Mettre de côté = **une** `StickyToolsRow` (2 cellules ≥48), plus 2×48 empilées. Épargne **toujours** en rangée (y compris compact ≤800) — écart vs premium-05 P1-2.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `DisplayFit.kt` L52–65 ; `HomeStickyHeader.kt` L37–40, L147–176 ; `HomeStickyChrome.kt` `StickyToolsRow` / `StickyToolCell` |
| **DÉCLARÉ** | inspection | `PROGRESS_LOG` 7.06 · audit sticky — estimation hauteur pliée ~200–230 dp compact (vs ~280–360 avant) |
| **NON VÉRIFIÉ** | smoke test manuel | liste vraiment « visible » doigt Athalia — non exécuté |

### Constat — plafond fontScale hero

`withFontScale` : caps visuels 34 / 30 / 28 si scale ≥1,25 / 1,5 / 1,8 ; tests unitaires présents (écart premium-05 P1-3 **fermé en code**).

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `DisplayFit.kt` L74–87 ; `DisplayFitTest.kt` `withFontScale_capsVisualHero` |
| **NON VÉRIFIÉ** | smoke | One UI Grand / Très grand sur ~780 dp — non exécuté |

### Constat — hauteur sticky estimée (plié, hors Détails)

Hypothèses : compact 780 dp · Courses+épargne · 1 ligne statut · pas d’aperçu futur.

| Bloc | ~dp |
|------|-----|
| Padding V (8×2) | 16 |
| Rangée mois | 48 |
| Gaps `sectionGap`×≈4 | ~16 |
| Titre + montant 34 sp | ~56 |
| Statut factures | ~24 |
| Rangée outils (1×48) | 48 |
| Divider | 1 |
| **Total plié** | **~210 dp** |

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | structure `HomeStickyHeader` + tokens `DisplayFit` |
| **NON VÉRIFIÉ** | smoke | mesure uiautomator / screencap — non exécuté |

---

## 2. Cibles tactiles One UI (≥ 48 · FAIT ≥ 96)

### Constat — tokens et composants socle

`A11yTokens.minTouchHeight = 48.dp` ; `screenPadding = 24.dp` ; boutons A11y / QuietLink / MonthStep 48×48 / FAIT `widthIn(min = 96.dp)` / grille jours `size(48.dp)`.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `A11yTokens.kt` L48–50 ; `A11yButton.kt` QuietLink ; `HomeTimelineRows.kt` ~L257 ; `DayOfMonthGrid.kt` L80 |

### Constat — QuietLink largeur min 96 (hors fill)

Si `fillMaxWidth = false` → `widthIn(min = 96.dp)` (aligné FAIT) — premium-05 P1-5 **fermé en code**.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `A11yButton.kt` L141–149 |
| **NON VÉRIFIÉ** | smoke | faux clic Copier/Photo vs FAIT sur 360 dp — non exécuté |

### Constat — cellules sticky demi-largeur

Deux `StickyToolCell` `weight(1f)` + gap 8 dp sur ~360 dp → ~156 dp chacune, hauteur ≥48. Labels ellipsis ; TalkBack via `contentDescription` riche.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `HomeStickyChrome.kt` L32–101 |
| **NON VÉRIFIÉ** | smoke | pouce senior / gros doigt sur demi-cellule — non exécuté |

### Constat — zone boutons système

`bottomSystemClearance` 28 dp **une fois** (`MainActivity`) + sheets `rememberSheetBottomClearance` (plancher 48).

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `A11yTokens.kt` L54–58 ; `SheetBottomClearance.kt` ; charte §6 |
| **NON VÉRIFIÉ** | smoke | 3-boutons / geste S25 Ultra — non exécuté |

---

## 3. Contrastes (WCAG AAA ≥ 7:1)

### Constat — suite unitaire jour / nuit

`WcagContrastTest` : encre, muted, boutons, chips, danger/warn sur crème **et** PaperWarm ; matrice nuit surfaces × encres ; `inkMuted ≠ ink` nuit.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `WcagContrastTest.kt` |
| **NON VÉRIFIÉ** | test automatisé | `./gradlew :app:testDebugUnitTest` **non exécuté** cet appel (historique journal 7.06 : 411/412 — **DÉCLARÉ**) |

### Constat — recalcul local (cet appel)

| Paire | Ratio | Seuil 7:1 |
|-------|------:|:---------:|
| Ink / PaperCream | 15,43 | OK |
| InkMuted / PaperCream | 8,56 | OK |
| DangerInk / PaperWarm | 7,82 | OK |
| AccentWarn / PaperWarm | 8,38 | OK |
| OnGold / AccentAuto | **7,07** | OK (marge mince) |
| Night InkMuted / PaperCream | 9,77 | OK |
| Night Link / PaperWarm | 7,20 | OK |
| Widget muted / crème | 8,56 | OK |
| FieldLine / PaperCream | 3,41 | hors texte (trait) |

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | commande | script Python sRGB WCAG (cet appel) |
| **VÉRIFIÉ** | inspection | `A11yTokens.Day` / `Night` ; `colors.xml` widget_* |

---

## 4. TalkBack / sémantique (code)

### Constat — couverture large + fusions 6.96

`contentDescription` / `liveRegion` / `Role` sur Accueil, fiches, settings, lock… Menu badge `!` : `mergeDescendants` + description unique (P1-6 fermé). Sticky hero montant annoncé.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `HomeBottomBar.kt` L52–80 ; `HomeStickyHeader.kt` L125–127 ; grep semantics app |
| **NON VÉRIFIÉ** | smoke TalkBack | balayage Accueil / lock One UI — non exécuté |

### Constat — risque double annonce cellules outil

`StickyToolCell` : `clickable(onClickLabel=…)` **et** `.semantics { contentDescription = … }` sur le même Row.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** (risque structurel) | inspection | `HomeStickyChrome.kt` L40–45 |
| **NON VÉRIFIÉ** | smoke | doublon annoncé — non exécuté |

---

## 5. Font scale / grille jours

### Constat — DayOfMonthGrid adapte le glyphe

Si `fontScale ≥ 1.3` → `labelMedium` dans case 48 dp (P1-4 partiellement traité).

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `DayOfMonthGrid.kt` L48–56, L80 |
| **NON VÉRIFIÉ** | smoke | clip 10–31 à 200 % — non exécuté |

---

## 6. Widget RemoteViews (One UI / S25)

### Constat — contrats anti-crash host

Pas de `@font/` ; pas de `<View` nu ; filet `ImageView` ; serif / sans-serif système ; `minResize*` / `targetCell*`.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `widget_carnet.xml` ; `CarnetWidgetLayoutContractTest.kt` |

### Constat — typo muted senior (post-6.96)

Title **14 sp**, line **16 sp**, amount **34 sp** — encore sous `minBodySp` app (18) par densité 2×2, mais **au-dessus** du plancher 14 testé. Écart premium-05 (11/13) **fermé**.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `widget_carnet.xml` L32, L48, L74 ; contrat `widget_muted_text_atLeast14sp` |

### Constat — contrastes runtime + fond Apparence

`CarnetWidgetRefresher.render` : ink / muted / danger Day|Night ; `contentDescription` racine ; clic → `MainActivity`. BG crème/nuit drawables (pas inversion `values-night` téléphone).

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `CarnetWidgetRefresher.kt` L76–124 ; `widget_carnet_bg.xml` coins 16 ; `values-night/colors.xml` commentaire |
| **NON VÉRIFIÉ** | smoke | pose / inflation SM-S938B ce build 7.06 — non exécuté |
| **DÉCLARÉ** | inspection | journal historique 6.79–6.91 : pose OK sur S25 Ultra |

### Constat — filet jour XML vs runtime

`colors.xml` `widget_rule` = `#E2DDD2` ; `render()` jour utilise `#D8D2C6` — écart cosmétique mineur.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `colors.xml` L10 ; `CarnetWidgetRefresher.kt` L82–83 |

---

## 7. Icône notification

### Constat — smallIcon + largeIcon + teinte

`setSmallIcon(ic_notification)` silhouette blanche 24 dp ; `setLargeIcon` PNG **192×192** coloré ; `setColor` teal `#1B4D4E`. Actions FAIT / Remettre : `R.drawable.ic_notification` (plus `icon = 0` — P2-1 fermé).

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `PaymentNotificationHelper.kt` L75–78, L105–108, L114–125 ; `ic_notification.xml` ; PNG 192×192 |
| **VÉRIFIÉ** | inspection | `PaymentNotificationIconsTest.kt` |
| **NON VÉRIFIÉ** | smoke | barre d’état / shade collapsed One UI — non exécuté |

---

## 8. Priorités P0 / P1 / P2

### P0 — bloquant claim « a11y One UI senior OK / 9+ dans la main »

| Id | Faiblesse | Amélioration (conseil, **non appliqué**) |
|----|-----------|------------------------------------------|
| **P0-1** | Smoke **S26** + fontScale ≥1,3 + TalkBack **ouverts** | Campagne doigt : sticky basse + liste · FAIT · sheets · grille · lock · QuietLink |
| **P0-2** | Rescan **Device Care** / Maintenance appareil non confirmé sur 7.06 | Rescan Athalia post-install ; pas claim « 0 défaillance » sans preuve |

Pas de P0 code « contrastes cassés », « cibles 48 absentes », « widget `@font` / View nu », « sticky encore 2×48 empilées », « QuietLink 48 seul », « notif sans largeIcon » — tenus à l’inspection **7.06**.

### P1 — avant claim familial premium

| Id | Faiblesse | Amélioration (conseil) |
|----|-----------|------------------------|
| **P1-1** | Widget muted **14/16** &lt; corps app 18 | Fumer senior 2×2 ; remonter line à 17–18 **seulement** si densité tient |
| **P1-2** | `StickyToolCell` : `onClickLabel` + `contentDescription` | Vérifier TalkBack ; fusionner / `clearAndSetSemantics` si doublon |
| **P1-3** | Demi-cellules Courses \| épargne ~156 dp | Smoke pouce ; libellés déjà courts — garder ≥48 hauteur |
| **P1-4** | Grille 48 dp + scale 200 % | Smoke clip ; éventuellement `labelSmall` au-delà de 1,6 |
| **P1-5** | OnGold / AccentAuto **7,07:1** | Marge AAA mince — ne pas éclaircir l’or |

### P2 — polish

| Id | Faiblesse | Amélioration (conseil) |
|----|-----------|------------------------|
| **P2-1** | Filet widget jour `#D8D2C6` vs XML `#E2DDD2` | Aligner runtime ↔ `widget_rule` |
| **P2-2** | Typo widget volontairement &lt; 18 sp | Documenter exception RemoteViews dans Aide / charte (déjà partiel) |
| **P2-3** | Pose widget / bannière notif 7.06 | Smoke SM-S938B (historique OK, build courant NON VÉRIFIÉ) |
| **P2-4** | Tests Gradle non relancés cet audit | Re-preuve `--rerun-tasks` avant claim push a11y |

---

## 9. Évolution vs premium-05 (6.91 → 7.06)

| Point premium-05 | État 7.06 (inspection) |
|------------------|------------------------|
| Note **8,0** | **8,7** craft code (plafond doigt inchangé) |
| Widget 11/13 sp | **14/16** + contrat test |
| QuietLink min 48 | **96** si non fill |
| Badge Menu nœud séparé | **mergeDescendants** |
| Épargne derrière Détails compact | **rangée outil** toujours |
| Pas de test fontScale | **DisplayFitTest** |
| Action notif icon 0 | **ic_notification** |
| Sticky dense / 2×48 | **7.06** densités + 1×48 outils |
| TalkBack / S26 / Device Care | **Toujours Athalia** |

---

## 10. Note /10 (détail)

| Sous-axe | Note | Lecture |
|----------|-----:|---------|
| Densités Accueil / DisplayFit | **9,0** | 7.06 crédible en code ; doigt Athalia ouvert |
| Contrastes palette | **9,2** | AAA testé + recalcul ; or 7,07 mince |
| Cibles / QuietLink / FAIT | **9,0** | Tokens + 96 ; smoke faux-clics ouvert |
| TalkBack sémantique code | **8,5** | Large couverture ; comportement réel NON VÉRIFIÉ |
| Widget One UI | **8,8** | Contrats + 14/16 + tokens ; pose 7.06 NON VÉRIFIÉE |
| Notif icon | **9,0** | small+large+teal+action ; shade Samsung NON VÉRIFIÉ |
| **Synthèse craft** | **8,7** | Moyenne pondérée honnête |
| **« Dans la main »** | **ouvert** | Tant que P0-1 / P0-2 ouverts → pas 9,5–10 main |

---

## Limites de cet audit

- Un seul domaine : **a11y polish** (densités, contrastes, One UI, widget, notif). Aide / hub / retard / résumé = hors périmètre (audits séparés du jour).
- Pas d’exécution Gradle ni TalkBack ni install device dans cet appel.
- Les claims journal « 411 tests » / smoke S25 historiques = **DÉCLARÉ**, pas re-prouvé ici.
- Inspection ≠ validation fonctionnelle doigt.

**Domaine couvert :** a11y polish Mon Mois Clair `0.1.0-7.06`.  
**aucune modification faite** hors rédaction de ce fichier demandé par Athalia.
