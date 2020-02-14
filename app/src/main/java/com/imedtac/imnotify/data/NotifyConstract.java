    package com.imedtac.imnotify.data;

    import android.provider.BaseColumns;

    public class NotifyConstract {
        public NotifyConstract(){

        }
        public static final String DBNAME = "Notify.db";
        public static final String TABLE_NAME = "Notify";
        public static final int VERSION = 1;

        public static final String AUTOHORITY = "com.imedtac.www.imnotify";
        public static final String PATH = "Notify";

        public static final class NotifyEntity implements BaseColumns{
            public static final String APP_NAME = "appname";
            public static final String APP_VERSION = "appversion";
            public static final String APP_DOMAIN="appdomain";
            public static final String APP_START="appstart";
        }

    }
