# Page Notes Cliniques (Clinical Notes) - WellCare Connect

## 📋 Vue d'ensemble

La page **Clinical Notes** est un formulaire complet pour la saisie des notes SOAP (Subjectif, Objectif, Assessment, Plan) utilisées en pratique médicale. Cette page offre une interface intuitive pour documenter les consultations médicales.

## 📁 Fichiers créés

1. **FXML UI**: `src/main/resources/fxml/clinical-notes.fxml`
   - Définit l'interface utilisateur
   - Structure en deux colonnes: sidebar gauche et éditeur principal

2. **Contrôleur**: `src/main/java/org/example/controller/ClinicalNotesController.java`
   - Gère la logique de la page
   - Gestion des formulaires dynamiques
   - Sérialisation des données SOAP

## 🏗️ Architecture

### Structure de la page

```
┌─────────────────────────────────────────────────┐
│ SIDEBAR GAUCHE         │  CONTENU PRINCIPAL      │
├─────────────────────────────────────────────────┤
│ • Logo WellCare        │ • Formulaire SOAP       │
│ • Carte Patient        │   - Subjective (S)      │
│ • Historique Notes     │   - Objective (O)       │
│ • Actions Rapides      │   - Assessment (A)      │
│ • Chatbot Triage       │   - Plan (P)            │
│                        │ • Vitaux (TA, Pouls)    │
│                        │ • Diagnostics           │
│                        │ • Médicaments           │
│                        │ • Examens               │
│                        │ • Suivi                 │
└─────────────────────────────────────────────────┘
```

## 🎯 Fonctionnalités principales

### 1. **Formulaire SOAP**
   - **Subjectif (S)**: Symptômes rapportés par le patient
   - **Objectif (O)**: Signes vitaux et examen physique
   - **Assessment (A)**: Évaluation et diagnostics
   - **Plan (P)**: Plan de traitement et suivi

### 2. **Signes Vitaux**
   - Tension artérielle (TA)
   - Pouls
   - Température
   - SpO2 (Saturation en oxygène)

### 3. **Gestion des Diagnostics**
   - Ajout/Suppression de diagnostics (CIM-10)
   - Tags visuels pour chaque diagnostic
   - Association aux médicaments

### 4. **Ordonnance Médicamenteuse**
   - Ajouter/Supprimer des médicaments
   - Détails: nom, dosage, forme, fréquence, durée
   - Instructions spécifiques

### 5. **Examens Complémentaires**
   - Sélection du type d'examen
   - Date demandée
   - Statut (Prescrit/Réalisé)
   - Notes supplémentaires

### 6. **Suivi**
   - Date du prochain rendez-vous
   - Type (Consultation/Contrôle)
   - Priorité (Routine/Urgent)

## 💾 Structure des données SOAP

```json
{
  "consultation": {
    "chiefComplaint": "string",
    "subjective": "string",
    "objective": "string",
    "assessment": "string",
    "plan": "string",
    "vitals": {
      "bloodPressure": {
        "systolic": "number",
        "diastolic": "number"
      },
      "pulse": "number",
      "temperature": "number",
      "spo2": "number"
    }
  },
  "diagnoses": ["string"],
  "medications": [
    {
      "name": "string",
      "dosage": "string",
      "frequency": "string",
      "duration": "string",
      "instructions": "string"
    }
  ],
  "labTests": [
    {
      "type": "string",
      "name": "string",
      "requestedDate": "date",
      "status": "string"
    }
  ],
  "followUp": {
    "date": "date",
    "type": "string",
    "priority": "string"
  }
}
```

## 🎨 Design et Styles

### Palette de couleurs SOAP
- **S (Subjectif)**: Bleu (#3b82f6)
- **O (Objectif)**: Vert (#10b981)
- **A (Assessment)**: Orange (#f59e0b)
- **P (Plan)**: Rouge (#ef4444)

### Éléments UI
- Cards pour médicaments et examens
- Tags pour les diagnostics
- GridPane pour les signes vitaux
- TextArea pour les textes longs
- ComboBox pour les sélections

## 🚀 Utilisation

### Démarrage
1. Ouvrir la page clinical-notes.fxml
2. Les données du patient sont chargées automatiquement
3. Remplir le formulaire SOAP
4. Ajouter médicaments/examens si nécessaire
5. Cliquer sur "Enregistrer"

### Raccourcis
- **+ Nouvelle note**: Créer une nouvelle note vierge
- **+ Nouvelle ordonnance**: Ajouter rapidement un médicament
- **Ajouter diagnostic**: Cliquer sur le bouton "Ajouter"

## 🔧 Extensibilité

Le contrôleur peut être étendu pour:
- Charger les données depuis la base de données
- Sauvegarder les notes en base de données
- Imprimer les notes
- Générer des rapports PDF
- Synchroniser avec des systèmes externes

## 📝 Notes de développement

- Les médicaments et examens sont ajoutés/supprimés dynamiquement
- Les diagnostics sont gérés via des tags supprimables
- Les valeurs par défaut des vitaux peuvent être modifiées
- La page est responsive et scrollable

## ✅ Prochaines étapes

1. Intégrer la sauvegarde en base de données
2. Ajouter le chargement de notes existantes
3. Implémenter l'historique des notes
4. Ajouter la validation des formulaires
5. Générer des PDF/exports


