package com.lightbend.akka.sample.httpsample;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Product {
    final String id;
    final String price;

    @JsonCreator
    public Product(@JsonProperty("id") String id,
                   @JsonProperty("price") String price) {
        this.id = id;
        this.price = price;
    }

    public String getPrice() {
        return price;
    }

    public String getId() {
        return id;
    }
}
