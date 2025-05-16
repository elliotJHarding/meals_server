package com.harding.meals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


@SpringBootTest
class MealsApplicationTests {

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

}
