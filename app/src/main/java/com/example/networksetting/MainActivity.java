package com.example.networksetting;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import static com.example.networksetting.NetworkUtil.CONNECT_TYPE_ETHERNET;

public class MainActivity extends AppCompatActivity implements OnClickListener, OnCheckedChangeListener, Handler.Callback {
    private static final int MESSAGE_SWITCH_CONNECT_TYPE = 1000;
    //    private static final int MESSAGE_ENABLE_WIFI = 1001;
    private static final int MESSAGE_REFRESH_UI = 1002;
    private Handler handler;
    private ProgressDialog loadingDialog;
    private boolean isEthernet;
    private boolean isManual;

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case MESSAGE_SWITCH_CONNECT_TYPE:
                onMessageSwitchConnectType();
                break;
            case MESSAGE_REFRESH_UI:
                onMessageRefreshUi();
                break;
        }
        return false;
    }

    private void onMessageSwitchConnectType() {
        showLoadingDialog("切換中", "請稍後 ...");
        NetworkUtil.setEthernetEnabled(this, isEthernet);
        handler.sendEmptyMessageDelayed(MESSAGE_REFRESH_UI, 3000);
    }

    private void onMessageRefreshUi() {
        bindContentView();
        hideLoadingDialog();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler(this);
        NetworkUtil.setWifiEnabled(this, true);
        bindContentView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activityMain_confirmButton:
                onConfirmButtonClick();
                break;
            case R.id.activityMain_infoButton:
                onInfoButtonClick();
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group.getId() == R.id.activityMain_typeRadioGroup) {
            isEthernet = checkedId == R.id.activityMain_EthernetRadioButton;
            findViewById(R.id.activityMain_modeRadioGroup).setVisibility(isEthernet ? View.VISIBLE : View.GONE);
            handler.sendEmptyMessage(MESSAGE_SWITCH_CONNECT_TYPE);
        } else if (group.getId() == R.id.activityMain_modeRadioGroup) {
            isManual = checkedId == R.id.activityMain_staticRadioButton;
        }
    }

    private void onConfirmButtonClick() {
        if (isEthernet) {
            showLoadingDialog("設定中", "請稍後 ...");
            NetworkUtil.setEthernetMode(
                    isManual ? 1 : 0,
                    getIpAddressString(),
                    getGateWayString(),
                    getNetMaskString(),
                    getDNS1String(),
                    getDNS2String());
            handler.sendEmptyMessageDelayed(MESSAGE_REFRESH_UI, 3000);
        }
    }

    private void onInfoButtonClick() {
        if (NetworkUtil.getConnectType(this) == CONNECT_TYPE_ETHERNET) {
            if (StringUtil.isEquals("dhcp", NetworkUtil.getEthernetMode(this))) {
                Toast.makeText(this, "onInfoButtonClick : ip -> " + NetworkUtil.getIpAddressFromDhcpEthernet(this) +
                                " \n netMask -> " + NetworkUtil.getNetMaskFromDhcpEthernet(this) +
                                " \n gateway -> " + NetworkUtil.getGatewayFromDhcpEthernet(this) +
                                " \n Dns1    -> " + NetworkUtil.getDns1FromDhcpEthernet(this) +
                                " \n Dns2    -> " + NetworkUtil.getDns2FromDhcpEthernet(this)
                        , Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "onInfoButtonClick : Connect Type  -> " + NetworkUtil.getConnectType(this) +
                                " \n Ethernet State  -> " + NetworkUtil.getEthernetState(this) +
                                " \n Ethernet Mode   -> " + NetworkUtil.getEthernetMode(this)
                        , Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "onInfoButtonClick : ip -> " + NetworkUtil.getIpAddressFromManualEthernet(this) +
                                " \n netMask -> " + NetworkUtil.getNetMaskFromManualEthernet(this) +
                                " \n gateway -> " + NetworkUtil.getGatewayFromManualEthernet(this) +
                                " \n Dns1    -> " + NetworkUtil.getDnsAddressFromManualEthernet(this) +
                                " \n Dns2    -> " + NetworkUtil.getDns2AddressFromManualEthernet(this)
                        , Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "onInfoButtonClick : Connect Type  -> " + NetworkUtil.getConnectType(this) +
                                " \n Ethernet State  -> " + NetworkUtil.getEthernetState(this) +
                                " \n Ethernet Mode   -> " + NetworkUtil.getEthernetMode(this)
                        , Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "onInfoButtonClick : ip -> " + NetworkUtil.getIpAddressFromInternet(this) +
                            " \n netMask -> " + NetworkUtil.getNetMaskFromInternet(this) +
                            " \n gateway -> " + NetworkUtil.getGatewayFromInternet(this) +
                            " \n Dns1    -> " + NetworkUtil.getDns1FromInternet(this) +
                            " \n Dns2    -> " + NetworkUtil.getDns2FromInternet(this)
                    , Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "onInfoButtonClick : Connect Type  -> " + NetworkUtil.getConnectType(this) +
                            " \n Ethernet State  -> " + NetworkUtil.getEthernetState(this) +
                            " \n Ethernet Mode   -> " + NetworkUtil.getEthernetMode(this)
                    , Toast.LENGTH_SHORT).show();
        }
    }

    private void bindContentView() {
        bindIpAddressEditText();
        bindGatewayEditText();
        bindNetMaskEditText();
        bindDns1EditText();
        bindDns2EditText();
        bindTypeRadioGroup();
        bindTypeRadioButton();
        bindModeRadioGroup();
        bindModeRadioButton();
        bindClickView();
    }

    private void bindTypeRadioGroup() {
        RadioGroup radioGroup = findViewById(R.id.activityMain_typeRadioGroup);
        radioGroup.setOnCheckedChangeListener(this);
    }

    private void bindTypeRadioButton() {
        RadioButton radioButton = findViewById(
                NetworkUtil.getConnectType(this) == CONNECT_TYPE_ETHERNET
                        ? R.id.activityMain_EthernetRadioButton
                        : R.id.activityMain_wifiRadioButton);
        radioButton.setChecked(true);
    }

    private void bindModeRadioGroup() {
        RadioGroup radioGroup = findViewById(R.id.activityMain_modeRadioGroup);
        radioGroup.setOnCheckedChangeListener(this);
    }

    private void bindModeRadioButton() {
        if (NetworkUtil.getConnectType(this) == CONNECT_TYPE_ETHERNET) {
            RadioButton radioButton = findViewById(
                    StringUtil.isEquals("manual", NetworkUtil.getEthernetMode(this))
                            ? R.id.activityMain_staticRadioButton
                            : R.id.activityMain_dhcpRadioButton);
            radioButton.setChecked(true);
        }
    }

    private void bindIpAddressEditText() {
        EditText editText = findViewById(R.id.activityMain_ipAddressEditText);
        String ipAddress = NetworkUtil.getConnectType(this) == CONNECT_TYPE_ETHERNET
                ? StringUtil.isEquals("dhcp", NetworkUtil.getEthernetMode(this))
                ? NetworkUtil.getIpAddressFromDhcpEthernet(this)
                : NetworkUtil.getIpAddressFromManualEthernet(this)
                : NetworkUtil.getIpAddressFromInternet(this);
        editText.setText(ipAddress);
    }

    private void bindGatewayEditText() {
        EditText editText = findViewById(R.id.activityMain_gatewayEditText);
        String gateway = NetworkUtil.getConnectType(this) == CONNECT_TYPE_ETHERNET
                ? StringUtil.isEquals("dhcp", NetworkUtil.getEthernetMode(this))
                ? NetworkUtil.getGatewayFromDhcpEthernet(this)
                : NetworkUtil.getGatewayFromManualEthernet(this)
                : NetworkUtil.getGatewayFromInternet(this);
        editText.setText(gateway);
    }

    private void bindNetMaskEditText() {
        EditText editText = findViewById(R.id.activityMain_maskEditText);
        String netMask = NetworkUtil.getConnectType(this) == CONNECT_TYPE_ETHERNET
                ? StringUtil.isEquals("dhcp", NetworkUtil.getEthernetMode(this))
                ? NetworkUtil.getNetMaskFromDhcpEthernet(this)
                : NetworkUtil.getNetMaskFromManualEthernet(this)
                : NetworkUtil.getNetMaskFromInternet(this);
        editText.setText(netMask);
    }

    private void bindDns1EditText() {
        EditText editText = findViewById(R.id.activityMain_dns1EditText);
        String dns1 = NetworkUtil.getConnectType(this) == CONNECT_TYPE_ETHERNET
                ? StringUtil.isEquals("dhcp", NetworkUtil.getEthernetMode(this))
                ? NetworkUtil.getDns1FromDhcpEthernet(this)
                : NetworkUtil.getDnsAddressFromManualEthernet(this)
                : NetworkUtil.getDns1FromInternet(this);
        editText.setText(dns1);
    }

    private void bindDns2EditText() {
        EditText editText = findViewById(R.id.activityMain_dns2EditText);
        String dns2 = NetworkUtil.getConnectType(this) == CONNECT_TYPE_ETHERNET
                ? StringUtil.isEquals("dhcp", NetworkUtil.getEthernetMode(this))
                ? NetworkUtil.getDns2FromDhcpEthernet(this)
                : NetworkUtil.getDns2AddressFromManualEthernet(this)
                : NetworkUtil.getDns2FromInternet(this);
        editText.setText(dns2);
    }

    private void bindClickView() {
        findViewById(R.id.activityMain_confirmButton).setOnClickListener(this);
        findViewById(R.id.activityMain_infoButton).setOnClickListener(this);
    }

    private String getIpAddressString() {
        EditText editText = findViewById(R.id.activityMain_ipAddressEditText);
        return editText.getText().toString();
    }

    private String getNetMaskString() {
        EditText editText = findViewById(R.id.activityMain_maskEditText);
        return editText.getText().toString().trim();
    }

    private String getGateWayString() {
        EditText editText = findViewById(R.id.activityMain_gatewayEditText);
        return editText.getText().toString().trim();
    }

    private String getDNS1String() {
        EditText editText = findViewById(R.id.activityMain_dns1EditText);
        return editText.getText().toString().trim();
    }

    private String getDNS2String() {
        EditText editText = findViewById(R.id.activityMain_dns2EditText);
        return editText.getText().toString().trim();
    }

    private void showLoadingDialog(String title, String message) {
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(this);
        }
        loadingDialog.setTitle(title);
        loadingDialog.setMessage(message);
        if (!isFinishing()) {
            loadingDialog.show();
        }
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing() && !isFinishing()) {
            loadingDialog.dismiss();
        }
    }
}