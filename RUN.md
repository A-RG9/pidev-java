# Running the WellCare JavaFX Application

## Option 1: Run via Maven (Recommended)

The easiest way to run the JavaFX application is via Maven, which handles all JavaFX dependencies automatically:

```bash
mvn clean compile
mvn exec:java
```

This avoids the "JavaFX runtime components are missing" error because Maven uses the classified JARs from your local repository.

## Option 2: Run via IntelliJ Maven Tool Window

1. Open **Maven** tool window (View > Tool Windows > Maven)
2. Expand **Plugins** > **exec**
3. Double-click **exec:java**

## Option 3: Run via IntelliJ Run Button (Requires JavaFX SDK)

If you prefer using IntelliJ's run button, you need to download JavaFX SDK:

### Step 1: Download JavaFX SDK

1. Go to: https://openjfx.io/openjfx-docs/#download
2. Download **JavaFX SDK 21** for **Windows**
3. Extract the ZIP to a location like `C:\javafx-sdk-21`

### Step 2: Configure IntelliJ Run Configuration

1. Go to **Run > Edit Configurations**
2. Click **+** and create a new **Application** (if not already there)
3. Configure:
   - **Main class:** `org.example.WellCareApp`
   - **Use classpath of module:** `wellora`
4. In the **VM options** field, add:
   ```
   --module-path "C:\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics
   ```
   *(Replace C:\javafx-sdk-21 with your actual JavaFX SDK path)*
5. Click **OK** and run

## Troubleshooting

### "JavaFX runtime components are missing" error

This happens when running from IntelliJ's run button without the module path configured.

**Fix:** Use **Option 1** or **Option 2** above (run via Maven), OR configure the VM options as shown in **Option 3**.

### "Could not find main class" error

Make sure:
- The main class is set to `org.example.WellCareApp` in your run configuration
- You ran `mvn clean compile` at least once to build the project