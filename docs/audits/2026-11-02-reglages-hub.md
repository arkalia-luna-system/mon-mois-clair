# Audit — Hub Réglages (fouillis / débutant)

| Champ | Valeur |
|-------|--------|
| Date | 2026-11-02 |
| Branche | `develop` @ ~`0.1.0-7.03` |
| Domaine | Menu Réglages — hub + sous-groupes + sujets |
| Déclencheur | Smoke Athalia — impression fouillis / débutante |
| Nature | Inspection code (Compose + copy) · **pas** smoke doigt Athalia |

---

## 1. Carte complète (avant fix)

### Hub `SettingsScreen`

| Rang | Libellé | Cible | Type |
|------|---------|-------|------|
| 1 | **Votre code / copie** | `SettingsBackup` | Raccourci vital (P0-U1) — hors groupe |
| 2 | **Données & sauvegarde** | groupe Compte | HubRow |
| 3 | **Confort** | groupe Confort | HubRow |
| 4 | **Aide — Comment ça marche** | `HowItWorks` (direct) | HubRow |
| pied | Wordmark + étape + « Données sur ce téléphone… » | — | Tip |

Profondeur max charte : hub → groupe → sujet (2 niveaux). Aide saute le groupe vide `SettingsHelpScreen`.

### Groupe Données & sauvegarde

| Rang | Libellé | Cible | Doublon Accueil ? |
|------|---------|-------|-------------------|
| 1 | Sauvegarde | `SettingsBackup` | Non (aussi raccourci hub) |
| 2 | **Mettre de côté** | `SettingsSavings` | **Oui** — montant déjà sticky Accueil + sheet |
| 3 | Confidentialité | `SettingsPrivacy` | Non (bio) |
| 4 | Préparer un résumé *(si utile)* | `SettingsSummary` | Partiel — aussi Dossiers |

### Groupe Confort

| Rang | Libellé | Cible | Place juste ? |
|------|---------|-------|---------------|
| 1 | Apparence | thème | Oui — confort |
| 2 | Alertes | `ReminderHealth` | Oui — confort |
| 3 | **Échéances hors ce mois** | listes périodiques hors mois | **Non** — métier factures / année |

### Sous-pages (sujets)

| Page | Contenu utile 60+ | Copy / densité |
|------|-------------------|---------------|
| Sauvegarde | Code · dossier · envoyer · restaurer Danger | Dense mais pédagogique ; 3 étapes OK |
| Mettre de côté | **Jour 1–31 + retrait** (pas sur Accueil) | Tip long ; hub label = même mot qu’Accueil |
| Confidentialité | Empreinte / code | Clair |
| Résumé | Texte + PDF + QuietLink Dossiers | OK post-7.02 |
| Apparence | Clair / Sombre / Système | OK |
| Échéances | Liste ou « Aucune pour l’instant » | Empty sans *pourquoi* ; mal placée |
| Aide (route) | Bouton seul → HowItWorks | Quasi mort (hub bypasse) |
| HowItWorks | Sections figées | Contact volontairement absent (Menu) |

### Accueil (référence doublons)

| Entrée Accueil | Rôle | Lien Réglages |
|----------------|------|---------------|
| Sticky **Mettre de côté** | Montant rapide | Sheet → QuietLink « Jour et détail › » → `SettingsSavings` |
| Menu QuietLink Arkalia | Support | **Pas** dans Réglages (charte) |
| Dossiers → résumé | PDF / texte | Aussi Réglages si `showSummaryButton` |

---

## 2. Problèmes — priorités

### P0 — « Échéances hors ce mois » sous Confort

| | |
|--|--|
| **Preuve** | `SettingsComfortGroupScreen` L142 : `DEADLINES` avec Apparence / Alertes. Charte §7 : assurances / taxes = usage année, pas « confort ». |
| **Impact** | Débutante cherche une facture annuelle sous le mauvais tiroir ; Confort = thème + notifs. |
| **Fix** | Déplacer vers **Données & sauvegarde** (ordre après Sauvegarde). |

### P0 — Hub « Mettre de côté » = même geste qu’Accueil

| | |
|--|--|
| **Preuve** | `SettingsHubCopy.SAVINGS = "Mettre de côté"` ; Accueil sticky + sheet montant. Réglages ajoute **jour + retrait** (valeur réelle). |
| **Impact** | Doublon perçu → « peu pro / fouillis ». |
| **Fix** | **Garder** la page (raccourci utile 60+ pour jour/retrait) ; **reformuler** la ligne hub : « Jour du Mettre de côté ». Tip page raccourci. |

### P1 — Libellés hub un peu jargon / longs

| | |
|--|--|
| **Preuve** | `Aide — Comment ça marche` redondant avec titre HowItWorks ; `Votre code / copie` slash peu oral. |
| **Fix** | Hub Aide → **Comment ça marche** ; raccourci → **Code et copie**. |

### P1 — Empty Échéances sans pourquoi

| | |
|--|--|
| **Preuve** | `SettingsDeadlinesScreen` : « Aucune pour l’instant. » seul. |
| **Fix** | Une phrase : assurances / taxes hors mois courant. |

### P2 — Route `SettingsHelp` quasi orpheline

| | |
|--|--|
| **Preuve** | Hub `onOpenHelp` → `HowItWorks` ; `SettingsHelpScreen` = un Secondary. |
| **Fix** | Laisser route (pas redesign) ; pas d’entrée hub vers l’écran intermédiaire. |

### P2 — Raccourci hub + Sauvegarde dans le groupe

| | |
|--|--|
| **Preuve** | Deux portes vers `SettingsBackup` (volontaire P0-U1). |
| **Décision** | **Garder** — code vital sans ouvrir le groupe ; 60+ prioritaire sur anti-doublon pur. |

---

## 3. Plan fix (7.04)

1. Déplacer Échéances → Données & sauvegarde ; Confort = Apparence + Alertes seulement.
2. Reformuler ligne épargne hub ; tip page ; empty échéances.
3. Copy hub : Code et copie · Comment ça marche.
4. Tests `SettingsHubCopyTest` + Aide si besoin ; bump `0.1.0-7.04`.
5. Charte §7 : Échéances sous Données (pas Confort).

**Hors scope :** redesign nuit / Apparence ; fusion Dossiers↔Résumé ; suppression du raccourci code.

---

## 4. Limites

Inspection code seule. **NON VÉRIFIÉ** ici : smoke doigt Athalia post-fix ; dump UI device sur 7.03 pour ce point (plaintes orales).
