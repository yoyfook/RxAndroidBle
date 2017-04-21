package com.polidea.rxandroidble.sample.example3_discovery;

import android.content.Intent;
import android.os.Bundle;
import com.polidea.rxandroidble.sample.util.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.TextView;

import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.sample.DeviceActivity;
import com.polidea.rxandroidble.sample.R;
import com.polidea.rxandroidble.sample.SampleApplication;
import com.polidea.rxandroidble.sample.example4_characteristic.CharacteristicOperationExampleActivity;
import com.polidea.rxandroidble.sample.util.RxBleUtils;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

import static com.trello.rxlifecycle.android.ActivityEvent.DESTROY;
import static com.trello.rxlifecycle.android.ActivityEvent.PAUSE;

public class ServiceDiscoveryExampleActivity extends RxAppCompatActivity {

    @BindView(R.id.connection_state)
    TextView connectionStateView;
    @BindView(R.id.connect)
    Button connectButton;
    @BindView(R.id.scan_results)
    RecyclerView recyclerView;
    private DiscoveryResultsAdapter adapter;
    private RxBleDevice bleDevice;
    private String macAddress;

    @OnClick(R.id.connect)
    public void onConnectToggleClick() {
        adapter.clearScanResults();
        bleDevice.establishConnection(false)
                .flatMap(RxBleConnection::discoverServices)
                // 探索完成之后断开连接
                .first()
                .compose(bindUntilEvent(PAUSE))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(this::updateUI)
                .subscribe(adapter::swapScanResult, this::onConnectionFailure);

        updateUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_discovery);
        ButterKnife.bind(this);
        macAddress = getIntent().getStringExtra(DeviceActivity.EXTRA_MAC_ADDRESS);
        getSupportActionBar().setSubtitle(getString(R.string.mac_address, macAddress));
        bleDevice = SampleApplication.getRxBleClient(this).getBleDevice(macAddress);
        configureResultList();

        // 监听连接状态改变
        bleDevice.observeConnectionStateChanges()
                .compose(bindUntilEvent(DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionStateChange);
    }

    private void configureResultList() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        adapter = new DiscoveryResultsAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnAdapterItemClickListener(view -> {
            final int childAdapterPosition = recyclerView.getChildAdapterPosition(view);
            final DiscoveryResultsAdapter.AdapterItem itemAtPosition = adapter.getItem(childAdapterPosition);
            onAdapterItemClick(itemAtPosition);
        });
    }

    private void onAdapterItemClick(DiscoveryResultsAdapter.AdapterItem item) {
        if (item.type == DiscoveryResultsAdapter.AdapterItem.CHARACTERISTIC) {
            final Intent intent = new Intent(this, CharacteristicOperationExampleActivity.class);
            intent.putExtra(DeviceActivity.EXTRA_MAC_ADDRESS, macAddress);
            intent.putExtra(CharacteristicOperationExampleActivity.EXTRA_CHARACTERISTIC_UUID, item.uuid);
            startActivity(intent);
        }
    }

    private void onConnectionFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Snackbar.make(findViewById(android.R.id.content), "Connection error: " + throwable, Snackbar.LENGTH_LONG).show();
    }

    private void onConnectionStateChange(RxBleConnection.RxBleConnectionState newState) {
        connectionStateView.setText(getString(R.string.connection_state, RxBleUtils.getConnectionStateStr(newState)));
        updateUI();
    }


    private void updateUI() {
        final boolean onDoing = bleDevice.getConnectionState() != RxBleConnection.RxBleConnectionState.CONNECTED
                && bleDevice.getConnectionState() != RxBleConnection.RxBleConnectionState.DISCONNECTED;
        connectButton.setEnabled(!onDoing);
    }
}
