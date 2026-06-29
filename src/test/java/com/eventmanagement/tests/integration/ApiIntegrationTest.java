package com.eventmanagement.tests.integration;

import com.eventmanagement.base.TestBase;
import com.eventmanagement.models.BookingPayload;
import com.eventmanagement.models.EventPayload;
import com.eventmanagement.utils.TestDataGenerator;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

/**
 * API Integration Test Suite
 * --------------------------
 * End-to-end API workflows spanning multiple resources.
 * Mirrors: api-ui-hybrid.spec.js from the Playwright suite.
 *
 * These tests verify that multiple API endpoints work correctly together:
 * - Creating an event and immediately booking it
 * - Verifying data consistency across endpoints
 * - Auth → Event → Booking lifecycle
 */
@Epic("EventHub API")
@Feature("Integration Workflows")
public class ApiIntegrationTest extends TestBase {

    private final List<Integer> createdEventIds   = new ArrayList<>();
    private final List<Integer> createdBookingIds = new ArrayList<>();

    @AfterClass(alwaysRun = true)
    public void teardown() {
        for (int id : createdBookingIds) {
            try { api.cancelBooking(authToken, id); } catch (Exception ignore) { }
        }
        for (int id : createdEventIds) {
            try { api.deleteEvent(authToken, id); } catch (Exception ignore) { }
        }
    }

    @Test(description = "INTEGRATION -- Create event then book it; verify seat decrement")
    @Story("Event → Booking Workflow")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Full create-event → book-ticket → verify-seats workflow via API.")
    public void createEventThenBookIt() {
        // 1. Create event
        EventPayload eventPayload = TestDataGenerator.newEvent(50, 300);
        Response eventRes = api.createEvent(authToken, eventPayload);
        assertEquals(eventRes.statusCode(), 201, "Event creation should return 201");

        int eventId = eventRes.jsonPath().getInt("data.id");
        createdEventIds.add(eventId);
        int initialSeats = eventRes.jsonPath().getInt("data.availableSeats");
        assertEquals(initialSeats, 50, "Initial availableSeats should equal totalSeats");

        // 2. Book 3 seats
        BookingPayload bookingPayload = TestDataGenerator.newBooking(eventId, 3);
        Response bookingRes = api.createBooking(authToken, bookingPayload);
        assertEquals(bookingRes.statusCode(), 201, "Booking creation should return 201");

        int bookingId = bookingRes.jsonPath().getInt("data.id");
        createdBookingIds.add(bookingId);

        // 3. Verify seat count via GET /events/:id
        int seatsAfter = api.getEventById(authToken, eventId)
                .jsonPath().getInt("data.availableSeats");
        assertEquals(seatsAfter, 50 - 3, "Available seats should decrease by booking quantity");
    }

    @Test(description = "INTEGRATION -- Book → Retrieve by ref → Cancel → Verify seats")
    @Story("Booking Lifecycle")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Complete booking lifecycle: create booking, look up by ref, cancel, verify seats restored.")
    public void fullBookingLifecycle() {
        // Create event
        EventPayload eventPayload = TestDataGenerator.newEvent(30, 100);
        Response eventRes = api.createEvent(authToken, eventPayload);
        int eventId = eventRes.jsonPath().getInt("data.id");
        createdEventIds.add(eventId);

        // Book seats
        BookingPayload bookingPayload = TestDataGenerator.newBooking(eventId, 2);
        Response bookingRes = api.createBooking(authToken, bookingPayload);
        int bookingId     = bookingRes.jsonPath().getInt("data.id");
        String bookingRef = bookingRes.jsonPath().getString("data.bookingRef");
        createdBookingIds.add(bookingId);

        // Retrieve by reference
        Response refRes = api.getBookingByRef(authToken, bookingRef);
        assertEquals(refRes.statusCode(), 200, "Should find booking by reference");
        assertEquals(refRes.jsonPath().getString("data.bookingRef"), bookingRef);

        // Cancel
        Response cancelRes = api.cancelBooking(authToken, bookingId);
        assertEquals(cancelRes.statusCode(), 200, "Cancellation should return 200");

        // Verify seats restored
        int seatsRestored = api.getEventById(authToken, eventId)
                .jsonPath().getInt("data.availableSeats");
        assertEquals(seatsRestored, 30, "Seats should be fully restored after cancellation");
    }

    @Test(description = "INTEGRATION -- Verify auth token can access all protected resources")
    @Story("Auth Coverage")
    @Severity(SeverityLevel.CRITICAL)
    public void singleTokenShouldAccessAllResources() {
        // Auth
        Response me = api.getMe(authToken);
        assertEquals(me.statusCode(), 200, "/auth/me should be accessible with token");

        // Events
        Response events = api.getEvents(authToken);
        assertEquals(events.statusCode(), 200, "/events should be accessible with token");

        // Bookings
        Response bookings = api.getBookings(authToken);
        assertEquals(bookings.statusCode(), 200, "/bookings should be accessible with token");
    }

    @Test(description = "INTEGRATION -- Update event then verify changes via GET")
    @Story("Event Update Verification")
    @Severity(SeverityLevel.NORMAL)
    public void updateEventAndVerifyViaGet() {
        // Create
        EventPayload original = TestDataGenerator.newEvent("Integration Update Test");
        Response created = api.createEvent(authToken, original);
        int eventId = created.jsonPath().getInt("data.id");
        createdEventIds.add(eventId);

        // Update
        EventPayload updated = EventPayload.builder()
                .title("Integration Updated Title")
                .description(original.getDescription())
                .category(original.getCategory())
                .venue(original.getVenue())
                .city(original.getCity())
                .eventDate(original.getEventDate())
                .price(7777)
                .totalSeats(original.getTotalSeats())
                .imageUrl(original.getImageUrl())
                .build();
        api.updateEvent(authToken, eventId, updated);

        // Verify via GET
        Response fetched = api.getEventById(authToken, eventId);
        assertEquals(fetched.jsonPath().getString("data.title"), "Integration Updated Title");
        assertEquals(fetched.jsonPath().getInt("data.price"), 7777);
    }
}
