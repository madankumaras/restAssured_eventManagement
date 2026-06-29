package com.eventmanagement.auth;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import com.eventmanagement.utils.ConfigReader;

import java.util.HashMap;
import java.util.Map;

public class AuthManager {

    private static String token;

    public static String getToken() {
        if (token != null) {
            return token;
        }

        Map<String, String> creds = new HashMap<>();
        creds.put("username", ConfigReader.getProperty("admin.username"));
        creds.put("password", ConfigReader.getProperty("admin.password"));

        Response response = RestAssured.given()
                .baseUri(ConfigReader.getProperty("base.url") != null ? 
                         ConfigReader.getProperty("base.url") : "http://localhost:3000")
                .contentType(ContentType.JSON)
                .body(creds)
                .post("/api/auth/login");

        if (response.getStatusCode() == 200) {
            token = response.jsonPath().getString("token");
        } else {
            System.err.println("Failed to fetch token: " + response.getStatusLine());
        }

        return token;
    }
}
