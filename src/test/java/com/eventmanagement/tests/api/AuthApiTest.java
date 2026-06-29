package com.eventmanagement.tests.api;

import com.eventmanagement.base.TestBase;
import com.eventmanagement.models.UserPayload;
import com.eventmanagement.utils.ConfigReader;
import com.eventmanagement.utils.TestDataGenerator;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

/**
 * Auth API Test Suite
 * -------------------
 * Covers: Registration, Login, Token validation, Error handling
 * Mirrors: auth.api.spec.js from the Playwright suite
 */
@Epic("EventHub API")
@Feature("Authentication")
public class AuthApiTest extends TestBase {

    /* =========== REGISTRATION =========== */

    @Test(description = "POST /auth/register -- should register a new user successfully")
    @Story("User Registration")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Register a brand-new user with a unique email; expect 201 + token in response.")
    public void shouldRegisterNewUserSuccessfully() {
        UserPayload user = TestDataGenerator.newUser();

        Response res = api.register(user.getEmail(), user.getPassword());

        assertEquals(res.statusCode(), 201, "Expected 201 Created");
        assertTrue(res.jsonPath().getBoolean("success"), "success should be true");
        assertNotNull(res.jsonPath().getString("token"), "token should be present");
        assertEquals(res.jsonPath().getString("user.email"), user.getEmail());
    }

    @Test(description = "POST /auth/register -- should reject duplicate email")
    @Story("User Registration")
    @Severity(SeverityLevel.NORMAL)
    public void shouldRejectDuplicateEmail() {
        UserPayload user = TestDataGenerator.newUser();

        // First registration
        api.register(user.getEmail(), user.getPassword());

        // Duplicate registration
        Response res = api.register(user.getEmail(), user.getPassword());

        assertEquals(res.statusCode(), 400, "Expected 400 for duplicate email");
        assertFalse(res.jsonPath().getBoolean("success"), "success should be false");
    }

    @Test(description = "POST /auth/register -- should reject invalid email formats")
    @Story("User Registration")
    @Severity(SeverityLevel.NORMAL)
    public void shouldRejectInvalidEmailFormats() {
        for (String email : TestDataGenerator.invalidEmails()) {
            Response res = api.register(email, "ValidPass123");
            assertTrue(res.statusCode() == 400 || res.statusCode() == 422,
                    "Expected 400/422 for invalid email: " + email);
            assertFalse(res.jsonPath().getBoolean("success"),
                    "success should be false for email: " + email);
        }
    }

    @Test(description = "POST /auth/register -- should reject short password (< 6 chars)")
    @Story("User Registration")
    @Severity(SeverityLevel.NORMAL)
    public void shouldRejectShortPassword() {
        UserPayload user = TestDataGenerator.newUser();
        Response res = api.register(user.getEmail(), "ab");

        assertEquals(res.statusCode(), 400, "Expected 400 for short password");
        assertFalse(res.jsonPath().getBoolean("success"), "success should be false");
    }

    /* =========== LOGIN =========== */

    @Test(description = "POST /auth/login -- should login with valid credentials")
    @Story("User Login")
    @Severity(SeverityLevel.BLOCKER)
    public void shouldLoginWithValidCredentials() {
        String email    = ConfigReader.getProperty("user.email");
        String password = ConfigReader.getProperty("user.password");

        Response res = api.login(email, password);

        assertEquals(res.statusCode(), 200, "Expected 200 OK");
        assertTrue(res.jsonPath().getBoolean("success"), "success should be true");
        String token = res.jsonPath().getString("token");
        assertNotNull(token, "token should not be null");
        assertTrue(token.length() > 10, "token should have length > 10");
    }

    @Test(description = "POST /auth/login -- should reject wrong password")
    @Story("User Login")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldRejectWrongPassword() {
        String email = ConfigReader.getProperty("user.email");

        Response res = api.login(email, "WrongPassword123");

        assertEquals(res.statusCode(), 400, "Expected 400 for wrong password");
        assertFalse(res.jsonPath().getBoolean("success"), "success should be false");
        assertNotNull(res.jsonPath().getString("error"), "error message should be present");
    }

    @Test(description = "POST /auth/login -- should reject unregistered email")
    @Story("User Login")
    @Severity(SeverityLevel.NORMAL)
    public void shouldRejectUnregisteredEmail() {
        Response res = api.login("nonexistent_user_99999@test.com", "SomePassword123");

        assertTrue(res.statusCode() == 400 || res.statusCode() == 404,
                "Expected 400 or 404 for unregistered email");
        assertFalse(res.jsonPath().getBoolean("success"), "success should be false");
        assertNotNull(res.jsonPath().getString("error"), "error message should be present");
    }

    /* =========== TOKEN VALIDATION =========== */

    @Test(description = "GET /auth/me -- should return user identity with valid token")
    @Story("Token Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldReturnUserIdentityWithValidToken() {
        String email = ConfigReader.getProperty("user.email");

        Response res = api.getMe(authToken);

        assertEquals(res.statusCode(), 200, "Expected 200 OK");
        assertTrue(res.jsonPath().getBoolean("success"), "success should be true");
        assertEquals(res.jsonPath().getString("user.email"), email);
    }

    @Test(description = "GET /auth/me -- should return 401 without token")
    @Story("Token Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldReturn401WithoutToken() {
        Response res = api.getMe("");

        assertEquals(res.statusCode(), 401, "Expected 401 when no token is provided");
        assertFalse(res.jsonPath().getBoolean("success"), "success should be false");
    }

    @Test(description = "GET /auth/me -- should return 401 with invalid token")
    @Story("Token Validation")
    @Severity(SeverityLevel.NORMAL)
    public void shouldReturn401WithInvalidToken() {
        Response res = api.getMe("invalid.token.here");

        assertEquals(res.statusCode(), 401, "Expected 401 for invalid token");
    }
}
