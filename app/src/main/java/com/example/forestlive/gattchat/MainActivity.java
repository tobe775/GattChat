package com.example.forestlive.gattchat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity implements View.OnClickListener,GattServerManager.onGattServerManagerListener {


    private final String TAG = "MainActivity";

    private GattServerManager mGattManager = null;

    // Data
    private Handler mHandler = null;

    // Layout
    private TextView tv_name = null;
    private Button bt_start_adv = null;
    private Button bt_stop_adv = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initLayout();
    }

    private void init() {
        mHandler = new Handler();
        mGattManager = new GattServerManager(this,this);
    }

    private void initLayout() {

        tv_name = (TextView) findViewById(R.id.tv_name);

        bt_start_adv = (Button) findViewById(R.id.bt_start_adv);
        bt_start_adv.setOnClickListener(this);
        bt_stop_adv = (Button) findViewById(R.id.bt_stop_adv);
        bt_stop_adv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_start_adv:
                mGattManager.startAdvertise();
                break;

            case R.id.bt_stop_adv:
                mGattManager.stopAdvertise();
                break;
        }
    }

    @Override
    public void onManageStateChange(GattTYPE type) {
        switch (type){
            case SucADV:
                bt_start_adv.setVisibility(View.GONE);
                bt_stop_adv.setVisibility(View.VISIBLE);
                break;
            case FilADV:
                break;
            case StopADV:
                bt_start_adv.setVisibility(View.VISIBLE);
                bt_stop_adv.setVisibility(View.GONE);
                break;
            case CONNECT:
                break;
            case BEING:
                break;
            case DISCONNECT:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
