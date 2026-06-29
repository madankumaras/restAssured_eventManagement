# Customization Guide — Advanced REST Assured Event Management API Tests

> Step-by-step guide to adapt and extend this API testing framework.
>
> **Product by TheActualEngineeringProjects**

---

## 1. Change the Target API

Update `config.properties`:
```properties
base.url=https://your-api.com
auth.email=your_test_user@example.com
auth.password=your_password
```

Update `TestBase.java` if auth flow differs:
```java
public class TestBase {
    @BeforeClass
    public void setup() {
        RestAssured.baseURI = ConfigReader.get("base.url");
        // Change auth strategy here
    }
}
```

## 2. Add New API Endpoints

Create a new test class:
```java
// src/test/java/com/eventmanagement/tests/api/VenuesApiTest.java
public class VenuesApiTest extends TestBase {
    
    @Test(priority = 1, description = "Create a venue")
    public void testCreateVenue() {
        VenuePayload venue = new VenuePayload("Conference Hall", "NYC", 500);
        
        Response response = given()
            .header("Authorization", "Bearer " + AuthManager.getToken())
            .contentType(ContentType.JSON)
            .body(venue)
        .when()
            .post("/api/venues")
        .then()
            .statusCode(201)
            .body("name", equalTo("Conference Hall"))
            .extract().response();
        
        venueId = response.jsonPath().getString("id");
    }
}
```

Add Pojo model:
```java
// src/test/java/com/eventmanagement/models/VenuePayload.java
public class VenuePayload {
    private String name;
    private String location;
    private int capacity;
    // Constructor, getters, setters
}
```

## 3. Add Custom Assertions

```java
public class CustomAssertions {
    public static void assertPaginated(Response response) {
        assertNotNull(response.jsonPath().get("data"));
        assertNotNull(response.jsonPath().get("totalPages"));
        assertNotNull(response.jsonPath().get("currentPage"));
        assertTrue(response.jsonPath().getInt("totalPages") >= 0);
    }
    
    public static void assertErrorResponse(Response response, int status) {
        assertEquals(response.getStatusCode(), status);
        assertNotNull(response.jsonPath().get("message"));
    }
}
```

## 4. Add Request/Response Logging

```java
// In TestBase.java
RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

// Or per-request
given()
    .log().all()    // Log request
.when()
    .get("/api/events")
.then()
    .log().all()    // Log response
    .statusCode(200);
```

## 5. Add Schema Validation

```java
// Store JSON schema in src/test/resources/schemas/event-schema.json
@Test
public void testEventResponseSchema() {
    given()
        .header("Authorization", "Bearer " + token)
    .when()
        .get("/api/events")
    .then()
        .assertThat()
        .body(matchesJsonSchemaInClasspath("schemas/event-schema.json"));
}
```

## 6. Add Performance Testing Thresholds

```java
@Test
public void testAPIResponseTime() {
    given()
        .header("Authorization", "Bearer " + token)
    .when()
        .get("/api/events")
    .then()
        .time(lessThan(2000L));  // Must respond within 2 seconds
}
```

## 7. Add Allure Reporting Enhancements

```java
@Epic("Event Management API")
@Feature("Bookings")
@Story("Create Booking")
@Severity(SeverityLevel.CRITICAL)
@Test
public void testCreateBooking() {
    Allure.step("Prepare booking payload", () -> {
        // setup
    });
    Allure.step("Send POST request", () -> {
        // request
    });
    Allure.step("Verify response", () -> {
        // assertions
    });
}
```

## 8. Add CI/CD Pipeline

```yaml
# .github/workflows/api-tests.yml
name: API Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      - run: mvn test -DsuiteXmlFile=testng.xml
      - uses: actions/upload-artifact@v4
        if: always()
        with: { name: allure-results, path: target/allure-results/ }
```
