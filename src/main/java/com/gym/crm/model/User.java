package com.gym.crm.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
public abstract class User {
    private final String firstName;
    private final String lastName;
    private final String username;
    private final String password;
    private final Boolean isActive;
}
