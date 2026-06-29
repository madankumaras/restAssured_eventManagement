package com.eventmanagement.utils;

import com.eventmanagement.models.BookingPayload;
import com.eventmanagement.models.EventPayload;
import com.eventmanagement.models.UserPayload;
import com.github.javafaker.Faker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * TestDataGenerator -- Dynamic Test Data Factory
 * -----------------------------------------------
 * Java equivalent of TestDataGenerator.js.
 * Generates realistic, randomized test data using JavaFaker.
 * Every method returns fresh data on each call.
 */
public class TestDataGenerator {

    private static final Faker faker = new Faker();
    private static final Random random = new Random();

    private static final List<String> CATEGORIES = Arrays.asList(
            "Conference", "Concert", "Sports", "Workshop", "Festival"
    );
    private static final List<String> CITIES = Arrays.asList(
            "Bangalore", "Mumbai", "Delhi", "Hyderabad", "Chennai", "Pune"
    );

    /** Generate a random user for registration. */
    public static UserPayload newUser() {
        long timestamp = System.currentTimeMillis();
        return UserPayload.builder()
                .email("testuser_" + timestamp + "@test.com")
                .password("Test@" + faker.regexify("[A-Za-z0-9]{8}"))
                .build();
    }

    /** Generate a random event payload for API creation. */
    public static EventPayload newEvent() {
        return newEvent(null, null, null, -1, -1);
    }

    /** Generate event with a custom title. */
    public static EventPayload newEvent(String title) {
        return newEvent(title, null, null, -1, -1);
    }

    /** Generate event with a custom city. */
    public static EventPayload newEventWithCity(String city) {
        return newEvent(null, null, city, -1, -1);
    }

    /** Generate event with custom totalSeats and price. */
    public static EventPayload newEvent(int totalSeats, int price) {
        return newEvent(null, null, null, totalSeats, price);
    }

    /** Full builder — any argument left as null/-1 will use a random value. */
    public static EventPayload newEvent(String title, String category, String city,
                                       int totalSeats, int price) {
        String futureDate = LocalDateTime.now()
                .plusDays(faker.number().numberBetween(30, 365))
                .format(DateTimeFormatter.ISO_DATE_TIME);

        return EventPayload.builder()
                .title(title != null ? title
                        : faker.company().catchPhrase() + " " + faker.number().numberBetween(2025, 2027))
                .description(faker.lorem().paragraph())
                .category(category != null ? category : CATEGORIES.get(random.nextInt(CATEGORIES.size())))
                .venue(faker.address().streetAddress() + ", " + faker.address().city())
                .city(city != null ? city : CITIES.get(random.nextInt(CITIES.size())))
                .eventDate(futureDate)
                .price(price > 0 ? price : faker.number().numberBetween(100, 5000))
                .totalSeats(totalSeats > 0 ? totalSeats : faker.number().numberBetween(50, 500))
                .imageUrl("https://picsum.photos/seed/" + faker.regexify("[a-z0-9]{6}") + "/800/400")
                .build();
    }

    /** Generate a full booking payload (customer + event reference). */
    public static BookingPayload newBooking(int eventId) {
        return newBooking(eventId, faker.number().numberBetween(1, 5));
    }

    /** Generate a booking with a specific quantity. */
    public static BookingPayload newBooking(int eventId, int quantity) {
        return BookingPayload.builder()
                .eventId(eventId)
                .quantity(quantity)
                .customerName(faker.name().fullName())
                .customerEmail(faker.internet().emailAddress())
                .customerPhone("+91-" + faker.regexify("[0-9]{10}"))
                .build();
    }

    /** Generate invalid email formats for negative testing. */
    public static List<String> invalidEmails() {
        return Arrays.asList(
                "noatsign.com",
                "missing@domain",
                "@nodomain.com",
                "spaces in@email.com"
        );
    }

    /** Generate invalid passwords for negative testing. */
    public static List<String> invalidPasswords() {
        return Arrays.asList(
                "",       // empty
                "ab",     // too short (< 6)
                "12345"   // 5 chars
        );
    }
}
