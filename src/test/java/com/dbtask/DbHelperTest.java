package com.dbtask;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DbHelperTest {
    @Test
    void getCollection() {
        assertDoesNotThrow(() -> new DbHelper().getCollection());
    }
}