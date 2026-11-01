> **ARCHIVÉ** 2026-11-01 — historique figé. Vivant = `docs/audits/2026-11-01-premium-*` + journal.

# Audit final — livrable famille (cadeau maman)

| | |
|--|--|
| **Date** | 2026-10-26 |
| **Profil** | Product owner — **checklist Go / No-Go** avant remise du cadeau |
| **Produit** | Mon Mois Clair · journal **~6.82** · `0.1.0-6.82` · Room **8** · **332/332** unit (claim journal) |
| **Portée** | **AUDIT ONLY** — doc seule. **Pas de code.** Pas de claim « 9/10 dans la main » ni « coffre parfait ». |
| **Sources** | `PROGRESS_LOG` · `README` · `PRIVACY` · `DISTRIBUTION_FAMILLE` · `PLAN_PRODUIT_FINI` · smoke S25 · a11y S26 · crypto 6.81 · polish 6.82 · évaluation cadeau |

**Question unique :** *Est-ce qu’on peut installer Mon Mois Clair sur le téléphone de maman (S26), lui expliquer le code de copie, et dormir tranquille si elle écrit à Arkalia ?*

---

## 0. Verdict exécutif (à date du journal)

| Dimension | État code | Preuve **dans la main** | Gate cadeau |
|-----------|-----------|-------------------------|-------------|
| APK release sideload | **OK** (même keystore) | Ultra installé 6.82 ; **S26 maman = ouvert** | **Bloquant** |
| Crypto + code de copie | **OK** 6.81–6.82 | Smoke restore / partage **ouvert** | **Bloquant** |
| Restore (`.mmc` + grace clair) | **OK** code | Parcours doigt **ouvert** | **Bloquant** |
| Smoke S26 (cible n°1) | Layout codé | **Jamais tenu en main** | **Bloquant** |
| Copy 60+ (jargon) | **OK** 6.82 | Relecture doigt **ouverte** | Fort |
| Support Arkalia | QuietLink + mail | À revalider Gmail Compose | Fort |
| Dependabot UI GitHub | Force local 6.80 | Rescan encore ouvert | **Non bloquant** cadeau |
| Claim premium / Play | — | Explicitement **non** | Hors scope |

**Go / No-Go aujourd’hui :** **NO-GO** pour remise « sans Arkalia à côté ».

Raison courte : le craft (crypto, copy, widget P0, tests) est là ; la **preuve doigt sur S26** + **rituel code de copie / restore** ne le sont pas. Un cadeau livré sans ces deux preuves = risque émotionnel (panne, mauvaise install, code perdu) plus élevé que le gain.

**Go conditionnel possible** seulement si : (1) smoke S26 minimal vert, (2) restore testé une fois de bout en bout avec le code noté, (3) notice install + Arkalia lues à voix haute — voir §6–7.

---

## 1. Définition « LIVRABLE » (cadeau, pas boutique)

Livrable **famille** ≠ vendable Play. Critères **tous** requis :

1. **Install** — APK release signé, version affichée = journal, données conservées si mise à jour.
2. **Code de copie** — maman (ou proche) a **noté une fois** le code 8 chiffres ; copy UI sans jargon « chiffré ».
3. **Restore** — une restauration réussie sur un 2ᵉ appareil *ou* après désinstall simulée, avec le code ; message d’erreur mauvais code compréhensible.
4. **Smoke S26** — checklist §4 verte (ou proxy 360×780 + texte Grand **et** S26 réel pour sticky / pouce).
5. **Copy** — Accueil / Sauvegarde / Aide / partage : phrases 60+, pas de noms démo (`loyer3`, etc.) sur le téléphone cadeau.
6. **Support Arkalia** — Menu → QuietLink ouvre un brouillon mail vers Arkalia Système avec version + modèle.

Hors livrable volontaire : Play Store, sync cloud, claim « 9/10 », coffre parfait, rescan Dependabot, carrière GitHub.

---

## 2. Checklist PO — à cocher (✅ / ❌ / N/A)

### A. Install & distribution

| # | Check | Attendu | Preuve | ☐ |
|---|--------|---------|--------|---|
| A1 | Build `assembleRelease` | APK `dist/MonMoisClair-release.apk` ~14 Mo ARM | `aapt dump badging` → `0.1.0-6.82` (ou version journal) | |
| A2 | Même keystore | Mise à jour sans perte Room | Pas de « app non installée » / signature différente | |
| A3 | Canal envoi | Drive **préféré** ; WhatsApp souvent bloque `.apk` | Lien Drive testé sur Galaxy famille | |
| A4 | Chez maman | Autoriser source → Installer → ouvrir → Réglages = version attendue | Capture ou note doigt | |
| A5 | Pas debug | Jamais APK debug sur téléphone cadeau | Signature ≠ debug | |
| A6 | Données démo | Pas de `loyer3` / personas test sur le device remis | Accueil propre ou mois neuf | |

Réf. : [`docs/DISTRIBUTION_FAMILLE.md`](../DISTRIBUTION_FAMILLE.md).

### B. Code de copie (cœur sérénité post-6.81)

| # | Check | Attendu | Preuve | ☐ |
|---|--------|---------|--------|---|
| B1 | Affichage | Réglages → Sauvegarde : **code gros**, « notez une fois » | Écran lisible S26 / Grand texte | |
| B2 | Rituel | Code recopié **papier** ou coffre famille (pas seulement WhatsApp) | Arkalia confirme « noté » | |
| B3 | Partage `.mmc` | Sans le code = illisible ; phrase avant chooser sans jargon | Envoi test Drive/WA du fichier seul | |
| B4 | SAF `MMC1B64` | Copie Documents chiffrée | Fichier présent + restore demande code | |
| B5 | Mauvais code | Message clair (pas stack / crypto) | Saisie volontaire fausse → grace UI | |
| B6 | Honnêteté | « Code oublié + téléphone mort sans copie = perdu » dit une fois | Aide / PRIVACY alignés | |

### C. Restore

| # | Check | Attendu | Preuve | ☐ |
|---|--------|---------|--------|---|
| C1 | Chemin UI | Réglages → Données & sauvegarde → Restaurer (**pas** dans Menu Accueil) | Charte tenue | |
| C2 | Copie chiffrée | Restore avec **bon** code → mois / factures / photos OK | Parcours doigt complet | |
| C3 | Grace pré-6.81 | Ancienne copie claire encore restaurable (si existe) | Ou N/A si aucune | |
| C4 | Échec soft | Mauvais fichier / mauvais code → **aucune** écrasement silencieux | Base intacte | |
| C5 | Après restore | Reste cohérent ; widget rafraîchi si présent | Accueil + home | |

### D. Smoke S26 (cible n°1 charte)

Device : **Galaxy S26 6,3″** (ou hauteur courte réelle). Texte One UI **≥ Grand (~1,3)** pour les items marqués *.

| # | Check | Attendu | ☐ |
|---|--------|---------|---|
| D1 | Cold start / 1ʳᵉ ouverture | Splash → Accueil ; bio si ON sans blocage mort | |
| D2 | Sticky * | Reste lisible ; Courses ≥ 48 dp ; **pas** de logo | |
| D3 | Plus → épargne * | « Mettre de côté » accessible ; liste pas mangée | |
| D4 | FAIT ✓ | Cible confortable ; snackbar Annuler | |
| D5 | Menu | Réglages · À venir · Dossiers (si N) · mois · QuietLink Arkalia **pas** sous barre | |
| D6 | Fiche grille 1–31 * | Jour **31** tappable (clearance sticky) | |
| D7 | Widget | Ajoutable ; Reste (ou masqué si bio) — pas « Impossible d’ajouter » | |
| D8 | Notif (si alertes ON) | Teal + silhouette ; montant masqué si bio ON | |
| D9 | Nuit | Sticky / retard lisibles | |
| D10 | Photo / PDF | Ouvrir pièce ; pas de crash cache `view/` | |

Réf. a11y 10 points : [`2026-10-23-nuit-06-a11y-oneui.md`](2026-10-23-nuit-06-a11y-oneui.md) §4.  
Filet Ultra déjà partiel : [`2026-10-23-smoke-device.md`](2026-10-23-smoke-device.md) — **ne remplace pas** S26.

### E. Copy (60+)

| # | Check | Attendu | ☐ |
|---|--------|---------|---|
| E1 | Sauvegarde / Restore / Aide | Pas de « chiffrement / SQLCipher / Keystore / AES » en UI | |
| E2 | À venir | Phrase courte (C02 6.82) — pas parcours technique | |
| E3 | Fiche Annuler | QuietLink lisible entre CTA (C03) | |
| E4 | Partage | « Code de copie » / « notez une fois » — pas « passphrase » | |
| E5 | Accueil | Zéro nom de test / persona archive | |

### F. Support Arkalia

| # | Check | Attendu | ☐ |
|---|--------|---------|---|
| F1 | QuietLink Menu | « Un souci ? Écrire à Arkalia Système » visible bas Menu | |
| F2 | Intent mail | Gmail / Mail s’ouvre avec destinataire Arkalia + version | |
| F3 | Corps | Place pour décrire + rappel « joindre capture dans Mail » | |
| F4 | Notice orale | Maman sait **où** cliquer sans Arkalia à côté | |
| F5 | Canal Arkalia | Boîte `arkalia.luna.system@gmail.com` surveillée le week-end de remise | |

### G. Hors gate (noter, ne pas bloquer le cadeau)

| # | Sujet | État journal | Action |
|---|--------|--------------|--------|
| G1 | Dependabot GitHub UI | Force local OK ; rescan ouvert | Plus tard |
| G2 | Archives `docs/archive` PII | REPORTÉ | Repo reste privé |
| G3 | God-file Home | Dette eng | Non bloquant |
| G4 | Badges README (tests/version) | Croiser journal avant push public | Hors cadeau jour J |

---

## 3. Matrice Go / No-Go

| Décision | Conditions |
|----------|------------|
| **GO** | A1–A6 · B1–B5 · C1–C2 · C4 · **D1–D7** · E1 · E4–E5 · F1–F5 tous ✅. D8–D10 et B6 fortement recommandés. |
| **GO conditionnel** | S26 smoke D1–D7 ✅ **ou** proxy + S26 sticky OK ; restore C2 fait **par Arkalia** le jour J ; maman a le code papier ; Arkalia dispo 48 h pour support. Démo noms nettoyés. |
| **NO-GO** | Pas de smoke S26 (même partiel) · **ou** code de copie jamais noté · **ou** restore jamais prouvé · **ou** install Drive/WA non testée · **ou** QuietLink mail cassé. |

**Rappel charte :** usage principal = **S26**. Un Ultra vert ne suffit pas pour le claim « dans la main de maman ».

---

## 4. Risques émotionnels (pourquoi ces gates)

| Si on livre trop tôt… | Effet maman |
|------------------------|-------------|
| APK WhatsApp bloqué / mauvaise signature | « Ça ne s’installe pas » → abandon |
| Code jamais noté + téléphone cassé | Carnet perdu ; pas de recovery cloud |
| Restore jamais testé | Panique le jour où on en a besoin |
| Sticky / jour 31 / widget cassés sur S26 | Sentiment « pas pour moi » |
| Support introuvable | Appel Arkalia en urgence = cadeau stressant |

---

## 5. État factuel au 2026-10-26 (sans maquillage)

| Fait | Source |
|------|--------|
| Étape journal **6.82** · tests **332/332** · vc **87** | `PROGRESS_LOG` |
| Crypto P0–P2 **codés** ; copy code gros **6.82** | Journal 6.81–6.82 |
| Widget P0 **OK** Ultra 6.79 ; polish 6.80–6.82 | Smoke device + journal |
| Smoke S25 **partiel** ; P1 H01/F01/D01 **code** à revalider doigt | `smoke-device.md` |
| **S26 réel** | Toujours **ouvert** (`PLAN_PRODUIT_FINI`, synthèse nuit) |
| Prochaine sous-étape journal | Smoke doigt 1ʳᵉ ouverture / code / WA / restore · S26 · rescan Dependabot |

---

## 6. Tâches finales **ordonnées** (jour J − n)

Ordre strict — ne pas sauter une étape bloquante.

| Ordre | Tâche | Qui | Gate |
|------:|-------|-----|------|
| **1** | Relire `PROGRESS_LOG` + version Réglages = `0.1.0-6.82` (ou suivante clôturée) | Arkalia | — |
| **2** | `assembleRelease` + `aapt dump badging` ; copier APK vers Drive test | Arkalia | A1 |
| **3** | Install test sur **S26** (ou téléphone maman avant remise) ; vérifier version | Arkalia | A4 |
| **4** | Smoke **D1–D7** (minimum) ; noter ❌ bloquants | Arkalia | D |
| **5** | Si ❌ bloquant UI → **correctif + tests** (hors ce doc) puis revenir à 3 | Dev | NO-GO tant que rouge |
| **6** | Réglages → afficher **code de copie** ; le noter **papier** (double) | Arkalia + maman | B1–B2 |
| **7** | Créer une copie `.mmc` / SAF ; restore sur 2ᵉ device **ou** après clear data de test | Arkalia | C2 |
| **8** | Tester mauvais code (phrase claire) | Arkalia | B5 · C4 |
| **9** | Nettoyer données démo sur device cadeau (ou fresh install + saisie réelle) | Arkalia | A6 · E5 |
| **10** | QuietLink → brouillon mail Arkalia (F1–F3) | Arkalia | F |
| **11** | Envoyer APK via **Drive** ; faire installer **sans USB** une fois | Maman / proche | A3–A4 |
| **12** | Notice orale 2 min : mois · FAIT · Menu · code papier · « Un souci ? Arkalia » | Arkalia | Livrable humain |
| **13** | Cocher matrice §3 → **GO** ou **GO conditionnel** daté | PO | Décision |
| **14** | Remise cadeau ; surveiller mail Arkalia 48 h | Arkalia | Post-GO |
| **15** | (Après) Rescan Dependabot / badges README — **pas** le jour J | Arkalia | G |

**Interdit le jour J :** refonte features, claim premium, merge `main` sans demande, push sans tests verts.

---

## 7. Script notice (2 minutes, à voix haute)

1. « Ce n’est **pas** une banque : c’est le carnet du mois sur *ton* téléphone. »  
2. « Le gros chiffre = ce qu’il **reste** après les factures. Tu coches **FAIT** quand c’est payé. »  
3. « Menu (bas) → Réglages si besoin ; **Un souci ?** tout en bas du Menu → mail à Arkalia. »  
4. « **Code de copie** : on l’a noté ici *(papier)*. Sans ce code, une copie WhatsApp ne se relit pas — et on ne peut pas le retrouver sur un serveur. »  
5. « Pour mettre à jour plus tard : je t’envoie un fichier Drive ; tu ouvres l’APK ; la version dans Réglages doit monter. »

---

## 8. Phrase PO (décision)

> **NO-GO** tant que le S26 n’a pas fumé le sticky / Menu / widget et qu’un restore avec code de copie n’a pas été fait une fois pour de vrai.  
> **GO** (ou GO conditionnel) seulement quand la checklist §2 A–F minimale est verte et que maman a le code **sur papier**.

---

## 9. Liens utiles

| Doc | Rôle |
|-----|------|
| [`DISTRIBUTION_FAMILLE.md`](../DISTRIBUTION_FAMILLE.md) | Install sans USB |
| [`PRIVACY.md`](../../PRIVACY.md) | Offline · code · limites |
| [`2026-10-23-smoke-device.md`](2026-10-23-smoke-device.md) | Filet Ultra (pas S26) |
| [`2026-10-23-nuit-06-a11y-oneui.md`](2026-10-23-nuit-06-a11y-oneui.md) | 10 points hauteur courte |
| [`2026-10-26-securite-chiffrement.md`](2026-10-26-securite-chiffrement.md) | Contexte menace (pré-impl) |
| [`2026-10-23-evaluation-cadeau-maman.md`](2026-10-23-evaluation-cadeau-maman.md) | Devis construction (~14 k€) — autre sujet |
| [`PLAN_PRODUIT_FINI.md`](../PLAN_PRODUIT_FINI.md) | « Pas encore objet fini dans la main » |

---

*Fin audit livrable famille · product owner · 2026-10-26 · état ~6.82 · AUDIT ONLY.*
