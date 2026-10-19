# Rapport test — 2026-10-19 (audit global post 3.4–3.5)

## Contexte
- Device : SM-S938B (S25 Ultra), Android 16, serial `R3CY60BJ3ZM`
- Version app : `0.1.0-3.5.1`
- Étape PROGRESS : audit après **3.5** Hub Réglages + fix verrou biométrie
- Build : `COPYFILE_DISABLE=1 ./gradlew :app:testDebugUnitTest :app:assembleDebug --rerun-tasks`
- Preuves : `dist/verify/audit/` (`home_fresh.png`, `z_settings.png`, `sante.png`, `edit.xml`, …)

## Synthèse
- Verdict : **OK avec réserves mineures**
- Bloquants : **0**
- Majeurs : **0** (1 corrigé pendant l’audit)
- Mineurs / hors API : Device Care manuel ; prompt biométrie réel à confirmer à la main

## Validation CODE
| Check | Résultat |
|-------|----------|
| Claims README / PROGRESS / `versionName` alignés | OK → `0.1.0-3.5.1` |
| Pas de `catch {}` silencieux métier | OK (intents Réglages remontent un message) |
| Montants métier en `Long` / centimes | OK |
| Tests unitaires | **84/84** |
| Fichiers 3.4–3.5 présents (bio, lock, settings hub) | OK |

## Correction faite pendant l’audit
| ID | Problème | Fix |
|----|----------|-----|
| A1 | Biométrie ON + ouverture écran système (batterie / fiche) → `ON_STOP` détruisait `NavHost` → perte pile Réglages | `MainActivity` : cold start = lock seul ; après 1er unlock, **overlay** sur NavHost conservé |
| A2 | `runCatching` silencieux sur intents système Réglages | Message d’erreur UI via `reportExternalIntentFailed()` |

## Parcours device
| Persona / scénario | Résultat | Preuve |
|--------------------|----------|--------|
| Home 3 boutons (Ajouter / Santé / Réglages) | OK | `home_fresh` / UI dump |
| Mois affiché « Octobre 2026 » (pas tronqué) | OK | dump `month` |
| Marge basse (gap ~568 px vs 3120) | OK | `bottom_gap_px` |
| Édition ligne (pension) | OK | `edit.xml` |
| Santé 4× OK + hint Device Care | OK | `sante.png` |
| Hub Réglages (bio + Device Care + SAF + Restore) | OK | `z_settings` / bot scroll |
| Backups `Teste/mon-mois-clair-backup-*.json` | OK | ls device (tours précédents + présence) |
| FATAL logcat | aucun | OK |
| Toggle FAIT | OK (tours précédents + UI) | — |

## Checklist visuelle
- [x] Marge basse vs boutons système
- [x] Pas de crash
- [x] Contraste Primary (crème sur vert)
- [x] Dialogues restore (confirm « Remplacer… ») — validé tour 3.3 / partiel ce tour
- [x] Home allégé / hub Réglages
- [ ] Prompt biométrie réel (empreinte) — **réserve humaine** (matériel dispo confirmé à l’écran)
- [ ] Device Care « Mise en veille » manuel One UI — **hors API**

## Non traité (hors scope)
- Picker fichier JSON restore E2E (SAF document picker interactif)
- Preuve alarme J−3 / Jour J à l’heure réelle

## Message pour l’expert validation
Audit code + Gradle **84/84** + device S25 Ultra OK sur Home / Santé / Réglages hub.  
Bug majeur de navigation sous biométrie **corrigé** en `0.1.0-3.5.1`.  
Prêt pour relecture claims + push si tu valides le diff MainActivity / Settings.
