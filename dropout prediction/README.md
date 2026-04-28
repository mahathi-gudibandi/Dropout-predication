# Student Dropout Prediction System

A full-stack web application to predict student dropout risk using academic, behavioral, financial, and psychological data.

## Tech Stack
- **Frontend**: HTML, CSS, JavaScript
- **Backend**: Java Spring Boot (REST API + JWT Auth)
- **Database**: MySQL
- **ML Service**: Python Flask (Decision Tree)

---

## Project Structure
```
student-dropout-prediction/
├── backend/          Spring Boot app
├── frontend/         Static HTML/CSS/JS
├── ml-service/       Python Flask ML API
└── database/         SQL schema
```

---

## Setup & Run

### 1. Database
```sql
-- Create DB and run schema
mysql -u root -p < database/schema.sql
```
Update `backend/src/main/resources/application.properties` with your MySQL credentials.

### 2. Backend (Spring Boot)
```bash
cd backend
./mvnw spring-boot:run
# Runs on http://localhost:8080
```

### 3. ML Service (Python Flask)
```bash
cd ml-service
pip install -r requirements.txt
python app.py
# Runs on http://localhost:5000
```

### 4. Frontend
Open `frontend/index.html` in a browser, or serve with:
```bash
cd frontend
npx serve .
# or use VS Code Live Server
```

---

## API Endpoints

| Method | Endpoint           | Auth     | Description              |
|--------|--------------------|----------|--------------------------|
| POST   | /api/auth/signup   | No       | Register new user        |
| POST   | /api/auth/login    | No       | Login, returns JWT       |
| POST   | /api/addStudent    | JWT      | Add student + predict    |
| GET    | /api/students      | JWT      | List all students        |
| GET    | /api/students/{id} | JWT      | Get student by ID        |
| POST   | /api/predict       | JWT      | Predict without saving   |

---

## Prediction Logic

Risk score is calculated from weighted factors:

| Factor              | Max Weight |
|---------------------|-----------|
| Attendance          | 25        |
| GPA                 | 20        |
| Behavior Score      | 10        |
| Engagement Level    | 10        |
| Fee Status          | 10        |
| Family Income       | 10        |
| Stress Level        | 10        |
| Scholarship         | 5         |
| Counseling + Stress | 5         |

- Score < 35 → **LOW**
- Score 35–65 → **MEDIUM**
- Score > 65 → **HIGH**
