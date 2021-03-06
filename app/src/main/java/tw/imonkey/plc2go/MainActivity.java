package tw.imonkey.plc2go;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


public class MainActivity extends Activity {

    ListView devicesView;
    FirebaseListAdapter mDeviceAdapter;
    DatabaseReference mDelDevice,presenceRef,lastOnlineRef;
    public static String memberEmail,myDeviceId,deviceId;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    StorageReference mImageRef;
    Boolean exit = false;
    Toast toast;
    public static final String devicePrefs = "devicePrefs";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Taipei"));
        SharedPreferences settings = getSharedPreferences(devicePrefs, Context.MODE_PRIVATE);
        myDeviceId = settings.getString("deviceId",null);

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d("getIntent", "Key: " + key + " Value: " + value);
            }
        }
        // [END handle_data_extras]
        memberCheck();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
        toast.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDeviceAdapter.cleanup();

    }

    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
        } else {
            Toast.makeText(this, "再按一次退出App?",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);
        }
    }

    private void memberCheck(){
        //initializing firebase auth object
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user!=null){
                    memberEmail=user.getEmail();
                    if(myDeviceId==null) {
                        Intent intent = new Intent(MainActivity.this, AddUserActivity.class);
                        intent.putExtra("memberEmail", memberEmail);
                        startActivity(intent);
                        finish();
                    }
                    getDevices();
                    phoneOnline();
                }
            }
        };
    }

    private void getDevices(){
        Query refDevice = FirebaseDatabase.getInstance().getReference("/FUI/"+memberEmail.replace(".", "_")).orderByChild("companyId");
        devicesView = (ListView) findViewById(R.id.listViewDevices);
        mDeviceAdapter= new FirebaseListAdapter<Device>(this, Device.class,R.layout.device_layout, refDevice) {

            @Override
            protected void populateView(View view, Device device, int position) {
                if (device.getTopics_id()!=null && device.getCompanyId()!=null && device.getDevice()!=null) {
                    FirebaseMessaging.getInstance().subscribeToTopic(device.getTopics_id());
                    if (device.getDeviceType().equals("主機")) {
                        ((TextView) view.findViewById(R.id.deviceName)).setText(device.getCompanyId() + "." + device.getDevice() + "." + "上線" + ":" + device.getDescription());
                    }else if (device.getConnection() != null) {
                        ((TextView) view.findViewById(R.id.deviceName)).setText(device.getCompanyId() + "." + device.getDevice() + "." + "上線" + ":" + device.getDescription());
                    } else {
                        ((TextView) view.findViewById(R.id.deviceName)).setText(device.getCompanyId() + "." + device.getDevice() + "." + "離線" + ":" + device.getDescription());
                    }

                    String devicePhotoPath = "/devicePhoto/" + device.getTopics_id();
                    mImageRef = FirebaseStorage.getInstance().getReference(devicePhotoPath);
                    ImageView imageView = (ImageView) view.findViewById(R.id.deviceImage);
                    Glide.with(MainActivity.this)
                            .using(new FirebaseImageLoader())
                            .load(mImageRef)
                            .into(imageView);

                    if (device.getAlert().get("message") != null) {
                        Calendar timeStamp = Calendar.getInstance();
                        timeStamp.setTimeInMillis(Long.parseLong(device.getAlert().get("timeStamp").toString()));
                        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss MM/dd", Locale.TAIWAN);
                        ((TextView) view.findViewById(R.id.deviceMessage)).setText(device.getAlert().get("message").toString());
                        ((TextView) view.findViewById(R.id.deviceType)).setText(df.format(timeStamp.getTime())+"  "+device.getDeviceType());
                    } else {
                        ((TextView) view.findViewById(R.id.deviceMessage)).setText("");
                        ((TextView) view.findViewById(R.id.deviceType)).setText(device.getDeviceType());
                    }
                }
            }
        };
        devicesView.setAdapter(mDeviceAdapter);

        devicesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                deviceId=mDeviceAdapter.getRef(position).getKey();
                mDeviceAdapter.getRef(position).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String deviceType=snapshot.child("deviceType").getValue().toString();
                        if (deviceType.equals("主機")) {
                            Intent intent = new Intent(MainActivity.this, BossActivity.class);
                            intent.putExtra("deviceId", deviceId);
                            intent.putExtra("memberEmail", memberEmail);
                            if (snapshot.child("masterEmail").getValue().toString().equals(memberEmail)){
                                intent.putExtra("master", true);
                            }else{
                                intent.putExtra("master", false);
                            }
                            startActivity(intent);
                        }else if(deviceType.equals("PLC監控機")){
                            Intent intent = new Intent(MainActivity.this, DevicePLCActivity.class);
                            intent.putExtra("deviceId", deviceId);
                            intent.putExtra("memberEmail", memberEmail);
                            if (snapshot.child("masterEmail").getValue().toString().equals(memberEmail)){
                                intent.putExtra("master", true);
                            }else{
                                intent.putExtra("master", false);
                            }
                            startActivity(intent);
                        }else if(deviceType.equals("GPIO智慧機")){
                            Intent intent = new Intent(MainActivity.this, DeviceRPI3IOActivity.class);
                            intent.putExtra("deviceId", deviceId);
                            intent.putExtra("memberEmail", memberEmail);
                            if (snapshot.child("masterEmail").getValue().toString().equals(memberEmail)){
                                intent.putExtra("master", true);
                            }else{
                                intent.putExtra("master", false);
                            }
                            startActivity(intent);
                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
            }
        });
        devicesView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //delDevice
                final String deviceId=mDeviceAdapter.getRef(position).getKey();
                String company_device=((TextView)view.findViewById(R.id.deviceName)).getText().toString();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setMessage("刪除智慧機:"+company_device);
                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDelDevice= FirebaseDatabase.getInstance().getReference("/FUI/" +memberEmail.replace(".", "_"));
                        mDelDevice.child(deviceId).removeValue();
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(deviceId);
                        dialog.cancel();
                    }
                });
                alertDialog.show();
                return true;
            }
        });
    }

    private void phoneOnline(){
        //phone online check
        // Write a string when this client loses connection
        presenceRef = FirebaseDatabase.getInstance().getReference("/USER/"+memberEmail.replace(".", "_")+"/connections");
        presenceRef.setValue(true);
        presenceRef.onDisconnect().setValue(null);
        lastOnlineRef =FirebaseDatabase.getInstance().getReference("/USER/"+memberEmail.replace(".", "_")+"/lastOnline");
        lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (!connected) {
                   toast=Toast.makeText(MainActivity.this,"手機失聯",Toast.LENGTH_SHORT);
                    toast.show();
                }else{
                    toast=Toast.makeText(MainActivity.this,"手機上線",Toast.LENGTH_SHORT);
                    toast.show();
                    presenceRef.setValue(true);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

}

