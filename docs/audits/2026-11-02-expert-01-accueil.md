# Audit expert 01 — Accueil sticky (hauteur réelle post-7.06)

| Champ | Valeur |
|-------|--------|
| Date | 2026-11-02 |
| Version | `0.1.0-7.06` · `versionCode` 111 · commit `6d877b9` |
| Device référence smoke | SM-S938B (S25 Ultra) — Athalia |
| Domaine | **Accueil uniquement** — sticky + viewport liste (pas Aide / Réglages / retard) |
| Nature | Inspection Compose · estimation hauteur en dp · **pas** mesure Layout Inspector · **pas** smoke doigt de cet appel |
| Déclencheur | Athalia : Accueil « **n’a quasi pas changé** » après sticky 7.06 |
| Verdict | **P0 confirmé** — 7.06 a fusionné les outils, pas assez pour ≥40 % de liste |

**Note Accueil (espace liste / sticky) : 4,5 / 10**  
(structure inchangée à l’œil ; gain réel ~20–30 % sticky, pas la cible −≈40 % de l’audit matin ; liste encore étouffée sur Ultra.)

---

## 0. Chemin & périmètre

| | |
|--|--|
| Workspace | `/Volumes/T7/Mon Mois Clair` — **aligné** (pwd) |
| Branche | `develop` @ 7.06 |
| Hors mission | Aide, hub Réglages, retard/accord, résumé, S26 restore |
| Preuve runtime doigt | **NON VÉRIFIÉ** cet appel — perception Athalia = **DÉCLARÉ** |

---

## 1. Constat Athalia vs claim 7.06

| Source | Claim | Statut |
|--------|-------|--------|
| `PROGRESS_LOG.md` L24–40 | « Accueil sticky basse — liste factures visible » · rangée Courses\|épargne | **DÉCLARÉ** (journal) |
| Audit matin `2026-11-02-accueil-sticky.md` §5–6 | Cible pliée **~180–220 dp** tall (−≈40 %) | **DÉCLARÉ** (doc) |
| Code actuel `HomeStickyHeader` + `DisplayFit` | Encore **4–5 bandes** empilées ; Ultra en mode **tall** | **VÉRIFIÉ** (inspection) |
| Athalia | « quasi pas changé » | **DÉCLARÉ** (utilisateur) |

**Désaccord :** le journal promet une sticky « basse » ; l’estimation code post-7.06 reste **~230–285 dp** pliée (Ultra, Détails fermés) — **au-dessus** de la cible 180–220. Perception « quasi pas changé » **cohérente** avec le code : même ordre mois → hero → statut → outils → Détails ; seul changement visible = 2 liens → 1 rangée + pads −6 dp.

---

## 2. Inventaire sticky actuel (7.06) — preuves lignes

Fichier : `app/src/main/java/be/monmoisclair/app/ui/home/HomeStickyHeader.kt`

| Rang | Élément | Lignes | Toujours ? | Hauteur forcée |
|------|---------|--------|------------|----------------|
| 1 | ‹ mois › (`MonthStep` 48 + libellé `minHeight` 48) | L66–98, `HomeStickyChrome` L147–160 | Oui | **≥48 dp** |
| 2 | Titre Reste (`bodyMedium` LH 24) + montant (`remainingAmountSp` +4 LH) | L108–129 | Oui | ~24 + 2 + **~38–44** |
| 3 | « Factures ouvertes… » / « Tout est payé » | L130–146 | Si factures | ~**26** (`bodyLarge` LH) |
| 4 | `StickyToolsRow` Courses \| épargne | L161–176 · Chrome L65–101 | Hors aperçu futur | **≥48** (`heightIn`) |
| 5 | Tip aperçu futur | L178–192 | Si futur | ~24–48 |
| 6 | « Détails / Masquer » | L207–243 | Si extras | **≥48** |
| 7 | Contenu Détails (Après FAIT, tips, courriers, Mois en cours) | L244–337 | Si ouvert | variable |

Padding colonne : `fit.stickyPaddingVertical` ×2 · `spacedBy(fit.sectionGap)` — L61–63.  
Divider : L339.

`DisplayFit.from` (**tall** si hauteur > 800) — `DisplayFit.kt` L52–65 :

```
stickyPaddingVertical = if (compact) 8.dp else 10.dp
sectionGap            = if (compact) 4.dp else 6.dp
remainingAmountSp     = if (compact) 34f else 40f
COMPACT_HEIGHT_DP     = 800
```

**S25 Ultra (SM-S938B)** : fenêtre typique **> 800 dp** → **chemin tall** (10 / 6 / 40 sp). Le resserrement « compact » **ne s’applique pas** au téléphone Athalia.  
Statut : **VÉRIFIÉ** (inspection code + constante) · comportement fenêtre réelle device : **NON VÉRIFIÉ** (Configuration non lue ici).

---

## 3. Hauteur réelle estimée (sticky plié)

Hypothèses : mois courant · ≥1 facture · Courses + épargne visibles · Détails **fermés** · pas aperçu futur · `DisplayFit` tall (Ultra).

| Bloc | ~dp | Preuve |
|------|-----|--------|
| Padding V 10×2 | 20 | `DisplayFit.kt` L60 |
| Rangée mois | 48 | `minTouchHeight` · `MonthStep` 48 |
| Gap | 6 | `sectionGap` tall |
| Titre Reste | 24 | `bodyMedium` LH 24 — `MonMoisClairTheme.kt` L179–183 |
| Gap interne hero | 2 | `HomeStickyHeader.kt` L108 |
| Montant 40 sp / LH 44 | 44 | L116–118 · `remainingAmountSp` 40 |
| Gap | 6 | |
| Statut factures | 26 | `bodyLarge` LH 26 — Theme L172–177 |
| Gap | 6 | |
| Rangée outils | 48 | `StickyToolCell` `heightIn(min=48)` — Chrome L42 |
| Gap (si Détails) | 6 | |
| Bouton Détails (souvent présent si `stillToPay` → Après FAIT) | 48 | Header L210–213 |
| Divider | 1 | L339 |
| **Total sans Détails** | **~230** | |
| **Total avec Détails plié** | **~285** | |

Avant 7.06 (audit matin) : **~330–360** (2× StickyLinkRow + Spacer + pad 16 / gap 10).  
Après 7.06 : **~230–285**.  
**Gain sticky ≈ 20–30 %**, pas −40 %. Cible 180–220 **non atteinte** (désaccord doc/cible vs code).

### Viewport liste (ordre de grandeur Ultra ~900 dp utiles)

| Zone | ~dp |
|------|-----|
| Sticky plié typique | 230–285 |
| LazyColumn `contentPadding` top | 12 (`HomeScreen.kt` L395) |
| Socle `HomeBottomBar` (pad 8+ boutons 48+8 + divider) | ~65+ |
| Clearance système (hors écran Compose, `MainActivity`) | 28 + insets — **hors sticky** |
| **Reste liste** | **~500–580** sur ~900 → sticky mange encore **~25–32 %** du cadre app |

Cartes `ExpenseRow` : pad 14 + titre + montant + FAIT + `ExpenseStatusLine` + éventuellement Copier/Photo **≥48** (`HomeTimelineRows.kt` L152–328) → **~110–160 dp / carte**.  
Avec ~520 dp utiles liste → **~3–4 cartes** si courtes, **~2–3** si Copier/Photo. Impression « quasi rien » si plusieurs factures riches : **cohérente**.

---

## 4. Pourquoi encore trop haut (causes racines)

1. **Squelette inchangé (5 bandes)** — 7.06 a fusionné Courses+épargne ; mois / hero 2 lignes / statut / outils / Détails restent. Gain perceptuel faible.
2. **Trois cibles 48 dp dans le sticky** (mois, outils, Détails) = **144 dp** avant texte — a11y OK, densité fatale pour la liste.
3. **Ultra = tall** — Athalia ne reçoit pas `sectionGap` 4 / pad 8 / hero 34. `COMPACT_HEIGHT_DP = 800` **exclut** S25 Ultra (`DisplayFit.kt` L50–53).
4. **Hero toujours 2 lignes** (titre + 40 sp) ≈ **70 dp** — signal n°1 charte, mais dominant.
5. **Statut « Factures ouvertes »** toujours sous le Reste si factures (`StickyStillToPayLabel.kt` L16–29) → +26 + gap.
6. **Détails ≥48** dès qu’il reste à payer (`afterFaitLabel` non null) — cas **fréquent** en mois actif.
7. **Liste elle-même haute** (FAIT + pastilles + QuietLinks) — même sticky −40 dp ≈ **moins d’une demi-carte** ; sans couper le chrome, l’œil ne bouge pas.

---

## 5. P0 agressifs — viser ≥40 % viewport liste

Objectif contrat : passer sticky pliée Ultra de **~250 → ≤150 dp** (−≥40 % chrome) **ou** libérer **≥200 dp** nets pour la liste. Capacités (mois, Reste, Courses sheet, épargne sheet, FAIT, Menu) **préservées** — emplacement peut bouger.

| Id | Action | Gain ~dp | Risque / garde |
|----|--------|----------|----------------|
| **P0-1** | **Forcer tokens sticky « liste d’abord » sur toutes hauteurs** (pad V 6–8, gap 2–4, hero **34–36** même >800) — ou lever `COMPACT_HEIGHT_DP` ≥ 1000 / flag `aggressiveHomeChrome` | 20–35 | Charte étude 36–42 confort : assumer **liste > air Ultra** tant que smoke Accueil rouge |
| **P0-2** | **Hero une ligne visuelle** : montant seul à l’écran ; titre « Reste… » = `contentDescription` / label court inline | 24–28 | TalkBack : garder libellé complet en semantics (déjà L125–127) |
| **P0-3** | **Sortir le statut « Factures ouvertes » du sticky** → pastille / 1ʳᵉ ligne liste / Détails | 32 | Ne pas perdre la distinction Reste vs à payer (charte UX 60+) — sheet ou chip liste |
| **P0-4** | **Outils hors sticky** : QuietLink unique « Courses · Mettre de côté » dans Menu **ou** sous la 1ʳᵉ carte — **ôter `StickyToolsRow` du chrome fixe** | **54+** | Plus gros levier ; sheets inchangées ; Accueil = ledger |
| **P0-5** | **Détails** : ne plus réserver une rangée 48 si seul « Après les FAIT » — le fondre en chip liste **ou** icône disclosure 24 visuelle dans la rangée mois (touch 48 via `defaultMinSize` sans bande pleine largeur empilée) | 48 | Extras critiques (courriers, revenu manquant) doivent rester découvrables |
| **P0-6** | **Mois compact** : chevrons + libellé en **40 dp visuels** avec hit 48 via padding négatif / `minimumInteractiveComponentSize` sans empiler une 2ᵉ bande | 8–12 | Respect a11y 48 hit — pas réduire la zone tactile réelle |

### Combinaisons recommandées (pour ≥40 %)

| Pack | Contenu | Sticky pliée estimée | Liste |
|------|---------|----------------------|-------|
| **A — chrome** | P0-1 + P0-2 + P0-3 | ~170–190 | Insuffisant seul vs Athalia |
| **B — agressif** | Pack A + **P0-4** (outils hors sticky) | **~110–140** | **−≈45–55 %** vs ~250 → **oui** |
| **C — maximal** | B + P0-5 | **~90–120** | Liste d’abord ; Détails non bloquants |

**Recommandation audit :** Pack **B** minimum. Sans **P0-4** (ou équivalent −48 outils), impossible d’atteindre −40 % sticky tout en gardant mois 48 + hero + a11y.

**Hors P0 (ne pas faire ce tour) :** sticky collapse au scroll type banking (change le modèle mental carnet) ; regrossir les cartes liste ; toucher Menu/Réglages.

---

## 6. Rangées liste — ne pas « compenser » en gonflant

| Élément | Preuve | Note audit |
|---------|--------|------------|
| `ExpenseRow` multi-bandes | `HomeTimelineRows.kt` L181–328 | Déjà dense ; **ne pas agrandir** |
| `cardGap` 12 | `A11yTokens.kt` L53 · `HomeScreen` L399 | Charte faux-clics — garder |
| LazyColumn top pad 12 tall | `HomeScreen.kt` L395 | P1 possible → 4–8 si sticky coupe |
| Socle Menu / + Ajouter | `HomeBottomBar.kt` L44–48 | Hors sticky ; ~65 dp fixes |

P0 = **sticky**, pas redesign cartes.

---

## 7. Note /10 (honnête)

| Critère | Note | Lecture |
|---------|-----:|---------|
| Espace liste dès l’ouverture | **3,5** | 2–4 cartes ; sticky 25–32 % cadre |
| Fidélité claim 7.06 « sticky basse » | **4** | Rangée outils OK ; hauteur cible rate |
| Clarté Reste (signal n°1) | **8** | Hero lisible ; trop de voisins |
| A11y cibles 48 | **9** | Respecté — **cause** de la hauteur |
| Alignement device Athalia (Ultra) | **3** | Tall path ; compact réservé ≤800 |
| **Synthèse Accueil sticky** | **4,5 / 10** | |

Pas de claim « dans la main » — smoke Athalia **ouvert**.

---

## 8. Preuves clés (extraits)

- Sticky ordre & pads : `HomeStickyHeader.kt` L56–176, L207–243  
- Tools 1×48 : `HomeStickyChrome.kt` L31–101  
- Tall vs compact : `DisplayFit.kt` L50–65  
- Typo LH : `MonMoisClairTheme.kt` L110–114, L172–183  
- Wire Accueil : `HomeScreen.kt` L352–400, L610  
- Cible ratée : `docs/audits/2026-11-02-accueil-sticky.md` L117 vs estimation §3 ci-dessus  

---

## Clôture auditeur

| | |
|--|--|
| **Domaine couvert** | Accueil — hauteur sticky / DisplayFit / rangées liste (viewport) |
| **Non audité** | Aide, Réglages, retard/accord, résumé mois, runtime Layout Inspector, smoke doigt |
| **Limites** | Hauteurs = **estimation dp** (inspection), pas mesure pixel device ; `screenHeightDp` Ultra **NON VÉRIFIÉ** sur appareil |
| **Modifications code** | aucune (fichier audit uniquement, ordre Athalia) |

**aucune modification faite** (hors rédaction obligatoire de ce rapport).
