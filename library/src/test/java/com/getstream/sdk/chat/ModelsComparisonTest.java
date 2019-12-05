package com.getstream.sdk.chat;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.core.Client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelsComparisonTest {

    private static String TEST_CHANNEL_TYPE = "testChannelType";

    private static String TEST_CHANNEL_ID_1 = "testChannelId_1";
    private static String TEST_CHANNEL_ID_2 = "testChannelId_2";
    private static String TEST_CHANNEL_ID_3 = "testChannelId_3";

    @Mock
    Client client;

    private Channel x, y, z, x_another;
    private boolean xy, yz, zx;

    @BeforeEach
    void initTest() {
        MockitoAnnotations.initMocks(this);

        x = new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID_1);
        x_another = new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID_1);
        y = new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID_2);
        z = new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID_3);
    }

    void  initEquals(){
        xy = x.equals(y);
        yz = y.equals(z);
        zx = x.equals(z);
    }

    @Test
    void comparisonChannels() {

        assertTrue(x.equals(x));
        assertTrue(x.equals(x_another));
        assertTrue(x_another.equals(x));
        assertTrue(x.equals(x_another) == x_another.equals(x));
        assertTrue(x.equals(y) == y.equals(x));

        y.setId(x.getId());
        assertTrue(x.equals(y) && y.equals(x));

        initTest();
        initEquals();

        if ((xy && yz))
            assertTrue(zx);

    }
}
