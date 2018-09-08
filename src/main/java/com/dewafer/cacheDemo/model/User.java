package com.dewafer.cacheDemo.model;

import lombok.Value;

import javax.validation.constraints.NotEmpty;

@Value(staticConstructor = "of")
public class User {

    @NotEmpty
    private final String username;

    private final String displayName;

    private final Integer age;

    private final Gender gender;

    private final String address;

}
