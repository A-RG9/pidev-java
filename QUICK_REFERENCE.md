# ⚡ Quick Reference - WellCare Navigation

## 🚀 Commandes Essentielles

```bash
# Compiler l'application
mvn clean compile -DskipTests

# Lancer l'application
mvn javafx:run

# Créer le JAR
mvn package -DskipTests

# Nettoyer (supprimer le /target)
mvn clean

# Compiler + Lancer
mvn clean compile -DskipTests && mvn javafx:run
```

## 📍 Fichiers Clés

```
/fxml/
├── sidebar.fxml ...................... Sidebar partagé
├── appointments.fxml ................. Page rendez-vous
├── doctor-search.fxml ................ Recherche médecin
├── doctor-profile.fxml ............... Profil médecin
├── booking.fxml ...................... Réservation
├── lab-results.fxml .................. Résultats labo
└── prescriptions.fxml ................ Ordonnances

/controller/
├── SidebarController.java ............ Navigation (NOUVEAU ✨)
├── AppointmentsController.java ....... Page rendez-vous
├── DoctorSearchController.java ....... Recherche médecin
└── ...

/resources/
└── styles.css ........................ Stylesheets

/java/org/example/
├── WellCareApp.java .................. Point d'entrée
└── Main.java
```

## 🎮 Navigation Menu

| Menu | Destination | FXML |
|------|-------------|------|
| Tableau de bord | Rendez-vous | appointments.fxml |
| Mes Rendez-vous | Rendez-vous | appointments.fxml |
| Trouver Médecin | Médecin | doctor-search.fxml |
| Profil Médecin | Médecin | doctor-profile.fxml |
| Prendre RDV | Réservation | booking.fxml |
| Résultats Labo | Santé | lab-results.fxml |
| Ordonnances | Santé | prescriptions.fxml |

## 🔧 Architecture

```
┌─ SidebarController.java
│  ├─ handleMenuClick() → navigateTo()
│  ├─ toggleSubmenu() → bascule sous-menu
│  └─ navigateTo() → charge FXML + CSS
│
└─ sidebar.fxml (inclus partout)
   ├─ <fx:controller="SidebarController">
   └─ <fx:include> dans chaque page
```

## ✅ Vérifications

### Compilation réussie?
```bash
mvn compile -DskipTests
# ✓ BUILD SUCCESS
```

### SidebarController existe?
```bash
ls src/main/java/org/example/controller/SidebarController.java
# ✓ Fichier existe
```

### sidebar.fxml configuré?
```bash
grep "fx:controller" src/main/resources/fxml/sidebar.fxml
# ✓ fx:controller="org.example.controller.SidebarController"
```

### Application lance?
```bash
mvn javafx:run
# ✓ La fenêtre s'ouvre
```

## 🐛 Dépannage Rapide

### Navigation ne fonctionne pas
- [ ] Vérifier les logs console
- [ ] Vérifier que le fichier FXML existe
- [ ] Vérifier le texte du bouton (case-sensitive!)
- [ ] Recompiler: `mvn clean compile`

### Sidebar pas visible
- [ ] Vérifier que `<fx:include source="sidebar.fxml" />` est dans le FXML
- [ ] Vérifier que le HBox inclut la sidebar
- [ ] Vérifier les stylesheets

### CSS ne s'applique pas
- [ ] Vérifier que styles.css existe
- [ ] Vérifier le chemin: `/css/styles.css`
- [ ] Redémarrer l'application

### Submenu ne s'ouvre pas
- [ ] Vérifier que `fx:id="submenuContainer"` existe
- [ ] Vérifier que `visible="false" managed="false"` est défini
- [ ] Vérifier le bouton `onAction="#toggleSubmenu"`

## 📝 Codes de Réponse Console

```
"Menu clicked: Trouver un Médecin" .......... ✓ Clic détecté
"Navigated to: doctor-search.fxml" ......... ✓ Navigation réussie
"Error navigating to X" ..................... ✗ Erreur de navigation
"Cannot get stage for navigation" .......... ✗ Problème de fenêtre
"Unknown menu item: X" ...................... ✗ Texte bouton incorrect
```

## 🎯 Checklist de Déploiement

- [ ] Compilation réussie (`BUILD SUCCESS`)
- [ ] JAR créé (`wellora-1.0-SNAPSHOT.jar`)
- [ ] Application lance sans erreur
- [ ] Sidebar visible au démarrage
- [ ] Clic sur menu → navigation fonctionne
- [ ] Sous-menu Rendez-vous se dérouле
- [ ] Retour en arrière → sidebar toujours là
- [ ] CSS appliqué sur chaque page
- [ ] Aucune erreur dans la console

## 📞 Points de Contact du Code

| Problème | Fichier | Ligne |
|----------|---------|-------|
| Navigation ne fonctionne | SidebarController.java | 20-63 |
| Submenu ne s'ouvre pas | SidebarController.java | 65-72 |
| Page ne change pas | SidebarController.java | 74-110 |
| Sidebar pas trouvée | sidebar.fxml | 8-12 |
| Contrôleur pas chargé | sidebar.fxml | 11 |

## 🔄 Cycle de Développement

1. **Modification code** → 2. **Compiler** → 3. **Lancer** → 4. **Tester** → 5. **Déboguer**

```bash
# Cycle complet
mvn clean compile && mvn javafx:run
```

## 📊 Métriques

| Métrique | Valeur |
|----------|--------|
| Fichiers Java modifiés | 1 |
| Fichiers FXML modifiés | 1 |
| Lignes de code ajoutées | ~125 |
| Temps de navigation | < 100ms |
| Pages navigables | 7 |
| Éléments de menu | 10 |

## 🎨 Couleurs et Styles

```css
/* Primary Colors */
--primary: #00A790 (Teal)
--gray-100: #f9fafb (Light gray)
--gray-700: #374151 (Dark gray)

/* Button Colors */
-fx-background-color: #00A790 (Active)
-fx-background-color: transparent (Inactive)
-fx-text-fill: #00A790 (Text)
```

## 📱 Dimensions

- **Fenêtre:** 1200 x 800 px
- **Sidebar:** 280 px (fixe)
- **Contenu:** Flexible
- **Padding:** 32-48 px

## 🔐 Contrôles d'Accès

```java
// Tous les contrôles @FXML sont publics
@FXML
public void handleMenuClick(ActionEvent event)

@FXML
public void toggleSubmenu(ActionEvent event)

// Les méthodes de navigation sont privées
private void navigateTo(String fxmlFile, String title)
private void handleLogout()
```

## 📦 Dépendances JavaFX

```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>21</version>
</dependency>
```

## 🚨 Erreurs Courantes

| Erreur | Cause | Solution |
|--------|-------|----------|
| `NullPointerException` | Stage null | Vérifier que la scene existe |
| `FileNotFoundException` | FXML pas trouvé | Vérifier le chemin `/fxml/` |
| `Cannot determine type` | Property invalide | Vérifier les noms @FXML |
| `ParseError` | XML invalide | Valider le XML du FXML |

## ✨ Bonnes Pratiques

- ✓ Utiliser FXMLLoader pour charger les pages
- ✓ Inclure sidebar.fxml dans chaque page
- ✓ Utiliser @FXML pour lier le contrôleur
- ✓ Vérifier null avant utiliser Stage
- ✓ Appliquer CSS après créer Scene
- ✓ Afficher logs pour déboguer
- ✓ Gérer les exceptions

## 🚀 Performance Tips

- Cache les FXML si besoin
- Utilise Platform.runLater() pour UI updates
- Évite les threads bloquants dans UI
- Lazy load les pages complexes

## 📚 Ressources Utiles

- JavaFX Documentation: https://openjfx.io/
- FXML Guide: https://docs.oracle.com/javase/8/javafx/
- Maven Guide: https://maven.apache.org/

---

**Gardez ce fichier à proximité pour un accès rapide aux commandes et configurations!**

