# Wellora - Connected Health & Wellness (Desktop)

## 🌟 Overview
**Wellora** is an integrated desktop application designed to centralize health monitoring, fitness planning, and medical consultations. Developed as part of the PIDEV project at **Esprit School of Engineering** (Academic Year 2025-2026), this Java-based platform offers a seamless experience for patients, doctors, coaches, and nutritionists.

---

## 🚀 Key Modules & Features

### 👤 User Management & Security
- **Authentication**: Secure login/registration with **Google OAuth2** and **2FA** (Two-Factor Authentication).
- **Security**: Password hashing using **BCrypt** and role-based access control (RBAC).
- **Profile Management**: Customizable user profiles for Doctors, Patients, Coaches, and Nutritionists.
- **Admin Panel**: User verification queue and system-wide management.

### 🏋️ Fitness & AI Coaching
- **Workout Planner**: Create personalized exercise routines.
- **Exercise Library**: Access to a database of exercises with detailed instructions and videos.
- **AI Coach**: Intelligent coaching using integrated AI (OpenAI/Gemini) to provide personalized fitness advice.
- **Fitness Dashboard**: Real-time tracking of physical activity and progress.

### 🥗 Nutrition & Dietetics
- **Meal Planning**: Automated daily and weekly meal plans.
- **Recipe Discovery**: Suggestions based on dietary goals.
- **Apport Tracker**: Monitor caloric and macronutrient intake.

### 🏃‍♂️ Health Paths (Parcours de Santé)
- **Interactive Trails**: Create and manage health-focused walking/running paths.
- **Geolocation**: View paths near your current location.
- **Weather Integration**: Real-time weather data for selected paths.
- **Social Features**: Comment, rate, and share experiences on specific paths.

### 📊 Health Journal & Analysis
- **Daily Journal**: Track mood, sleep, and overall wellness.
- **Health Indicators**: Log blood pressure, weight, and blood sugar levels.
- **Evolution Reports**: Visualize progress through interactive charts and PDF exports.

### 🩺 Consultations & Medical Monitoring
- **Booking System**: Online appointment scheduling with specialized professionals.
- **Video Calls**: Integrated interface for remote medical consultations.
- **Digital Prescriptions**: Manage and view prescriptions directly within the app.

---

## 🛠 Tech Stack
- **Language**: Java 17+
- **Framework**: JavaFX 17/21
- **Build Tool**: Apache Maven
- **Database**: MySQL (with JDBC & Hibernate)
- **Styling**: CSS (Modern themes with dark/light mode support)
- **External APIs**: Google Auth, OpenWeatherMap, OpenAI/Gemini
- **Libraries**: ControlsFX, MapJFX, Jackson (JSON), BCrypt

---

## 📂 Project Structure
- `src/main/java/com/wellcare`: Core application logic, Fitness, and Auth modules.
- `src/main/java/com/wellora`: Health Journal, Nutrition, and Parcours modules.
- `src/main/resources/fxml`: UI layouts designed in SceneBuilder.
- `src/main/resources/css`: Modern styling and theme management.

---

## ⚙️ Installation & Running

### Prerequisites
- **Java JDK 17** or higher.
- **Maven** (included in the root as `apache-maven-3.9.6`).
- **MySQL Server** (XAMPP/WAMP recommended).

### Running from Terminal
1. **Clone the repository**:
   ```bash
   git clone [URL_DU_DEPOT]
   cd pi
