package com.polidea.rxandroidble.sample.example1_scanning;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleScanResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class ScanResultsAdapter extends RecyclerView.Adapter<ScanResultsAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(android.R.id.text1)
        public TextView line1;
        @BindView(android.R.id.text2)
        public TextView line2;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnAdapterItemClickListener {

        void onAdapterViewClick(View view);
    }

    private static final Comparator<RxBleScanResult> SORTING_COMPARATOR =
            (lhs, rhs) -> String.valueOf(lhs.getRssi()).compareTo(String.valueOf(rhs.getRssi()));
    private final List<RxBleScanResult> data = new ArrayList<>();
    private OnAdapterItemClickListener onAdapterItemClickListener;
    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (onAdapterItemClickListener != null) {
                onAdapterItemClickListener.onAdapterViewClick(v);
            }
        }
    };

    public void setFilterStr(String filterStr) {
        mFilterStr = filterStr;
    }

    private String mFilterStr;

    public void addScanResult(RxBleScanResult bleScanResult) {
        final RxBleDevice newDevice = bleScanResult.getBleDevice();

        // 检查该结果是否应该被过滤
        if (!matchFilter(newDevice.getMacAddress(), newDevice.getName())) {
            return;
        }

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getBleDevice().equals(newDevice)) {
                data.set(i, bleScanResult);
                notifyItemChanged(i);
                return;
            }
        }

        data.add(bleScanResult);
        Collections.sort(data, SORTING_COMPARATOR);
        notifyDataSetChanged();
    }

    private boolean matchFilter(String mac, String name) {
        if (TextUtils.isEmpty(mFilterStr)) {
            return true;
        }

        // 忽略大小写比较
        return (!TextUtils.isEmpty(mac) && mac.toLowerCase().contains(mFilterStr.toLowerCase())) ||
                (!TextUtils.isEmpty(name) && name.toLowerCase().contains(mFilterStr.toLowerCase()));
    }

    public void clearScanResults() {
        data.clear();
        notifyDataSetChanged();
    }

    public RxBleScanResult getItemAtPosition(int childAdapterPosition) {
        return data.get(childAdapterPosition);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final RxBleScanResult rxBleScanResult = data.get(position);
        final RxBleDevice bleDevice = rxBleScanResult.getBleDevice();
        holder.line1.setText(String.format("%s (%s)", bleDevice.getMacAddress(), bleDevice.getName()));
        holder.line2.setText(String.format("RSSI: %d", rxBleScanResult.getRssi()));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.two_line_list_item, parent, false);
        itemView.setOnClickListener(onClickListener);
        return new ViewHolder(itemView);
    }

    public void setOnAdapterItemClickListener(OnAdapterItemClickListener onAdapterItemClickListener) {
        this.onAdapterItemClickListener = onAdapterItemClickListener;
    }
}
