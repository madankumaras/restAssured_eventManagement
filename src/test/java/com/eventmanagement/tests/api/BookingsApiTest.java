package com.eventmanagement.tests.api;

import com.eventmanagement.base.TestBase;
import com.eventmanagement.models.BookingPayload;
import com.eventmanagement.models.EventPayload;
import com.eventmanagement.utils.TestDataGenerator;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.eventmanagement.utils.ApiClient.params;
import static org.testng.Assert.*;

/**
 * Bookings API Test Suite
 * -----------------------
 * Covers: Booking creation, listing, lookup, cancellation, seat management
 * Mirrors: bookings.api.spec.js from the Playwright suite
 */
@Epic("EventHub API")
@Feature("Bookings")
public class BookingsApiTest extends TestBase {

    private int testEventId;
    private final List<Integer> createdBookingIds = new ArrayList<>();
    private final List<Integer> createdEventIds  = new ArrayList<>();

    @BeforeClass(alwaysRun = true)
    @Override
    public void setup() {
        super.setup();

        // Create a dedicated event for booking tests
        EventPayload eventData = TestDataGenerator.newEvent(100, 500);
        eventData = EventPayload.builder()
                .title("Booking Test Event")
                .description(eventData.getDescription())
                .category(eventData.getCategory())
                .venue(eventData.getVenue())
                .city(eventData.getCity())
                .eventDate(eventData.getEventDate())
                .price(500)
                .totalSeats(100)
                .imageUrl(eventData.getImageUrl())
                .build();

        Response res = api.createEvent(authToken, eventData);
        testEventId = res.jsonPath().getInt("data.id");
        createdEventIds.add(testEventId);
    }

    @AfterClass(alwaysRun = true)
    public void teardown() {
        for (int id : createdBookingIds) {
            try { api.cancelBooking(authToken, id); } catch (Exception ignore) { }
        }
        for (int id : createdEventIds) {
            try { api.deleteEvent(authToken, id); } catch (Exception ignore) { }
        }
    }

    /* =========== CREATE BOOKING =========== */

    @Test(description = "POST /bookings -- should create a booking successfully")
    @Story("Booking Creation")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Create a booking for 2 seats; verify 201, totalPrice = price × qty, bookingRef starts with EVT-, status = confirmed.")
    public void shouldCreateBookingSuccessfully() {
        BookingPayload payload = TestDataGenerator.newBooking(testEventId, 2);

        Response res = api.createBooking(authToken, payload);

        assertEquals(res.statusCode(), 201, "Expected 201 Created");
        assertTrue(res.jsonPath().getBoolean("success"), "success should be true");
        assertNotNull(res.jsonPath().get("data"), "data should be present");
        assertEquals(res.jsonPath().getInt("data.eventId"), testEventId);
        assertEquals(res.jsonPath().getInt("data.quantity"), 2);
        assertEquals(res.jsonPath().getInt("data.totalPrice"), 500 * 2, "totalPrice should be price × quantity");
        assertTrue(res.jsonPath().getString("data.bookingRef").startsWith("E-"),
                "bookingRef should start with E-");
        assertEquals(res.jsonPath().getString("data.status"), "confirmed");

        createdBookingIds.add(res.jsonPath().getInt("data.id"));
    }

    @Test(description = "POST /bookings -- should generate unique booking reference")
    @Story("Booking Creation")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldGenerateUniqueBookingReference() {
        Response res1 = api.createBooking(authToken, TestDataGenerator.newBooking(testEventId, 1));
        Response res2 = api.createBooking(authToken, TestDataGenerator.newBooking(testEventId, 1));

        createdBookingIds.add(res1.jsonPath().getInt("data.id"));
        createdBookingIds.add(res2.jsonPath().getInt("data.id"));

        assertNotEquals(
                res1.jsonPath().getString("data.bookingRef"),
                res2.jsonPath().getString("data.bookingRef"),
                "Booking references should be unique"
        );
    }

    @Test(description = "POST /bookings -- should reject booking for non-existent event")
    @Story("Booking Validation")
    @Severity(SeverityLevel.NORMAL)
    public void shouldRejectBookingForNonExistentEvent() {
        BookingPayload payload = TestDataGenerator.newBooking(999999);

        Response res = api.createBooking(authToken, payload);

        assertEquals(res.statusCode(), 404, "Expected 404 for non-existent event");
        assertFalse(res.jsonPath().getBoolean("success"), "success should be false");
    }

    @Test(description = "POST /bookings -- should reject invalid quantity (0)")
    @Story("Booking Validation")
    @Severity(SeverityLevel.NORMAL)
    public void shouldRejectQuantityZero() {
        BookingPayload payload = TestDataGenerator.newBooking(testEventId, 0);

        Response res = api.createBooking(authToken, payload);

        assertEquals(res.statusCode(), 400, "Expected 400 for quantity = 0");
        assertFalse(res.jsonPath().getBoolean("success"), "success should be false");
    }

    @Test(description = "POST /bookings -- should reject quantity exceeding limit (> 10)")
    @Story("Booking Validation")
    @Severity(SeverityLevel.NORMAL)
    public void shouldRejectQuantityExceedingLimit() {
        BookingPayload payload = TestDataGenerator.newBooking(testEventId, 11);

        Response res = api.createBooking(authToken, payload);

        assertEquals(res.statusCode(), 400, "Expected 400 for quantity > 10");
        assertFalse(res.jsonPath().getBoolean("success"), "success should be false");
    }

    /* =========== READ BOOKINGS =========== */

    @Test(description = "GET /bookings -- should list all bookings (paginated)")
    @Story("Booking Listing")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldListAllBookingsPaginated() {
        Response res = api.getBookings(authToken, params("page", 1, "limit", 10));

        assertEquals(res.statusCode(), 200, "Expected 200 OK");
        assertTrue(res.jsonPath().getBoolean("success"), "success should be true");
        assertNotNull(res.jsonPath().getList("data"), "data should be an array");
        assertNotNull(res.jsonPath().get("pagination"), "pagination should be present");
    }

    @Test(description = "GET /bookings -- should filter by eventId")
    @Story("Booking Listing")
    @Severity(SeverityLevel.NORMAL)
    public void shouldFilterBookingsByEventId() {
        Response res = api.getBookings(authToken, params("eventId", testEventId));

        assertEquals(res.statusCode(), 200, "Expected 200 OK");
        List<Integer> eventIds = res.jsonPath().getList("data.eventId");
        for (int id : eventIds) {
            assertEquals(id, testEventId, "All bookings should belong to testEventId");
        }
    }

    @Test(description = "GET /bookings -- should filter by status")
    @Story("Booking Listing")
    @Severity(SeverityLevel.NORMAL)
    public void shouldFilterBookingsByStatus() {
        Response res = api.getBookings(authToken, params("status", "confirmed"));

        assertEquals(res.statusCode(), 200, "Expected 200 OK");
        List<String> statuses = res.jsonPath().getList("data.status");
        for (String status : statuses) {
            assertEquals(status, "confirmed", "All bookings should have status = confirmed");
        }
    }

    @Test(description = "GET /bookings/:id -- should get a single booking by ID")
    @Story("Booking Listing")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldGetSingleBookingById() {
        BookingPayload payload = TestDataGenerator.newBooking(testEventId, 1);
        Response created = api.createBooking(authToken, payload);
        int bookingId = created.jsonPath().getInt("data.id");
        String bookingRef = created.jsonPath().getString("data.bookingRef");
        createdBookingIds.add(bookingId);

        Response res = api.getBookingById(authToken, bookingId);

        assertEquals(res.statusCode(), 200, "Expected 200 OK");
        assertEquals(res.jsonPath().getInt("data.id"), bookingId);
        assertEquals(res.jsonPath().getString("data.bookingRef"), bookingRef);
    }

    @Test(description = "GET /bookings/:id -- should return 404 for non-existent booking")
    @Story("Booking Listing")
    @Severity(SeverityLevel.NORMAL)
    public void shouldReturn404ForNonExistentBooking() {
        Response res = api.getBookingById(authToken, 999999);
        assertEquals(res.statusCode(), 404, "Expected 404 Not Found");
    }

    /* =========== LOOKUP BY REFERENCE =========== */

    @Test(description = "GET /bookings/ref/:ref -- should find booking by reference code")
    @Story("Booking Lookup")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldFindBookingByReference() {
        BookingPayload payload = TestDataGenerator.newBooking(testEventId, 1);
        Response created = api.createBooking(authToken, payload);
        int bookingId = created.jsonPath().getInt("data.id");
        String bookingRef = created.jsonPath().getString("data.bookingRef");
        createdBookingIds.add(bookingId);

        Response res = api.getBookingByRef(authToken, bookingRef);

        assertEquals(res.statusCode(), 200, "Expected 200 OK");
        assertEquals(res.jsonPath().getString("data.bookingRef"), bookingRef);
        assertEquals(res.jsonPath().getInt("data.id"), bookingId);
    }

    @Test(description = "GET /bookings/ref/:ref -- should return 404 for invalid reference")
    @Story("Booking Lookup")
    @Severity(SeverityLevel.NORMAL)
    public void shouldReturn404ForInvalidReference() {
        Response res = api.getBookingByRef(authToken, "EVT-INVALID");
        assertEquals(res.statusCode(), 404, "Expected 404 for invalid reference");
    }

    /* =========== CANCEL BOOKING =========== */

    @Test(description = "DELETE /bookings/:id -- should cancel booking and restore seats")
    @Story("Booking Cancellation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Create an event with 50 seats, book 3, verify seats decrement, cancel, verify seats restored.")
    public void shouldCancelBookingAndRestoreSeats() {
        // Create a fresh event to track seat changes
        EventPayload eventData = TestDataGenerator.newEvent(50, 100);
        Response eventRes = api.createEvent(authToken, eventData);
        int freshEventId = eventRes.jsonPath().getInt("data.id");
        createdEventIds.add(freshEventId);

        // Book 3 seats
        BookingPayload booking = TestDataGenerator.newBooking(freshEventId, 3);
        Response bookingRes = api.createBooking(authToken, booking);
        int bookingId = bookingRes.jsonPath().getInt("data.id");

        // Verify seats decremented
        int seatsAfterBooking = api.getEventById(authToken, freshEventId)
                .jsonPath().getInt("data.availableSeats");
        assertEquals(seatsAfterBooking, 50 - 3, "Available seats should decrease by quantity booked");

        // Cancel
        Response cancelRes = api.cancelBooking(authToken, bookingId);
        assertEquals(cancelRes.statusCode(), 200, "Expected 200 OK for cancellation");
        assertTrue(cancelRes.jsonPath().getBoolean("success"), "success should be true");

        // Verify seats restored
        int seatsAfterCancel = api.getEventById(authToken, freshEventId)
                .jsonPath().getInt("data.availableSeats");
        assertEquals(seatsAfterCancel, 50, "Available seats should be fully restored after cancellation");
    }

    @Test(description = "DELETE /bookings/:id -- should return 404 for non-existent booking")
    @Story("Booking Cancellation")
    @Severity(SeverityLevel.NORMAL)
    public void shouldReturn404WhenCancellingNonExistentBooking() {
        Response res = api.cancelBooking(authToken, 999999);
        assertEquals(res.statusCode(), 404, "Expected 404 Not Found");
    }

    /* =========== SEAT MANAGEMENT =========== */

    @Test(description = "POST /bookings -- should reject when insufficient seats available")
    @Story("Seat Management")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldRejectWhenInsufficientSeatsAvailable() {
        // Event with only 2 seats
        EventPayload eventData = TestDataGenerator.newEvent(2, 100);
        Response eventRes = api.createEvent(authToken, eventData);
        int seatEventId = eventRes.jsonPath().getInt("data.id");
        createdEventIds.add(seatEventId);

        // Try to book 5 seats
        BookingPayload payload = TestDataGenerator.newBooking(seatEventId, 5);
        Response res = api.createBooking(authToken, payload);

        assertEquals(res.statusCode(), 400, "Expected 400 when booking exceeds available seats");
        assertFalse(res.jsonPath().getBoolean("success"), "success should be false");
        assertTrue(res.jsonPath().getString("error").toLowerCase().contains("available"),
                "Error message should mention 'available'");
    }

    @Test(description = "POST /bookings -- should atomically decrement available seats")
    @Story("Seat Management")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldDecrementAvailableSeatsAtomically() {
        EventPayload eventData = TestDataGenerator.newEvent(20, 200);
        Response eventRes = api.createEvent(authToken, eventData);
        int seatEventId = eventRes.jsonPath().getInt("data.id");
        createdEventIds.add(seatEventId);

        // Book 5 seats
        BookingPayload booking = TestDataGenerator.newBooking(seatEventId, 5);
        Response bookingRes = api.createBooking(authToken, booking);
        createdBookingIds.add(bookingRes.jsonPath().getInt("data.id"));

        int availableSeats = api.getEventById(authToken, seatEventId)
                .jsonPath().getInt("data.availableSeats");
        assertEquals(availableSeats, 20 - 5, "Available seats should decrement by quantity booked");
    }
}
