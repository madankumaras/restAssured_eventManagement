# 🚀 qa-RestAssured-eventManagement-test-Suite

Advanced REST API testing framework for the **EventHub** platform — built with **RestAssured + TestNG + Allure**, mirroring the structure of the companion Playwright suite.

---

## 📁 Project Structure

```
├── src/
│   ├── main/java/com/eventmanagement/
│   │   ├── models/          → UserPayload, EventPayload, BookingPayload (Lombok POJOs)
│   │   └── utils/
│   │       ├── ApiClient.java          → REST API wrapper (mirrors APIUtils.js)
│   │       ├── TestDataGenerator.java  → Javafaker data factory
│   │       └── ConfigReader.java       → config.properties reader
│   └── test/
│       ├── java/com/eventmanagement/
│       │   ├── base/TestBase.java      → RestAssured global config + auth token
│       │   └── tests/
│       │       ├── api/                → Auth, Events, Bookings, Health
│       │       ├── integration/        → Multi-resource workflow tests
│       │       ├── performance/        → SLA / response time tests
│       │       └── e2e/                → Full journey test (7 sequential steps)
│       └── resources/config.properties → Environment configuration
├── docs/
│   ├── ARCHITECTURE.md
│   ├── INTERVIEW-QA.md
│   └── HOW-TO-EXPLAIN.md
├── .github/workflows/ci.yml
├── pom.xml
└── testng.xml
```

---

## ⚙️ Prerequisites

| Tool | Version |
|------|---------|
| Java | 11+ |
| Maven | 3.8+ |

---

## 🔧 Setup

```bash
# 1. Clone the repository
git clone <your-repo-url>
cd qa-RestAssured-eventManagement-test-Suite

# 2. Dependencies are defined in pom.xml — Maven fetches them automatically
mvn dependency:resolve
```

The file `src/test/resources/config.properties` is already configured:
```properties
api.url=https://api.eventhub.rahulshettyacademy.com/api
user.email=mafdangowda8095@gmail.com
user.password=Madan@123
```

---

## ▶️ Running Tests

```bash
# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=HealthApiTest
mvn test -Dtest=AuthApiTest
mvn test -Dtest=EventsApiTest
mvn test -Dtest=BookingsApiTest
mvn test -Dtest=ApiIntegrationTest
mvn test -Dtest=PerformanceTest
mvn test -Dtest=FullJourneyTest

# Run only API tests
mvn test -Dtest="AuthApiTest,EventsApiTest,BookingsApiTest,HealthApiTest"
```

---

## 📊 Allure Report

```bash
# After running tests
mvn allure:report

# Open report
open target/site/allure-maven-plugin/index.html
```

---

## 🧪 Test Coverage

| Suite | Test Class | API Coverage |
|-------|-----------|-------------|
| **API** | `AuthApiTest` | POST /auth/register, POST /auth/login, GET /auth/me |
| **API** | `EventsApiTest` | Full CRUD on /events + filter/search/pagination |
| **API** | `BookingsApiTest` | Full CRUD on /bookings + seat management |
| **API** | `HealthApiTest` | GET /health, GET /config |
| **Integration** | `ApiIntegrationTest` | Cross-resource workflows |
| **Performance** | `PerformanceTest` | SLA validation (< 2s) for all critical endpoints |
| **E2E** | `FullJourneyTest` | Register → Login → Create → Book → Cancel → Delete |

---

## 🏗️ Technology Stack

| Layer | Technology |
|-------|-----------|
| API Testing | RestAssured 5.5.0 |
| Test Runner | TestNG 7.10.2 |
| Reporting | Allure 2.29.0 |
| Serialization | Jackson 2.17.2 |
| Test Data | Javafaker 1.0.2 |
| Boilerplate | Lombok 1.18.34 |
| Build | Maven |

---

## 📖 Docs

- [Architecture](docs/ARCHITECTURE.md) — Framework design and layer descriptions
- [Interview Q&A](docs/INTERVIEW-QA.md) — 20 questions on RestAssured and this framework
- [How to Explain](docs/HOW-TO-EXPLAIN.md) — Demo walkthrough guide

---

## 🔄 CI / CD

Tests run automatically on every push via GitHub Actions.
Add repository secrets: `BASE_URL`, `API_URL`, `USER_EMAIL`, `USER_PASSWORD`.

See [`.github/workflows/ci.yml`](.github/workflows/ci.yml).
