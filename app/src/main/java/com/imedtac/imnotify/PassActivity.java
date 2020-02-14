package com.imedtac.imnotify;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.imedtac.imnotify.data.NotifyDao;
import com.imedtac.imnotify.services.NotifyJobService;

import java.util.List;

public class PassActivity extends AppCompatActivity {
    SharedPreferences IPpref;
    TextView version,entercount;
    ImageView imageView4;

    JobScheduler js;
    JobInfo jobInfo;
    final String TAG="PassActivity";
    private static final int GOTO_MAIN_ACTIVITY = 0;
    private static final int POP_UP_PASSWORD = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass);
        final EditText edit = new EditText(PassActivity.this);
        IPpref = getApplicationContext().getSharedPreferences("NOTIFY_SETTING", MODE_PRIVATE);
        version = (TextView) findViewById(R.id.version);
        entercount = (TextView) findViewById(R.id.entercount);
        version.setText("版本:" + BuildConfig.VERSION_NAME);
        imageView4=(ImageView)findViewById(R.id.imageView4);
        imageView4.bringToFront();
        int openCount = IPpref.getInt("openCount", 0);
        entercount.setText(openCount + "");

        String appname = "iMOR";
        String packagename = "com.imedtac.imsafeor";
        String startactivity = ".ActWelcomePag";

        ContentResolver cr = getContentResolver();
        NotifyDao.DataConstract data = new NotifyDao.DataConstract();
        data.setAPPDOAMIN(packagename);
        data.setAPPSTART(startactivity);
        data.setAPPNAME(appname);


        if (openCount == 0) {
            Log.e(TAG, data.getAPPDOAMIN() + "," + data.getAPPNAME());
            ComponentName componentName = new ComponentName(this, NotifyJobService.class);
            jobInfo = new JobInfo.Builder(1, componentName)
                    .setRequiresCharging(false)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPersisted(true)    //重啟運行
                    .setRequiresDeviceIdle(false)
                    .setRequiresCharging(false)
                    //.setPeriodic(15*60*1000)
                    .setBackoffCriteria(10000, JobInfo.BACKOFF_POLICY_LINEAR)
                    .setOverrideDeadline(10000)//最長延遲30秒
                    //.setMinimumLatency(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS)//最小延遲30秒
                    .setMinimumLatency(10000)//最小延遲30秒
                    .build();
            js = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            int resultCode = js.schedule(jobInfo);
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                Log.d(TAG, "Job scheduled");
            } else {
                Log.d(TAG, "Job scheduled failed");
            }
            if (NotifyDao.insert(cr, data)) {
                Toast.makeText(PassActivity.this, "新增成功", Toast.LENGTH_LONG).show();
            } else {
                NotifyDao.DataConstract querybyname_data = NotifyDao.querybyname(cr, appname);
                if (querybyname_data != null) {
                    int success_count = NotifyDao.update(cr, data);
                    if (success_count == 1) {
                        Toast.makeText(PassActivity.this, "更新" + success_count + "筆成功", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(PassActivity.this, "更新" + success_count + "筆成功,請檢查資料狀態", Toast.LENGTH_LONG).show();
                    }
                }
            }
            openCount++;
            IPpref.edit().putInt("openCount", openCount).commit();
        } else {
            openCount++;
            ComponentName componentName = new ComponentName(this, NotifyJobService.class);
            jobInfo = new JobInfo.Builder(1, componentName)
                    .setRequiresCharging(false)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPersisted(true)    //重啟運行
                    .setRequiresDeviceIdle(false)
                    .setRequiresCharging(false)
                    //.setPeriodic(15*60*1000)
                    .setBackoffCriteria(10000, JobInfo.BACKOFF_POLICY_LINEAR)
                    .setOverrideDeadline(10000)//最長延遲30秒
                    //.setMinimumLatency(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS)//最小延遲30秒
                    .setMinimumLatency(10000)//最小延遲30秒
                    .build();
            js = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            int resultCode = js.schedule(jobInfo);
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                Log.d(TAG, "Job scheduled");
            } else {
                Log.d(TAG, "Job scheduled failed");
            }
            IPpref.edit().putInt("openCount", openCount).commit();
        }


        Handler mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {

                switch (msg.what) {
                    case GOTO_MAIN_ACTIVITY:
                        Intent intent = new Intent();
                        //將原本Activity的換成MainActivity
                        intent.setClass(PassActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    case POP_UP_PASSWORD:
                        AlertDialog.Builder alert = new AlertDialog.Builder(PassActivity.this);
                        alert.setTitle("通知")
                                .setView(edit)
                                .setMessage("請輸入推播設定密碼")
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String str_pw=edit.getText().toString();
                                        if(str_pw.equals("42856576")){
                                            Intent intent=new Intent(PassActivity.this,MainActivity.class);
                                            startActivity(intent);
                                            PassActivity.this.finish();
                                        }else{
                                            Toast.makeText(PassActivity.this,"請輸入正確密碼",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                })
                                .create();
                        alert.show();
                        break;
                    default:
                        break;
                }
            }

        };
        mHandler.sendEmptyMessageDelayed(POP_UP_PASSWORD, 5000);
    }


    /*    button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_pw=password.getText().toString();
                if(str_pw.equals("42856576")){
                    Intent intent=new Intent(PassActivity.this,MainActivity.class);
                    startActivity(intent);
                    PassActivity.this.finish();
                }else{
                    Toast.makeText(PassActivity.this,"請輸入正確密碼",Toast.LENGTH_LONG).show();
                }
            }
        });

    */
}
