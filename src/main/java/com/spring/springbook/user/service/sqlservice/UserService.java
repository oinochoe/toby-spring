package com.spring.springbook.user.service.sqlservice;

import com.spring.springbook.user.domain.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface UserService {
    void add(User user);
    void deleteAll();
    void update(User user);

    @Transactional(readOnly=true)
    User get(String id);

    @Transactional(readOnly=true)
    List<User> getAll();

    void upgradeLevels();
}