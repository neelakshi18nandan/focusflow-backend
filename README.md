# Focus Flow — Backend Setup Guide
## Spring Boot + MySQL

---

## 📁 What's in this project

```
focusflow/
│
├── pom.xml                          ← Maven build file (dependencies)
├── schema.sql                       ← Run this in MySQL first!
├── focusflow-api.js                 ← Copy to your HTML folder
│
├── index.html                       ← Timer page (updated with DB calls)
├── analysis.html                    ← Analytics page (reads from DB)
│
└── src/main/java/com/focusflow/
    ├── FocusFlowApplication.java    ← Main entry point
    │
    ├── model/
    │   ├── User.java                ← user table
    │   ├── Session.java             ← session table
    │   ├── StudyLog.java            ← study_log table
    │   └── Dto.java                 ← API request/response shapes
    │
    ├── repository/
    │   ├── UserRepository.java
    │   ├── SessionRepository.java
    │   └── StudyLogRepository.java
    │
    ├── service/
    │   ├── UserService.java         ← register + login logic
    │   ├── SessionService.java      ← save session + upsert daily log
    │   └── StudyLogService.java     ← analytics + streak calculation
    │
    ├── controller/
    │   ├── AuthController.java      ← POST /api/auth/register & /login
    │   ├── SessionController.java   ← POST /api/sessions
    │   └── StudyLogController.java  ← GET  /api/logs/{userId}
    │
    └── config/
        ├── SecurityConfig.java      ← BCrypt bean + disable default login page
        └── CorsConfig.java          ← Allow your HTML files to call the API
```

---

## 🚀 Step-by-step Setup

### Step 1 — Install required tools

- **Java 17+** → https://adoptium.net
- **Maven 3.8+** → https://maven.apache.org/download.cgi
- **MySQL 8+** → https://dev.mysql.com/downloads/mysql/

### Step 2 — Create the MySQL database

Open MySQL Workbench (or the mysql command line) and run:

```sql
-- Open schema.sql and run the whole file, OR paste this:
CREATE DATABASE focusflow_db CHARACTER SET utf8mb4;
```

Then run the full `schema.sql` file. This creates the 3 tables:
`user`, `session`, `study_log`

### Step 3 — Configure your database credentials

Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/focusflow_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root          ← your MySQL username
spring.datasource.password=your_password ← your MySQL password
```

### Step 4 — Run the backend

```bash
cd focusflow
mvn spring-boot:run
```

You should see:
```
Started FocusFlowApplication on port 8080
```

### Step 5 — Set up the frontend

Copy these files to your HTML project folder (same folder as style.css, script.js):
- `focusflow-api.js`
- `index.html` (updated version)
- `analysis.html` (updated version)

### Step 6 — Open the app

Open `index.html` with VS Code Live Server (port 5500), or simply open it in your browser.

---

## 🔌 API Endpoints

| Method | URL | What it does |
|--------|-----|-------------|
| `POST` | `/api/auth/register` | Create new account |
| `POST` | `/api/auth/login` | Sign in |
| `POST` | `/api/sessions` | Save a study session |
| `GET`  | `/api/sessions/{userId}` | Get all sessions for user |
| `GET`  | `/api/logs/{userId}` | Get full analytics summary |
| `GET`  | `/api/logs/{userId}/range?from=&to=` | Get logs in date range |

### Example — Register
```json
POST http://localhost:8080/api/auth/register
{
  "username": "neelakshi",
  "email": "n@email.com",
  "password": "mypassword"
}
```

### Example — Save Session
```json
POST http://localhost:8080/api/sessions
{
  "userId": 1,
  "plannedSec": 1500,
  "durationSec": 1320,
  "date": "2026-03-15"
}
```

---

## 🔄 How the data flows

```
User opens app
    ↓
Clicks Sign In → POST /api/auth/login
    ↓ userId saved in sessionStorage
Timer runs (index.html)
    ↓
Clicks "End the Day" → POST /api/sessions
    ↓ backend saves session + auto-updates study_log for today
User opens Analysis page
    ↓
GET /api/logs/{userId} → returns streak, totals, all daily logs
    ↓ charts render with real data
```

---

## ❓ Troubleshooting

| Problem | Fix |
|---------|-----|
| `Connection refused` | Make sure MySQL is running |
| `Access denied for user` | Check username/password in application.properties |
| `CORS error` in browser | Make sure you opened HTML via Live Server on port 5500 |
| `Table doesn't exist` | Re-run schema.sql in MySQL |
| Port 8080 already in use | Change `server.port=8081` in application.properties |
