package tw.imonkey.plc2go;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static tw.imonkey.plc2go.MainActivity.devicePrefs;

public class AddUserActivity extends AppCompatActivity {
    private static final int RC_CHOOSE_PHOTO = 101;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE =102 ;
    //private static final int RC_IMAGE_PERMS = 102;
    StorageReference mImageRef;
    DatabaseReference mUserFile, mAddDevice ,mAddMaster;
    String memberEmail,deviceId ,token;// deviceId=shopId=topics_id
    ImageView imageViewAddUser;
    Uri selectedImage ;
    EditText editTextAddCompanyId;
    EditText editTextAddUser;
    EditText editTextAddDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        Bundle extras = getIntent().getExtras();
        memberEmail =extras.getString("memberEmail");
        imageViewAddUser=(ImageView)(findViewById(R.id.imageViewAddUser));
        editTextAddCompanyId = (EditText) (findViewById(R.id.editTextAddCompanyId));
        editTextAddUser = (EditText) (findViewById(R.id.editTextAddUser));
        editTextAddDescription = (EditText) (findViewById(R.id.editTextAddDescription));
        if (checkSelfPermission(READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    READ_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to read the contacts
            }

            requestPermissions(new String[]{READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
            // app-defined int constant that should be quite unique

            return;
        }

    }

    public void addDevice(View view){
        String companyId = editTextAddCompanyId.getText().toString().trim();
        String username = editTextAddUser.getText().toString().trim();
        String description = editTextAddDescription.getText().toString().trim();
        if (!(TextUtils.isEmpty(companyId) ||TextUtils.isEmpty(username)||TextUtils.isEmpty(description))) {
            mAddMaster= FirebaseDatabase.getInstance().getReference("/FUI/" +memberEmail.replace(".", "_"));
            deviceId =mAddMaster.push().getKey();
            Map<String, Object> addMaster = new HashMap<>();
            addMaster.put("companyId",companyId) ;
            addMaster.put("device",username);
            addMaster.put("deviceType","主機");
            addMaster.put("description",description);
            addMaster.put("masterEmail",memberEmail) ;
            addMaster.put("timeStamp", ServerValue.TIMESTAMP);
            addMaster.put("topics_id",deviceId) ;
            mAddMaster.child(deviceId).setValue(addMaster);

            mAddDevice = FirebaseDatabase.getInstance().getReference("/DEVICE/"+deviceId);
            Map<String, Object> addDevice = new HashMap<>();
            addDevice.put("companyId",companyId);
            addDevice.put("device",username);
            addDevice.put("deviceType","主機");
            addDevice.put("description",description);
            addDevice.put("masterEmail",memberEmail) ;
            addDevice.put("timeStamp",ServerValue.TIMESTAMP);
            addDevice.put("topics_id",deviceId) ;
            mAddDevice.setValue(addDevice);


            mUserFile= FirebaseDatabase.getInstance().getReference("/USER/" +memberEmail.replace(".", "_"));
            token = FirebaseInstanceId.getInstance().getToken();
            Map<String, Object> addUser = new HashMap<>();
            addUser.put("memberEmail",memberEmail);
            addUser.put("deviceId",deviceId);
            addUser.put("username",username);
            addUser.put("token",token);
            addUser.put("timeStamp", ServerValue.TIMESTAMP);
            mUserFile.setValue(addUser);

            FirebaseMessaging.getInstance().subscribeToTopic(deviceId);
            SharedPreferences.Editor editor = getSharedPreferences(devicePrefs, Context.MODE_PRIVATE).edit();
            editor.putString("deviceId",deviceId);
            editor.putString("memberEmail",memberEmail);
            editor.apply();
            if (selectedImage!=null) {
                uploadPhoto(selectedImage);
            }

            Toast.makeText(this, "add user...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AddUserActivity.this,MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) {
                selectedImage = data.getData();
                imageViewAddUser.setImageURI(selectedImage);

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
                        Toast.makeText(AddUserActivity.this, "Image uploaded",
                                Toast.LENGTH_SHORT).show();


                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "uploadPhoto:onError", e);
                        Toast.makeText(AddUserActivity.this, "Upload failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

