package tw.imonkey.plc2go;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


class MessageHolder extends RecyclerView.ViewHolder {
    private final TextView mDeviceField;
    private final TextView mMessageField;
    private final TextView mDeviceTypeField;
    private final ImageView mPhotoField;
    public MessageHolder(View itemView) {
        super(itemView);
        mDeviceField = (TextView) itemView.findViewById(R.id.deviceName);
        mMessageField = (TextView) itemView.findViewById(R.id.deviceMessage);
        mDeviceTypeField=(TextView) itemView.findViewById(R.id.deviceType);
        mPhotoField =(ImageView) itemView.findViewById(R.id.deviceImage);
    }

    void setDevice(String device) {
        mDeviceField.setText(device);
    }
    void setDeviceType(String deviceType) {
        mDeviceTypeField.setText(deviceType);
    }
    void setMessage(String message) {
        mMessageField.setText(message);
    }
    void setPhoto(String Topics_id) {
        String devicePhotoPath = "/devicePhoto/" +Topics_id;
        StorageReference mImageRef = FirebaseStorage.getInstance().getReference(devicePhotoPath);
        Glide.with(mPhotoField.getContext())
                .using(new FirebaseImageLoader())
                .load(mImageRef)
                .into(mPhotoField);
    }
}
