# Audit premium — passe 3

Date : 2026-10-20.  
Journal en tête au moment de la lecture : **6.19**.  
**Suivi :** code passe 3 en **6.20** ; S04 archive en **6.21** ; smoke P3-Q04 encore ouvert.  
Ne remplace pas les deux fichiers précédents. Il les pousse.

- Origine : `docs/AUDIT_PREMIUM_2026-10-20.md`
- Suite : `docs/AUDIT_PREMIUM_2026-10-20-SUITE.md`
- Cette passe : ce qui reste **après** 6.16–6.19, lu dans le code, pas au doigt.

Domaines couverts : visuel, fonctionnel, fiabilité, produit fini.  
Hors sujet (charte) : banque, cloud, pub, graphiques, report automatique du solde, troisième bouton bas.

Pas de smoke Galaxy. Un point « vu dans le code » n’a pas été rejoué sur le téléphone.

---

## Déjà tenu — ne pas rouvrir

Mois d’ajout, libellés Reste / À payer / Déficit, photo décodée dans l’app, scan d’un QR non belge, FAIT en bouton contour, coins 12 dp, barre d’état crème, libellés ≥ 18 sp, cartes Réglages, M au verrou et au splash, divers repliable avec « Ouvrir / Fermer », bandeau Annuler qui n’est plus chassé par « Copié », or interdit en couleur de lettres.

La police embarquée (V16 / S03) reste le grand écart visuel **différé**. Cette passe n’en fait pas le sujet : elle liste ce qui casse encore la confiance ou la lecture.

---

## Visuel

### P3-V01 — « Après divers » crie plus fort que « À payer »

**Vu dans** `HomeScreen`.  
Si des courses sont notées, l’en-tête affiche « À payer … » en `bodyLarge` (18 sp, encre atténuée), puis « Après divers … » en `titleLarge` (22 sp, encre pleine). Le chiffre indicatif passe devant le chiffre d’action. La même idée est répétée dans le bloc Courses.

**Correction.** Dans l’en-tête, « Après divers » reste en `bodyLarge`, même encre atténuée que « À payer ». Le bloc du bas garde le détail. Un seul endroit pour le montant fort : le Reste.

### P3-V02 — Mois vide : deux boutons teal « Ajouter »

**Vu dans** `HomeScreen`, branche `timeline.isEmpty`.  
Un `PrimaryA11yButton` « + Ajouter une entrée » est dans la liste, et la barre du bas a déjà « + Ajouter ». Deux pleins teal. La charte n’en veut qu’un.

**Correction.** Garder la phrase « Mois vide » et le bouton du bas. Retirer le second bouton, ou le passer en contour « Ajouter le salaire ou une facture » si on veut un geste au centre — pas un second plein.

### P3-V03 — Recherche sans phrase quand rien ne correspond

**Vu dans** `HomeScreen`. Le champ « Chercher une facture » apparaît à partir de 4 lignes. Si le mot ne matche pas, la liste des cartes disparaît. Il reste le bloc Courses. On dirait que le mois s’est vidé. Les totaux du haut, eux, ne changent pas (ils sont justes : c’est tout le mois). Rien ne le dit.

**Correction.** Si la recherche n’est pas vide et `filteredTimeline` est vide : une ligne « Rien pour “loyer”. Les totaux du haut restent ceux du mois. » Ne pas cacher les totaux. Ne pas les recalculer sur le filtre.

### P3-V04 — Rappels qui parlent en code

**Vu dans** `ReminderSchedulerImpl.titleFor` et `strings.xml`.  
Titres : « Rappel paiement (J−3) ». Canal : « Rappels J−3 et jour d’échéance ». Maman lit la barre d’état, pas un cahier de spec.

**Correction.** « Dans 3 jours : Loyer », « Aujourd’hui : Loyer », « En retard : Loyer ». Le corps a déjà le montant et le jour. Le canal : « Rappels de factures ».

---

## Fonctionnel

### P3-F01 — Enregistrer une échéance hors mois peut décaler l’année

**Vu dans** `EditTemplateViewModel.save` et `UpdateExpenseTemplateUseCase`.  
L’écran ne garde que le **mois** (1–12), pas l’année d’ancre. À l’enregistrement, `RecurrenceAnchor.nextYearMonth` recalcule la prochaine année à partir d’aujourd’hui et **remplace** `anchorYearMonth`.

`RecurrenceRules.shouldMaterialize` exige `mois cible >= ancre`. Si la nouvelle ancre saute en avant, les échéances qui étaient encore devant disparaissent.

Exemple. Assurance tous les 3 mois, ancre janvier 2026. On est en mai 2026. Prochaines lignes : juillet et octobre 2026. On ouvre Réglages seulement pour corriger le nom. Le champ affiche « Janvier ». L’enregistrement écrit l’ancre **janvier 2027**. Juillet et octobre 2026 ne sont plus créés.

**Correction.** Ne pas recalculer l’ancre si le mois choisi n’a pas changé : garder la chaîne `yyyy-MM` déjà en base. Si le mois change, partir de l’ancre actuelle et avancer jusqu’au nouveau mois, sans sauter une occurrence encore à venir. Afficher l’année (« Janvier 2027 ») pour que le décalage se voie. Test : trimestre, correction du nom seule, juillet reste matérialisable.

### P3-F02 — Le rappel ouvre la mauvaise ligne dès qu’il y a une recherche

**Vu dans** `HomeScreen`, `LaunchedEffect(state.highlightExpenseId)`.  
L’index vient de `state.timeline` (liste complète). La `LazyColumn` affiche `filteredTimeline`, et un champ recherche en **premier** item dès 4 factures. `animateScrollToItem(index)` vise donc la carte d’avant, ou une autre carte si un filtre est actif. Le surlignage teal est sur la bonne carte, le scroll non. Le cas réel : on touche la notification, le mois a 4 factures ou plus.

**Correction.** Chercher l’index dans `filteredTimeline`. Ajouter 1 si l’item « search » est présent. Si le filtre cache la facture, vider la recherche puis scroller.

### P3-F03 — Retirer plus de divers que le total ne dit rien

**Vu dans** `AdjustDiversUseCase`.  
`next = (divers + delta).coerceAtLeast(0)`. On retire 80 € alors qu’il n’y a que 12 € notés : le total devient 0, l’écran dit succès, le champ se vide. On croit avoir retiré 80.

**Correction.** Si le retrait dépasse le total : message « Il n’y a que 12,00 € en courses. Tout a été retiré. » ou refuser et demander le montant exact. Ne pas présenter ça comme un retrait du montant tapé.

---

## Fiabilité

### P3-R01 — Une seule facture mal datée coupe tous les rappels

**Vu dans** `ReminderTiming.dueDate` : `yearMonth.atDay(dayOfMonth)`.  
Le 31 en février lève une exception. `ReminderSchedulerImpl.reconcileAll` n’isole pas les factures : l’exception sort de la boucle. `BootReceiver` et `ReminderReceiver` avalent l’erreur et **aucune** alarme n’est reposée, y compris celles qui étaient valides.

Les créations normales passent par `MonthDayClamp`, donc le cas est rare. Il suffit d’une vieille ligne ou d’un oubli de clamp.

`ExactAlarmGatewayImpl.schedule` appelle `setExactAndAllowWhileIdle` sans regarder `canScheduleExactAlarms()`. Si Samsung retire le droit, `SecurityException` : même effet, plus aucun rappel.

**Correction.** Clamp du jour dans `ReminderTiming` (comme `ExpenseOverdue`). Autour de chaque facture dans `reconcileAll`, attraper l’erreur et continuer. Avant `setExact`, si le droit manque : annuler cette alarme et sortir sans exception. Test : jour 31, février, les autres factures du mois sont quand même programmées.

### P3-R02 — Le message d’erreur de restauration peut être du texte technique

**Vu dans** `BackupRestorer` et `RestoreMessages.IoError`.  
Un `Exception` inattendu devient « Impossible de lire la sauvegarde : » plus `e.message`. Selon l’erreur, c’est de l’anglais de plateforme.

**Correction.** Phrase fixe : « Impossible de lire cette sauvegarde. Le mois en cours n’a pas été modifié. » Le détail reste dans le log, pas à l’écran. Le dire seulement si la base n’a vraiment pas bougé (c’est déjà le cas quand l’échec est avant `applyDocument`).

---

## Produit fini

### P3-Q01 — La notification ne propose pas l’action

On touche la notification : l’app s’ouvre sur la facture. Il n’y a pas d’action « FAIT » sur la notification elle-même. Pour une facture déjà payée dans l’app bancaire, c’est un aller-retour. Ce n’est pas un swipe caché : un bouton d’action Android, libellé « FAIT ».

**Correction.** `NotificationCompat.addAction` vers un receiver qui appelle `ToggleExpenseDoneUseCase` pour **cette** facture, puis ferme la notification. Pas d’action groupée. Le corps reste le montant et le nom.

### P3-Q02 — La recherche et les totaux ne sont pas expliqués ensemble

Lié à P3-V03. Un produit cher dit, sous le champ, tant qu’il y a un mot : « Le reste en haut est celui de tout le mois. » Une ligne, encre atténuée. Elle disparaît quand le champ est vide.

### P3-Q03 — Police système, toujours

`FontFamily.Serif` / `SansSerif` dans `MonMoisClairTheme`. Différé volontairement (journal, S03). Tant que ce n’est pas fait, le chiffre n’a pas la voix d’un carnet. À traiter **seul**, après les bugs de cette passe, pas mélangé avec F01.

### P3-Q04 — Smoke téléphone encore ouvert

À faire sur le Galaxy, dans cet ordre :

1. Notification avec au moins 4 factures : la bonne ligne est-elle sous le doigt ?
2. Échéance « tous les 3 mois » : corriger seulement le nom, vérifier que le prochain mois n’a pas sauté.
3. Mois vide : un seul bouton teal.
4. Recherche « zzzz » : une phrase, les totaux toujours là.
5. Retrait divers plus grand que le total.
6. Splash : M teal entier, pas coupé.

---

## Ordre de correction

| Ordre | Ids | Pourquoi |
|-------|-----|----------|
| 1 | P3-F01, P3-R01 | Une sauvegarde d’échéance ou un rappel peut perdre des mois ou tout le silence |
| 2 | P3-F02, P3-F03 | Le geste notification et le geste courses mentent |
| 3 | P3-V01, P3-V02, P3-V03, P3-V04, P3-Q02 | Lecture |
| 4 | P3-R02, P3-Q01 | Finition |
| 5 | P3-Q03 | Police, passe dédiée |
| 6 | P3-Q04 | Téléphone, après le code |

Chaque ordre : code, test qui échoue avant la correction quand c’est une règle (F01 ancre, R01 jour 31), puis `./gradlew :app:testDebugUnitTest --rerun-tasks` **sans** un second Gradle en parallèle.

---

## Fichiers

- `app/src/main/java/be/monmoisclair/app/ui/entry/EditTemplateViewModel.kt`
- `app/src/main/java/be/monmoisclair/app/domain/usecase/UpdateExpenseTemplateUseCase.kt`
- `app/src/main/java/be/monmoisclair/app/domain/RecurrenceAnchor.kt`
- `app/src/main/java/be/monmoisclair/app/domain/RecurrenceRules.kt`
- `app/src/main/java/be/monmoisclair/app/ui/home/HomeScreen.kt`
- `app/src/main/java/be/monmoisclair/app/domain/usecase/AdjustDiversUseCase.kt`
- `app/src/main/java/be/monmoisclair/app/data/reminder/ReminderTiming.kt`
- `app/src/main/java/be/monmoisclair/app/data/reminder/ReminderSchedulerImpl.kt`
- `app/src/main/java/be/monmoisclair/app/data/reminder/ExactAlarmGatewayImpl.kt`
- `app/src/main/java/be/monmoisclair/app/data/reminder/PaymentNotificationHelper.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/java/be/monmoisclair/app/ui/restore/RestoreMessages.kt`

---

## Vérification 6.20 / 6.21

Mesure : `./gradlew :app:testDebugUnitTest --rerun-tasks` → **148/148** (42 fichiers, 0 échec).  
Version : `0.1.0-6.21` / `versionCode` 26.

| ID | État | Note |
|----|------|------|
| P3-V01 | ✅ | « Après divers » = `bodyLarge` + encre atténuée |
| P3-V02 | ✅ | Mois vide : phrase seule, un seul teal en bas |
| P3-V03 | ✅ | « Rien pour « … » » + totaux inchangés |
| P3-V04 | ✅ | Titres humains + canal « Rappels de factures » |
| P3-F01 | ✅ | `resolveTemplateAnchor` + année affichée + tests |
| P3-F02 | ✅ | Scroll sur `filteredTimeline` + offset recherche |
| P3-F03 | ✅ | `ReducedToZero` + message honnête |
| P3-R01 | ✅ | Clamp jour + boucle isolée + garde exact alarms + test |
| P3-R02 | ✅ | Phrase fixe, sans détail plateforme |
| P3-Q01 | ✅ | Action notification FAIT → `markDoneIfTodo` |
| P3-Q02 | ✅ | Phrase sous le champ recherche |
| P3-Q03 | ✅ | Source Serif 4 / Source Sans 3 embarqués |
| P3-Q04 | ⏸ | Smoke téléphone — pas encore rejoué au doigt |

### Smoke P3-Q04 (à faire sur Galaxy)

1. Notification avec ≥4 factures : bonne ligne sous le doigt ?
2. Échéance « tous les 3 mois » : corriger seulement le nom → mois futurs intacts ?
3. Mois vide : un seul bouton teal ?
4. Recherche « zzzz » : phrase + totaux toujours là ?
5. Retrait divers > total : message honnête ?
6. Splash : M teal entier, pas coupé ?
