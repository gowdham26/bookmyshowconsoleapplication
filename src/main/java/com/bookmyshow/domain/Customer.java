package com.bookmyshow.domain;

public record Customer(String name, String contact) {
    public Customer {
        name = Movie.requireText(name, "name");
        contact = Movie.requireText(contact, "contact");
    }

    public String getDetails() {
        return name + " (" + contact + ")";
    }
}
