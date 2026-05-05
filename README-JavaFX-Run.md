# WellCare Healthcare Management System - JavaFX Sprint 2

## 🚀 How to Run the JavaFX Application

### Prerequisites

Before running the WellCare JavaFX application, you need one of the following setups:

#### Option 1: JavaFX SDK (Recommended)
1. Download JavaFX SDK 17+ from [Gluon JavaFX](https://gluonhq.com/products/javafx/)
2. Extract to a folder (e.g., `C:\javafx-sdk-17.0.2`)

#### Option 2: IDE with JavaFX Support
- **IntelliJ IDEA**: Built-in JavaFX support
- **Eclipse**: With e(fx)clipse plugin
- **NetBeans**: JavaFX support included

#### Option 3: Maven with JavaFX Plugin
Configure Maven to handle JavaFX runtime automatically.

---

## 🏃‍♂️ Running Methods

### Method 1: Command Line with JavaFX SDK

1. **Download and Install JavaFX SDK:**
   ```bash
   # Download from: https://gluonhq.com/products/javafx/
   # Extract to: C:\javafx-sdk-17.0.2
   ```

2. **Compile the Application:**
   ```bash
   cd C:\Users\lenovo\Desktop\pijava\wellora

   # Compile with JavaFX in module path
   javac --module-path "C:\javafx-sdk-17.0.2\lib" --add-modules javafx.controls,javafx.fxml -cp "target\classes;C:\Users\lenovo\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar;C:\Users\lenovo\.m2\repository\com\google\protobuf\protobuf-java\3.25.1\protobuf-java-3.25.1.jar;C:\Users\lenovo\.m2\repository\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar" src/main/java/com/wellcare/javafx/*.java src/main/java/com/wellcare/javafx/**/*.java -d target/classes
   ```

3. **Run the Application:**
   ```bash
   java --module-path "C:\javafx-sdk-17.0.2\lib" --add-modules javafx.controls,javafx.fxml -cp "target\classes;C:\Users\lenovo\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar;C:\Users\lenovo\.m2\repository\com\google\protobuf\protobuf-java\3.25.1\protobuf-java-3.25.1.jar;C:\Users\lenovo\.m2\repository\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar" com.wellcare.javafx.MainApplication
   ```

### Method 2: IntelliJ IDEA (Easiest)

1. **Open Project in IntelliJ IDEA:**
   - File → Open → Select `wellora` folder

2. **Configure JavaFX Library:**
   - File → Project Structure → Libraries
   - Add → Java → Select JavaFX SDK folder
   - Apply and OK

3. **Run Configuration:**
   - Run → Edit Configurations
   - Add new Application configuration
   - Main class: `com.wellcare.javafx.MainApplication`
   - VM options: `--module-path "C:\javafx-sdk-17.0.2\lib" --add-modules javafx.controls,javafx.fxml`
   - Working directory: `$PROJECT_DIR$`
   - Run

### Method 3: Maven with JavaFX Plugin

Add to `pom.xml`:
```xml
<build>
    <plugins>
        <!-- JavaFX Maven Plugin -->
        <plugin>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-maven-plugin</artifactId>
            <version>0.0.8</version>
            <configuration>
                <mainClass>com.wellcare.javafx.MainApplication</mainClass>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Run with Maven:
```bash
mvn clean compile javafx:run
```

---

## 🎯 What You'll See

### 1. Login Screen
- Professional medical-themed login page
- Email/password validation
- Registration link
- Loading states

### 2. Patient Dashboard (Default)
- Welcome message with personalized greeting
- Statistics cards (Appointments, Prescriptions, Messages)
- Upcoming appointments list
- Recent activity feed
- Navigation sidebar

### 3. Admin Dashboard
- System overview with metrics
- User distribution charts
- System alerts
- Quick actions

### 4. User Management (Admin)
- Advanced data table with sorting/filtering
- Search functionality
- Bulk operations
- Pagination

---

## 🎨 Design Features

- **Medical Theme**: Blue (#667eea) to Purple (#764ba2) gradients
- **Typography**: Segoe UI for medical readability
- **Icons**: Medical emoji (🏥 ❤️ 🩺 💊 📅)
- **Layout**: Card-based design with shadows
- **Interactive**: Hover effects, validation feedback

---

## 🔧 Troubleshooting

### Common Issues:

1. **"JavaFX runtime not found"**
   - Ensure JavaFX SDK is properly installed
   - Check module path configuration

2. **"Class not found" errors**
   - Verify classpath includes all dependencies
   - Check Maven dependencies are downloaded

3. **Compilation errors**
   - Ensure Java 17+ is used
   - Check JavaFX version compatibility

4. **Runtime errors**
   - Verify FXML files are in correct location
   - Check CSS file paths

---

## 📋 System Requirements

- **Java**: JDK 17 or higher
- **JavaFX**: SDK 17.0.2 or compatible
- **Memory**: 512MB minimum
- **Display**: 1200x800 minimum resolution

---

## 🚀 Quick Start Commands

```bash
# 1. Backend Test (Always works)
java -cp "target\classes;..." com.wellcare.javafx.TestBackend

# 2. With JavaFX SDK
java --module-path "C:\javafx-sdk-17.0.2\lib" --add-modules javafx.controls,javafx.fxml -cp "target\classes;..." com.wellcare.javafx.MainApplication

# 3. Via Maven (if plugin configured)
mvn clean compile javafx:run
```

---

## 📞 Support

If you encounter issues:
1. Verify JavaFX SDK installation
2. Check Java version (17+)
3. Ensure all dependencies are downloaded
4. Try running in IntelliJ IDEA first

**WellCare JavaFX Application - Ready to Run! 🏥✨**