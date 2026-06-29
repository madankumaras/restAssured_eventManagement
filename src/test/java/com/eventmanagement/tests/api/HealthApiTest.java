package com.eventmanagement.tests.api;

import com.eventmanagement.base.TestBase;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.*;

/**
 * Health & Config API Test Suite
 * --------------------------------
 * Covers: Health check endpoint, feature flag endpoint, response time
 * Mirrors: health.api.spec.js from the Playwright suite
 */
@Epic("EventHub API")
@Feature("Health & Config")
public class HealthApiTest extends TestBase {

    @Test(description = "GET /health -- should return healthy status")
    @Story("Health Check")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Confirm /health responds with status=ok and dbStatus=connected.")
    public void shouldReturnHealthyStatus() {
        Response res = api.healthCheck();

        // HTTP status should be 200
        assertEquals(res.statusCode(), 200, "Expected HTTP 200");

        // JSON body: { status: 'ok', dbStatus: 'connected', timestamp: '...' }
        assertEquals(res.jsonPath().getString("status"), "ok", "status should be 'ok'");
        assertEquals(res.jsonPath().getString("dbStatus"), "connected", "dbStatus should be 'connected'");
        assertNotNull(res.jsonPath().getString("timestamp"), "timestamp should be present");
    }

    @Test(description = "GET /health -- response time should be under 2 seconds")
    @Story("Health Check")
    @Severity(SeverityLevel.NORMAL)
    public void healthResponseTimeShouldBeUnder2Seconds() {
        long start = System.currentTimeMillis();
        Response res = api.healthCheck();
        long duration = System.currentTimeMillis() - start;

        assertEquals(res.statusCode(), 200, "Expected HTTP 200");
        assertTrue(duration < 2000, "Health endpoint should respond in under 2 seconds; took " + duration + "ms");
    }

    @Test(description = "GET /config -- should return feature flag object")
    @Story("Feature Flags")
    @Severity(SeverityLevel.NORMAL)
    public void shouldReturnFeatureFlagObject() {
        Response res = api.getConfig();

        assertEquals(res.statusCode(), 200, "Expected HTTP 200");
        // Feature flags exist as booleans in response
        assertNotNull(res.jsonPath().get("showExploreLinks"),
                "showExploreLinks flag should be present in /config response");
    }
}
