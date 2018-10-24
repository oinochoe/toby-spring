package com.spring.springbook.user.service;

import com.spring.springbook.user.domain.User;

public interface UserService {
    void add(User user);
    void upgradeLevels();
}