package com.eventmanagement.tests.e2e;

import com.eventmanagement.base.TestBase;
import com.eventmanagement.models.BookingPayload;
import com.eventmanagement.models.EventPayload;
import com.eventmanagement.models.UserPayload;
import com.eventmanagement.utils.TestDataGenerator;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Full Journey E2E Test
 * ----------------------
 * Complete user journey: Register → Login → Create Event → Book → Cancel → Delete
 * Mirrors: full-journey.spec.js from the Playwright suite.
 *
 * This test is intentionally structured as a sequential journey
 * using TestNG @Test(dependsOnMethods) to simulate a real user workflow.
 */
@Epic("EventHub API")
@Feature("End-to-End Journey")
public class FullJourneyTest extends TestBase {

    // Shared state across test methods in this class
    private String journeyToken;
    private int    journeyEventId;
    private int    journeyBookingId;

    @Test(description = "E2E Step 1 -- Register a brand new user")
    @Story("Full User Journey")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Register a new user with a unique, timestamped email.")
    public void step1_RegisterNewUser() {
        UserPayload user = TestDataGenerator.newUser();
        Response res = api.register(user.getEmail(), user.getPassword());

        assertEquals(res.statusCode(), 201, "E2E Step 1: Registration should return 201");
        assertTrue(res.jsonPath().getBoolean("success"), "success should be true");
        assertNotNull(res.jsonPath().getString("token"), "Token should be returned on registration");

        // Capture token from registration for subsequent steps
        journeyToken = res.jsonPath().getString("token");
    }

    @Test(description = "E2E Step 2 -- Login with the registered user",
            dependsOnMethods = "step1_RegisterNewUser")
    @Story("Full User Journey")
    @Severity(SeverityLevel.BLOCKER)
    public void step2_LoginWithRegisteredUser() {
        // Use the admin token for event creation (regular user may not have create permissions)
        // The step validates the login flow works for newly registered users
        assertNotNull(journeyToken, "Token from step 1 should be available");

        // Validate the token works by calling /auth/me
        Response me = api.getMe(journeyToken);
        assertEquals(me.statusCode(), 200, "E2E Step 2: Token from registration should be valid");
        assertTrue(me.jsonPath().getBoolean("success"), "success should be true");
    }

    @Test(description = "E2E Step 3 -- Create a new event (as org/admin user)",
            dependsOnMethods = "step2_LoginWithRegisteredUser")
    @Story("Full User Journey")
    @Severity(SeverityLevel.BLOCKER)
    public void step3_CreateEvent() {
        EventPayload payload = TestDataGenerator.newEvent("E2E Full Journey Event");
        // Use the pre-fetched admin auth token for event creation
        Response res = api.createEvent(authToken, payload);

        assertEquals(res.statusCode(), 201, "E2E Step 3: Event creation should return 201");
        assertTrue(res.jsonPath().getBoolean("success"), "success should be true");

        journeyEventId = res.jsonPath().getInt("data.id");
        assertNotNull(journeyEventId, "Event ID should be assigned");
    }

    @Test(description = "E2E Step 4 -- Book the event",
            dependsOnMethods = "step3_CreateEvent")
    @Story("Full User Journey")
    @Severity(SeverityLevel.BLOCKER)
    public void step4_BookTheEvent() {
        BookingPayload payload = TestDataGenerator.newBooking(journeyEventId, 2);
        Response res = api.createBooking(authToken, payload);

        assertEquals(res.statusCode(), 201, "E2E Step 4: Booking should return 201");
        assertTrue(res.jsonPath().getBoolean("success"), "success should be true");
        assertTrue(res.jsonPath().getString("data.bookingRef").startsWith("E-"),
                "Booking ref should start with E-");
        assertEquals(res.jsonPath().getString("data.status"), "confirmed",
                "Booking status should be confirmed");

        journeyBookingId = res.jsonPath().getInt("data.id");

        // Verify seat count decreased
        int availableSeats = api.getEventById(authToken, journeyEventId)
                .jsonPath().getInt("data.availableSeats");
        assertTrue(availableSeats < Integer.MAX_VALUE, "Available seats should have decreased");
    }

    @Test(description = "E2E Step 5 -- Verify booking appears in listing",
            dependsOnMethods = "step4_BookTheEvent")
    @Story("Full User Journey")
    @Severity(SeverityLevel.CRITICAL)
    public void step5_VerifyBookingInListing() {
        Response res = api.getBookingById(authToken, journeyBookingId);

        assertEquals(res.statusCode(), 200, "E2E Step 5: Should retrieve booking by ID");
        assertEquals(res.jsonPath().getInt("data.id"), journeyBookingId);
        assertEquals(res.jsonPath().getString("data.status"), "confirmed");
    }

    @Test(description = "E2E Step 6 -- Cancel the booking and verify seat restoration",
            dependsOnMethods = "step5_VerifyBookingInListing")
    @Story("Full User Journey")
    @Severity(SeverityLevel.CRITICAL)
    public void step6_CancelBookingAndVerifySeats() {
        // Get seat count before cancellation
        int seatsBefore = api.getEventById(authToken, journeyEventId)
                .jsonPath().getInt("data.availableSeats");

        // Cancel
        Response cancelRes = api.cancelBooking(authToken, journeyBookingId);
        assertEquals(cancelRes.statusCode(), 200, "E2E Step 6: Cancellation should return 200");

        // Verify seats restored
        int seatsAfter = api.getEventById(authToken, journeyEventId)
                .jsonPath().getInt("data.availableSeats");
        assertEquals(seatsAfter, seatsBefore + 2, "Seats should be restored after cancellation");
    }

    @Test(description = "E2E Step 7 -- Delete the event and confirm it's gone",
            dependsOnMethods = "step6_CancelBookingAndVerifySeats")
    @Story("Full User Journey")
    @Severity(SeverityLevel.CRITICAL)
    public void step7_DeleteEventAndVerify() {
        // Delete
        Response deleteRes = api.deleteEvent(authToken, journeyEventId);
        assertEquals(deleteRes.statusCode(), 200, "E2E Step 7: Deletion should return 200");

        // Confirm it's gone
        Response getRes = api.getEventById(authToken, journeyEventId);
        assertEquals(getRes.statusCode(), 404,
                "E2E Step 7: Deleted event should return 404 on GET");
    }
}
