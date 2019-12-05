package com.getstream.sdk.chat.models;

import com.getstream.sdk.chat.rest.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserComparisonTest {

    private static String TEST_USER_ID_1 = "testUserId_1";
    private static String TEST_USER_ID_2 = "testUserId_2";
    private static String TEST_USER_ID_3 = "testUserId_3";


    private User x, y, z, x_clone;

    @BeforeEach
    void initTest() {
        MockitoAnnotations.initMocks(this);

        x = new User(TEST_USER_ID_1);
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("group", "group chat");
        x_clone = new User(TEST_USER_ID_1, extraData);
        y = new User(TEST_USER_ID_2);
        z = new User(TEST_USER_ID_3);
    }


    @Test
    void equalsTest() {
        assertTrue(x.equals(x));
        assertTrue(x.equals(x_clone));
        assertTrue(x_clone.equals(x));
        assertTrue(x.equals(x_clone) == x_clone.equals(x));

        assertFalse(x.equals(y));
        assertTrue(x.equals(y) == y.equals(x));

        y.setId(x.getId()); z.setId(y.getId());

        if ((x.equals(y) && y.equals(z)))
            assertTrue(x.equals(z));
    }

    @Test
    void hashCodeTest() {
        assertEquals(x.hashCode(), x_clone.hashCode());
        assertNotEquals(x.hashCode(), y.hashCode());

        y.setId(x.getId());
        assertEquals(x.hashCode(), y.hashCode());
    }
}
