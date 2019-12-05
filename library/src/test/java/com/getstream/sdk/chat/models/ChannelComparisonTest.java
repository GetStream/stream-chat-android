package com.getstream.sdk.chat.models;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.core.Client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChannelComparisonTest {

    private static String TEST_CHANNEL_TYPE = "testChannelType";

    private static String TEST_CHANNEL_ID_1 = "testChannelId_1";
    private static String TEST_CHANNEL_ID_2 = "testChannelId_2";
    private static String TEST_CHANNEL_ID_3 = "testChannelId_3";

    @Mock
    Client client;

    private Channel x, y, z, x_clone;

    @BeforeEach
    void initTest() {
        MockitoAnnotations.initMocks(this);

        x = new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID_1);
        x_clone = new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID_1);
        y = new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID_2);
        z = new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID_3);
    }


    @Test
    void equalsTest() {

        assertTrue(x.equals(x));
        assertTrue(x.equals(x_clone));
        assertTrue(x_clone.equals(x));
        assertTrue(x.equals(x_clone) == x_clone.equals(x));

        x_clone.setName("Test Channel 1");
        assertTrue(x_clone.equals(x_clone));

        // TODO: I'm wandering that after changing some parameters x and x_clone should be the same.
        assertTrue(x.equals(x_clone));
        assertTrue(x_clone.equals(x));

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
