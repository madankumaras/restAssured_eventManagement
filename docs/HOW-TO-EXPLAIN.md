# 🗣️ How to Explain This Framework

## The 30-Second Pitch

> "This is an advanced RestAssured test suite for the EventHub API.
> It mirrors the folder structure and test philosophy of our Playwright suite —
> so both teams share the same mental model. Tests are organized by concern:
> API unit tests, integration workflows, performance SLA checks, and a full end-to-end journey.
> Allure gives us beautiful HTML reports, and the whole thing runs on CI in GitHub Actions."

---

## Walking Through the Project (Interview Demo Order)

### 1. Start with `testng.xml`
> "This is our test runner config. It organises tests into four suites:
> API, Integration, Performance, and E2E — exactly mirroring the Playwright structure."

### 2. Show `TestBase.java`
> "All test classes extend this. It sets up RestAssured's base URI, content type,
> the Allure filter for capturing requests, and pre-fetches the auth token.
> This means zero boilerplate in individual test files."

### 3. Show `ApiClient.java`
> "This is our Java equivalent of `APIUtils.js`. Every API call in the whole suite
> goes through this class. `createEvent()`, `createBooking()`, `cancelBooking()` —
> all centralised. If the API path changes, we fix it once."

### 4. Show `TestDataGenerator.java`
> "Javafaker gives us realistic random test data. `newEvent()` returns a different
> event payload every time — preventing test pollution and hard-coded dependencies."

### 5. Show `AuthApiTest.java`
> "Here we cover registration, login, duplicate email rejection, wrong-password,
> and token validation — both valid and invalid. Public endpoints are tested
> with and without auth headers."

### 6. Show `BookingsApiTest.java`
> "We assert the business rules: totalPrice = price × quantity, booking references
> start with `EVT-`, seat counts decrement correctly, and cancellation restores them.
> Cleanup happens in `@AfterClass` so tests don't leave orphan data."

### 7. Show `FullJourneyTest.java`
> "Seven sequential steps using `dependsOnMethods`. If step 3 fails, steps 4–7
> are automatically skipped. This simulates the complete user journey:
> register → login → create event → book → verify → cancel → delete."

### 8. Show Allure Report
> ```bash
> mvn test && mvn allure:report
> open target/site/allure-maven-plugin/index.html
> ```
> "Every HTTP request and response is captured automatically by the AllureRestAssured filter.
> No extra code needed — it just works."

---

## Common Questions You'll Be Asked

| Question | Quick Answer |
|---|---|
| Why RestAssured over other tools? | BDD syntax, first-class Java support, TestNG integration |
| How do you isolate tests? | Fresh random data via Faker + API-driven cleanup in @AfterClass |
| How do you handle auth? | `getToken()` in TestBase, passed as `Bearer` header in ApiClient |
| How do you run on CI? | `mvn test` triggered by GitHub Actions on push/PR |
| How do you generate reports? | Allure filter + `mvn allure:report` |
| What's in the E2E test? | 7 sequential steps covering the full user journey |
