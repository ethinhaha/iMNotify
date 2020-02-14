package com.imedtac.imnotify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.imedtac.imnotify.data.NotifyDao;
import com.imedtac.imnotify.services.NotifyJobService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button bt_save,bt_query,save_config;
    EditText et_appname,et_packagename,et_startactivity,et_ip,et_cycletime;
    RecyclerView applistview;
    TextView versionName;
    private MyListAdapter listAdapter;
    SharedPreferences IPpref;
    JobScheduler js;
    JobInfo jobInfo;
    final String TAG="MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        save_config=(Button)findViewById(R.id.save_config);
        bt_save=(Button)findViewById(R.id.bt_save);
        bt_query=(Button)findViewById(R.id.bt_query);
        et_appname=(EditText)findViewById(R.id.et_appname);
        et_packagename=(EditText)findViewById(R.id.et_packagename);
        et_startactivity=(EditText)findViewById(R.id.et_startactivity);
        applistview=(RecyclerView)findViewById(R.id.query_list);
        et_ip=(EditText)findViewById(R.id.et_ip);
        et_cycletime=(EditText)findViewById(R.id.et_cycletime);
        versionName=(TextView)findViewById(R.id.versionName);
        IPpref=getApplicationContext().getSharedPreferences("NOTIFY_SETTING",MODE_PRIVATE);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        applistview.setLayoutManager(layoutManager);
        applistview.setAdapter(listAdapter);
        versionName.setText(BuildConfig.VERSION_NAME);
        /*ComponentName componentName = new ComponentName(this,NotifyJobService.class);
        jobInfo=new JobInfo.Builder(1, componentName)
                .setRequiresCharging(false)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPersisted(true)    //重啟運行
                .setRequiresDeviceIdle(false)
                .setRequiresCharging(false)
                //.setPeriodic(15*60*1000)
                .setBackoffCriteria(10000,JobInfo.BACKOFF_POLICY_LINEAR)
                .setOverrideDeadline(10000)//最長延遲30秒
                //.setMinimumLatency(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS)//最小延遲30秒
                .setMinimumLatency(10000)//最小延遲30秒
                .build();
        js=(JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int resultCode = js.schedule(jobInfo);
        if(resultCode == JobScheduler.RESULT_SUCCESS){
            Log.d(TAG,"Job scheduled");
        }else{
            Log.d(TAG,"Job scheduled failed");
        }*/

        et_ip.setText(IPpref.getString("IP",""));
        et_cycletime.setText(IPpref.getString("CYCLE_TIME",""));
        save_config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IPpref.edit()
                .putString("IP",et_ip.getText().toString())
                .putString("CYCLE_TIME",et_cycletime.getText().toString())
                .commit();
                Log.d("TAG",et_ip.getText().toString()+et_cycletime.getText().toString());
            }
        });
        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appname=et_appname.getText().toString();
                String packagename=et_packagename.getText().toString();
                String startactivity=et_startactivity.getText().toString();

                ContentResolver cr = getContentResolver();
                NotifyDao.DataConstract data=new NotifyDao.DataConstract();

                data.setAPPNAME(appname);
                data.setAPPDOAMIN(packagename);
                data.setAPPSTART(startactivity);
                if(!appname.equals("")||appname!=null) {
                    if (NotifyDao.insert(cr, data)) {
                        et_appname.setText("");
                        et_packagename.setText("");
                        et_startactivity.setText("");
                        Toast.makeText(MainActivity.this, "新增成功", Toast.LENGTH_LONG).show();
                        List<NotifyDao.DataConstract> list=NotifyDao.query(cr);
                        if(listAdapter!=null){
                            listAdapter.replaceAll(list);
                        }else{
                            listAdapter = new MyListAdapter(MainActivity.this, list);
                        }
                        applistview.setAdapter(listAdapter);
                    }else{
                        NotifyDao.DataConstract querybyname_data=NotifyDao.querybyname(cr,appname);
                        if(querybyname_data!=null){
                            int success_count=NotifyDao.update(cr,data);
                            if(success_count==1){
                                Toast.makeText(MainActivity.this, "更新"+success_count+"筆成功", Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(MainActivity.this, "更新"+success_count+"筆成功,請檢查資料狀態", Toast.LENGTH_LONG).show();
                            }
                        }else {
                            Toast.makeText(MainActivity.this, "新增失敗", Toast.LENGTH_LONG).show();
                        }
                    }
                }else{
                    Toast.makeText(MainActivity.this, "請輸入APPNAME", Toast.LENGTH_LONG).show();
                }
            }
        });

        bt_query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appname=et_appname.getText().toString();
                ContentResolver cr = getContentResolver();
                if(appname.equals("")||appname==null){
                    et_appname.setText("");
                    et_packagename.setText("");
                    et_startactivity.setText("");

                    List<NotifyDao.DataConstract> list=NotifyDao.query(cr);
                    if(list.size()!=0) {
                        if(listAdapter!=null){
                            listAdapter.replaceAll(list);
                        }else{
                            listAdapter = new MyListAdapter(MainActivity.this, list);
                        }
                        applistview.setAdapter(listAdapter);
                    }else{
                        Toast.makeText(MainActivity.this, "沒有資料", Toast.LENGTH_LONG).show();
                    }
                }else{
                    NotifyDao.DataConstract temp_data;
                    temp_data=NotifyDao.querybyname(cr,appname);
                    List<NotifyDao.DataConstract> list=new ArrayList<>();
                    if(temp_data!=null){
                        list.add(temp_data);
                        if(listAdapter!=null){
                            listAdapter.replaceAll(list);
                        }else{
                            listAdapter = new MyListAdapter(MainActivity.this, list);
                        }
                        applistview.setAdapter(listAdapter);
                    }else{
                        Toast.makeText(MainActivity.this, "沒有資料", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }
    public class MyListAdapter extends RecyclerView .Adapter<MyListAdapter.MyViewHolder>{
        private Context mContext;
        private List<NotifyDao.DataConstract> datalist;

        public MyListAdapter (Context context,List<NotifyDao.DataConstract> datalist) {
            this.mContext = context;
            this.datalist=datalist;
        }

        @NonNull
        @Override
        public MyListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.applist, parent, false);
            MyViewHolder vh = new MyViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull MyListAdapter.MyViewHolder holder, int position) {
            final NotifyDao.DataConstract data=datalist.get(position);
            final int final_position=position;
            holder.tvAppname.setText(data.getAPPNAME());
            holder.tvAppcontent.setText(data.getAPPDOAMIN()+"/"+data.getAPPSTART());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,final_position+"",Toast.LENGTH_LONG).show();
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                    alert.setTitle("警告")
                    .setMessage("你確定要刪除資料嗎?")
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this,"沒事沒事,知道錯就好",Toast.LENGTH_LONG).show();
                        }
                    })
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            datalist.remove(final_position);
                            ContentResolver cr = getContentResolver();
                            NotifyDao.delete(cr,data.getAPPNAME());
                            notifyItemRemoved(final_position);
                            List<NotifyDao.DataConstract> list=NotifyDao.query(cr);
                            if(list.size()!=0) {
                                if(listAdapter!=null){
                                    listAdapter.replaceAll(list);
                                }else{
                                    listAdapter = new MyListAdapter(MainActivity.this, list);
                                }
                                applistview.setAdapter(listAdapter);
                            }else{
                                Toast.makeText(MainActivity.this, "沒有資料", Toast.LENGTH_LONG).show();
                            }
                        }
                    }).create();
                    alert.show();
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return datalist.size();
        }
        public void replaceAll(List<NotifyDao.DataConstract> datalist) {
            this.datalist = datalist;
            notifyDataSetChanged();
        }
        class MyViewHolder extends RecyclerView.ViewHolder {

            public TextView tvAppname;
            public TextView tvAppcontent;


            public MyViewHolder(View itemView) {
                super(itemView);
                tvAppname=(TextView)itemView.findViewById(R.id.list_appname);
                tvAppcontent=(TextView)itemView.findViewById(R.id.list_appcontent);
            }
        }
    }
}
