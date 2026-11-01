# Audit premium-05 — a11y + One UI Samsung (senior)

| Champ | Valeur |
|-------|--------|
| Date | 2026-11-01 |
| Rôle | Auditeur Arkalia — **AUDIT ONLY** (pas de correction code) |
| Domaine | Accessibilité + One UI Samsung (cibles, contrastes, TalkBack, font scale, widget RemoteViews, icône notif, Device Care / défaillances) |
| Workspace | `/Volumes/T7/Mon Mois Clair` — **aligné** au projet transmis |
| Branche | `develop` |
| SHA | `189a32c` (contrôle Git de cet appel) |
| Version | `0.1.0-6.91` · `versionCode` 96 (`app/build.gradle.kts`) |
| Méthode | Relecture code + calculs WCAG locaux + croisement docs/tests ; **pas** de TalkBack device ; **pas** de `./gradlew` dans cet appel |
| Réfs croisées | `docs/audits/2026-11-01-samsung-defaillances-notif-icon.md` · archive `2026-10-23-nuit-06-a11y-oneui.md` |

**Note /10 (code a11y–One UI) : 8,0 / 10**  
Socle tenu (48 dp, AAA testés, plafond fontScale hero, contrats widget, largeIcon notif, sortie Direct Boot). Pas 9+ : smoke S26 + TalkBack réel + rescan Device Care encore **NON VÉRIFIÉS** ; typo widget &lt; 18 sp ; épargne compacte derrière « Détails ».

---

## 0. Périmètre / hors périmètre

**Couvert :** touch targets · contrastes palette · sémantique TalkBack (code) · fontScale / DisplayFit · RemoteViews widget · icônes notification · chemin Device Care / BootReceiver.

**Non audité ici :** Dependabot / Kotlin CVE · CI · restore `.mmc` doigt · sécurité vault hors a11y · DA pure hors contraste.

---

## 1. Cibles tactiles (≥ 48 dp)

### Constat — token et boutons socle

`A11yTokens.minTouchHeight = 48.dp` ; `PrimaryA11yButton` / `SecondaryA11yButton` / `DangerA11yButton` / `QuietLinkA11y` / `CarnetHubRow` / `StickyLinkRow` / `MonthStep` (48×48) / grille jours `size(48.dp)`.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `app/src/main/java/be/monmoisclair/app/ui/theme/A11yTokens.kt` L50 ; `A11yButton.kt` L50–51, L93–94, L141–143 ; `HomeStickyChrome.kt` L40, L74 ; `DayOfMonthGrid.kt` (commentaire + `size(A11yTokens.minTouchHeight)`) |

### Constat — FAIT compact aligné charte ≥ 96 dp

Bouton FAIT / Remettre : `widthIn(min = 96.dp)` (plus le 88 dp de l’audit 6.75).

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `HomeTimelineRows.kt` L252–253 |

### Constat — zone boutons système One UI

`MainActivity` : `safeDrawingPadding` + `tappableElement` bas + `bottomSystemClearance` (28 dp) **une seule fois**.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `MainActivity.kt` ~L164–173 ; commentaire `A11yTokens.kt` L54–58 |

### Constat — QuietLink largeur ≈ texte

`QuietLinkA11y` impose hauteur ≥ 48 et `widthIn(min = 48)` seulement si `fillMaxWidth = false` ; libellés courts (« Copier », « Photo ») restent étroits sur ~360 dp.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** (risque structurel) | inspection | `A11yButton.kt` L139–143 |
| **NON VÉRIFIÉ** | smoke test manuel | faux clic vs FAIT voisin sur S26 — non exécuté |

### Constat — pastille « ! » Menu

Badge décoratif sur Menu (`HomeBottomBar`) : nœud sémantique séparé (« Alertes à régler… ») au-dessus du bouton Menu.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `HomeBottomBar.kt` L57–69 |
| **NON VÉRIFIÉ** | smoke TalkBack | fusion / double annonce — non exécuté |

---

## 2. Contrastes (WCAG AAA revendiqué ≥ 7:1)

### Constat — tests unitaires jour / nuit

`WcagContrastTest` couvre encre, muted, boutons, chips, danger/warn sur **crème et PaperWarm** (jour), et matrice nuit surfaces × encres.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `WcagContrastTest.kt` (fichier entier) |
| **NON VÉRIFIÉ** | test automatisé | `./gradlew :app:testDebugUnitTest` **non exécuté** dans cet appel |

### Constat — recalcul local (couleurs opaques)

| Paire | Ratio | Seuil 7:1 |
|-------|-------|-----------|
| Ink / PaperCream jour | 15,43:1 | OK |
| InkMuted / PaperCream | 8,56:1 | OK |
| DangerInk / PaperWarm | 7,82:1 | OK |
| AccentWarn / PaperWarm | 8,38:1 | OK |
| ButtonText / AccentWarn (badge !) | 10,09:1 | OK |
| OnGold / AccentAuto | 7,07:1 | OK (limite) |
| FieldLine / PaperCream | 3,41:1 | hors texte courant (trait) |

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | commande | script Python local WCAG sRGB (cet appel) |
| **DÉCLARÉ** | inspection | ancienne archive 6.75 : danger/warm « sous AAA » — **contredit** par palette + tests actuels |

### Constat — FieldLine / PaperBorder

Traits de champ / filets volontairement faibles (pas texte).

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection + commande | `A11yTokens.Day.fieldLine` ; ratio FieldLine 3,41:1 |

---

## 3. TalkBack / sémantique (code)

### Constat — couverture sémantique large

`contentDescription`, `liveRegion`, `Role.Button` / Radio / Checkbox sur Home, fiches, scan, settings, santé, restore, lock, widget root.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | grep `semantics` / `contentDescription` sous `app/src/main` (nombreux écrans) |

### Constat — lock ne laisse pas lire le NavHost

`BiometricLockScreen` : consume pointeurs + `contentDescription = "Application verrouillée"` ; commentaire Activity `clearAndSetSemantics`.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `BiometricLockScreen.kt` L76–99 |
| **NON VÉRIFIÉ** | smoke TalkBack | fuite sous-jacent — non exécuté |

### Constat — mois sticky sans 4ᵉ cible calendrier

Libellé mois tapppable « Choisir un mois » ; ‹ › = `MonthStep` ; commentaire « plus de 4ᵉ cible ».

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `HomeStickyHeader.kt` L64–89 |
| **DÉCLARÉ** | inspection | archive 6.75 P1-6 doublon mois+calendrier — **écart** vs code actuel |

### Constat — comportement TalkBack réel

| Statut | Nature | Preuve |
|--------|--------|--------|
| **NON VÉRIFIÉ** | smoke test manuel | TalkBack One UI S26/S25 — non exécuté |

---

## 4. Font scale / hauteur courte (S26)

### Constat — DisplayFit compact ≤ 800 dp

`compactHomeChrome` ; Reste 34 sp compact / 40 confort ; paddings sticky resserrés.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `DisplayFit.kt` L45–66 ; `DisplayFitTest.kt` |

### Constat — plafond visuel hero si fontScale ≥ 1,25

Pré-compensation : `remainingAmountSp = visualCap / fontScale` (caps 34 / 30 / 28 selon paliers).

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `DisplayFit.kt` L78–92 |
| **NON VÉRIFIÉ** | test automatisé | **aucun** test unitaire du chemin `fontScale` / `visualCap` (grep test vide) |
| **NON VÉRIFIÉ** | smoke | One UI « Grand / Très grand » sur hauteur ~780 dp — non exécuté |

### Constat — typo Compose ≥ 18 sp

Slots Material utilisés ≥ `minBodySp` (18 sp) Source Sans / Serif brand.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `MonMoisClairTheme.kt` L105–176 ; `A11yTokens.minBodySp` |

### Constat — grille jours en dp fixes

Cases 48×48 dp : à gros scale, glyphes 10–31 peuvent clipper.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** (risque) | inspection | `DayOfMonthGrid.kt` |
| **NON VÉRIFIÉ** | smoke | non exécuté |

### Constat — épargne derrière « Détails » en compact

Sur hauteur courte, « Mettre de côté » n’est pas au premier balayage sticky.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `HomeStickyHeader.kt` L167–189, L261–275 |

---

## 5. Widget RemoteViews (One UI)

### Constat — contrats anti-crash host

Pas de `@font/` ; pas de `<View` nu ; séparateur `ImageView` ; serif / sans-serif système.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `widget_carnet.xml` ; `CarnetWidgetLayoutContractTest.kt` |

### Constat — contraste runtime jour/nuit

`CarnetWidgetRefresher.render` aligne ink / muted / danger sur tokens ; `contentDescription` racine ; clic → `MainActivity`.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `CarnetWidgetRefresher.kt` L76–125 |

### Constat — typo widget sous minBodySp app

Title **11 sp**, line **13 sp**, amount **34 sp** — hors plancher corps app 18 sp (contrainte densité widget).

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `widget_carnet.xml` L32, L48, L74 |

### Constat — colors night XML vs runtime

Commentaire `values-night/colors.xml` : widget « fixes dans values/ » ; la nuit réelle est **programmée** dans `render()`.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `values-night/colors.xml` L3 ; `CarnetWidgetRefresher.kt` |

### Constat — pose / inflation device

| Statut | Nature | Preuve |
|--------|--------|--------|
| **NON VÉRIFIÉ** | smoke | ajout widget One UI — non exécuté |
| **DÉCLARÉ** | inspection | `PROGRESS_LOG` / audits finaux : widget enregistré, pose home souvent ouverte |

---

## 6. Icône notification

### Constat — smallIcon + largeIcon

`setSmallIcon(R.drawable.ic_notification)` (silhouette blanche) + `setLargeIcon` bitmap coloré `ic_notification_large` ; teinte teal charte.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `PaymentNotificationHelper.kt` L110–127 ; `ic_notification.xml` ; présence `ic_notification_large.png` |
| **VÉRIFIÉ** | inspection | `PaymentNotificationIconsTest.kt` (existence / decode / builder) |
| **NON VÉRIFIÉ** | smoke | lisibilité barre d’état / bannière collapsed Samsung — non exécuté |
| **DÉCLARÉ** | inspection | `2026-11-01-samsung-defaillances-notif-icon.md` §B (historique session ship) |

### Constat — action FAIT sans icône drawable

`addAction(0, …)` — libellé seul.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `PaymentNotificationHelper.kt` L75–78 |

---

## 7. Device Care / défaillances / batterie

### Constat — sortie Direct Boot (cause ENOENT)

Manifest : **pas** `directBootAware` ; **pas** `LOCKED_BOOT_COMPLETED`. `BootReceiver` garde `UserManager.isUserUnlocked`.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `AndroidManifest.xml` L52–63 ; `BootReceiver.kt` L21–34, L65–67 |

### Constat — hub Santé / copy Samsung sans jargon

Écran Alertes : notifs / réveil / batterie + hint `DeviceCareCopy` (sans « Device Care » dans le texte utilisateur).

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `ReminderHealthScreen.kt` ; `DeviceCareCopy.kt` |

### Constat — score Device Care post-6.91 sur device

| Statut | Nature | Preuve |
|--------|--------|--------|
| **NON VÉRIFIÉ** | smoke | rescan Maintenance appareil — non exécuté |
| **DÉCLARÉ** | inspection | audit samsung du jour : historique Device Care peut rester ; batterie intermittente ; à faire Athalia (`PROGRESS_LOG` prochaine sous-étape) |

---

## 8. Sheets / IME (filet One UI)

`rememberSheetBottomClearance` (Menu, mois, Courses, Épargne) ; `imePadding` / `imeOpen` Home + fiches.

| Statut | Nature | Preuve |
|--------|--------|--------|
| **VÉRIFIÉ** | inspection | `SheetBottomClearance.kt` ; usages Home*Sheet / EntryFormScaffold |
| **NON VÉRIFIÉ** | smoke | coupe boutons 3-boutons / geste — non exécuté |

---

## 9. Faiblesses + améliorations (priorisées)

### P0 — bloquant claim « a11y One UI senior OK / 9/10 dans la main »

| Id | Faiblesse | Amélioration (conseil, non appliqué) |
|----|-----------|--------------------------------------|
| **P0-1** | Smoke **S26** + texte ≥ 1,3 + TalkBack **non faits** | Campagne doigt D1–D7 (sticky, FAIT, sheets, grille, lock) |
| **P0-2** | Rescan **Device Care** post-6.91 non confirmé | Optimiser / rescanner après install ; ne pas claim « 0 défaillance » sans preuve device |

Pas de P0 « contraste nuit cassé », « cibles 48 absentes », « widget @font », « Direct Boot ENOENT encore dans le manifest » — ces points code sont tenus à l’inspection.

### P1 — avant claim familial premium

| Id | Faiblesse | Amélioration (conseil) |
|----|-----------|------------------------|
| **P1-1** | Widget title/line **11/13 sp** | Remonter muted ≥ 14–16 sp si densité 2×2 le permet ; fumer senior |
| **P1-2** | Épargne **derrière Détails** en compact | Découvrabilité TalkBack / 1ʳᵉ visite senior |
| **P1-3** | Pas de test du plafond **fontScale** hero | Test unitaire / composition du `visualCap` |
| **P1-4** | Grille jours **48 dp** + gros texte | Clip / lisibilité 10–31 |
| **P1-5** | QuietLink largeur ≈ texte | Faux clics 360 dp vs FAIT / Photo |
| **P1-6** | Badge « ! » Menu = nœud séparé | Fusion sémantique avec Menu |

### P2 — polish

| Id | Faiblesse | Amélioration (conseil) |
|----|-----------|------------------------|
| **P2-1** | Action notif `icon = 0` | Icône monochrome optionnelle |
| **P2-2** | Commentaire colors night widget vs runtime | Aligner doc XML |
| **P2-3** | OnGold / AccentAuto ~7,07:1 | Marge AAA mince — surveiller pastille Auto |
| **P2-4** | ExpenseRow zone « modifier » sans `heightIn` explicite | Contenu compense ; borner si layouts vides |

---

## 10. Écarts vs audits antérieurs

| Sujet | Archive / doc (DÉCLARÉ) | Code 6.91 (VÉRIFIÉ) |
|-------|-------------------------|---------------------|
| Hero sp × fontScale sans plafond | P0-1 nuit-06 | Pré-compensation `visualCap / fontScale` |
| FAIT compact 88 dp | P1-1 nuit-06 | `widthIn(min = 96.dp)` |
| Danger/Warn &lt; 7:1 sur PaperWarm | P1-2 nuit-06 | Tests + recalcul ≥ 7,8:1 |
| Doublon mois + icône calendrier | P1-6 nuit-06 | Calendrier retiré ; libellé seul |
| Device Care défaillances | Audit samsung du jour | Manifest/BootReceiver corrigés ; score device encore ouvert |

---

## 11. Note /10 — lecture senior

| Axe | Note | Commentaire |
|-----|------|-------------|
| Cibles 48 / One UI chrome | 8,5 | Socle cohérent ; QuietLink / badge Menu = filet |
| Contrastes AAA | 8,5 | Tests + recalcul OK ; FieldLine hors scope texte |
| TalkBack | 7,0 | Code riche ; **zéro** preuve device cet appel |
| Font scale / S26 | 7,5 | Plafond hero codé ; non fumé ; grille dp fixe |
| Widget RemoteViews | 8,0 | Contrats P0 One UI OK ; typo petite |
| Notif icon | 8,0 | largeIcon présent ; smoke Samsung ouvert |
| Device Care | 7,5 | Cause crash fermée en code ; historique device ouvert |
| **Synthèse code** | **8,0** | Honnête vs « 9/10 dans la main » (interdit sans S26) |

---

## 12. Clôture auditeur

- **Domaine couvert :** a11y + One UI Samsung (cibles, contrastes, TalkBack code, fontScale, widget, notif icon, Device Care).
- **Limites (NON VÉRIFIÉ) :** TalkBack réel · smoke S26 · fontScale device · pose widget · lisibilité notif barre d’état · rescan Device Care · exécution Gradle des tests dans cet appel.
- **Fichier :** `docs/audits/2026-11-01-premium-05-a11y-oneui.md` créé sur oui explicite Athalia.
- **aucune modification faite** au code applicatif (MD d’audit uniquement).
