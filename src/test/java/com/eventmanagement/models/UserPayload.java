package com.eventmanagement.models;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * UserPayload -- Request body for /auth/register and /auth/login
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserPayload {
    private String email;
    private String password;

    private UserPayload() {}

    public String getEmail()    { return email; }
    public String getPassword() { return password; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final UserPayload obj = new UserPayload();
        public Builder email(String v)    { obj.email = v;    return this; }
        public Builder password(String v) { obj.password = v; return this; }
        public UserPayload build()        { return obj; }
    }
}
