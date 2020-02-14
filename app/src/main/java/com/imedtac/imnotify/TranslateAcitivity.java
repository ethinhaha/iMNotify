package com.imedtac.imnotify;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;


public class TranslateAcitivity extends AppCompatActivity {
    final String TAG="TranslateAcitivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.traslate_layout);
        Log.d(TAG,"onCreate");
        Intent intent = this.getIntent();
//取得傳遞過來的資料
        Bundle bundle= intent.getExtras();
        String pkg="";
        String cls="";
        if (bundle != null) {
            pkg = bundle.getString("pkg");
            cls = bundle.getString("cls");
        }
        Log.d(TAG,pkg);
        //notifyIntent.setComponent(new ComponentName("tw.org.cch.www.imrobot","tw.org.cch.www.imrobot.LoginActivity"));
//        notifyIntent.setComponent(new ComponentName(pkg,cls));
//        startActivity(notifyIntent);
//        this.finish();
        PackageManager manager = getPackageManager();
        Log.d(TAG,pkg);
        intent = manager.getLaunchIntentForPackage(pkg);
        //intent.addCategory(Intent.CATEGORY_LAUNCHER);
        if(intent!=null) {
            startActivity(intent);
        }else{
            Log.d(TAG,pkg);
            Log.d(TAG,cls);
            String package_name = pkg;
            String activity_path = cls;
            Intent notifyIntent=new Intent();
            //notifyIntent.setComponent(new ComponentName("tw.org.cch.www.imrobot","tw.org.cch.www.imrobot.LoginActivity"));
            //notifyIntent.setComponent(new ComponentName(pkg,cls));
            notifyIntent.setComponent(new ComponentName("com.imedtac.myapplication","com.imedtac.myapplication.WelcomeActivity"));
            startActivity(notifyIntent);
        }
        this.finish();
    }
    protected void onNewIntent(Intent intent){
        handleIntent(intent);
    }
    private void handleIntent(Intent intent) {

    }

}
