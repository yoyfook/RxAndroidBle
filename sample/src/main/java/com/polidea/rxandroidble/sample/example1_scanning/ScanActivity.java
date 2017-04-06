package com.polidea.rxandroidble.sample.example1_scanning;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.EditText;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleScanResult;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.sample.DeviceActivity;
import com.polidea.rxandroidble.sample.R;
import com.polidea.rxandroidble.sample.SampleApplication;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import static com.polidea.rxandroidble.sample.util.ToastUtils.ts_show;

public class ScanActivity extends AppCompatActivity {

    @BindView(R.id.scan_filter_str)
    EditText scanFilterStr;
    @BindView(R.id.scan_toggle_btn)
    Button scanToggleButton;
    @BindView(R.id.scan_results)
    RecyclerView recyclerView;
    private RxBleClient rxBleClient;
    private Subscription scanSubscription;
    private ScanResultsAdapter resultsAdapter;
    private Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;

        setContentView(R.layout.activity_scan);
        ButterKnife.bind(this);
        rxBleClient = SampleApplication.getRxBleClient(this);
        configureResultList();
    }

    private void configureResultList() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        resultsAdapter = new ScanResultsAdapter();
        recyclerView.setAdapter(resultsAdapter);
        resultsAdapter.setOnAdapterItemClickListener(view -> {
            final int childAdapterPosition = recyclerView.getChildAdapterPosition(view);
            final RxBleScanResult itemAtPosition = resultsAdapter.getItemAtPosition(childAdapterPosition);
            onAdapterItemClick(itemAtPosition);
        });
    }

    private void onAdapterItemClick(RxBleScanResult scanResults) {
        // 跳转到设备页面，传递设备的MAC地址
        final String macAddress = scanResults.getBleDevice().getMacAddress();
        final Intent intent = new Intent(this, DeviceActivity.class);
        intent.putExtra(DeviceActivity.EXTRA_MAC_ADDRESS, macAddress);
        startActivity(intent);
    }

    private boolean isScanning() {
        return scanSubscription != null;
    }

    @OnClick(R.id.scan_toggle_btn)
    public void onScanToggleClick() {
        // 判断当前扫描状态
        if (isScanning()) {
            // 停止扫描
            scanSubscription.unsubscribe();
        } else {
            // 设置过滤字符串
            resultsAdapter.setFilterStr(scanFilterStr.getText().toString());
            // 开始扫描
            scanSubscription = rxBleClient
                    .scanBleDevices()
                    .observeOn(AndroidSchedulers.mainThread())
                    // 清理状态
                    .doOnUnsubscribe(this::clearSubscription)
                    // 处理扫描结果
                    .subscribe(resultsAdapter::addScanResult, this::onScanFailure);
        }

        // 更新扫描按钮状态
        updateButtonUIState();
    }

    private void onScanFailure(Throwable th) {
        if (th instanceof BleScanException) {
            BleScanException bleScanException = (BleScanException) th;
            switch (bleScanException.getReason()) {
                case BleScanException.BLUETOOTH_NOT_AVAILABLE:
                    ts_show(mActivity, "蓝牙不可用");
                    break;
                case BleScanException.BLUETOOTH_DISABLED:
                    ts_show(mActivity, "请打开蓝牙");
                    break;
                case BleScanException.LOCATION_PERMISSION_MISSING:

                    ts_show(mActivity, "无法获取位置信息权限");
                    break;
                case BleScanException.LOCATION_SERVICES_DISABLED:
                    ts_show(mActivity, "请打开位置信息");
                    break;
                case BleScanException.BLUETOOTH_CANNOT_START:
                default:
                    ts_show(mActivity, "无法执行扫描");
                    break;
            }
        } else {
            ts_show(mActivity, "发生未知错误：" + th.getMessage());
        }
    }

    private void updateButtonUIState() {
        final boolean isScanning = isScanning();
        scanToggleButton.setText(isScanning ? R.string.stop_scan : R.string.start_scan);
        scanFilterStr.setEnabled(!isScanning);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isScanning()) {
            scanSubscription.unsubscribe();
        }
    }

    private void clearSubscription() {
        scanSubscription = null;
        resultsAdapter.clearScanResults();
        updateButtonUIState();
    }
}
