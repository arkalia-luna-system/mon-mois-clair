# Campagne produit fini — tests poussés sur téléphone

Version cible : lire **`PROGRESS_LOG.md`** (journal actuel, schéma Room **8**).  
**Règle d’or :** pas de « OK » sans geste **sur le téléphone branché** + preuve (`uiautomator` / screenshot dans `dist/verify/`).  
Lire le code ne remplace **pas** le doigt. Survoler la matrice = échec de campagne.

Package : `be.monmoisclair.app` · APK famille = **release** signée (jamais debug sur le tel famille).

```bash
COPYFILE_DISABLE=1 ./gradlew :app:assembleRelease
adb install -r dist/MonMoisClair-release.apk
# ou depuis le cache Gradle release
adb shell dumpsys package be.monmoisclair.app | grep versionName
# doit afficher versionName du journal (ex. 0.1.0-6.49)
```

## 0. Protocol device (OBLIGATOIRE pour chaque agent)

1. Vérifier `adb devices` → 1 device `device` (pas `unauthorized`).
2. Installer la **release** à jour ; confirmer `versionName` = journal.
3. Pour **chaque** scénario coché ✅ :
   - faire le geste (tap / saisie / partage) ;
   - dump UI ou screenshot daté sous `dist/verify/campagne-YYYY-MM-DD/` ;
   - noter l’attendu vs observé en une ligne.
4. Chasser le **petit** défaut : texte coupé, bouton 47 dp, double-tap, fond identique à un autre bandeau, jargon, scroll qui mange FAIT, clavier qui cache Enregistrer.
5. Minimum **8 frictions** notées (mineures OK) — « rien à signaler » = campagne invalide.
6. Corriger P0/P1 si tu es Correcteur/Visuel ; sinon ticket clair pour Validation.
7. **NE PAS** `pm clear` sauf persona Base vide / demande humaine (sinon tu effaces les données famille).
8. Un seul Gradle à la fois ; `COPYFILE_DISABLE=1`.

### Outils adb utiles

```bash
adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml dist/verify/…
adb exec-out screencap -p > dist/verify/…/ecran.png
adb shell input tap X Y
adb shell input swipe X1 Y1 X2 Y2 DUR
adb logcat -d -t 80 | grep -iE 'monmoisclair|AndroidRuntime|FATAL'
```

Mesurer une cible : hauteur bounds ≥ `48 * densité / 160` px.

## 1. Personae

| Persona | Qui | Objectif | Interdit à l’écran |
|---------|-----|---------|-------------------|
| **Maman** | Carnet simple | Reste, FAIT, courses | Huissier, Mes dossiers si 0, € dette sur Home |
| **Alex** | Retards + accords | IBAN, Écrire, rythme « si », soldé | « 3 sur 12 », « fin prévue », total dettes hero |
| **Installateur** | Sideload | SAF Documents, backup, bio OFF | Jargon WorkManager / SAF |
| **Médiateur** | Mois cassé | Résumé honnête | PDF CPAS, date certaine |
| **Base vide** | `pm clear` seulement si demandé | Onboarding → Home vide | Menus fantômes |
| **Multi-dossiers** | ≥2 accords + pending | Bandeau, conversion, Mes dossiers | Pending dans À payer |

+ tempérament : `PROFILS_TESTEURS.md`.

## 2. Matrice exhaustive (cocher = fait sur device)

### Carnet — C

| # | Geste téléphone | Attendu / défaut à chasser |
|---|-----------------|----------------------------|
| C1 | Home froid | Reste + À payer lisibles **sans** scroll ; pas de plaque sticky absurde |
| C2 | FAIT puis Remettre | Badge date ; snackbar Annuler ~10 s ; **double-tap** FAIT = 1 seule écriture |
| C3 | + Ajouter loyer | Grille 1–31 ; virgule ; défaut ce mois-ci seul ; Enregistrer au-dessus clavier |
| C4 | Divers + clavier | +/− encore tapables (`imePadding`) |
| C5 | Menu | Mois + Réglages ; **pas** Mes dossiers si 0 accord et 0 pending |
| C6 | Ouvrir bloc retard puis Retour/Quitter sans reste | **Aucun** dossier créé |
| C7 | Recherche `zzzz` | État vide clair, pas crash |
| C8 | Scroll liste longue (20+ lignes) | Pas de jank ; FAIT toujours ≥ 48 dp |
| C9 | Mois Préc./Suiv. | Grisé compréhensible aux bornes |
| C10 | Facture en retard | Pastille En retard ; contour ; pas criard illisible |

### Dossiers / rythme — D (6.37)

| # | Geste | Attendu / défaut |
|---|-------|------------------|
| D1 | Créer accord (cause + reste) | Pastille Accord ; **pas** € reste sur Home |
| D2 | FAIT 30 € sur forfait 50 | Reste −**30** (Mes dossiers / fiche) |
| D3 | Forfait inchangé, plusieurs FAIT | Phrase « Encore environ N mois **si** … » sur **fiche** + Mes dossiers ; **absente** de Home |
| D4 | FAIT d’un montant ≠ forfait | Phrase rythme **disparaît** |
| D5 | Soldé | Plus de ligne mois suivant ; historique FAIT reste |
| D6 | Copier `+++…+++` valide | Presse-papiers = **12 chiffres** |
| D7 | Copier IBAN (avec espaces / préfixe « IBAN ») | Majuscules, sans espaces ; refuse si checksum KO |
| D8 | Écrire (e-mail rempli) | Gmail/Mail Compose ; rien stocké dans l’app |
| D9 | Frais sans montant + motif | Refusé ; avec montant → cumul |
| D10 | 2ᵉ photo / Voir / Enlever | Pas de remplacement silencieux de la 1ʳᵉ |
| D11 | Cadence trimestrielle/annuelle | Libellé `€ / trimestre|an` ; rythme dit « versements » pas « mois » si non mensuel |
| D12 | Photo + « aussi mois suivants » | Mois d’après : photo visible sans reprise |

### Pending — P

| # | Geste | Attendu |
|---|-------|---------|
| P1 | Ranger sans plan | Bandeau Home ; **pas** dans À payer |
| P2 | Convertir → accord | Pending disparait ; dossier apparaît |
| P3 | Double-tap Ranger / Convertir | Une seule écriture |
| P4 | Menu | Mes dossiers (N) si pending ou accords |

### Mettre de côté — E

| # | Geste | Attendu |
|---|-------|---------|
| E1 | Activer montant + jour | Ligne facture du mois |
| E2 | Montant qui creuse le Reste | Alerte **avant** enregistrement |
| E3 | FAIT | Fiche : déjà viré / pas dispo ici |
| E4 | Aller-retour Réglages pendant saisie | Montant **conservé** |
| E5 | Retirer | Ligne disparaît ; Home propre si plus d’activation |

### Résumé — F

| # | Geste | Attendu |
|---|-------|---------|
| F1 | Préparer un résumé | Revenus, factures, dossiers 4 colonnes, mis de côté **à part**, ligne rythme si estimable |
| F2 | Partage mail/WhatsApp | Phrase créancier/conseiller ; pas d’identité inventée |
| F3 | Bouton | Mes dossiers ; Réglages si déficit ou dossier |
| F4 | Double-tap partager | Pas de double intent |

### Samsung / a11y / praticité — S / V

| # | Geste | Attendu |
|---|-------|---------|
| S1 | Envoyer copie / archive | ZIP ; restore annonce photos |
| S2 | Restore (si backup test) | Pas d’écran qui ment |
| S3 | Bio OFF | Pas de lock forcé |
| S4 | dens. 640 ou police 1,3 | Compact : Accord **sur** ligne du nom ; FAIT ≥ 48 dp |
| S5 | Zone bas écran | Menu / + Ajouter hors boutons système |
| S6 | Contraste | Primary texte clair ; pastilles lisibles ; bandeau « Vérifiez » ≠ « Copié » |
| S7 | Rotation (si autorisée) | Pas de crash ; layout acceptable ou locked portrait noté |
| S8 | Cold start | Splash → Home sans freeze > 3 s ressenti |
| S9 | Notif (si possible) | Multi-factures : bonne ligne ; FAIT + Annuler |
| V1 | Chaque écran du persona | Aucun texte coupé, ellipsis abusif, bouton hors écran |
| V2 | Dialogues danger | Titre + conséquence + Annuler évidents |
| V3 | États vides | Message + action suivante claire |

## 3. Sessions (chaque chat = 1 expert + 1 persona + 1 tempérament)

| # | Prompt | Focus | Matrice mini |
|---|--------|-------|--------------|
| 1a | Correcteur | Hostile + Multi-dossiers | D*, P*, C2, double-tap |
| 1b | Correcteur | Null + Base vide | C*, onboarding (clear seulement si OK humain) |
| 2a | Visuel | Chiant + Maman | S4–S6, V*, Home |
| 2b | Visuel | Perf + Alex | C8, caméra, Mes dossiers, fiche longue |
| 3a | Produit | Fidélité + Maman | C5–C6, abandon, zéro pixel |
| 3b | Produit | Fidélité + Médiateur | E*, F*, D3–D4 |
| 4 | Senior | Maman | Peurs, jargon, Device Care |
| fin | Validation | Claims 6.37 + 233/233 + P0/P1 | Lecture rapports |

## 4. Interdits

- Banque / cloud / graphiques / huissier / snowball.
- « Tout parfait » sans matrice + preuves device.
- Debug APK sur tel famille.
- Inventer un N/N tests sans `--rerun-tasks` + XML.
- Masquer un défaut « parce que c’est petit ».

## 5. En-tête de rapport (obligatoire)

```text
Profil : …
Persona : …
Version device : 0.1.0-6.37 (vérifiée adb)
Device : SM-…
Matrice : C1 ✅ C2 ❌ …
Preuves : dist/verify/campagne-…/
Frictions (≥8) : …
Verdict : GO / NO-GO famille
```
