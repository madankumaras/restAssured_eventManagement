# 🏗️ Framework Architecture Documentation

## Design Philosophy

This framework is built on the same three core principles as the companion Playwright suite:

1. **Separation of Concerns** — Tests, models, utilities, and base config are in isolated layers
2. **API-First** — All test data created and cleaned up purely via REST API
3. **Convention over Configuration** — Consistent naming, folder structure, and annotation patterns

---

## Project Structure

```
qa-RestAssured-eventManagement-test-Suite/
├── src/
│   ├── main/java/com/eventmanagement/
│   │   ├── models/                     ← POJO request bodies (Lombok @Data @Builder)
│   │   │   ├── UserPayload.java
│   │   │   ├── EventPayload.java
│   │   │   └── BookingPayload.java
│   │   └── utils/
│   │       ├── ApiClient.java          ← REST API wrapper (mirrors APIUtils.js)
│   │       ├── TestDataGenerator.java  ← Faker-based data factory (mirrors TestDataGenerator.js)
│   │       └── ConfigReader.java       ← Reads config.properties
│   └── test/
│       ├── java/com/eventmanagement/
│       │   ├── base/
│       │   │   └── TestBase.java       ← RestAssured global config + auth (mirrors fixtures/)
│       │   └── tests/
│       │       ├── api/                ← mirrors tests/api/
│       │       │   ├── AuthApiTest.java
│       │       │   ├── EventsApiTest.java
│       │       │   ├── BookingsApiTest.java
│       │       │   └── HealthApiTest.java
│       │       ├── integration/        ← mirrors tests/integration/
│       │       │   └── ApiIntegrationTest.java
│       │       ├── performance/        ← mirrors tests/performance/
│       │       │   └── PerformanceTest.java
│       │       └── e2e/                ← mirrors tests/e2e/
│       │           └── FullJourneyTest.java
│       └── resources/
│           └── config.properties       ← mirrors .env
├── docs/
│   ├── ARCHITECTURE.md
│   ├── INTERVIEW-QA.md
│   └── HOW-TO-EXPLAIN.md
├── .github/workflows/ci.yml            ← mirrors .github/workflows/
├── pom.xml
├── testng.xml
└── README.md
```

---

## Data Flow

```
┌──────────────────────────────────────────────────────────┐
│                     Test Execution                        │
│                                                          │
│  @Test                                                   │
│  public void shouldCreateEvent() {                       │
│    EventPayload p = TestDataGenerator.newEvent(...);     │
│    Response res = api.createEvent(authToken, p);         │
│    assertEquals(res.statusCode(), 201);                  │
│  }                                                       │
└─────────────────────┬────────────────────────────────────┘
                      │
         ┌────────────┼──────────────┐
         ▼            ▼              ▼
  ┌────────────┐ ┌──────────┐ ┌──────────────┐
  │  TestBase  │ │ ApiClient│ │TestDataGen.  │
  │            │ │          │ │              │
  │ @BeforeClass│ │ wraps    │ │ generates    │
  │ • requestSpec│ │ given()  │ │ Faker-based  │
  │ • authToken│ │ .post()  │ │ payloads     │
  └────────────┘ └──────────┘ └──────────────┘
         │            │              │
         └────────────┼──────────────┘
                      │
                      ▼
           ┌─────────────────────┐
           │  EventHub Platform  │
           │  REST API           │
           │  (api.eventhub.*)   │
           └─────────────────────┘
```

---

## Layer Descriptions

### 1. Test Layer (`tests/`)
- Organized by concern: `api/`, `integration/`, `performance/`, `e2e/`
- Each test class extends `TestBase` — no RestAssured boilerplate in tests
- Allure `@Epic/@Feature/@Story/@Severity` annotations on every test

### 2. Utility Layer (`utils/`)
- `ApiClient.java` — Stateless class wrapping all REST calls with JWT token support
- `TestDataGenerator.java` — Factory pattern using Javafaker
- `ConfigReader.java` — Properties file reader (driven by `config.properties`)

### 3. Model Layer (`models/`)
- Lombok `@Data @Builder @JsonInclude` POJOs for request bodies
- Jackson handles serialization automatically via RestAssured

### 4. Base Layer (`base/TestBase.java`)
- Configures RestAssured `RequestSpecification` globally
- Attaches Allure and logging filters
- Pre-fetches auth token for all test subclasses

---

## Naming Conventions

| Type | Pattern | Example |
|------|---------|---------|
| Test class | `PascalCase + Test` | `AuthApiTest`, `FullJourneyTest` |
| Test method | `camelCase + should...` | `shouldCreateEventSuccessfully()` |
| API method | `verb + Resource` | `createEvent()`, `getBookings()` |
| Model | `PascalCase + Payload` | `EventPayload`, `BookingPayload` |
| Config key | `dot.separated` | `api.url`, `user.email` |

---

## Test Isolation Strategy

```
Before Each Test Class (@BeforeClass):
  ├── RequestSpecification is configured (baseURI, content-type, filters)
  ├── Auth token fetched from config.properties credentials
  └── Resource-specific setup (e.g. create test event for bookings)

After Each Test Class (@AfterClass):
  └── API-driven cleanup (deleteEvent, cancelBooking) using collected IDs
```
