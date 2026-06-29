package com.eventmanagement.tests.api;

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
import static org.testng.Assert.*;

/**
 * Events API Test Suite
 * ---------------------
 * Covers: Full CRUD on /events, filtering, pagination, validation
 * Mirrors: events.api.spec.js from the Playwright suite
 */
@Epic("EventHub API")
@Feature("Events")
public class EventsApiTest extends TestBase {

    private final List<Integer> createdEventIds = new ArrayList<>();

    @AfterClass(alwaysRun = true)
    public void teardown() {
        for (int id : createdEventIds) {
            try { api.deleteEvent(authToken, id); } catch (Exception ignore) { }
        }
    }

    /* =========== CREATE =========== */

    @Test(description = "POST /events -- should create an event successfully")
    @Story("Event CRUD")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Create a new event; verify 201, all fields in response, and availableSeats == totalSeats.")
    public void shouldCreateEventSuccessfully() {
        EventPayload payload = TestDataGenerator.newEvent("API Test Event -- Create");

        Response res = api.createEvent(authToken, payload);

        assertEquals(res.statusCode(), 201, "Expected 201 Created");
        assertTrue(res.jsonPath().getBoolean("success"), "success should be true");
        assertNotNull(res.jsonPath().get("data"), "data should be present");
        assertEquals(res.jsonPath().getString("data.title"), payload.getTitle());
        assertEquals(res.jsonPath().getString("data.category"), payload.getCategory());
        assertEquals(res.jsonPath().getInt("data.price"), (int) payload.getPrice());
        assertEquals(res.jsonPath().getInt("data.totalSeats"), (int) payload.getTotalSeats());
        assertEquals(res.jsonPath().getInt("data.availableSeats"), (int) payload.getTotalSeats());

        int id = res.jsonPath().getInt("data.id");
        createdEventIds.add(id);
    }

    @Test(description = "POST /events -- should reject event with missing required fields")
    @Story("Event Validation")
    @Severity(SeverityLevel.NORMAL)
    public void shouldRejectEventWithMissingRequiredFields() {
        EventPayload emptyPayload = EventPayload.builder().title("").build();

        Response res = api.createEvent(authToken, emptyPayload);

        assertEquals(res.statusCode(), 400, "Expected 400 for missing fields");
        assertFalse(res.jsonPath().getBoolean("success"), "success should be false");
    }

    /* =========== READ =========== */

    @Test(description = "GET /events -- should list events with pagination")
    @Story("Event Listing")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldListEventsWithPagination() {
        Response res = api.getEvents(authToken, params("page", 1, "limit", 5));

        assertEquals(res.statusCode(), 200, "Expected 200 OK");
        assertTrue(res.jsonPath().getBoolean("success"), "success should be true");
        assertTrue(res.jsonPath().getList("data").size() > 0, "data should be a non-empty array");
        assertNotNull(res.jsonPath().get("pagination"), "pagination should be present");
        assertEquals(res.jsonPath().getInt("pagination.page"), 1);
        assertEquals(res.jsonPath().getInt("pagination.limit"), 5);
    }

    @Test(description = "GET /events -- should filter by category")
    @Story("Event Listing")
    @Severity(SeverityLevel.NORMAL)
    public void shouldFilterEventsByCategory() {
        Response res = api.getEvents(authToken, params("category", "Conference"));

        assertEquals(res.statusCode(), 200, "Expected 200 OK");
        List<String> categories = res.jsonPath().getList("data.category");
        for (String cat : categories) {
            assertEquals(cat, "Conference", "All returned events should be Conferences");
        }
    }

    @Test(description = "GET /events -- should filter by city")
    @Story("Event Listing")
    @Severity(SeverityLevel.NORMAL)
    public void shouldFilterEventsByCity() {
        // Create an event in Bangalore first
        EventPayload payload = TestDataGenerator.newEventWithCity("Bangalore");
        payload = EventPayload.builder()
                .title("Bangalore City Filter Test")
                .description(payload.getDescription())
                .category(payload.getCategory())
                .venue(payload.getVenue())
                .city("Bangalore")
                .eventDate(payload.getEventDate())
                .price(payload.getPrice())
                .totalSeats(payload.getTotalSeats())
                .imageUrl(payload.getImageUrl())
                .build();
        Response created = api.createEvent(authToken, payload);
        createdEventIds.add(created.jsonPath().getInt("data.id"));

        Response res = api.getEvents(authToken, params("city", "Bangalore"));

        assertEquals(res.statusCode(), 200, "Expected 200 OK");
        List<String> cities = res.jsonPath().getList("data.city");
        for (String city : cities) {
            assertEquals(city, "Bangalore", "All returned events should be in Bangalore");
        }
    }

    @Test(description = "GET /events -- should search by keyword")
    @Story("Event Search")
    @Severity(SeverityLevel.NORMAL)
    public void shouldSearchEventsByKeyword() {
        String keyword = "UniqueSearchTerm2026";
        EventPayload payload = TestDataGenerator.newEvent(keyword + " Summit");
        Response created = api.createEvent(authToken, payload);
        createdEventIds.add(created.jsonPath().getInt("data.id"));

        Response res = api.getEvents(authToken, params("search", keyword));

        assertEquals(res.statusCode(), 200, "Expected 200 OK");
        List<String> titles = res.jsonPath().getList("data.title");
        assertTrue(titles.stream().anyMatch(t -> t.contains(keyword)),
                "At least one returned event should contain the search keyword");
    }

    @Test(description = "GET /events/:id -- should get a single event by ID")
    @Story("Event CRUD")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldGetSingleEventById() {
        EventPayload payload = TestDataGenerator.newEvent("Single Event Fetch Test");
        Response created = api.createEvent(authToken, payload);
        int eventId = created.jsonPath().getInt("data.id");
        createdEventIds.add(eventId);

        Response res = api.getEventById(authToken, eventId);

        assertEquals(res.statusCode(), 200, "Expected 200 OK");
        assertTrue(res.jsonPath().getBoolean("success"), "success should be true");
        assertEquals(res.jsonPath().getInt("data.id"), eventId);
        assertEquals(res.jsonPath().getString("data.title"), payload.getTitle());
    }

    @Test(description = "GET /events/:id -- should return 404 for non-existent event")
    @Story("Event CRUD")
    @Severity(SeverityLevel.NORMAL)
    public void shouldReturn404ForNonExistentEvent() {
        Response res = api.getEventById(authToken, 999999);

        assertEquals(res.statusCode(), 404, "Expected 404 Not Found");
        assertFalse(res.jsonPath().getBoolean("success"), "success should be false");
    }

    /* =========== UPDATE =========== */

    @Test(description = "PUT /events/:id -- should update an event")
    @Story("Event CRUD")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldUpdateEvent() {
        EventPayload original = TestDataGenerator.newEvent("Event To Update");
        Response created = api.createEvent(authToken, original);
        int eventId = created.jsonPath().getInt("data.id");
        createdEventIds.add(eventId);

        EventPayload updated = EventPayload.builder()
                .title("Updated Event Title")
                .description(original.getDescription())
                .category(original.getCategory())
                .venue(original.getVenue())
                .city(original.getCity())
                .eventDate(original.getEventDate())
                .price(9999)
                .totalSeats(original.getTotalSeats())
                .imageUrl(original.getImageUrl())
                .build();

        Response res = api.updateEvent(authToken, eventId, updated);

        assertEquals(res.statusCode(), 200, "Expected 200 OK");
        assertTrue(res.jsonPath().getBoolean("success"), "success should be true");
        assertEquals(res.jsonPath().getString("data.title"), "Updated Event Title");
        assertEquals(res.jsonPath().getInt("data.price"), 9999);
    }

    /* =========== DELETE =========== */

    @Test(description = "DELETE /events/:id -- should delete an event")
    @Story("Event CRUD")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldDeleteEvent() {
        EventPayload payload = TestDataGenerator.newEvent("Event To Delete");
        Response created = api.createEvent(authToken, payload);
        int eventId = created.jsonPath().getInt("data.id");

        Response deleteRes = api.deleteEvent(authToken, eventId);
        assertEquals(deleteRes.statusCode(), 200, "Expected 200 OK for delete");
        assertTrue(deleteRes.jsonPath().getBoolean("success"), "success should be true");

        // Verify it's gone
        Response getRes = api.getEventById(authToken, eventId);
        assertEquals(getRes.statusCode(), 404, "Expected 404 after deletion");
    }

    @Test(description = "DELETE /events/:id -- should return 404 for non-existent event")
    @Story("Event CRUD")
    @Severity(SeverityLevel.NORMAL)
    public void shouldReturn404WhenDeletingNonExistentEvent() {
        Response res = api.deleteEvent(authToken, 999999);
        assertEquals(res.statusCode(), 404, "Expected 404 Not Found");
    }
}
