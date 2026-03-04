package com.research.devicemanager.model;

import lombok.Getter;

import java.util.Set;

@Getter
public enum State {
    AVAILABLE("available"),
    IN_USE("in_use"),
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

    public static boolean canTransition(State source, State target) {
        // Assumed a state-machine type of validation where:
        // - AVAILABLE devices can move to IN_USE or directly to INACTIVE
        // - IN_USE devices can move back to AVAILABLE or to INACTIVE
        // - INACTIVE devices cannot be re-used
        return switch (source) {
            case AVAILABLE -> Set.of(IN_USE, INACTIVE).contains(target);
            case IN_USE -> Set.of(AVAILABLE, INACTIVE).contains(target);
            case INACTIVE -> false;
        };
    }
}
