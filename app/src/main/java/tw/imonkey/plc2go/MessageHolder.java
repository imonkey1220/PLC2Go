package tw.imonkey.plc2go;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;


public class MessageHolder extends RecyclerView.ViewHolder {
    private final TextView mNameField;
    private final TextView mMessageField;

    public MessageHolder(View itemView) {
        super(itemView);
        mNameField = (TextView) itemView.findViewById(android.R.id.text1);
        mMessageField = (TextView) itemView.findViewById(android.R.id.text2);
    }

    public void setName(String name) {
        mNameField.setText(name);
    }

    public void setMessage(String message) {
        mMessageField.setText(message);
    }

}
