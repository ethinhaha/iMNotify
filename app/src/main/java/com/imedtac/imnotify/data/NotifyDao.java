package com.imedtac.imnotify.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class NotifyDao {

    public static final String DBNAME = NotifyConstract.DBNAME;
    public static final String TABLE_NAME = NotifyConstract.TABLE_NAME;
    public static final int VERSION = NotifyConstract.VERSION;

    public static final String AUTOHORITY = NotifyConstract.AUTOHORITY;
    public static final String PATH = NotifyConstract.PATH;

    final static int CODE_INSERT = 0;
    final static int CODE_QUERY = 1;
    final static int CODE_DELETE = 2;
    final static int CODE_UPDATE = 3;

    final static Uri URI_INSERT=Uri.parse("content://" + AUTOHORITY + "/"+PATH+"/insert");
    final static Uri URI_QUERY=Uri.parse("content://" + AUTOHORITY + "/"+PATH+"/query");
    final static Uri URI_DELETE=Uri.parse("content://" + AUTOHORITY + "/"+PATH+"/delete");
    final static Uri URI_UPDATE=Uri.parse("content://" + AUTOHORITY + "/"+PATH+"/update");

    public static boolean insert(ContentResolver resolver,DataConstract updatevconstract) {
        ContentValues values = new ContentValues();
        values.put(NotifyConstract.NotifyEntity.APP_NAME, updatevconstract.getAPPNAME());
        values.put(NotifyConstract.NotifyEntity.APP_VERSION, updatevconstract.getAPPVERSION());
        values.put(NotifyConstract.NotifyEntity.APP_DOMAIN, updatevconstract.getAPPDOAMIN());
        values.put(NotifyConstract.NotifyEntity.APP_START, updatevconstract.getAPPSTART());
        Log.d("Daoinsert",updatevconstract.getAPPNAME()+","+updatevconstract.getAPPVERSION());
        Uri newID=resolver.insert(URI_INSERT, values);
        if(newID!=null) {
            return true;
        }else{
            return false;
        }
    }

    public static int delete(ContentResolver resolver, String appname) {
        return resolver.delete(URI_DELETE, NotifyConstract.NotifyEntity.APP_NAME+" = '" + appname+"'", null);
    }
    public static int deleteAll(ContentResolver resolver) {
        return resolver.delete(URI_DELETE, null, null);
    }
    public static int update(ContentResolver resolver, DataConstract updatevconstract){
        ContentValues values = new ContentValues();
        values.put(NotifyConstract.NotifyEntity.APP_NAME, updatevconstract.getAPPNAME());
        values.put(NotifyConstract.NotifyEntity.APP_VERSION, updatevconstract.getAPPVERSION());
        values.put(NotifyConstract.NotifyEntity.APP_DOMAIN, updatevconstract.getAPPDOAMIN());
        values.put(NotifyConstract.NotifyEntity.APP_START, updatevconstract.getAPPSTART());
        return resolver.update(URI_UPDATE,values,NotifyConstract.NotifyEntity.APP_NAME+"='"+updatevconstract.getAPPNAME()+"'",null);
    }

    public static List<DataConstract> query(ContentResolver resolver) {
        List<DataConstract> list = new ArrayList<>();
        Cursor cursor = resolver.query(URI_QUERY, null, null, null, null);
        while (cursor.moveToNext()) {
            DataConstract dataConstract = new DataConstract();
            dataConstract.setAPPNAME(cursor.getString(cursor.getColumnIndex(NotifyConstract.NotifyEntity.APP_NAME)));
            dataConstract.setAPPVERSION(cursor.getString(cursor.getColumnIndex(NotifyConstract.NotifyEntity.APP_VERSION)));
            dataConstract.setAPPDOAMIN(cursor.getString(cursor.getColumnIndex(NotifyConstract.NotifyEntity.APP_DOMAIN)));
            dataConstract.setAPPSTART(cursor.getString(cursor.getColumnIndex(NotifyConstract.NotifyEntity.APP_START)));
            list.add(dataConstract);
        }
        cursor.close();
        return list;
    }
    public static DataConstract querybyname(ContentResolver resolver,String appName) {
        List<DataConstract> list = new ArrayList<>();
        Cursor cursor = resolver.query(URI_QUERY, null, ""+NotifyConstract.NotifyEntity.APP_NAME+"='"+appName+"'", null, null);
        int index=0;
        DataConstract data = new DataConstract();
        while (cursor.moveToNext()) {
            data.setAPPNAME(cursor.getString(cursor.getColumnIndex(NotifyConstract.NotifyEntity.APP_NAME)));
            data.setAPPVERSION(cursor.getString(cursor.getColumnIndex(NotifyConstract.NotifyEntity.APP_VERSION)));
            data.setAPPDOAMIN(cursor.getString(cursor.getColumnIndex(NotifyConstract.NotifyEntity.APP_DOMAIN)));
            data.setAPPSTART(cursor.getString(cursor.getColumnIndex(NotifyConstract.NotifyEntity.APP_START)));
            list.add(index, data);
            index++;
        }
        cursor.close();
        return data;
    }
    public static class DataConstract{
        public DataConstract(){
        }
        private String APPNAME;
        private String APPVERSION;
        private String APPDOAMIN;
        private String APPSTART;

        public String getAPPNAME() {
            return APPNAME;
        }

        public String getAPPVERSION() {
            return APPVERSION;
        }

        public String getAPPDOAMIN() {
            return APPDOAMIN;
        }

        public String getAPPSTART() {
            return APPSTART;
        }

        public void setAPPNAME(String APPNAME) {
            this.APPNAME = APPNAME;
        }

        public void setAPPVERSION(String APPVERSION) {
            this.APPVERSION = APPVERSION;
        }

        public void setAPPDOAMIN(String APPDOAMIN) {
            this.APPDOAMIN = APPDOAMIN;
        }

        public void setAPPSTART(String APPSTART) {
            this.APPSTART = APPSTART;
        }
    }
}

