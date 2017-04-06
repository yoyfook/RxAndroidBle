package com.polidea.rxandroidble.sample.example2_connection;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SwitchCompat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.sample.DeviceActivity;
import com.polidea.rxandroidble.sample.R;
import com.polidea.rxandroidble.sample.SampleApplication;
import com.polidea.rxandroidble.sample.util.RxBleUtils;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import static com.polidea.rxandroidble.sample.util.ToastUtils.ts_show;
import static com.trello.rxlifecycle.android.ActivityEvent.DESTROY;
import static com.trello.rxlifecycle.android.ActivityEvent.PAUSE;

public class ConnectionExampleActivity extends RxAppCompatActivity {

    @BindView(R.id.connection_state)
    TextView connectionStateView;
    @BindView(R.id.connect_toggle)
    Button connectButton;
    @BindView(R.id.newMtu)
    EditText textMtu;
    @BindView(R.id.set_mtu)
    Button setMtuButton;
    @BindView(R.id.autoconnect)
    SwitchCompat autoConnectToggleSwitch;
    private RxBleDevice bleDevice;
    private Subscription connectionSubscription;
    private Subscription mtuSubscription;

    @OnClick(R.id.connect_toggle)
    public void onConnectToggleClick() {
        if (isConnected()) {
            triggerDisconnect();
        } else {
            connectionSubscription = bleDevice.establishConnection(autoConnectToggleSwitch.isChecked())
                    .compose(bindUntilEvent(PAUSE))
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnUnsubscribe(this::clearSubscription)
                    .subscribe(this::onConnectionReceived, this::onConnectionFailure);
        }
    }

    @OnClick(R.id.set_mtu)
    public void onSetMtu() {
        int mtu = 0;

        try {
            mtu = Integer.valueOf(textMtu.getText().toString());
        } catch (Exception e) {
        }

        if (mtu <= 0) {
            ts_show(this, "请输入正确的MTU值");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int finalMtu = mtu;
            mtuSubscription = bleDevice.establishConnection(false)
                    .flatMap(rxBleConnection -> rxBleConnection.requestMtu(finalMtu))
                    // 设置完成后自动断开
                    .first()
                    .compose(bindUntilEvent(PAUSE))
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnUnsubscribe(this::clearSubscription)
                    .subscribe(this::onMtuReceived, this::onConnectionFailure);
        } else {
            ts_show(this, "Android 5.1及以上版本有效");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        ButterKnife.bind(this);
        String macAddress = getIntent().getStringExtra(DeviceActivity.EXTRA_MAC_ADDRESS);
        getSupportActionBar().setSubtitle(getString(R.string.mac_address, macAddress));
        bleDevice = SampleApplication.getRxBleClient(this).getBleDevice(macAddress);

        // How to listen for connection state changes
        bleDevice.observeConnectionStateChanges()
                .compose(bindUntilEvent(DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionStateChange);
    }

    private boolean isConnected() {
        return bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    private void onConnectionFailure(Throwable throwable) {
        Snackbar.make(findViewById(android.R.id.content), "Connection error: " + throwable, Snackbar.LENGTH_SHORT).show();
    }

    private void onConnectionReceived(RxBleConnection connection) {
        Snackbar.make(findViewById(android.R.id.content), "Connection received", Snackbar.LENGTH_SHORT).show();
    }

    private void onConnectionStateChange(RxBleConnection.RxBleConnectionState newState) {
        connectionStateView.setText(getString(R.string.connection_state, RxBleUtils.getConnectionStateStr(newState)));
        updateUI();
    }

    private void onMtuReceived(Integer mtu) {
        Snackbar.make(findViewById(android.R.id.content), "MTU received: " + mtu, Snackbar.LENGTH_SHORT).show();
    }

    private void clearSubscription() {
        connectionSubscription = null;
        mtuSubscription = null;
        updateUI();
    }

    private void triggerDisconnect() {
        if (connectionSubscription != null) {
            connectionSubscription.unsubscribe();
        }

        if (mtuSubscription != null) {
            mtuSubscription.unsubscribe();
        }
    }

    private void updateUI() {
        final boolean connected = isConnected();
        final boolean onDoing = bleDevice.getConnectionState() != RxBleConnection.RxBleConnectionState.CONNECTED
                && bleDevice.getConnectionState() != RxBleConnection.RxBleConnectionState.DISCONNECTED;

        connectButton.setText(connected ? R.string.disconnect : R.string.connect);
        connectButton.setEnabled(!onDoing);

        autoConnectToggleSwitch.setEnabled(!onDoing && !connected);

        textMtu.setEnabled(!onDoing && !connected);
        setMtuButton.setEnabled(!onDoing && !connected);
    }
}
