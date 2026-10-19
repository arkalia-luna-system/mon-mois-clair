# Charte & Architecture — Mon Mois Clair

Mise à jour : 2026-10-19 (lot **6.x** — assistant papiers BE).

## 1. Vision

App **locale, privée, hors-ligne** (Samsung S25 / One UI). Sideload **release signé**.

### Persona (verrouillé)

| Question | Réponse |
|----------|---------|
| Factures | **Majoritairement papier** |
| Paiements | **App bancaire sur le même S25** |

### Freeze Home

Pas de nouvelles options quotidiennes sur l’accueil.  
**Exception L** : bandeau « À prévoir le mois prochain » seulement si utile.

### Stack

Kotlin + Compose + Room + Hilt · ML Kit Barcode offline + CameraX · `Europe/Brussels` · `be.monmoisclair.app`

## 2. Décisions

**A–I + T1–T8** : inchangés (lots 1–5).

| ID | Décision |
|----|----------|
| **J** | **Scanner une facture** (QR EPC/Girocode BE) → préremplit nom, montant, communication. ML Kit offline. |
| **K** | Photo WebP ≤800px dans SAF (`receipts/{id}.webp`) + champ Communication + **Copier** (presse-papiers). **Jamais** Base64 dans le JSON. |
| **L** | Bandeau Home si périodique (intervalle > 1) due **le mois prochain** ; sinon invisible. |
| **T9** | Photos = fichiers SAF ; JSON = chemin relatif seulement. |
| **T10** | Parser EPC unitaire ; scan CameraX + ML Kit. |

## 3. Données

Room / backup schema **3** : `paymentNote`, `receiptRelativePath` sur `expense_instances`.

## 4. Plan

| Étape | Contenu |
|-------|---------|
| **6.0** | Charte J–L + persona | ✅ |
| **6.1** | Scanner EPC | |
| **6.2** | Photo + note + Copier | |
| **6.3** | Bandeau mois prochain | |

Journal : `PROGRESS_LOG.md`.
