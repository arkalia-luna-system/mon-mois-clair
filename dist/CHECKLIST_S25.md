# Checklist validation Samsung Galaxy S25

APK : `dist/MonMoisClair-debug.apk`

## Installation
1. Sur le S25 : Paramètres → Sécurité → autoriser installation sources inconnues (Fichiers / USB).
2. Copier l’APK (câble, Drive, ou `adb install -r dist/MonMoisClair-debug.apk` si USB debug).
3. Ouvrir Mon Mois Clair.

## Dossier sauvegarde (SAF)
1. « Choisir un dossier » → Documents → créer/choisir `MonMoisClair`.
2. Vérifier apparition de `mon-mois-clair-backup-1.json` après une action.

## Batterie + Device Care (critique One UI)
1. Autoriser notifications.
2. Autoriser alarmes exactes.
3. Désactiver optimisation batterie (pop-up 1 clic).
4. **Device Care** : Paramètres → Batterie → Limites d’utilisation en arrière-plan
   - Retirer Mon Mois Clair de « Mise en veille » et « Mise en veille profonde ».
5. Écran « Santé des rappels » : tout OK.

## Test rappel (optionnel)
Créer une dépense avec échéance dans 3+ jours, cocher rappels, attendre J−3 10h ou avancer l’horloge (si labo).

