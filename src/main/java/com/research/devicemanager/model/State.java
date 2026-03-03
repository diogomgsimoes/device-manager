package com.research.devicemanager.model;

import lombok.Getter;

@Getter
public enum State {
    AVAILABLE("available"),
    IN_USE("in-use"),
    INACTIVE("inactive");

    private final String name;

    State(String name) {
        this.name = name;
    }

}
