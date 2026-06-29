package com.eventmanagement.base;

import com.eventmanagement.utils.ApiClient;
import com.eventmanagement.utils.ConfigReader;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;

/**
 * TestBase -- Foundation for all API test classes
 * ------------------------------------------------
 * Configures RestAssured globally, builds a shared RequestSpecification,
 * initialises the ApiClient utility, and provides a cached auth token.
 *
 * All test classes extend this.
 */
public class TestBase {

    protected RequestSpecification requestSpec;
    protected ApiClient api;
    protected String authToken;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        String apiUrl = ConfigReader.getProperty("api.url") != null
                ? ConfigReader.getProperty("api.url")
                : "https://api.eventhub.rahulshettyacademy.com/api";

        RestAssured.baseURI = apiUrl;

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(apiUrl)
                .setContentType(ContentType.JSON)
                .addFilter(new AllureRestAssured())          // Allure request/response capture
                .addFilter(new RequestLoggingFilter())       // Console logs for debugging
                .addFilter(new ResponseLoggingFilter())
                .build();

        api = new ApiClient(requestSpec);

        // Pre-fetch token for tests that need it
        String email    = ConfigReader.getProperty("user.email");
        String password = ConfigReader.getProperty("user.password");
        if (email != null && password != null) {
            try {
                authToken = api.getToken(email, password);
            } catch (Exception e) {
                System.err.println("[TestBase] Could not fetch auth token: " + e.getMessage());
            }
        }
    }
}
