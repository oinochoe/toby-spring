package com.spring.springbook.user.service;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static com.spring.springbook.user.service.UserService.MIN_LOGCOUNT_FOR_SILVER;
import static com.spring.springbook.user.service.UserService.MIN_RECCOMEND_FOR_GOLD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.spring.springbook.user.dao.UserDao;
import com.spring.springbook.user.domain.Level;
import com.spring.springbook.user.domain.User;
import org.springframework.transaction.PlatformTransactionManager;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/test-applicationContext.xml")
public class UserServiceTest {
    @Autowired UserService userService;
    @Autowired UserDao userDao;
    @Autowired MailSender mailSender;
    @Autowired PlatformTransactionManager transactionManager;

    List<User> users; // fixture

    @Before
    public void setUp() {
        users = Arrays.asList(
                // 테스트에서는 가능한한 경계값 사용 (ex) -1)
                new User("kym", "김영민", "p1", "copstyle@naver.com", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER-1, 0),
                new User("psm", "박성민", "p2", "copstyle@naver.com", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0),
                new User("aji", "안재일", "p3", "copstyle@naver.com", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD-1),
                new User("cuh", "최운호", "p4", "copstyle@naver.com", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD),
                new User("lsb", "이수봉", "p5", "copstyle@naver.com", Level.GOLD, 100, Integer.MAX_VALUE)
        );
    }

    @Test @DirtiesContext
    public void upgradeLevels() {
        userDao.deleteAll();
        for(User user : users) userDao.add(user);

        MockMailSender mockMailSender = new MockMailSender();
        userService.setMailSender(mockMailSender);

        userService.upgradeLevels();

        checkLevelUpgraded(users.get(0), false);
        checkLevelUpgraded(users.get(1), true);
        checkLevelUpgraded(users.get(2), false);
        checkLevelUpgraded(users.get(3), true);
        checkLevelUpgraded(users.get(4), false);

        List<String> request = mockMailSender.getRequests();
        assertThat(request.size(), is(2));
        assertThat(request.get(0), is(users.get(1).getEmail()));
        assertThat(request.get(1), is(users.get(3).getEmail()));
    }

    static class MockMailSender implements MailSender {
        private List<String> requests = new ArrayList<String>();

        public List<String> getRequests() {
            return requests;
        }

        public void send(SimpleMailMessage mailMessage) throws MailException {
            requests.add(mailMessage.getTo()[0]);
        }

        public void send(SimpleMailMessage[] mailMessage) throws MailException {
        }
    }


    private void checkLevelUpgraded(User user, boolean upgraded) {
        User userUpdate = userDao.get(user.getId());
        if (upgraded) {
            assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
        }
        else {
            assertThat(userUpdate.getLevel(), is(user.getLevel()));
        }
    }

    @Test
    public void add() {
        userDao.deleteAll();

        User userWithLevel = users.get(4);	  // GOLD 레벨
        User userWithoutLevel = users.get(0);
        userWithoutLevel.setLevel(null);

        userService.add(userWithLevel);
        userService.add(userWithoutLevel);

        User userWithLevelRead = userDao.get(userWithLevel.getId());
        User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

        assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel()));
        assertThat(userWithoutLevelRead.getLevel(), is(Level.BASIC));
    }

    @Test
    public void upgradeAllOrNothing() {
        UserService testUserService = new TestUserService(users.get(3).getId());
        testUserService.setUserDao(this.userDao);
        testUserService.setTransactionManager(this.transactionManager);
        testUserService.setMailSender(this.mailSender);

        userDao.deleteAll();
        for(User user : users) userDao.add(user);

        try {
            testUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        }
        catch(TestUserServiceException e) {
        }

        checkLevelUpgraded(users.get(1), false);
    }


    static class TestUserService extends UserService {
        private String id;

        private TestUserService(String id) {
            this.id = id;
        }

        protected void upgradeLevel(User user) {
            if (user.getId().equals(this.id)) throw new TestUserServiceException();
            super.upgradeLevel(user);
        }
    }

    static class TestUserServiceException extends RuntimeException {
    }



}