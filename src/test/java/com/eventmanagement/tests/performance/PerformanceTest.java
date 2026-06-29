package com.eventmanagement.tests.performance;

import com.eventmanagement.base.TestBase;
import com.eventmanagement.models.EventPayload;
import com.eventmanagement.utils.TestDataGenerator;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.eventmanagement.utils.ApiClient.params;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

/**
 * Performance / Timing Test Suite
 * ---------------------------------
 * Validates that critical API endpoints respond within defined SLA thresholds.
 * Mirrors: load-timing.spec.js from the Playwright suite.
 */
@Epic("EventHub API")
@Feature("Performance")
public class PerformanceTest extends TestBase {

    private static final int SLA_MS = 2000;  // 2-second SLA for all endpoints

    private final List<Integer> createdEventIds = new ArrayList<>();

    @AfterClass(alwaysRun = true)
    public void teardown() {
        for (int id : createdEventIds) {
            try { api.deleteEvent(authToken, id); } catch (Exception ignore) { }
        }
    }

    @Test(description = "PERF -- GET /health should respond under 2s")
    @Story("Response Time")
    @Severity(SeverityLevel.NORMAL)
    public void healthEndpointShouldRespondUnder2s() {
        long start = System.currentTimeMillis();
        Response res = api.healthCheck();
        long duration = System.currentTimeMillis() - start;

        assertEquals(res.statusCode(), 200);
        assertTrue(duration < SLA_MS,
                "/health took " + duration + "ms — exceeds SLA of " + SLA_MS + "ms");
    }

    @Test(description = "PERF -- GET /events should respond under 2s")
    @Story("Response Time")
    @Severity(SeverityLevel.NORMAL)
    public void listEventsEndpointShouldRespondUnder2s() {
        long start = System.currentTimeMillis();
        Response res = api.getEvents(authToken, params("page", 1, "limit", 10));
        long duration = System.currentTimeMillis() - start;

        assertEquals(res.statusCode(), 200);
        assertTrue(duration < SLA_MS,
                "/events took " + duration + "ms — exceeds SLA of " + SLA_MS + "ms");
    }

    @Test(description = "PERF -- POST /auth/login should respond under 2s")
    @Story("Response Time")
    @Severity(SeverityLevel.NORMAL)
    public void loginEndpointShouldRespondUnder2s() {
        String email    = com.eventmanagement.utils.ConfigReader.getProperty("user.email");
        String password = com.eventmanagement.utils.ConfigReader.getProperty("user.password");

        long start = System.currentTimeMillis();
        Response res = api.login(email, password);
        long duration = System.currentTimeMillis() - start;

        assertEquals(res.statusCode(), 200);
        assertTrue(duration < SLA_MS,
                "/auth/login took " + duration + "ms — exceeds SLA of " + SLA_MS + "ms");
    }

    @Test(description = "PERF -- POST /events should respond under 2s")
    @Story("Response Time")
    @Severity(SeverityLevel.NORMAL)
    public void createEventEndpointShouldRespondUnder2s() {
        EventPayload payload = TestDataGenerator.newEvent("Perf Test Event");

        long start = System.currentTimeMillis();
        Response res = api.createEvent(authToken, payload);
        long duration = System.currentTimeMillis() - start;

        assertEquals(res.statusCode(), 201);
        createdEventIds.add(res.jsonPath().getInt("data.id"));
        assertTrue(duration < SLA_MS,
                "/events POST took " + duration + "ms — exceeds SLA of " + SLA_MS + "ms");
    }

    @Test(description = "PERF -- GET /events/:id should respond under 2s")
    @Story("Response Time")
    @Severity(SeverityLevel.NORMAL)
    public void getEventByIdShouldRespondUnder2s() {
        // Create an event first
        EventPayload payload = TestDataGenerator.newEvent("Perf GetById Event");
        Response created = api.createEvent(authToken, payload);
        int eventId = created.jsonPath().getInt("data.id");
        createdEventIds.add(eventId);

        long start = System.currentTimeMillis();
        Response res = api.getEventById(authToken, eventId);
        long duration = System.currentTimeMillis() - start;

        assertEquals(res.statusCode(), 200);
        assertTrue(duration < SLA_MS,
                "/events/:id GET took " + duration + "ms — exceeds SLA of " + SLA_MS + "ms");
    }

    @Test(description = "PERF -- GET /bookings should respond under 2s")
    @Story("Response Time")
    @Severity(SeverityLevel.NORMAL)
    public void listBookingsEndpointShouldRespondUnder2s() {
        long start = System.currentTimeMillis();
        Response res = api.getBookings(authToken, params("page", 1, "limit", 10));
        long duration = System.currentTimeMillis() - start;

        assertEquals(res.statusCode(), 200);
        assertTrue(duration < SLA_MS,
                "/bookings took " + duration + "ms — exceeds SLA of " + SLA_MS + "ms");
    }
}
