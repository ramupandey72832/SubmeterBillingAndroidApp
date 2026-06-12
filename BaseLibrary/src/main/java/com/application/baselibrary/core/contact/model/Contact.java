package com.application.baselibrary.core.contact.model;

import java.util.ArrayList;
import java.util.List;

public class Contact {
    public String id;
    public String name;
    public List<String> phones = new ArrayList<>();
    public List<String> emails = new ArrayList<>();
    public long lastUpdated;

    @Override
    public String toString() {
        return "Contact{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", phones=" + phones +
                '}';
    }
}

