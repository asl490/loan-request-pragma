package com.pragma.bootcamp.enums;

import java.util.Arrays;

public enum Role {
    ADMIN(1L),
    ASESOR(2L),
    CLIENTE(3L);

    private final Long id;

    Role(Long id) {
        this.id = id;
    }

    public static Role fromId(Long id) {
        return Arrays.stream(values())
                .filter(role -> role.id.equals(id))
                .findFirst()
                .orElse(null);
    }

    public Long getId() {
        return id;
    }
}