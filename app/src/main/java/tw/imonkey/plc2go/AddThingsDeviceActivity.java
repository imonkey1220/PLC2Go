package tw.imonkey.plc2go;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static tw.imonkey.plc2go.MainActivity.devicePrefs;


public class AddThingsDeviceActivity extends AppCompatActivity {
    private static final int RC_CHOOSE_PHOTO = 101;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE =102;
    //private static final int RC_IMAGE_PERMS = 102;
    StorageReference mImageRef;
    String memberEmail,deviceId,companyId,device,deviceType,description,serverIP;
    private WebSocketClient mWebSocketClient;
    ImageView imageViewAddDevice;
    Uri selectedImage ;
    EditText editTextAddCompanyId;
    EditText editTextAddDevice;
    EditText editTextAddDescription;
    EditText editTextServerIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_things_device);
        Bundle extras = getIntent().getExtras();
        deviceType = extras.getString("deviceType");
        SharedPreferences settings = getSharedPreferences(devicePrefs, Context.MODE_PRIVATE);
        memberEmail = settings.getString("memberEmail",null);

        imageViewAddDevice=(ImageView)(findViewById(R.id.imageViewAddThingsDevice));
        editTextAddCompanyId = (EditText) (findViewById(R.id.editTextAddThingsCompanyId));
        editTextAddDevice = (EditText) (findViewById(R.id.editTextAddThingsDevice));
        editTextAddDescription = (EditText) (findViewById(R.id.editTextAddThingsDescription));
        editTextServerIP = (EditText) (findViewById(R.id.editTextThingsIP));

        if (ContextCompat.checkSelfPermission(AddThingsDeviceActivity.this,
                READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(AddThingsDeviceActivity.this,
                    READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(AddThingsDeviceActivity.this,
                        new String[]{READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) {
                selectedImage = data.getData();
                imageViewAddDevice.setImageURI(selectedImage);

            } else {
                Toast.makeText(this, "No image chosen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void choosePhoto(View view) {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RC_CHOOSE_PHOTO);
    }

    protected void uploadPhoto(Uri uri) {

        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();

        // Upload to Firebase Storage
        String devicePhotoPath = "/devicePhoto/"+deviceId;
        mImageRef = FirebaseStorage.getInstance().getReference(devicePhotoPath);
        mImageRef.putFile(uri)
                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //      Log.d("TAG", "uploadPhoto:onSuccess:" +
                        //              taskSnapshot.getMetadata().getReference().getPath());
                        Toast.makeText(AddThingsDeviceActivity.this, "Image uploaded",
                                Toast.LENGTH_SHORT).show();


                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "uploadPhoto:onError", e);
                        Toast.makeText(AddThingsDeviceActivity.this, "Upload failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void toFirebase(){
        DatabaseReference mAddMaster= FirebaseDatabase.getInstance().getReference("/FUI/" +memberEmail.replace(".", "_"));
        deviceId =mAddMaster.push().getKey();
        Map<String, Object> addMaster = new HashMap<>();
        addMaster.put("companyId",companyId) ;
        addMaster.put("device",device);
        addMaster.put("deviceType",deviceType); //PLC監控機;GPIO智慧機
        addMaster.put("description",description);
        addMaster.put("masterEmail",memberEmail) ;
        addMaster.put("timeStamp", ServerValue.TIMESTAMP);
        addMaster.put("topics_id",deviceId);
        mAddMaster.child(deviceId).setValue(addMaster);

        DatabaseReference mAddDevice = FirebaseDatabase.getInstance().getReference("/DEVICE/"+deviceId);//DEVICE for friends
        Map<String, Object> addDevice = new HashMap<>();
        addDevice.put("companyId",companyId);
        addDevice.put("device",device);
        addDevice.put("deviceType",deviceType);
        addDevice.put("description",description);
        addDevice.put("masterEmail",memberEmail) ;
        addDevice.put("timeStamp",ServerValue.TIMESTAMP);
        addDevice.put("topics_id",deviceId) ;
        mAddDevice.setValue(addDevice);

        if (selectedImage != null) {
            uploadPhoto(selectedImage);
        }
    }

    private void connectWebSocket(String serverIP) {
        URI uri;
        try {
            uri = new URI("ws://"+serverIP+":9402");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                //      Log.i("Websocket", "Opened");
                mWebSocketClient.send(memberEmail+","+deviceId);
            }
            @Override
            public void onMessage(String s) {}
            @Override
            public void onClose(int i, String s, boolean b) {}
            @Override
            public void onError(Exception e) {}
        };
        mWebSocketClient.connect();
    }

    public void addThingsDevice(View view) {
         companyId = editTextAddCompanyId.getText().toString().trim();
         device = editTextAddDevice.getText().toString().trim();
         description = editTextAddDescription.getText().toString().trim();
         serverIP = editTextServerIP.getText().toString().trim();
        if (!(TextUtils.isEmpty(serverIP) || TextUtils.isEmpty(companyId) || TextUtils.isEmpty(device) || TextUtils.isEmpty(description))) {
            toFirebase();
            connectWebSocket(serverIP);
            Toast.makeText(this, "add device...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AddThingsDeviceActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,Main2Activity.class);
        startActivity(intent);
        finish();
    }
}

