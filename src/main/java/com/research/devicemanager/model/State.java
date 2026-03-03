package com.research.devicemanager.model;

import lombok.Getter;

@Getter
public enum State {
    AVAILABLE("available"),
    IN_USE("in-use"),
    INACTIVE("inactive");

    private final String value;

    State(String value) {
        this.value = value;
    }

    public static State fromValue(String value) {
        for (State state : values()) {
            if (state.value.equalsIgnoreCase(value)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Invalid state: " + value);
    }
}
