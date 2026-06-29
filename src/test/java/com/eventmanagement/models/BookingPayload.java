package com.eventmanagement.models;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * BookingPayload -- Request body for POST /bookings
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingPayload {
    private Integer eventId;
    private Integer quantity;
    private String  customerName;
    private String  customerEmail;
    private String  customerPhone;

    private BookingPayload() {}

    public Integer getEventId()       { return eventId; }
    public Integer getQuantity()      { return quantity; }
    public String  getCustomerName()  { return customerName; }
    public String  getCustomerEmail() { return customerEmail; }
    public String  getCustomerPhone() { return customerPhone; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final BookingPayload obj = new BookingPayload();
        public Builder eventId(int v)         { obj.eventId       = v; return this; }
        public Builder quantity(int v)        { obj.quantity       = v; return this; }
        public Builder customerName(String v) { obj.customerName   = v; return this; }
        public Builder customerEmail(String v){ obj.customerEmail  = v; return this; }
        public Builder customerPhone(String v){ obj.customerPhone  = v; return this; }
        public BookingPayload build()         { return obj; }
    }
}
