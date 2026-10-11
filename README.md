# Pocket Mentor: Backend

REST API for **Pocket Mentor**, an AI-powered study companion that turns class notes into summaries, flashcards and quizzes, and detects *blind spots* (answers a student was sure about but got wrong).

- **Live API:** `https://pocketmentor-d724.onrender.com`
- **Frontend repo:** `https://github.com/sumanmondal02/pocketmentor-frontend`
- **Developer:** Suman Mondal

## Features
- Register and login with JWT authentication and BCrypt password hashing
- Upload a PDF or TXT file, or paste text; text is extracted and cleaned
- AI generation (Google Gemini): 60-second summary, topics, flashcards, multiple-choice quiz
- Asynchronous generation: returns `202 Accepted`, the client polls a status endpoint
- Quiz with confidence rating (`SURE`, `NOT_SURE`, `GUESSING`) and server-side scoring
- Blind-spot detection: `blindSpot = !correct && confidence == SURE`
- Progress: accuracy by topic, mastery level (`BEGINNER` / `LEARNING` / `MASTERED`), quiz history
- Focused retake quiz built from the questions the student got wrong
- Profile (rename, change password), rate limiting, global JSON error handling

## Tech stack
| Layer | Technology |
|---|---|
| Language / framework | Java 21, Spring Boot 4.1.1 (Maven) |
| Security | Spring Security, JWT (jjwt), BCrypt |
| Persistence | Spring Data JPA, Hibernate |
| Database | TiDB Cloud Serverless (MySQL-compatible) |
| PDF parsing | Apache PDFBox |
| AI | Google Gemini API via Spring `RestClient` |
| Deployment | Docker on Render |

## Architecture (MVC)
```
React (View)  ->  Controller  ->  Service  ->  Repository  ->  TiDB (Model)
```
```
com.pocketmentor.pocketmentor
 config/       SecurityConfig, AsyncConfig
 controller/   Auth, Note, Quiz, Progress, Profile, Ping
 service/      Auth, Note, Generation, Quiz, Progress, GeminiAi, RateLimiter, JobRegistry
 repository/   Spring Data JPA repositories
 model/        User, Note, Summary, Topic, Flashcard, Quiz, Question, QuizAttempt, AttemptAnswer
 dto/          Request/response records (entities are never exposed)
 security/     JwtUtil, JwtAuthFilter
 exception/    GlobalExceptionHandler
```

## Database
Tables are created automatically by Hibernate (`ddl-auto=update`):
`users`, `notes`, `summaries`, `topics`, `flashcards`, `quizzes`, `questions`, `quiz_attempts`, `attempt_answers`.

## API endpoints
All routes except those marked *public* need `Authorization: Bearer <token>`.

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Create account (*public*) |
| POST | `/api/auth/login` | Login, returns JWT (*public*) |
| POST | `/api/notes/text` | Save pasted text |
| POST | `/api/notes/upload` | Upload PDF or TXT (multipart) |
| GET | `/api/notes` | List notes with status and counts |
| GET / DELETE | `/api/notes/{id}` | Read / delete a note |
| POST | `/api/notes/{id}/generate` | Start AI generation (202) |
| GET | `/api/notes/{id}/status` | `IDLE`, `PROCESSING`, `COMPLETED`, `FAILED` |
| GET | `/api/notes/{id}/content` | Summary, topics, latest quiz id |
| GET | `/api/notes/{id}/flashcards` | Flashcards |
| POST | `/api/notes/{id}/retake-weak` | Quiz of previously wrong questions |
| GET | `/api/quizzes/{id}` | Questions (correct answers hidden) |
| POST | `/api/quizzes/{id}/submit` | Submit answers with confidence |
| GET | `/api/attempts/{id}` | Attempt result |
| GET | `/api/blindspots` | Recheck list |
| GET | `/api/progress` | Topic accuracy, mastery, history |
| GET / PUT | `/api/profile` | View / rename |
| PUT | `/api/profile/password` | Change password |
| GET | `/api/ping` | Health check including database (*public*) |
| GET | `/api/hello` | Server check (*public*) |

## Security
- Passwords stored as BCrypt hashes; stateless JWT with expiry
- Every query is scoped to the logged-in user (users cannot read each other's notes)
- Correct answers are never sent to the client before submission
- Input validation, 10 MB upload limit, file-type checks
- CORS restricted to the frontend URL; secrets only in environment variables
- Rate limit on generation (10 per hour per user)

## Run locally
**Requirements:** Java 21, a MySQL-compatible database (TiDB Cloud free tier works), a Gemini API key.

1. Create `src/main/resources/application-local.properties` (it is git-ignored):
   ```properties
   spring.datasource.url=jdbc:mysql://HOST:4000/pocketmentor?sslMode=VERIFY_IDENTITY
   spring.datasource.username=YOUR_USER
   spring.datasource.password=YOUR_PASSWORD
   app.jwt.secret=a-long-random-string-of-at-least-32-characters
   ai.api-key=YOUR_GEMINI_KEY
   ai.model=gemini-flash-lite-latest
   ```
2. Start the app:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```
3. Open `http://localhost:8080/api/ping`; it should return `{"status":"ok"}`.

## Environment variables (production)
| Variable | Purpose |
|---|---|
| `DB_URL` | JDBC URL of the database |
| `DB_USER` / `DB_PASS` | Database credentials |
| `JWT_SECRET` | JWT signing secret (32+ characters) |
| `AI_API_KEY` | Gemini API key |
| `FRONTEND_URL` | Exact frontend origin, used for CORS |
| `PORT` | Set by Render |

## Deployment
Built from the included `Dockerfile` and deployed on Render (free tier).

## Known limitations
- Generation job status is held in memory, so a server restart clears a running job
- No email-based password reset