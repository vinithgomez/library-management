# Library Management System (Spring Boot)

REST API built with Java 17, Spring Boot 3.3.5 and Maven.
Three-layer architecture: Controller -> Service -> Repository -> H2 database.

## How to Run Locally

**Prerequisites:** JDK 17 or newer (`java -version`) and an IDE (IntelliJ IDEA / Eclipse STS / VS Code with the Java + Spring extensions). The IDE bundles Maven; otherwise install Maven 3.9+ (`mvn -v`).

### Option A - IDE (recommended)
1. Unzip the file.
2. IntelliJ: File > Open > select the `library-management` folder (the one with `pom.xml`). Eclipse: File > Import > Existing Maven Projects.
3. Wait for Maven to download dependencies (first time needs internet).
4. Open `LibraryManagementApplication.java` and click the green Run button.
5. Console should show `Tomcat started on port 8080` and `Started LibraryManagementApplication`.

### Option B - Terminal
```bash
cd library-management
mvn spring-boot:run
```

### Run the automated tests
```bash
mvn test
```
(or in the IDE: right-click `src/test` > Run Tests)

### Try the API (app must be running)
Open in the browser: http://localhost:8080/api/books

Other calls (curl, Postman, or Thunder Client):
```bash
# list members / loans
curl http://localhost:8080/api/members
curl http://localhost:8080/api/loans

# borrow a book (member 1, book 1)  -> 201 Created
curl -X POST http://localhost:8080/api/loans/borrow -H "Content-Type: application/json" -d "{\"memberId\":1,\"bookId\":1}"

# return loan 1  -> 200 OK with fine
curl -X POST http://localhost:8080/api/loans/1/return

# add a book
curl -X POST http://localhost:8080/api/books -H "Content-Type: application/json" -d "{\"title\":\"Java Concurrency in Practice\",\"author\":\"Brian Goetz\",\"isbn\":\"9780321349606\",\"totalCopies\":2}"
```
Windows CMD note: the escaped quotes above work in CMD; in PowerShell use Postman or `Invoke-RestMethod`.

H2 database console: http://localhost:8080/h2-console  (JDBC URL `jdbc:h2:mem:librarydb`, user `sa`, empty password).
Data is in memory, so it resets every restart.

### Sample data loaded at startup
Books: 1 Clean Code (3), 2 Effective Java (2), 3 Head First Java (2), 4 Spring in Action (1), 5 Introduction to Algorithms (0 available - out of stock).
Members: 1 Arun Kumar, 2 Priya Sharma.

## REST endpoints
| Method | URL | Purpose |
|---|---|---|
| GET | /api/books | list books |
| GET | /api/books/{id} | one book |
| POST | /api/books | add book |
| GET | /api/members | list members |
| GET | /api/members/{id} | one member |
| POST | /api/members | register member |
| GET | /api/loans | all loans |
| GET | /api/loans/member/{memberId} | loans of a member |
| POST | /api/loans/borrow | borrow a book |
| POST | /api/loans/{loanId}/return | return a book |

## Business rules (Service layer)
1. A book can be borrowed only if a copy is available (else 409).
2. A member can hold at most 3 books at once (else 409).
3. Member email must be unique (else 409).
4. Due date = 14 days after borrowing; late return fine = Rs. 5 per day overdue.

## Deploy (Render, free tier)
1. Push the `library-management` folder to a GitHub repository (the `pom.xml` and `Dockerfile` must be at the repo root).
2. On render.com: New > Web Service > connect the repo.
3. Runtime: **Docker** (it detects the Dockerfile). Instance type: Free. No environment variables needed.
4. Click Create Web Service. After the build, open `https://<your-service>.onrender.com/api/books`.

Notes: the free tier sleeps after ~15 minutes idle (first request is slow), and the H2 data resets on every restart.
