# Audit expert-05 — Menu Accueil · sheets · hub Réglages · Dossiers

| Champ | Valeur |
|-------|--------|
| Date | 2026-11-02 |
| Branche | `develop` @ `0.1.0-7.06` · `versionCode` 111 |
| Domaine | **Cohérence de parcours** (Menu Accueil, sheets, hub Réglages post-7.04, Dossiers) |
| Mode | **AUDIT ONLY** — inspection code + charte + journal · **pas** de correctif |
| Nature | Inspection Compose / copy / nav · **pas** smoke doigt Athalia |
| Note | **8,2 / 10** (parcours) — honnête, sans claim « 10/10 dans la main » |

---

## 1. Carte des portes (état code 7.06)

### Accueil — sheets

| Sheet | Ouverture | Rôle | Charte |
|-------|-----------|------|--------|
| `HomeMenuSheet` | Socle **Menu** | Hub hors ledger | Lean : pas Aide / Restore |
| `HomeMonthPickerSheet` | Sticky libellé mois **ou** Menu → Choisir un mois | Calendrier carnet (années + pastilles) | Pas grille 31 / pas agenda Google |
| `HomeCoursesSheet` | Sticky rangée Courses | Édition divers seule | Unique UI Courses |
| `HomeSavingsSheet` | Sticky Mettre de côté | Montant rapide + QuietLink « Jour et détail › » | Jour / retrait → Réglages |

Forme commune **VÉRIFIÉ** : `ModalBottomSheet` + `CarnetSheetHandle` + `shapes.extraLarge` (28 dp) + `rememberSheetBottomClearance` + `PaperCream`.

### Menu Accueil — ordre réel

| Rang | Libellé | Condition | Cible |
|------|---------|-----------|-------|
| 0 | **Alertes** (AccentWarn) | `remindersBlocked` seulement | `ReminderHealth` (bypass Confort) |
| 1 | **Réglages** | toujours | `Settings` hub |
| 2 | **Dossiers** (+ compteurs) | `hasDossiers` ∨ count ∨ courriers | `Dossiers` |
| 3 | **À venir** | toujours | `Upcoming` |
| 4 | **Choisir un mois** | toujours | `HomeMonthPickerSheet` |
| cond. | **Mois en cours** (Secondary) | mois passé / aperçu futur | `goToCurrentMonth` |
| pied | QuietLink Arkalia | toujours | mail support |

**Pas** dans Menu : Aide, Restore, Apparence, résumé, échéances — aligné charte § Accueil.

### Hub Réglages (post-7.04) — ordre réel

| Rang | Libellé | Cible |
|------|---------|-------|
| 1 | **Code et copie** | `SettingsBackup` (raccourci P0-U1) |
| 2 | **Données & sauvegarde** | groupe |
| 3 | **Confort** | groupe |
| 4 | **Comment ça marche** | `HowItWorks` (direct) |

**Données :** Sauvegarde → Échéances hors ce mois → Jour du Mettre de côté → Confidentialité → Préparer un résumé *(si déficit ∨ dossierCount > 0)*.  
**Confort :** Apparence → Alertes seulement.

### Dossiers — entrées

| Porte | Quand |
|-------|--------|
| Menu → Dossiers | Contenu (accords / soldés / courriers) |
| Sticky Détails → « N courrier(s) sans plan · Voir » | `pendingLetterCount > 0` |
| Réglages → Préparer un résumé → QuietLink « Aussi dans Dossiers » | Si ligne résumé visible |
| Aide copy « Menu → Dossiers » | Texte figé |

Maison résumé = section bas **Résumé à montrer** (texte + PDF). Réglages = 2ᵉ porte (branchée post-7.02 — boutons + `SettingsShareEffect`).

---

## 2. Alignements charte / journal (constats)

### Constat A — Menu lean vs Réglages

| | |
|--|--|
| **Statut** | **VÉRIFIÉ** |
| **Nature** | inspection |
| **Preuve** | `HomeMenuSheet.kt` L28–30, L100–152 ; `CHARTE_ET_ARCHITECTURE.md` L30, L49 ; `SettingsScreen.kt` L25–28 |
| **Lecture** | Séparation ledger / outils / config **tenue**. Contact = Menu seul. |

### Constat B — Hub 7.04 (Échéances + libellé épargne)

| | |
|--|--|
| **Statut** | **VÉRIFIÉ** (code) · smoke doigt hub = **DÉCLARÉ** (journal) |
| **Nature** | inspection · journal = inspection historique |
| **Preuve** | `SettingsHubCopy.kt` L16–18, L27, L44–52 ; `SettingsScreen.kt` L120–143 ; `PROGRESS_LOG.md` §7.04 |
| **Lecture** | P0 audit hub **appliqués** dans le code 7.06 : Échéances sous Données ; `SAVINGS_ROW = "Jour du Mettre de côté"` ; Confort = 2 lignes. |

### Constat C — Sheets édition Accueil

| | |
|--|--|
| **Statut** | **VÉRIFIÉ** |
| **Nature** | inspection |
| **Preuve** | `HomeScreen.kt` L108–118, L314–342 ; `HomeCoursesSheet` / `HomeSavingsSheet` ; charte L41–44 |
| **Lecture** | Courses / épargne = sheets sticky ; fermés en aperçu futur. Pont épargne → Réglages via QuietLink. |

### Constat D — Ordre Menu vs charte

| | |
|--|--|
| **Statut** | **VÉRIFIÉ** (désaccord doc ↔ code) |
| **Nature** | inspection |
| **Preuve** | Charte L30 : Réglages · **À venir** · **Dossiers** · Choisir un mois. Code : Réglages · **Dossiers** · **À venir** · Choisir un mois (`HomeMenuSheet` L100–129). |
| **Lecture** | Ordre **différent** ; comportement non cassé. Document = DÉCLARÉ ; fichier = VÉRIFIÉ. |

### Constat E — Aide « Menu → Dossiers » vs ligne conditionnelle

| | |
|--|--|
| **Statut** | **VÉRIFIÉ** (écart copy / UI) |
| **Nature** | inspection |
| **Preuve** | `HowItWorksCopy.kt` L55, L72 ; `HomeMenuSheet` L52, L108 ; KDoc `DossiersScreen` « Absent du Menu s’il n’y a ni accord ni courrier » |
| **Lecture** | Aide affirme une porte toujours nommée ; UI la **retire** si vide. Volontaire (anti-fourre-tout) mais friction débutante. |

### Constat F — Aide « Réglages → Alertes »

| | |
|--|--|
| **Statut** | **VÉRIFIÉ** (raccourci copy) |
| **Nature** | inspection |
| **Preuve** | `HowItWorksCopy.kt` L79 « Réglages → Alertes » ; chemin réel = Réglages → **Confort** → Alertes (`SettingsComfortGroupScreen`) ; exception Menu si `remindersBlocked` |
| **Lecture** | Copy raccourcit un niveau — OK 60+ en panique notif, mais pas le chemin hub nominal. |

### Constat G — Montant « Mettre de côté » encore éditable dans Réglages

| | |
|--|--|
| **Statut** | **VÉRIFIÉ** |
| **Nature** | inspection |
| **Preuve** | Tip `SettingsAccountPages.kt` L112–113 « Montant : depuis Accueil » **et** `OutlinedTextField` montant L118–134 ; sticky + `HomeSavingsSheet` aussi |
| **Lecture** | Récit 7.04 « montant = Accueil ; jour/retrait = Réglages » **partiel** : la page Réglages ré-édite encore le montant. |

### Constat H — Route `SettingsHelp` orpheline

| | |
|--|--|
| **Statut** | **VÉRIFIÉ** |
| **Nature** | inspection |
| **Preuve** | Hub `onOpenHelp` → `HowItWorks` (`AppNavHost` L147–148) ; `AppRoute.SettingsHelp` + `SettingsHelpScreen` = Secondary seul |
| **Lecture** | Cul-de-sac de navigation interne ; pas exposé au hub. |

### Constat I — Comportement doigt (Menu / hub / Dossiers / sheets)

| | |
|--|--|
| **Statut** | **NON VÉRIFIÉ** |
| **Nature** | — |
| **Preuve** | non exécuté (pas de smoke doigt dans cet appel) |
| **Lecture** | README / journal : re-smoke Athalia Accueil + hub encore ouverts. |

---

## 3. Problèmes — priorités (parcours)

### P0 — aucun bloquant parcours à l’inspection 7.06

Les P0 hub (Échéances mal placées, libellé épargne homonyme) sont **corrigés en code** (constat B).  
Pas de cul-de-sac Menu → écran mort détecté sur les portes listées §1 (hors route Help orpheline non exposée).

*Comportement réel device = NON VÉRIFIÉ.*

### P1 — Aide promet « Menu → Dossiers » alors que la ligne peut manquer

| | |
|--|--|
| **Impact** | Débutante suit Aide / Retard-accord → ouvre Menu → pas de Dossiers → doute « app cassée ». |
| **Piste** (non appliquée) | Copy Aide : « Menu → Dossiers (quand un accord ou un courrier existe) » · **ou** ligne Menu grisée / tip empty — choix produit Athalia. |

### P1 — Double édition montant épargne (Accueil sheet + page Réglages)

| | |
|--|--|
| **Impact** | Contredit le tip et le claim 7.04 ; impression de doublon / fouillis (même famille que l’audit hub). |
| **Piste** | Page Réglages = jour + retrait (+ historique) **sans** champ montant · **ou** tip « aussi modifiable ici ». |

### P1 — Aide Alertes / Sauvegarde raccourcit le hub

| | |
|--|--|
| **Impact** | « Réglages → Alertes » / « Réglages → Sauvegarde » omettent Confort / Données (le raccourci **Code et copie** sauve Sauvegarde). |
| **Piste** | Aligner HowItWorks sur chemins hub réels, ou documenter le raccourci code en tête Sauvegarde (déjà partiel). |

### P2 — Ordre Menu ≠ charte (Dossiers vs À venir)

| | |
|--|--|
| **Impact** | Faible ; aligner charte **ou** code. |
| **Piste** | Une seule source de vérité (charte L30 vs `HomeMenuSheet`). |

### P2 — Filets Menu si Alertes bloquées

| | |
|--|--|
| **Preuve** | Alertes `showDivider = false` ; Réglages `showDivider = !remindersBlocked` → **deux** premières lignes sans filet entre elles. |
| **Impact** | Micro DA ; pas bloquant. |

### P2 — `SettingsHelp` quasi mort

| | |
|--|--|
| **Impact** | Dette nav ; risque de lien futur vers l’écran intermédiaire. |
| **Piste** | Laisser (volontaire) ou retirer route plus tard — hors redesign. |

### P2 — Deux portes résumé (Dossiers + Réglages conditionnel)

| | |
|--|--|
| **Impact** | Volontaire (maison = Dossiers). Risque « laquelle est la vraie » si Athalia n’a pas re-smoké post-7.02. |
| **Décision audit** | **Garder** en l’état tant que QuietLink croisé existe ; ne pas fusionner sans oui. |

---

## 4. Note /10 (parcours navigation)

| Critère | Note | Commentaire |
|---------|-----:|-------------|
| Menu Accueil lean | **9,0** | Charte respectée ; contact isolé ; Alertes conditionnelles OK |
| Sheets Accueil | **8,7** | Pattern unifié ; pont épargne ; Courses unique |
| Hub Réglages 7.04 | **8,5** | Groupes cohérents ; raccourci code utile ; montant encore double |
| Dossiers / croisés | **7,8** | Portes claires si contenu ; friction Aide + empty ; résumé bi-porte |
| Copy Aide ↔ nav | **7,5** | Plusieurs raccourcis / omissions de niveau |
| **Synthèse parcours** | **8,2** | Structure solide post-7.04 ; restes P1 copy / doublon montant |

**Pas** de note « dans la main » : smoke Accueil / hub / Dossiers = **Athalia** (NON VÉRIFIÉ ici).

---

## 5. Domaine · limites

- **Domaine couvert :** cohérence parcours Menu Accueil · sheets · hub Réglages (7.04) · Dossiers.
- **Non audité ici :** DA sticky 7.06 détail, retard/accord métier, perf PDF résumé, a11y TalkBack, sécurité backup, widget.
- **Limites :** inspection seule ; tests unitaires non réexécutés dans cet appel ; aucun dump UI device ; claims journal smoke = DÉCLARÉ.
- **aucune modification faite** *(hors ce fichier d’audit, demandé explicitement par Athalia)*.
)
