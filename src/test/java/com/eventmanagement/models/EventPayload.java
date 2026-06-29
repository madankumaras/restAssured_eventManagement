package com.eventmanagement.models;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * EventPayload -- Request body for POST /events and PUT /events/:id
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventPayload {
    private String  title;
    private String  description;
    private String  category;
    private String  venue;
    private String  city;
    private String  eventDate;
    private Integer price;
    private Integer totalSeats;
    private String  imageUrl;

    private EventPayload() {}

    public String  getTitle()       { return title; }
    public String  getDescription() { return description; }
    public String  getCategory()    { return category; }
    public String  getVenue()       { return venue; }
    public String  getCity()        { return city; }
    public String  getEventDate()   { return eventDate; }
    public Integer getPrice()       { return price; }
    public Integer getTotalSeats()  { return totalSeats; }
    public String  getImageUrl()    { return imageUrl; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final EventPayload obj = new EventPayload();
        public Builder title(String v)       { obj.title       = v; return this; }
        public Builder description(String v) { obj.description = v; return this; }
        public Builder category(String v)    { obj.category    = v; return this; }
        public Builder venue(String v)       { obj.venue       = v; return this; }
        public Builder city(String v)        { obj.city        = v; return this; }
        public Builder eventDate(String v)   { obj.eventDate   = v; return this; }
        public Builder price(int v)          { obj.price       = v; return this; }
        public Builder totalSeats(int v)     { obj.totalSeats  = v; return this; }
        public Builder imageUrl(String v)    { obj.imageUrl    = v; return this; }
        public EventPayload build()          { return obj; }
    }
}
