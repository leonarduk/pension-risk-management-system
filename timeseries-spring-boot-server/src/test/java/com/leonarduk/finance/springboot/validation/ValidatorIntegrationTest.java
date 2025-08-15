package com.leonarduk.finance.springboot.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ValidatorIntegrationTest {

    static class SampleBean {
        @NotNull
        private String value;
    }

    @Autowired
    private Validator validator;

    @Test
    void validatesConstraints() {
        SampleBean bean = new SampleBean();
        Set<ConstraintViolation<SampleBean>> violations = validator.validate(bean);
        assertEquals(1, violations.size());
    }
}
