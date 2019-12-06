package com.getstream.sdk.chat.models;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.core.Client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

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

    private Channel a, b, c, aClone;

    @BeforeEach
    void initTest() {
        MockitoAnnotations.initMocks(this);

        a = new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID_1);
        aClone = new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID_1);
        b = new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID_2);
        c = new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID_3);
    }

    @Test
    void equalsIsReflexive() {
        assertTrue(a.equals(a));
    }

    @Test
    void equalsIsClone() {
        assertTrue(a.equals(aClone) && aClone.equals(a));
    }

    @Test
    void notEqualsIsOther() {
        assertFalse(a.equals(b) && b.equals(a));
    }

    @Test
    void equalsIsTransitive() {
        b.setId(a.getId());
        c.setId(b.getId());
        assertTrue(a.equals(b) && b.equals(c) && a.equals(c));
    }

    @Test
    void equalsIsChangedProperty() {
        aClone.setName("Test Channel 1");
        // I'm wandering that after changing some parameters a and aClone should be the same.
        assertTrue(a.equals(aClone) && aClone.equals(a));
    }


    @Test
    void hashCodeEqualIsClone() {
        assertEquals(a.hashCode(), aClone.hashCode());
    }

    @Test
    void hashCodeNotEqualsIsOther() {
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCodeBucketLocation() {
        Map<Channel, String> map1 = new HashMap<>();
        Map<Channel, String> map2 = new HashMap<>();

        map1.put(a, "a");
        map1.put(aClone, "aClone");

        map2.put(a, "a");
        map2.put(b, "b");

        assertTrue(map1.size() == 1
                && map1.get(a).equals("aClone")
                && map1.get(aClone).equals("aClone")
                && map2.size() == 2
                && map2.get(a).equals("a")
                && map2.get(b).equals("b"));
    }
}
