package com.spring.springbook.user.domain;

import lombok.Data;

@Data
public class User {
    String id;
    String name;
    String password;

    public User() {
    }

    public User(String id, String name, String password) {
        super();
        this.id = id;
        this.name = name;
        this.password = password;
    }

}
