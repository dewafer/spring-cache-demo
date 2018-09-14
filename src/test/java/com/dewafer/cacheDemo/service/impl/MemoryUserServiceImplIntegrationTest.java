package com.dewafer.cacheDemo.service.impl;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.util.stream.IntStream;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import com.dewafer.cacheDemo.model.Gender;
import com.dewafer.cacheDemo.model.User;
import com.dewafer.cacheDemo.service.UserService;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MemoryUserServiceImplIntegrationTest {

    @LocalServerPort
    private int port;

    @Before
    public void setUp() throws Exception {
        RestAssured.port = this.port;
    }

    private static final String[] users = { "john", "james", "emma", "david", "alfred" };

    @SpyBean
    private UserService memoryUserService;

    @Test
    public void test_get_cached() throws Exception {
        // call 100 times
        // @formatter:off
        IntStream.range(0, 100).forEach(i -> log.info(
            given()
                .log().uri()
            .when()
                .get("/user/{username}", users[i % users.length])
            .then()
                .log().all()
                .statusCode(is(HttpStatus.SC_OK))
            .extract()
                .asString()));
        // @formatter:on

        for (String username : users) {
            // should only execute exactly once only for each username
            then(this.memoryUserService).should(times(1)).findUserByUsername(username);
        }
    }

    @Test
    public void test_get_notFound() throws Exception {
        // @formatter:off
        IntStream.range(0, 100).forEach(i -> log.info(
            given()
                .log().uri()
            .when()
                .get("/user/{username}", "not_exists_user")
            .then()
                .log().all()
                .statusCode(is(HttpStatus.SC_NOT_FOUND))
            .extract()
                    .asString()));
        // @formatter:on

        // execution with exception will not be cached
        then(this.memoryUserService).should(times(100)).findUserByUsername(eq("not_exists_user"));
    }

    @Test
    public void test_created_and_cached() throws Exception {

        // create jack
        User jack = User.of("jack", "Jack", 55, Gender.MALE, null);

        // @formatter:off
        given()
            .log().all()
            .contentType(ContentType.JSON)
            .body(jack)
        .when()
            .post("/user/")
        .then()
            .log().all()
            .statusCode(is(HttpStatus.SC_OK));
        // @formatter:on

        // should be cached

        // @formatter:off
        given()
            .log().uri()
        .when()
            .get("/user/{username}", jack.getUsername())
        .then()
            .log().all()
            .statusCode(is(HttpStatus.SC_OK));
        // @formatter:on

        // add jack once
        then(this.memoryUserService).should(times(1)).addUser(eq(jack));
        // never queried because already cached
        then(this.memoryUserService).should(never()).findUserByUsername(eq(jack.getUsername()));

    }

    @Test
    public void test_updated_and_cached() throws Exception {

        // create jack
        User jack = User.of("jack", "Jack", 55, Gender.MALE, null);
        this.memoryUserService.addUser(jack);

        // update
        User updatedJack = User.of(jack.getUsername(), jack.getDisplayName(), 35, jack.getGender(), jack.getAddress());

        // @formatter:off
        given()
            .log().all()
            .contentType(ContentType.JSON)
            .body(updatedJack)
        .when()
            .put("/user/{username}", updatedJack.getUsername())
        .then()
            .log().all()
            .statusCode(is(HttpStatus.SC_OK));
        // @formatter:on

        // should be cached

        // @formatter:off
        given()
            .log().uri()
        .when()
            .get("/user/{username}", jack.getUsername())
        .then()
            .log().all()
            .statusCode(is(HttpStatus.SC_OK))
            .body("age", is(updatedJack.getAge()));
        // @formatter:on

        // update jack once
        then(this.memoryUserService).should(times(1)).updateUser(eq(updatedJack));
        // never queried because already cached
        then(this.memoryUserService).should(never()).findUserByUsername(eq(jack.getUsername()));

    }

    @Test
    public void test_deleted_and_evicted() throws Exception {

        // create jack
        User jack = User.of("jack", "Jack", 55, Gender.MALE, null);
        this.memoryUserService.addUser(jack);

        // confirm it's cached

        // @formatter:off
        given()
            .log().uri()
        .when()
            .get("/user/{username}", jack.getUsername())
        .then()
            .log().all()
            .statusCode(is(HttpStatus.SC_OK));
        // @formatter:on

        // delete
        // @formatter:off
        given()
            .log().all()
            .contentType(ContentType.JSON)
        .when()
            .delete("/user/{username}", jack.getUsername())
        .then()
            .log().all()
            .statusCode(is(HttpStatus.SC_OK));
        // @formatter:on

        // should be evicted

        // @formatter:off
        given()
            .log().uri()
        .when()
            .get("/user/{username}", jack.getUsername())
        .then()
            .log().all()
            .statusCode(is(HttpStatus.SC_NOT_FOUND));
        // @formatter:on

        // delete jack once
        then(this.memoryUserService).should(times(1)).deleteUser(jack.getUsername());
        // queried only once because cache has been evicted after deletion
        then(this.memoryUserService).should(times(1)).findUserByUsername(eq(jack.getUsername()));

    }
}
