@echo off
echo Compiling all Java files...

javac --module-path "javafx-sdk-17.0.2\lib" --add-modules javafx.controls,javafx.fxml -cp "bcrypt-0.10.2.jar;bytes-1.5.0.jar;mysql-connector-j-8.3.0.jar;src\main\resources" -d out ^
src/main/java/com/wellcare/Main.java ^
src/main/java/com/wellcare/javafx/MainApplication.java ^
src/main/java/com/wellcare/javafx/TestAdminLogin.java ^
src/main/java/com/wellcare/javafx/TestDatabase.java ^
src/main/java/com/wellcare/javafx/TestPasswordHash.java ^
src/main/java/com/wellcare/javafx/TestBackend.java ^
src/main/java/com/wellcare/javafx/controller/admin/UserManagementController.java ^
src/main/java/com/wellcare/javafx/controller/admin/ProfessionalManagementController.java ^
src/main/java/com/wellcare/javafx/controller/admin/VerificationQueueController.java ^
src/main/java/com/wellcare/javafx/controller/auth/LoginController.java ^
src/main/java/com/wellcare/javafx/controller/auth/ProfessionalRegistrationController.java ^
src/main/java/com/wellcare/javafx/controller/auth/ProfessionalTypeChoiceController.java ^
src/main/java/com/wellcare/javafx/controller/auth/RegisterPatientController.java ^
src/main/java/com/wellcare/javafx/controller/dashboard/AdminDashboardController.java ^
src/main/java/com/wellcare/javafx/controller/dashboard/PatientDashboardController.java ^
src/main/java/com/wellcare/javafx/controller/dashboard/DoctorDashboardController.java ^
src/main/java/com/wellcare/javafx/controller/dashboard/CoachDashboardController.java ^
src/main/java/com/wellcare/javafx/controller/dashboard/NutritionistDashboardController.java ^
src/main/java/com/wellcare/javafx/dao/UserDAO.java ^
src/main/java/com/wellcare/javafx/model/User.java ^
src/main/java/com/wellcare/javafx/service/UserService.java ^
src/main/java/com/wellcare/javafx/util/DatabaseConnection.java ^
src/main/java/com/wellcare/javafx/util/MyDataBase.java ^
src/main/java/com/wellcare/javafx/util/PasswordStrengthIndicator.java ^
src/main/java/com/wellcare/javafx/util/SceneManager.java ^
src/main/java/com/wellcare/javafx/util/ValidationUtils.java ^
src/main/java/com/wellcare/ui/LandingController.java

echo Compilation completed.