> **ARCHIVÉ / FIGÉ** (2026-10-26) — ne plus lire pour l’état code. Vérité = `PROGRESS_LOG.md` + PRIVACY/SECURITY + audits `final-0*`.

# Audit GitHub carrière — nuit 07 (2026-10-23)

**Profil d’audit :** tech recruiter / brand GitHub  
**Cible :** valoriser une **pro Kotlin/Compose** (empowerment + privacy BE) — pas une « débutante qui ne gère pas ses finances ».  
**Périmètre :** profil `arkalia-luna-system` + dépôt `arkalia-luna-system/mon-mois-clair` (workspace local croisé).  
**Mode :** **AUDIT ONLY** — aucun push, aucun passage en public, aucun force-push.

État capturé le **2026-10-23** (API GitHub + README/PRIVACY locaux).

---

## Verdict en 30 secondes

| Signal | Lecture recruteur |
|--------|-------------------|
| Histoire publique | **Python / Flutter / Azure en apprentissage** |
| Artefact le plus fort | **Mon Mois Clair** (Kotlin/Compose, 305 tests, privacy-first BE) — **dépôt privé → invisible** |
| Risque brand | Bio + pins = « autodidacte / pas encore certifiée » ; zéro Kotlin en vitrine |
| Risque PII si un jour public | Archives famille (`Senior` / `Accords` / prénom civil), journal, device |

**Conclusion :** le craft Android est réel ; la **vitrine le cache**. Demain = aligner bio/pins/README **sans** ouvrir le dépôt.

---

## 1. Checklist vitrine (score rapide)

| # | Élément | État | Score | Action demain |
|---|---------|------|-------|---------------|
| 1 | Bio = Kotlin/Compose + BE privacy | Non — Python/Flutter/Azure + « autodidacte » | 2/10 | Réécrire bio (§2) |
| 2 | Pins = produits shippés, pas carnet d’exam | Non — pin #1 = AZ-104 « pas encore certifiée » | 3/10 | Re-pinner (§3) |
| 3 | README profil = stack + preuve | Partiel — pas de Kotlin ni Mon Mois Clair | 4/10 | Blocs « En ce moment » + « Android » (§4) |
| 4 | mon-mois-clair visible en public | Non — `PRIVATE` | n/a | **Garder privé** demain ; option showcase (§7) |
| 5 | README dépôt = produit empowerment | OK en privé ; « familial » trop dominant pour une future vitrine | 6/10 | Préparer rewrite soft (§5) |
| 6 | PRIVACY.md crédible | Oui — pas d’INTERNET, local-first, clipboard sensible | 9/10 | Garder ; citer en une ligne sur le profil |
| 7 | Hygiene PII avant tout public | Non prêt | 2/10 | Checklist scrub (§6) |
| 8 | Ton embauche (pro, pas « galère argent ») | Mitigé | 4/10 | Lexique (§8) |
| 9 | `Available for hire` / contact pro | `hireable` = null ; mail perso public | 5/10 | Décider demain (§9) |
| 10 | Repos bruyants / junior signals | `nours_interface` « pour débutants » + emojis | 3/10 | Archiver / réécrire description |

**Score global vitrine embauche Android :** ~**4/10** (contenu réel ~**8/10** caché).

---

## 2. Bio GitHub — état vs cible

### État (live)

```
Dev autodidacte · Belgique
Python / Flutter · projets open source
Azure : AZ-900 (Learn) · AZ-104 en préparation (exam 09/10/2026)
```

| Mot | Effet recruteur |
|-----|-----------------|
| **autodidacte** (× bio + README) | Souvent lu « junior / pas formée », pas « autonomie pro » |
| Python / Flutter only | **Invisible** le craft Kotlin/Compose du dépôt phare |
| AZ-104 en préparation | OK en 2ᵉ ligne ; **mauvais** comme identité principale |
| Pas de privacy / BE produit | Rate l’angle différenciant Mon Mois Clair |

### Cible (à coller demain — 160 car. max)

**Option A — Android first (recommandée si cible = app mobile BE)**

```
Android · Kotlin / Jetpack Compose · Belgique
Apps locales privacy-first (offline, pas de compte)
Azure AZ-900 · AZ-104 (préparation)
```

**Option B — polyglotte honnête (si tu gardes Flutter + Azure)**

```
Mobile (Kotlin/Compose · Flutter) · Python · Belgique
Privacy-first · offline · UX calme
Azure AZ-900 · AZ-104 en cours
```

**À retirer demain :** « autodidacte » de la bio (garder l’histoire en entretien si tu veux — pas en headline).  
**À ne pas écrire :** « je gère mon budget / mes factures / ma famille » comme pitch perso.

---

## 3. Pins — état vs cible

### Pins actuels (4)

| Ordre | Repo | Signal |
|-------|------|--------|
| 1 | `az104-portfolio` | Apprentissage, « pas encore certifiée » |
| 2 | `arkalia-cia` | Flutter santé offline — **bon** |
| 3 | `bbia-sim` | Robotique / sim — niche intéressante |
| 4 | `arkalia-luna-pro` | Expérimental IA — OK en 4ᵉ |

**Manque :** aucun pin Kotlin/Android. Impossible de pin `mon-mois-clair` tant qu’il est privé.

### Plan pins demain (sans rendre MMC public)

| Slot | Repo | Pourquoi |
|------|------|----------|
| 1 | `arkalia-cia` | App mobile shippée (bêta), offline — prouve mobile |
| 2 | `bbia-sim` | Depth technique (pas « todo app ») |
| 3 | `arkalia-luna-pro` | CI / Docker / monitoring |
| 4 | `az104-portfolio` | Azure — **en 4ᵉ**, pas en 1ᵉ |
| 5–6 | (libre) | Éviter `nours_interface` / templates / logos |

**Si un jour MMC public (plus tard) :** pin #1 = `mon-mois-clair`, description produit empowerment (pas « app familiale pour Senior »).

---

## 4. README profil (`arkalia-luna-system/arkalia-luna-system`)

### État

- Titre « Développeuse autodidacte » → même biais bio.
- « En ce moment » = 100 % AZ-104.
- Projets phares = Azure + Flutter + Python — **zéro Android**.
- Stack = Python · Dart/Flutter · FastAPI… — **pas Kotlin/Compose**.
- Contact = GitHub + `arkalia.luna.system@gmail.com` (OK, cohérent avec l’app).

### Actions demain (ordre)

1. Remplacer le H1 sous-titre : **« Développeuse mobile & systèmes · Belgique »** (ou équivalent sans « autodidacte »).
2. Ajouter une section **« Android »** (même si le repo reste privé) :

```markdown
## Android (Kotlin / Compose)

**Mon Mois Clair** — carnet de paiements mensuels **100 % hors ligne** (Belgique) :
pas de compte, pas de cloud, pas de pub · QR EPC · communication mod 97 · Room · Hilt · CI.
Dépôt privé (usage contrôlé) ; stack et discipline doc/tests tenues comme un dépôt public.
```

3. « En ce moment » : **2 puces** max — (a) Android / MMC polish, (b) AZ-104 — pas l’exam seul.
4. Stack : ajouter `Kotlin · Jetpack Compose · Room · Hilt · GitHub Actions`.
5. Ne pas coller de captures d’écran avec **données réelles** (IBAN, noms, montants famille).

---

## 5. README `mon-mois-clair` — prête pour une future vitrine ?

### Forces déjà là (à garder)

- Accroche produit claire : **« pas une banque »** + hors ligne.
- Badges Kotlin / Compose / CI / tests / MIT.
- Tableau fonctionnalités = craft (FAIT, EPC, PDF, biométrie).
- Lien **PRIVACY.md** + SECURITY — rare et crédible.
- Stack one-liner pro.

### Phrases à adoucir **avant** un éventuel public (pas demain nuit)

| Aujourd’hui | Risque lecture | Alternative empowerment |
|-------------|----------------|-------------------------|
| « usage familial » / « sideload familial » | Hobby / dépendance familiale | « distribution contrôlée (sideload) » / « local-first Belgique » |
| « Dépôt privé (usage familial) » | Exclut le lecteur pro | « Dépôt privé — docs/tests tenus au standard public » |
| CONTRIBUTING « Les PR externes ne sont en général pas attendues » | Fermé | OK en privé ; en public : « contributions ciblées bienvenues après lecture charte » |
| Archives nommées personas famille | PII + ton « on corrige pour Senior » | Déjà partiellement géré (« Archives figées… contact = Arkalia ») — **renforcer** |

### Angle produit recruteur (une phrase)

> App Android **privacy-first** pour clarifier le mois (reste à vivre, factures, papier belge) — **empowerment**, pas open banking ni lecture de comptes.

---

## 6. PII & hygiene — avant tout `visibility: public`

**Ne pas rendre public demain.** Si un jour :

### Bloquants (scrub obligatoire)

| Zone | Exemples | Action |
|------|----------|--------|
| `docs/archive/*` | `AUDIT_ALEX_MAMAN_*`, feedback famille, prénoms, modèles S25/S26 « usage principal » | Garder hors arbre public **ou** anonymiser (User A / User B) + retirer liens README |
| `PROGRESS_LOG.md` | Mentions prénom civil / Senior / Accords / smokes perso | Version « public » allégée **ou** rester privé |
| Device IDs | `SM-S938B` dans badges README | Remplacer par « Galaxy S25 Ultra (smoke interne) » sans serial |
| Mail | `arkalia.luna.system@gmail.com` dans code support | OK produit ; documenter comme contact **marque**, pas perso civil |
| Nom civil | Profil GitHub (nom civil) | Choix perso : OK pro BE ; sinon display name = Arkalia Luna |

### Déjà bien

- `PRIVACY.md` : pas de serveur, pas d’analytics.
- `SECURITY.md` : « Ne pas recopier le numéro de série… »
- Tests : `HowItWorksCopyTest` refuse le prénom civil dans copy UI.
- Contact produit = **Arkalia Système** (ortho tenue).

### Checklist « go public » (plus tard)

- [ ] Aucun prénom famille dans docs liées depuis README
- [ ] Aucune capture avec IBAN / montant réel
- [ ] Topics OK (`android`, `kotlin`, `jetpack-compose`, `privacy`, `offline-first`) — déjà présents
- [ ] Description GitHub repo = empowerment + BE (pas « familial »)
- [ ] Archive folder soit retirée du default branch public, soit marquée `historical / anonymized`
- [ ] Relecture ton : 0× « je galère », 0× « débutante », 0× « pour Senior » en surface

---

## 7. mon-mois-clair PRIVATE — options carrière (sans ouvrir demain)

| Option | Effort | Effet recruteur | Reco |
|--------|--------|-----------------|------|
| **A.** Rester 100 % privé + section Android sur README profil | 30 min | Signale le craft sans exposer code/famille | **Oui demain** |
| **B.** Repo `mon-mois-clair-showcase` public (README + architecture + screenshots fake + lien « private source ») | ½–1 j | Pin possible, preuves sans PII | Semaine prochaine |
| **C.** Public full source | Élevé | Max crédibilité technique | Seulement après scrub §6 |
| **D.** Ne rien changer | 0 | Recruteur Android = profil « Python/Azure learner » | Non |

**Interdit ce soir / demain sans demande explicite :** `gh repo edit --visibility public`, force-push, purge d’historique improvisée.

---

## 8. Lexique embauche (à coller / à bannir)

### Utiliser

- privacy-first · offline · local-first · Belgique  
- Kotlin · Jetpack Compose · Room · Hilt · CI  
- papier belge (EPC, mod 97) · sideload contrôlé  
- empowerment · clarté du mois · hors banque  

### Éviter en surface (bio, pins, README, About)

- autodidacte / débutante / « pour débutants »  
- « je ne gère pas mes finances » / galère / dettes comme identité  
- « app pour Senior » / « usage familial » comme pitch #1  
- « pas encore certifiée » en **premier** pin  
- emojis pédagogiques type `nours_interface` (🎓📚🌐)  

### Rewrite description `nours_interface` (quick win)

Avant : *« POC archivé… Example d'architecture propre pour débutants… »*  
Après : *« Archive 2025 — POC Flask historique (valeur pédagogique). »*

---

## 9. Ton embauche — checklist 10 min

- [ ] Bio sans « autodidacte », avec Kotlin/Compose  
- [ ] Pin #1 ≠ AZ-104  
- [ ] README profil : section Android + stack Kotlin  
- [ ] Décider : cocher **Available for hire** ou non (`hireable`)  
- [ ] Company / website : laisser vide OK ; blog = portfolio ou rien  
- [ ] Followers = 0 : normal ; la qualité des pins compte plus  
- [ ] Mail public : garder marque `arkalia.luna.system@…` (déjà aligné app)  
- [ ] Ne pas push de doc audit sur `main` sans review (ce fichier = local OK)

---

## 10. Plan d’action demain (ordre strict)

### Matin (≤ 45 min) — profil seulement

1. Éditer **bio** → Option A ou B (§2).  
2. Éditer **README profil** → sous-titre + section Android + stack (§4).  
3. **Re-pinner** : cia → bbia-sim → luna-pro → az104 (§3).  
4. Réécrire description **`nours_interface`** (§8).  
5. (Optionnel) `hireable: true` si tu cherches activement.

### Après-midi (si énergie) — sans public

6. Brouillon description GitHub `mon-mois-clair` (champ About) plus « empowerment BE » — **reste private**.  
7. Lister fichiers archive à anonymiser (checklist §6) — **ne pas merger scrub** tant que privé suffit.  
8. Décider Option A vs B showcase (§7) pour la semaine.

### Ne pas faire demain

- Rendre `mon-mois-clair` public  
- Force-push / rewrite history  
- Publier captures avec données réelles  
- Remplacer la bio par un discours « finance perso / famille »

---

## 11. Preuves techniques à citer en entretien (déjà vraies)

Utilisables **oralement** même si le repo reste privé :

- Kotlin 2.x · Jetpack Compose · Room (schéma 8) · Hilt  
- **305/305** tests unitaires + CI sur `develop`  
- Pas d’`INTERNET` · `allowBackup=false` · `FLAG_SECURE` + biométrie optionnelle  
- Spécificités BE : QR EPC, communication `+++` mod 97, PDF courrier  
- Discipline doc : charte, PRIVACY, SECURITY, journal d’étapes  

Pitch 20 s :

> « Je construis une app Android Compose **privacy-first** pour la Belgique : le mois reste clair (reste, factures, papier) sans compte ni cloud — empowerment utilisateur, pas agrégation bancaire. »

---

## 12. Références capturées (2026-10-23)

| Source | Fait |
|--------|------|
| `gh api users/arkalia-luna-system` | bio autodidacte ; Python/Flutter ; AZ ; location Belgique ; email public ; hireable null ; 13 public_repos ; 0 followers |
| Pins GraphQL | az104, cia, bbia-sim, luna-pro |
| `gh repo view …/mon-mois-clair` | **PRIVATE** ; topics android/kotlin/compose/privacy/offline-first/… ; Kotlin ; MIT |
| README local MMC | v0.1.0-6.75 ; 305/305 ; PRIVACY linked |
| PRIVACY.md | local-first, no INTERNET — solide |
| Archives / PROGRESS_LOG | PII famille si surface publique |

---

*Fin audit nuit 07. Fichier local : `docs/audits/2026-10-23-nuit-07-github-carriere.md`. Pas de changement GitHub effectué.*
