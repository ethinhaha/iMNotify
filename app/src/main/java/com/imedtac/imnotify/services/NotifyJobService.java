package com.imedtac.imnotify.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.imedtac.imnotify.R;
import com.imedtac.imnotify.TranslateAcitivity;
import com.imedtac.imnotify.data.NotifyDao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class NotifyJobService extends JobService {
    RequestQueue requestQueue;
    final String TAG="NotifyJobService";
    String time_now="";
        JobScheduler js;
    JobInfo.Builder builder;
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG,"Job Start");
        SharedPreferences pref=getSharedPreferences("NOTIFY_SETTING",MODE_PRIVATE);
        int cycletime=Integer.parseInt(pref.getString("CYCLE_TIME","30"))*1000;
        //int cycletime=Integer.parseInt(pref.getString("CYCLE_TIME","10"))*1000;
        try {
            doBackground(params);
        }catch (Exception e){
            Log.d(TAG,"Job Finished"+e.toString());
            jobFinished(params, true);
            ComponentName componentName = new ComponentName(getBaseContext(), NotifyJobService.class);
            JobInfo jobInfo = new JobInfo.Builder(1, componentName)
                    .setRequiresCharging(false)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPersisted(true)    //重啟運行
                    .setRequiresDeviceIdle(false)
                    .setRequiresCharging(false)
                    //.setPeriodic(15*60*1000)
                    .setBackoffCriteria(cycletime, JobInfo.BACKOFF_POLICY_LINEAR)
                    .setOverrideDeadline(cycletime)//最長延遲30秒
                    .setMinimumLatency(cycletime)//最小延遲30秒
                    .build();
            js = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            int resultCode = js.schedule(jobInfo);
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                Log.d(TAG, "Job scheduled");
            } else {
                Log.d(TAG, "Job scheduled failed");
            }
        }
            ComponentName componentName = new ComponentName(getBaseContext(), NotifyJobService.class);
            JobInfo jobInfo = new JobInfo.Builder(1, componentName)
                    .setRequiresCharging(false)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPersisted(true)//重啟運行
                    .setRequiresDeviceIdle(false)
                    .setRequiresCharging(false)
                    //.setPeriodic(15*60*1000)
                    .setBackoffCriteria(cycletime, JobInfo.BACKOFF_POLICY_LINEAR)
                    .setOverrideDeadline(cycletime)//最長延遲30秒
                    .setMinimumLatency(cycletime)//最小延遲30秒
                    .build();
            js = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            int resultCode = js.schedule(jobInfo);
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                Log.d(TAG, "Job scheduled");
            } else {
                Log.d(TAG, "Job scheduled failed");
            }

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG,"Job Canceld Before Completion"+params.getJobId());
        return true;
    }

    private void doBackground(final JobParameters params){
        ContentResolver cr = getContentResolver();
        final List<NotifyDao.DataConstract> list=NotifyDao.query(cr);
        SharedPreferences pref=getSharedPreferences("NOTIFY_SETTING",MODE_PRIVATE);
        final String ip=pref.getString("IP","172.28.16.51");
        final String IMEI=pref.getString("IMEI","000000000000000");

        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<list.size();i++){
                    NotifyDao.DataConstract data=list.get(i);
                    String appName=data.getAPPNAME();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date curDate = new Date(System.currentTimeMillis()) ;
                    String cureTime = formatter.format(curDate);
                    //String url="http://"+ip+"/EM006-QueryMaxVersion.ashx?SYS="+appName+"t="+cureTime;
                    String url="http://"+ip+"/json/Test-AlertMessage.ashx?SYS="+appName+"&t="+cureTime;
                    Log.d(TAG,appName);
                    if(appName.equals("iMOR")){
                        url="http://"+ip+":8080/imwardServer/operating/broadcast";
                        Log.d(TAG,url);
                        showNotifyPost(url,data);
                    }else if(appName.equals("iMwardMRC")){
                        url="http://"+ip+":8080/imwardServer/getPatientInfo?bedNum=R101A";
                        Log.d(TAG,url);
                        showNotifyIDB(url,data);
                    }
                    else {
                        Log.d(TAG,url);
                    }
                }
                Log.d(TAG,"Job Finished [THREAD]"+params.getJobId());
                jobFinished(params, true);
            }
        }).start();
    }
    private void showNotifyPost(String url,NotifyDao.DataConstract data){
        final String appNamePOST=data.getAPPNAME();
        final String appstartPOST="."+data.getAPPSTART();
        final String apppackagePOST=data.getAPPDOAMIN();
        SharedPreferences pref=getSharedPreferences("NOTIFY_SETTING",MODE_PRIVATE);
        int cycletime=Integer.parseInt(pref.getString("CYCLE_TIME","60"))*1000;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis()) ;
        String earlytime=formatter.format(new Date(System.currentTimeMillis()-1000*60*60*24));
        String cureTime = formatter.format(curDate);
        time_now=pref.getString("MSG_TIME",earlytime);
        Log.e("time_now",time_now);
        JSONObject js = new JSONObject();
        try {
            js.put("start",time_now);
            //js.put("room", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        time_now=cureTime;
        Log.e("time_now2",time_now);
        pref.edit().putString("MSG_TIME",cureTime).commit();
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.POST,url,js, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray data=response.getJSONArray("data");
                    ArrayList<String> arrayList=new ArrayList<String>();
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject jsonobject = data.getJSONObject(i);
                        String MSG = jsonobject.getString("content");
                        arrayList.add(MSG);
                        //Log.d(TAG,appNamePOST+","+MSG+","+apppackagePOST+","+appstartPOST);
                        //createNotify(appNamePOST,MSG,apppackagePOST,appstartPOST);
                    }
                    createNotifyarray(appNamePOST,arrayList,apppackagePOST,appstartPOST);

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getBaseContext(),"Something wrong",Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        });
        if(requestQueue==null) {
            requestQueue = Volley.newRequestQueue(getBaseContext());
        }
        jsonObjectRequest.setTag("Notify");
        requestQueue.add(jsonObjectRequest);

    }
    private void showNotifyIDB(String url,NotifyDao.DataConstract data){
        final String appName=data.getAPPNAME();
        final String appstart="."+data.getAPPSTART();
        final String apppackage=data.getAPPDOAMIN();

        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response.toString());
                    String data=jsonObject.getString("data");
                    //Boolean success = Boolean.parseBoolean(jsonObject.getString("success"));

                    JSONObject JSONObjectdata=new JSONObject(data);
                    String bedNum=JSONObjectdata.getString("bedNum");
                    String bedStatus=JSONObjectdata.getString("bedStatus");
                    if(bedStatus.equals("8")){
                        createNotify(appName,bedNum+"床空床警示",apppackage,appstart);
                    }else if(bedStatus.equals("1")){
                        createNotify(appName,bedNum+"床跌倒警示",apppackage,appstart);
                    }


                } catch (JSONException e) {
                    Log.e(TAG,"JSONException IDB");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,error.toString());
                //createNotify("iMRobot",error.toString(),"tw.org.cch.www.imrobot","tw.org.cch.www.imrobot.LoginActivity");
            }
        });
        if(requestQueue==null){
            requestQueue = Volley.newRequestQueue(getBaseContext());
        }
        else{
            requestQueue.cancelAll("Notify");
        }
        jsonObjectRequest.setTag("Notify");
        requestQueue.add(jsonObjectRequest);

    }
    private void showNotify(String url,NotifyDao.DataConstract data){
        final String appName=data.getAPPNAME();
        final String appstart="."+data.getAPPSTART();
        final String apppackage=data.getAPPDOAMIN();

        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response.toString());
                    String successcount=jsonObject.getString("total");
                    //Boolean success = Boolean.parseBoolean(jsonObject.getString("success"));
                    if(Integer.parseInt(successcount)>0){
                        String data=jsonObject.getString("data");
                        JSONArray jsonArray=new JSONArray(data);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonobject = jsonArray.getJSONObject(i);
                            //String SYS=jsonobject.getString("SYS");
                            String MSG = jsonobject.getString("MSG");
                            Log.e("MSG",MSG);
                            createNotify(appName,MSG,apppackage,appstart);
                        }
                    }else{
                        Log.e(TAG,"error");

                    }

                } catch (JSONException e) {
                    Log.e(TAG,"JSONException iMOR"+e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,error.toString());
                //createNotify("iMRobot",error.toString(),"tw.org.cch.www.imrobot","tw.org.cch.www.imrobot.LoginActivity");
            }
        });
        if(requestQueue==null){
            requestQueue = Volley.newRequestQueue(getBaseContext());
        }
        else{
            requestQueue.cancelAll("Notify");
        }
        jsonObjectRequest.setTag("Notify");
        requestQueue.add(jsonObjectRequest);
    }
    private void createNotifyarray(String appname,ArrayList<String> msg,String pkg,String cls){
        for(int i=0;i<msg.size();i++){
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Bundle bundle=new Bundle();
            Intent notifyIntent = new Intent(this, TranslateAcitivity.class);
            notifyIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
            bundle.putString("pkg",pkg);
            bundle.putString("cls",pkg+cls);
            notifyIntent.putExtras(bundle);
            //notifyIntent.putExtra("pkg","tw.org.cch.www.imrobot");
            //notifyIntent.putExtra("cls",pkg+cls);
            PendingIntent appIntent = PendingIntent.getActivity(getBaseContext(), 0, notifyIntent, FLAG_UPDATE_CURRENT);
            Notification.Builder notification;
            Log.d(TAG,pkg);
            notification= new Notification.Builder(getBaseContext())
                    .setContentIntent(appIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker(appname) // 設置狀態列的顯示的資訊
                    .setWhen(System.currentTimeMillis())// 設置時間發生時間
                    .setAutoCancel(true) // 設置通知被使用者點擊後是否清除  //notification.flags = Notification.FLAG_AUTO_CANCEL;
                    .setContentTitle(appname) // 設置下拉清單裡的標題
                    .setContentText(msg.get(i))// 設置上下文內容
                    .setOngoing(false);      //true使notification變為ongoing，用戶不能手動清除// notification.flags = Notification.FLAG_ONGOING_EVENT; notification.flags = Notification.FLAG_NO_CLEAR;
            //.setDefaults(Notification.DEFAULT_ALL);
            if(Build.VERSION.SDK_INT>26){
                NotificationChannel channel = new NotificationChannel(
                        appname+i,
                        appname+i,
                        NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription(appname);
                channel.enableLights(true);
                channel.enableVibration(true);
                mNotificationManager.createNotificationChannel(channel);
                notification.setChannelId(appname+i);

            }
            Notification notifyBuild= notification.build();
            //notifyBuild.flags=Notification.FLAG_INSISTENT;
            notifyBuild.flags=Notification.FLAG_AUTO_CANCEL;
            mNotificationManager.notify(i, notifyBuild);
        }
    }
    private void createNotify(String appname,String msg,String pkg,String cls){
            NotificationManager mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            Intent notifyIntent = new Intent(this, TranslateAcitivity.class);
            notifyIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
            notifyIntent.putExtra("pkg",pkg);
            notifyIntent.putExtra("cls",pkg+cls);
            PendingIntent appIntent = PendingIntent.getActivity(getBaseContext(), 0, notifyIntent, 0);
            Notification.Builder notification;
            Log.d(TAG,pkg+","+cls);
            notification= new Notification.Builder(getBaseContext())
                    .setContentIntent(appIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker(appname) // 設置狀態列的顯示的資訊
                    .setWhen(System.currentTimeMillis())// 設置時間發生時間
                    .setAutoCancel(true) // 設置通知被使用者點擊後是否清除  //notification.flags = Notification.FLAG_AUTO_CANCEL;
                    .setContentTitle(appname) // 設置下拉清單裡的標題
                    .setContentText(msg)// 設置上下文內容
                    .setOngoing(false);      //true使notification變為ongoing，用戶不能手動清除// notification.flags = Notification.FLAG_ONGOING_EVENT; notification.flags = Notification.FLAG_NO_CLEAR;
                    //.setDefaults(Notification.DEFAULT_ALL);
            if(Build.VERSION.SDK_INT>26){
                NotificationChannel channel = new NotificationChannel(
                        appname,
                        appname,
                        NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription(appname);
                channel.enableLights(true);
                channel.enableVibration(true);
                mNotificationManager.createNotificationChannel(channel);
                notification.setChannelId(appname);

            }
            Notification notifyBuild= notification.build();
            //notifyBuild.flags=Notification.FLAG_INSISTENT;
            notifyBuild.flags=Notification.FLAG_AUTO_CANCEL;
            mNotificationManager.notify(1, notifyBuild);
        }
}
