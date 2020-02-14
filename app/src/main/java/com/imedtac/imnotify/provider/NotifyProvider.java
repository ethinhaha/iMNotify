package com.imedtac.imnotify.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.imedtac.imnotify.data.NotifyConstract;


public class NotifyProvider extends ContentProvider {
    private static final String TAG = "NotiftProvider";
    private DBversionOpenHelper mDbHelper;
    public static final String DBNAME = NotifyConstract.DBNAME;
    public static final String TABLE_NAME = NotifyConstract.TABLE_NAME;
    public static final int VERSION = NotifyConstract.VERSION;

    public static final String AUTOHORITY = NotifyConstract.AUTOHORITY;
    public static final String PATH = NotifyConstract.PATH;

    final static int CODE_INSERT = 0;
    final static int CODE_QUERY = 1;
    final static int CODE_DELETE = 2;
    final static int CODE_UPDATE = 3;

    private static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE " + NotifyConstract.TABLE_NAME + " (" +
                NotifyConstract.NotifyEntity._ID + " INTEGER PRIMARY KEY," +
                NotifyConstract.NotifyEntity.APP_NAME + " TEXT NOT NULL UNIQUE, " +
                NotifyConstract.NotifyEntity.APP_VERSION + " TEXT , " +
                NotifyConstract.NotifyEntity.APP_DOMAIN + " TEXT NOT NULL, " +
                NotifyConstract.NotifyEntity.APP_START + " TEXT NOT NULL " +
                ")";
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    {
        URI_MATCHER.addURI(AUTOHORITY, PATH+"/insert", CODE_INSERT);
        URI_MATCHER.addURI(AUTOHORITY, PATH+"/query", CODE_QUERY);
        URI_MATCHER.addURI(AUTOHORITY, PATH+"/delete", CODE_DELETE);
        URI_MATCHER.addURI(AUTOHORITY, PATH+"/update", CODE_UPDATE);
    }
    @Override
    public boolean onCreate() {
        mDbHelper = new DBversionOpenHelper(this.getContext());
        Log.d(TAG, "open/create table");
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;
        qb.setTables(TABLE_NAME);
        switch (URI_MATCHER.match(uri)) {
            case CODE_QUERY:
                cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                //注册内容观察者，观察数据变化
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            default:
                throw new IllegalArgumentException("未识别的uri" + uri);
        }

//        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
//        SQLiteDatabase db = mDbHelper.getReadableDatabase();
//        qb.setTables(TABLE_NAME);
//        Cursor c = qb.query(db, projection, selection, null, null, null, sortOrder);
//        c.setNotificationUri(getContext().getContentResolver(), uri);
//        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case CODE_INSERT:
                long rowId = db.insert(TABLE_NAME, null, values);
                Log.d("rowID",rowId+"");
                if (rowId == -1) {
                    //添加失败
                    return null;
                } else {
                    //发送内容广播
                    getContext().getContentResolver().notifyChange(uri, null);
                    //添加成功
                    return ContentUris.withAppendedId(uri, rowId);
                }
            default:
                throw new IllegalArgumentException("未识别的uri" + uri);
        }


//        SQLiteDatabase db = mDbHelper.getWritableDatabase();
//        long rowId = db.insert(TABLE_NAME,"",values);
//        if (rowId > 0) {
//            Uri rowUri = ContentUris.appendId(UpdateVSConstract.UpdateEntity.CONTENT_URI.buildUpon(), rowId).build();
//            getContext().getContentResolver().notifyChange(rowUri, null);
//            Log.d("rowUri",rowUri.toString());
//            return rowUri;
//        }
//        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case CODE_DELETE:
                int number = db.delete(TABLE_NAME, selection, selectionArgs);
                //发送内容广播
                getContext().getContentResolver().notifyChange(uri, null);
                return number;
            default:
                throw new IllegalArgumentException("未识别的uri" + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case CODE_UPDATE:
                int number = db.update(TABLE_NAME, values, selection, selectionArgs);
                //发送内容广播
                getContext().getContentResolver().notifyChange(uri, null);
                return number;
            default:
                Toast.makeText(getContext(),"錯誤uri",Toast.LENGTH_LONG).show();
                throw new IllegalArgumentException("未识别的uri" + uri);
        }
    }

    private static  class DBversionOpenHelper extends SQLiteOpenHelper {
        DBversionOpenHelper(Context context) {
            super(context, DBNAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + NotifyConstract.TABLE_NAME);
            onCreate(db);
        }

    }
}
