package com.getstream.sdk.chat.models;

import com.getstream.sdk.chat.rest.Message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MessageComparisonTest {

    private static String TEST_MESSAGE_ID_1 = "testMessageId_1";
    private static String TEST_MESSAGE_ID_2 = "testMessageId_2";
    private static String TEST_MESSAGE_ID_3 = "testMessageId_3";

    private Message x, y, z, x_clone;

    @BeforeEach
    void initTest() {
        MockitoAnnotations.initMocks(this);
        x = new Message(); x.setId(TEST_MESSAGE_ID_1);

        x_clone = new Message(); x_clone.setId(TEST_MESSAGE_ID_1);
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("group", "group chat");
        x_clone.setExtraData(extraData);

        y = new Message(); y.setId(TEST_MESSAGE_ID_2);

        z = new Message(); z.setId(TEST_MESSAGE_ID_3);
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

        Map<Message, String> myMap = new HashMap<>();
        myMap.put(x,"x");
        myMap.put(x_clone, "x_clone");
        assertTrue(myMap.size() == 1
                && myMap.get(x).equals("x_clone")
                && myMap.get(x_clone).equals("x_clone"));

        assertNotEquals(x.hashCode(), y.hashCode());
        myMap.clear();
        myMap.put(x,"x");
        myMap.put(y, "y");
        assertTrue(myMap.size() == 2
                && myMap.get(x).equals("x")
                && myMap.get(y).equals("y"));

        y.setId(x.getId());
        assertEquals(x.hashCode(), y.hashCode());
    }
}
