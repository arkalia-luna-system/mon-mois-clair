# Charte & Architecture — Mon Mois Clair

Document de référence. Toute évolution technique doit rester conforme à ce fichier.  
Mise à jour : 2026-10-19 (lot **5.x** — freeze Home, durcissement 4.x, partage sauvegarde).

---

## 1. Vision

Application **locale, privée, hors-ligne** pour gérer les paiements mensuels de façon plus précise qu’un bloc-notes, sur **Samsung Galaxy S25 (One UI)** uniquement.

- Zéro connexion bancaire, zéro serveur tiers.
- Saisie manuelle uniquement.
- Distribution : **APK sideload familial** (hors Play Store) — **APK release signé** (pas le debug au quotidien familial).
- iPad / PWA / frameworks hybrides : **abandonnés définitivement**.

### Freeze produit (verrouillé)

L’app couvre déjà : entrées/sorties, dates, rappels, FAIT, reste après factures, divers, retard, prélèvement, récurrence.  
**Interdit** d’ajouter de nouvelles options / boutons / compteurs sur l’**écran d’accueil**.  
Toute nouveauté indispensable → formulaire d’ajout **ou** hub **Réglages** uniquement.

### Stack figée

| Couche | Choix |
|--------|--------|
| Langage / UI | Kotlin + Jetpack Compose |
| Persistance | Room (SQLite) |
| Async | Coroutines + Flow |
| DI | Hilt |
| Fuseau | `Europe/Brussels` |
| Package | `be.monmoisclair.app` |

---

## 2. Décisions verrouillées (A–E + F–I + T1–T8)

| ID | Décision |
|----|----------|
| **A** | Kotlin natif + Room + Jetpack Compose. Aucune surcouche hybride. |
| **B** | SQLite source de vérité + auto-backup JSON clair vers dossier Documents (SAF) à chaque mutation. |
| **C** | Rappels via `AlarmManager` exact (`USE_EXACT_ALARM`) + receivers boot / update / heure + exemption batterie + écran Santé. Fuseau `Europe/Brussels`. |
| **D** | Sideload APK **release** familial (keystore stable). |
| **E** | Ouverture directe sans code ; biométrie **optionnelle** ; si activée → `FLAG_SECURE` (aperçu multitâche masqué). |
| **F** | Facture **EN RETARD** automatique (visuel) + relance overdue quotidienne 10h jusqu’à FAIT + notif → deep link ligne. |
| **G** | Libellé **« Reste après factures »** (≠ solde banque) + ligne **Courses / divers** (+/−) sur le mois. |
| **H** | Case **Prélèvement automatique** → pastille Auto + texte notif « Vérifier le prélèvement… ». |
| **I** | Récurrence : **Une fois** / **Mensuel** / **Tous les 3 mois** / **1× par an** + sélecteur **Prochaine échéance (mois)** si intervalle > 1. |

### Correctifs Android (T1–T8)

| ID | Failles | Correctif figé |
|----|---------|----------------|
| **T1** | Sideload update annule les `AlarmManager` | Receiver : `MY_PACKAGE_REPLACED` + `TIME_SET` (`ACTION_TIME_CHANGED`) + `TIMEZONE_CHANGED` → `reconcileAll()` |
| **T2** | Debug APK = certificat jetable + perf | Keystore release `.jks` (hors git) + `assembleRelease` (minify R8) |
| **T3** | Multitâche montre le solde | `FLAG_SECURE` si biométrie ON |
| **T4** | FAIT sur mois passé sans frein | Confirm dialogue si `yearMonth < current` |
| **T5** | R8 pourrait casser les clés JSON backup | Codec **manuel** (`JSONObject` + littéraux) ; règles `-keep` backup ; test assert noms de champs ; vérif release device |
| **T6** | `anchorYearMonth` = mois courant sans choix | Sélecteur « Prochaine échéance : [Mois] » si 3 mois / annuel ; instance créée **seulement** si le mois courant matérialise ; liste en Réglages des périodiques hors mois |
| **T7** | `OVERDUE` one-shot sans replanif | `ReminderReceiver` après affichage notif → `reconcileAll()` (reprogramme J+1 à 10h) |
| **T8** | Documents seul = perte si casse/vol téléphone | Réglages : **Envoyer une copie de sécurité** (`ACTION_SEND` JSON → WhatsApp / Mail) |

---

## 3. Spécifications fonctionnelles & ergonomie

- **Home freeze** : pas de nouvelle option quotidienne sur l’accueil.
- **Reste après factures** (header sticky) : `sum(revenus) − sum(dépenses du mois)`.
- **Courses / divers** (même mois) : déjà livré (G) — pas d’extension Home.
- **Liste chronologique** : revenus (+) et dépenses (−).
- **FAIT** / **EN RETARD** / rappels J−3 / Jour J / **OVERDUE** : inchangés dans l’esprit (F).
- **OVERDUE chain (T7)** : chaque `setExactAndAllowWhileIdle` est one-shot ; après feu, `ReminderReceiver` **doit** appeler `reconcileAll()` pour reprogrammer le lendemain 10h tant que TODO+retard.
- **Récurrence (I + T6)** : si « Tous les 3 mois » ou « 1× par an », afficher **Prochaine échéance** (mois 1–12). `anchorYearMonth` = prochain `yyyy-MM` ≥ mois courant pour ce mois-calendaire. Pas d’instance dans le mois affiché si `!shouldMaterialize`.
- **Réglages — échéances hors mois** : liste courte des templates actifs (intervalle > 1) non dus ce mois (libellé + prochaine échéance).
- **Réglages — partage (T8)** : bouton partage du dernier `mon-mois-clair-backup-1.json` via `Intent.ACTION_SEND` (FileProvider cache).
- **Accessibilité** : thème clair, texte ≥ 18 sp, zones ≥ 48 dp.

---

## 4. Persistance & sauvegarde (B + T5 + T8)

JSON clair, rotation 3, SHA-256, sync, delete-before-rename.  
`schemaVersion` backup / Room : **2**.

**T5 — R8 / release :**  
`BackupJsonCodec` sérialise via **clés littérales** (`"label"`, `"amountCents"`, …), **pas** de réflexion Gson/Jackson.  
ProGuard : `-keep` sur `be.monmoisclair.app.data.backup.**` et entités Room.  
Test unitaire : le JSON encodé **contient** les noms de champs attendus.  
Vérif device release : ouvrir le fichier backup → noms lisibles + restore OK.

**T8 — coffre externe humain :** partage Intent du JSON (pas de cloud).

---

## 5. Rappels (C) — receivers

Manifest (actions) :
- `BOOT_COMPLETED` / `LOCKED_BOOT_COMPLETED` / `QUICKBOOT_POWERON`
- **`MY_PACKAGE_REPLACED`**
- **`TIME_SET`** (`ACTION_TIME_CHANGED`) / **`TIMEZONE_CHANGED`**

Boot/update/heure → `goAsync` + `reconcileAll()`.

**ReminderReceiver (T7)** : affiche la notif **puis** `reconcileAll()` (goAsync) — obligatoire pour la chaîne OVERDUE.

---

## 6. Modèle de données

### Enums

```text
ExpenseStatus : TODO | DONE
BackupHealth  : OK | BROKEN | NOT_CONFIGURED
ReminderKind  : J3 | JD | OVERDUE
```

### Champs (v2)

- `expense_templates` : `isAutoDebit`, `intervalMonths`, `anchorYearMonth`
- `expense_instances` : `isAutoDebit`
- `month_ledgers` : `diversSpentCents`

### Récurrence (I + T6)

`RecurrenceRules.shouldMaterialize(anchor, interval, target)`.  
`RecurrenceAnchor.nextYearMonth(monthOfYear, from)` pour le sélecteur UI.

### Calculs

- `remainingAfterBillsCents = sum(incomes) − sum(expenses)`
- `stillToPayCents = sum(expenses where TODO)`
- `afterDiversCents = remainingAfterBillsCents − diversSpentCents`

---

## 7. Navigation UI

```text
Home (freeze) · Ajout (récurrence + mois ancre) · Réglages (périodiques hors mois + partage) · …
+ deep link highlightExpenseId
```

---

## 8. Qualité & sécurité

- Typage fort, pas de catch silencieux, MutatingRepository unique.
- Biométrie ON ⇒ `FLAG_SECURE`.
- Release : `keystore.properties` + `.jks` **gitignorés**.
- Tests unitaires **debug** + asserts T5 ; smoke release device pour backup/restore.

---

## 9. Pre-Mortem

1. URI SAF révoqué → Santé + re-choix dossier.  
2. Device Care Samsung → hint permanent.  
3. Backup non atomique → delete + sync + rotation 3.  
4. Update APK sans MY_PACKAGE_REPLACED → silence (T1).  
5. Debug familial → update impossible (T2).  
6. Multitâche sans FLAG_SECURE → fuite (T3).  
7. Mois passé sans confirm → historique faussé (T4).  
8. Confusion « reste » vs banque → libellé + divers (G).  
9. Jour J passé sans overdue → oubli (F).  
10. **R8 renomme champs JSON → backup illisible / restore cassé** (T5).  
11. **Ancre = mois courant → taxe de mars impossible** (T6).  
12. **OVERDUE one-shot sans reconcile → une seule relance** (T7).  
13. **Documents seuls → perte totale si téléphone cassé/volé** (T8).

---

## 10. Plan d’implémentation

| Étape | Contenu | État |
|-------|---------|------|
| **1.1–3.5.1** | Socle + hub + bio | ✅ |
| **4.0–4.6** | F–I + T1–T4 | ✅ |
| **5.0** | Charte freeze Home + T5–T8 (ce document) | ✅ |
| **5.1** | T5 R8 keep + test clés JSON | ✅ |
| **5.2** | T6 sélecteur mois ancre + liste Réglages | ✅ |
| **5.3** | T7 ReminderReceiver → reconcileAll | ✅ |
| **5.4** | T8 partage ACTION_SEND | ✅ |
| **5.4.1** | Aide « Comment ça marche » (Réglages) | ✅ |

Aucune étape n’est « terminée » tant que `PROGRESS_LOG.md` n’est pas à jour.

---

## 11. Build sur volume T7

`COPYFILE_DISABLE=1`, `buildDir` `~/.cache/mon-mois-clair/`, `purgeAppleDouble`.  
Release : `./gradlew :app:assembleRelease` avec `keystore.properties` local.
