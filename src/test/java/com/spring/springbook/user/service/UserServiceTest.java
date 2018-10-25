package com.spring.springbook.user.service;

import static com.spring.springbook.user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static com.spring.springbook.user.service.UserServiceImpl.MIN_RECCOMEND_FOR_GOLD;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
    @Autowired UserServiceImpl userServiceImpl;
    @Autowired MailSender mailSender;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired ApplicationContext context;

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

    @Test
    public void upgradeLevels() throws Exception {
        UserServiceImpl userServiceImpl = new UserServiceImpl();

        MockUserDao mockUserDao = new MockUserDao(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        MockMailSender mockMailSender = new MockMailSender();
        userServiceImpl.setMailSender(mockMailSender);

        userServiceImpl.upgradeLevels();

        List<User> updated = mockUserDao.getUpdated();
        assertThat(updated.size(), is(2));
        checkUserAndLevel(updated.get(0), "psm", Level.SILVER);
        checkUserAndLevel(updated.get(1), "cuh", Level.GOLD);

        List<String> request = mockMailSender.getRequests();
        assertThat(request.size(), is(2));
        assertThat(request.get(0), is(users.get(1).getEmail()));
        assertThat(request.get(1), is(users.get(3).getEmail()));
    }

    private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel) {
        assertThat(updated.getId(), is(expectedId));
        assertThat(updated.getLevel(), is(expectedLevel));
    }


    static class MockUserDao implements UserDao {
        private List<User> users;
        private List<User> updated = new ArrayList();

        private MockUserDao(List<User> users) {
            this.users = users;
        }

        public List<User> getUpdated() {
            return this.updated;
        }

        public List<User> getAll() {
            return this.users;
        }

        public void update(User user) {
            updated.add(user);
        }

        public void add(User user) { throw new UnsupportedOperationException(); }
        public void deleteAll() { throw new UnsupportedOperationException(); }
        public User get(String id) { throw new UnsupportedOperationException(); }
        public int getCount() { throw new UnsupportedOperationException(); }
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
    @DirtiesContext
    public void upgradeAllOrNothing() {
        TestUserService testUserService = new TestUserService(users.get(3).getId());
        testUserService.setUserDao(userDao);
        testUserService.setMailSender(mailSender);

        ProxyFactoryBean txProxyFactoryBean =
                context.getBean("&userService", ProxyFactoryBean.class);
        txProxyFactoryBean.setTarget(testUserService);
        UserService txUserService = (UserService) txProxyFactoryBean.getObject();

        userDao.deleteAll();
        for(User user : users) userDao.add(user);

        try {
            txUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        }
        catch(TestUserServiceException e) {
        }

        checkLevelUpgraded(users.get(1), false);
    }


    static class TestUserService extends UserServiceImpl {
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