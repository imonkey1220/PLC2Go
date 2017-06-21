package tw.imonkey.plc2go;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;


class MessageHolder extends RecyclerView.ViewHolder {
    private final TextView mDeviceField;
    private final TextView mMessageField;

    public MessageHolder(View itemView) {
        super(itemView);
        mDeviceField = (TextView) itemView.findViewById(R.id.deviceName);
        mMessageField = (TextView) itemView.findViewById(R.id.deviceMessage);
    }

    void setDevice(String name) {
        mDeviceField.setText(name);
    }
    void setMessage(String message) {
        mMessageField.setText(message);
    }

}
