# 📋 Interview Q&A — RestAssured API Testing Framework

## 🔹 RestAssured Fundamentals

**Q1: What is RestAssured and why is it preferred for API testing in Java?**
> RestAssured is a Java DSL (Domain-Specific Language) for testing REST services. It's preferred because:
> - Fluent, BDD-style `given/when/then` syntax makes tests highly readable
> - Built-in JSON/XML path extraction, response validation, and assertion support
> - First-class integration with TestNG, JUnit, and Allure reporting
> - Handles authentication headers, query params, path params, and body serialization elegantly

**Q2: How do you set a base URI globally in RestAssured?**
> ```java
> RestAssured.baseURI = "https://api.eventhub.rahulshettyacademy.com/api";
> ```
> Or via `RequestSpecBuilder`:
> ```java
> RequestSpecification spec = new RequestSpecBuilder()
>     .setBaseUri(baseUri)
>     .setContentType(ContentType.JSON)
>     .build();
> ```

**Q3: What is `RequestSpecification` and why do we use it?**
> It's a reusable configuration object that holds base URI, headers, content type, filters, etc.
> Using it avoids repeating boilerplate in each test:
> ```java
> given(requestSpec).header("Authorization", "Bearer " + token).get("/events");
> ```

**Q4: How do you extract a value from a JSON response?**
> ```java
> String token = response.jsonPath().getString("token");
> int id       = response.jsonPath().getInt("data.id");
> List<String> names = response.jsonPath().getList("data.title");
> ```

**Q5: How do you verify response status code in RestAssured?**
> ```java
> // Using TestNG assertions (our approach):
> assertEquals(response.statusCode(), 201);
>
> // Or inline with RestAssured's then():
> response.then().statusCode(201);
> ```

---

## 🔹 Framework Design

**Q6: Explain the layered architecture of this framework.**
> - **Model layer** — Lombok POJOs for request payloads (serialized as JSON by Jackson)
> - **Utility layer** — `ApiClient` wraps all REST calls; `TestDataGenerator` produces Faker data
> - **Base layer** — `TestBase` configures RestAssured once (spec, token, filters)
> - **Test layer** — Test classes extend TestBase, use ApiClient, assert with TestNG
> - **Config layer** — `config.properties` + `ConfigReader` keep credentials out of code

**Q7: Why do you use `@BeforeClass` instead of `@BeforeMethod` in TestBase?**
> `@BeforeClass` runs once per test class, reducing the number of login/token-fetch calls.
> Each test class shares the same `RequestSpecification` and `authToken`.  
> If per-test isolation is needed (e.g., different users), `@BeforeMethod` would be used.

**Q8: How does your framework handle test data cleanup?**
> Each test class maintains `List<Integer> createdEventIds` / `createdBookingIds`.
> `@AfterClass(alwaysRun = true)` iterates these lists and calls the delete/cancel API.
> `alwaysRun = true` ensures cleanup runs even if tests fail.

**Q9: How is Allure integrated with RestAssured?**
> Via `AllureRestAssured` filter added to `RequestSpecBuilder`:
> ```java
> .addFilter(new AllureRestAssured())
> ```
> This automatically captures every request and response into the Allure report,
> enabling detailed HTTP-level debugging without manual logging.

**Q10: What is the Faker library used for in this project?**
> `Javafaker` generates realistic random data (names, emails, addresses, dates) so each
> test run uses unique test data. This prevents test pollution from leftover data and
> makes tests genuinely isolated.

---

## 🔹 Testing Strategies

**Q11: How do you handle JWT token management across tests?**
> `TestBase.setup()` calls `api.getToken(email, password)` once and stores it as `protected String authToken`.
> `ApiClient.getToken()` throws a `RuntimeException` if login fails, making the failure obvious.
> All subsequent API calls pass the token in the `Authorization: Bearer <token>` header.

**Q12: What is the difference between API tests, integration tests, and e2e tests in this framework?**
> - **API tests** (`tests/api/`) — Single endpoint in isolation (unit of the API)
> - **Integration tests** (`tests/integration/`) — Multiple endpoints working together (e.g., create event → book → verify seats)
> - **E2E tests** (`tests/e2e/`) — Full user journey from registration to deletion using `dependsOnMethods`

**Q13: How do you test negative scenarios (error cases)?**
> Example: invalid email, wrong password, non-existent ID, quantity = 0.
> These pass intentionally bad data and assert on 4xx status codes + `success: false` in response.

**Q14: How do you test response time / performance?**
> ```java
> long start = System.currentTimeMillis();
> Response res = api.healthCheck();
> long duration = System.currentTimeMillis() - start;
> assertTrue(duration < 2000, "Took " + duration + "ms");
> ```
> The `PerformanceTest` class validates SLAs for all critical endpoints.

**Q15: Explain the `dependsOnMethods` usage in `FullJourneyTest`.**
> `@Test(dependsOnMethods = "step1_RegisterNewUser")` creates a dependency chain
> so the E2E steps run in order. If step 3 fails, steps 4–7 are skipped automatically.
> Shared state (token, eventId, bookingId) is stored as instance fields between steps.

---

## 🔹 Running and Reporting

**Q16: How do you run only a specific test class?**
> ```bash
> mvn test -Dtest=AuthApiTest
> mvn test -Dtest=EventsApiTest,BookingsApiTest
> ```

**Q17: How do you generate the Allure HTML report?**
> ```bash
> mvn test          # Run tests (generates allure-results/)
> mvn allure:report # Build HTML report
> open target/site/allure-maven-plugin/index.html
> ```

**Q18: How would you run tests in parallel?**
> In `testng.xml`, change:
> ```xml
> <suite name="..." parallel="tests" thread-count="3">
> ```
> Each `<test>` block (API, Integration, Performance, E2E) would run in a separate thread.
> Note: E2E tests must remain `parallel="none"` due to `dependsOnMethods`.

**Q19: How are credentials managed securely in CI?**
> In GitHub Actions, they're stored as repository **Secrets** (`secrets.USER_EMAIL`, etc.)
> and written to `config.properties` at runtime — never hard-coded in source files.

**Q20: How does this framework compare to the Playwright counterpart?**
> | Aspect | Playwright Suite | RestAssured Suite |
> |---|---|---|
> | Language | JavaScript | Java |
> | Test runner | Playwright Test | TestNG |
> | API calls | `request.get/post/...` | `given().get/post/...` |
> | Data factory | `faker-js` | Javafaker |
> | Reports | Playwright HTML | Allure |
> | Fixtures/DI | `test.extend()` | `@BeforeClass` / extends TestBase |
> | Folder structure | `tests/api/, e2e/, integration/...` | Same structure in `src/test/java/...` |
