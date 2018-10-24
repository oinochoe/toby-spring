package com.spring.springbook.user.service;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static com.spring.springbook.user.service.UserService.MIN_LOGCOUNT_FOR_SILVER;
import static com.spring.springbook.user.service.UserService.MIN_RECCOMEND_FOR_GOLD;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.spring.springbook.user.dao.UserDao;
import com.spring.springbook.user.domain.Level;
import com.spring.springbook.user.domain.User;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/test-applicationContext.xml")
public class UserServiceTest {
    @Autowired 	UserService userService;
    @Autowired UserDao userDao;

    List<User> users;

    @Before
    public void setUp() {
        users = Arrays.asList(
                // 테스트에서는 가능한한 경계값 사용 (ex) -1)
                new User("kym", "김영민", "p1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER-1, 0),
                new User("psm", "박성민", "p2", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0),
                new User("aji", "안재일", "p3", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD-1),
                new User("cuh", "최운호", "p4", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD),
                new User("lsb", "이수봉", "p5", Level.GOLD, 100, Integer.MAX_VALUE)
        );
    }

    @Test
    public void upgradeLevels() {
        userDao.deleteAll();
        for(User user : users) userDao.add(user);

        userService.upgradeLevels();

        checkLevelUpgraded(users.get(0), false);
        checkLevelUpgraded(users.get(1), true);
        checkLevelUpgraded(users.get(2), false);
        checkLevelUpgraded(users.get(3), true);
        checkLevelUpgraded(users.get(4), false);
    }

    // 다음 레벨로 업그레이드 될 것인가 아닌가를 지정한다.
    private void checkLevelUpgraded(User user, boolean upgraded) {
        User userUpdate = userDao.get(user.getId());
        if (upgraded) {
            // 다음레벨이 무엇인가는 Level에게 물어보면 된다.
            assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
            // 업그레이드가 일어났는지 확인
        }
        else {
            assertThat(userUpdate.getLevel(), is(user.getLevel()));
            // 업그레이드가 일어나지 않았는지 확인
        }
    }

    @Test
    public void add() {
        userDao.deleteAll();

        // GOLD 레벨이 이미 지정된 User라면 레벨을 초기화하지 않아야 한다.
        User userWithLevel = users.get(4);	  // GOLD 레벨
        User userWithoutLevel = users.get(0);
        userWithoutLevel.setLevel(null);

        userService.add(userWithLevel);
        userService.add(userWithoutLevel);

        // DB에 저장된 결과를 가져와 확인한다.
        User userWithLevelRead = userDao.get(userWithLevel.getId());
        User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

        assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel()));
        assertThat(userWithoutLevelRead.getLevel(), is(Level.BASIC));
    }

    @Test
    public void upgradeAllOrNothing() {
        UserService testUserService = new TestUserService(users.get(3).getId());
        testUserService.setUserDao(this.userDao);
        userDao.deleteAll();
        for(User user : users) userDao.add(user);

        try {
            // TestUserService는 업그레이드 작업 중에 예외가 발생해야 한다.
            // 정상종료라면 문제가 있으니 실패.
            testUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        }
        catch(TestUserServiceException e) {
            // TestUserService가 던져주는 예외를 잡아서 계속 진행하도록 한다. 그 외의 예외라면 테스트 실패.
        }

        checkLevelUpgraded(users.get(1), false);
        // 예외가 발생하기 전에 레벨 변경이 있었던 사용자의 레벨이 처음 상태로 바뀌었나 확인.
    }

    static class TestUserService extends UserService {
        private String id;

        // 예외를 발생시킬 User 오브젝트의 id를 지정할 수 있게 만든다.
        private TestUserService(String id) {
            this.id = id;
        }

        protected void upgradeLevel(User user) { // UserService의 메소드를 오버라이드 한다.
            if (user.getId().equals(this.id)) throw new TestUserServiceException();
            // 지정된 id의 User 오브젝트가 발견되면 예외를 던져서 작업을 강제로 중단시킨다.
            super.upgradeLevel(user);
        }
    }

    static class TestUserServiceException extends RuntimeException {
    }
}
