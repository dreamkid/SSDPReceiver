package com.littlehotspot.www.ssdpreceiver;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.savor.operation.ssdp.ActivitiesManager;
import com.savor.operation.ssdp.SSDPService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SSDPService.OnSSDPReceivedListener {

    private EditText mIpAdressEt;
    private EditText mPortEt;
    private Button mStartBtn;
    private Button mStopBtn;
    private TextView mLogTv;
    private Intent intent;
    private ServiceConnection mSerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ssdpService = ((SSDPService.SSDPBinder)service).getService();
            ssdpService.setOnSSDPReceivedListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private SSDPService ssdpService;
    private ScrollView mScrollView;
    private Button mClearBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivitiesManager.getInstance().pushActivity(this);
        getViews();
        setListeners();
    }

    private void setListeners() {
        mStartBtn.setOnClickListener(this);
        mClearBtn.setOnClickListener(this);
        mStopBtn.setOnClickListener(this);
    }

    private void getViews() {
        mIpAdressEt = findViewById(R.id.et_adress);
        mPortEt = findViewById(R.id.et_port);
        mStartBtn = findViewById(R.id.btn_receive);
        mStopBtn = findViewById(R.id.btn_stop);
        mLogTv = findViewById(R.id.tv_log);
        mScrollView = findViewById(R.id.scrollview);
        mClearBtn = findViewById(R.id.btn_clear);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_clear:
                mLogTv.setText("");
                break;
            case R.id.btn_receive:
                String address = mIpAdressEt.getText().toString();
                String port = mPortEt.getText().toString();
                if(TextUtils.isEmpty(address)||TextUtils.isEmpty(port)) {
                    return;
                }
                intent = new Intent(this, SSDPService.class);
                intent.setAction("com.savor.operation.ssdp.action.SERVICE");
                intent.putExtra("port",port);
                intent.putExtra("address",address);
                bindService(intent,mSerConn, Service.BIND_AUTO_CREATE);
                startService(intent);
                mLogTv.append("\n开始接收...");
                break;
            case R.id.btn_stop:
                try {
                    stopSSDPService();
                }catch (Exception e){}
                mLogTv.append("\n停止接收.");
                break;
        }
    }

    private void stopSSDPService() {
        unbindService(mSerConn);
        stopService(intent);
    }

    public void updateLog(final String log) {
        mLogTv.post(new Runnable() {
            @Override
            public void run() {
                mLogTv.append("\n \n"+log);
                int offset = mLogTv.getLineCount()*mLogTv.getLineHeight();
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    @Override
    public void onSSDPReceivedListener(String address, String boxAddress, int hotelId, int roomId, String boxMac) {

    }

    @Override
    public void onSSDPReceived(String message) {
        updateLog(message);
    }

    @Override
    public void onBind(final String address, final String port) {
        mLogTv.post(new Runnable() {
            @Override
            public void run() {
                mLogTv.append("\n 监听地址address："+address+",端口："+port);
            }
        });
    }

    @Override
    public void onClose() {
        mLogTv.post(new Runnable() {
            @Override
            public void run() {
                mLogTv.append("\n 服务关闭");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSSDPService();
    }
}
