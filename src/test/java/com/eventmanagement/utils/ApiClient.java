package com.eventmanagement.utils;

import com.eventmanagement.models.BookingPayload;
import com.eventmanagement.models.EventPayload;
import com.eventmanagement.models.UserPayload;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * ApiClient -- Reusable REST API Utility Class
 * ---------------------------------------------
 * Java equivalent of APIUtils.js from the Playwright suite.
 * Encapsulates all EventHub API interactions with proper
 * token management, error handling, and request/response logging.
 *
 * Usage:
 *   ApiClient api = new ApiClient(requestSpec);
 *   String token = api.getToken(email, password);
 *   Response event = api.createEvent(token, payload);
 */
public class ApiClient {

    private final RequestSpecification requestSpec;

    public ApiClient(RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
    }

    /* =============== AUTH =============== */

    /**
     * Register a new user account.
     * POST /auth/register
     */
    public Response register(String email, String password) {
        UserPayload payload = UserPayload.builder()
                .email(email)
                .password(password)
                .build();
        return given(requestSpec)
                .body(payload)
                .post("/auth/register");
    }

    /**
     * Login and return the full response (for assertion purposes).
     * POST /auth/login
     */
    public Response login(String email, String password) {
        UserPayload payload = UserPayload.builder()
                .email(email)
                .password(password)
                .build();
        return given(requestSpec)
                .body(payload)
                .post("/auth/login");
    }

    /**
     * Login and extract the JWT token directly.
     * Throws if login fails.
     */
    public String getToken(String email, String password) {
        Response response = login(email, password);
        if (response.statusCode() != 200) {
            throw new RuntimeException("Login failed [" + response.statusCode() + "]: " + response.asString());
        }
        return response.jsonPath().getString("token");
    }

    /**
     * Validate token and get current user identity.
     * GET /auth/me
     */
    public Response getMe(String token) {
        return given(requestSpec)
                .header("Authorization", "Bearer " + token)
                .get("/auth/me");
    }

    /* =============== EVENTS =============== */

    /**
     * Create a new event.
     * POST /events
     */
    public Response createEvent(String token, EventPayload payload) {
        return given(requestSpec)
                .header("Authorization", "Bearer " + token)
                .body(payload)
                .post("/events");
    }

    /**
     * Get all events with optional query params.
     * GET /events?page=1&limit=5&category=...&city=...&search=...
     */
    public Response getEvents(String token, Map<String, Object> params) {
        var req = given(requestSpec)
                .header("Authorization", "Bearer " + token);
        if (params != null) {
            params.forEach((k, v) -> {
                if (v != null) req.queryParam(k, v);
            });
        }
        return req.get("/events");
    }

    /** GET /events (no filters) */
    public Response getEvents(String token) {
        return getEvents(token, null);
    }

    /**
     * Get a single event by ID.
     * GET /events/:id
     */
    public Response getEventById(String token, int eventId) {
        return given(requestSpec)
                .header("Authorization", "Bearer " + token)
                .pathParam("id", eventId)
                .get("/events/{id}");
    }

    /**
     * Update an event.
     * PUT /events/:id
     */
    public Response updateEvent(String token, int eventId, EventPayload payload) {
        return given(requestSpec)
                .header("Authorization", "Bearer " + token)
                .body(payload)
                .pathParam("id", eventId)
                .put("/events/{id}");
    }

    /**
     * Delete an event.
     * DELETE /events/:id
     */
    public Response deleteEvent(String token, int eventId) {
        return given(requestSpec)
                .header("Authorization", "Bearer " + token)
                .pathParam("id", eventId)
                .delete("/events/{id}");
    }

    /* =============== BOOKINGS =============== */

    /**
     * Create a booking (buy tickets).
     * POST /bookings
     */
    public Response createBooking(String token, BookingPayload payload) {
        return given(requestSpec)
                .header("Authorization", "Bearer " + token)
                .body(payload)
                .post("/bookings");
    }

    /**
     * Get all bookings with optional filters.
     * GET /bookings?eventId=...&status=...&page=1&limit=10
     */
    public Response getBookings(String token, Map<String, Object> params) {
        var req = given(requestSpec)
                .header("Authorization", "Bearer " + token);
        if (params != null) {
            params.forEach((k, v) -> {
                if (v != null) req.queryParam(k, v);
            });
        }
        return req.get("/bookings");
    }

    /** GET /bookings (no filters) */
    public Response getBookings(String token) {
        return getBookings(token, null);
    }

    /**
     * Get a single booking by numeric ID.
     * GET /bookings/:id
     */
    public Response getBookingById(String token, int bookingId) {
        return given(requestSpec)
                .header("Authorization", "Bearer " + token)
                .pathParam("id", bookingId)
                .get("/bookings/{id}");
    }

    /**
     * Look up a booking by reference code (e.g. EVT-A1B2C3).
     * GET /bookings/ref/:ref
     */
    public Response getBookingByRef(String token, String ref) {
        return given(requestSpec)
                .header("Authorization", "Bearer " + token)
                .pathParam("ref", ref)
                .get("/bookings/ref/{ref}");
    }

    /**
     * Cancel a booking (restores seats).
     * DELETE /bookings/:id
     */
    public Response cancelBooking(String token, int bookingId) {
        return given(requestSpec)
                .header("Authorization", "Bearer " + token)
                .pathParam("id", bookingId)
                .delete("/bookings/{id}");
    }

    /* =============== HEALTH / CONFIG =============== */

    /**
     * Health check endpoint.
     * GET /health
     */
    public Response healthCheck() {
        return given(requestSpec).get("/health");
    }

    /**
     * Feature flags endpoint.
     * GET /config
     */
    public Response getConfig() {
        return given(requestSpec).get("/config");
    }

    /* =============== HELPERS =============== */

    /** Build a params map with a single key-value pair. */
    public static Map<String, Object> params(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    /** Build a params map with two key-value pairs. */
    public static Map<String, Object> params(String k1, Object v1, String k2, Object v2) {
        Map<String, Object> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }
}
