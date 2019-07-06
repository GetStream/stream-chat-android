package com.getstream.sdk.chat;

public class Component {
    public static class Channel{
        public boolean invitation = true;
        private static boolean showReadIndicator = true;

        public static boolean isShowReadIndicator() {
            return showReadIndicator;
        }

        public static void setShowReadIndicator(boolean showReadIndicator_) {
            showReadIndicator = showReadIndicator_;
        }
    }

    public static class Chat{
        public static boolean groupChat = true;

    }

    public static class Message{

    }

    public static class User{

    }
}
