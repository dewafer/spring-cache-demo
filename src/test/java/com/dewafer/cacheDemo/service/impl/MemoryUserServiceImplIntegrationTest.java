package com.dewafer.cacheDemo.service.impl;

import com.dewafer.cacheDemo.service.UserService;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.stream.IntStream;

import static io.restassured.RestAssured.get;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MemoryUserServiceImplIntegrationTest {

    @LocalServerPort
    private int port;

    @Before
    public void setUp() throws Exception {
        RestAssured.port = port;
    }

    private static final String[] users = {"john", "james", "emma", "david", "alfred"};

    @SpyBean
    private UserService memoryUserService;

    @Test
    public void test_get_cached() throws Exception {
        // call 100 times
        IntStream.range(0, 100).forEach(i ->
                log.info(get("/user/{username}", users[i % users.length]).asString()));

        for (String username : users) {
            // should only execute at most 1 times for each username
            verify(memoryUserService, atMost(1)).findUserByUsername(username);
        }
    }

    @Test
    public void test_get_notFound() throws Exception {
        IntStream.range(0, 100).forEach(i ->
                log.info(get("/user/{username}", "not_exists_user").asString()));

        // execution with exception will not be cached
        verify(memoryUserService, times(100)).findUserByUsername(eq("not_exists_user"));
    }
}