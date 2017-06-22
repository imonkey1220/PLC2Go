package tw.imonkey.plc2go;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.view.View;

import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class Main2Activity extends AppCompatActivity {
    FirebaseRecyclerAdapter mDeviceAdapter;

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
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Taipei"));
        SharedPreferences settings = getSharedPreferences(devicePrefs, Context.MODE_PRIVATE);
        myDeviceId = settings.getString("deviceId",null);
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d("getIntent", "Key: " + key + " Value: " + value);
            }
        }
        memberCheck();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDeviceAdapter.cleanup();
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
                        Intent intent = new Intent(Main2Activity.this, AddUserActivity.class);
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
                    toast=Toast.makeText(Main2Activity.this,"手機失聯",Toast.LENGTH_SHORT);
                    toast.show();
                }else{
                    toast=Toast.makeText(Main2Activity.this,"手機上線",Toast.LENGTH_SHORT);
                    toast.show();
                    presenceRef.setValue(true);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void getDevices() {
        RecyclerView RV4 = (RecyclerView) findViewById(R.id.RV4);
        RV4.setLayoutManager(new LinearLayoutManager(this));
        DatabaseReference refDevice = FirebaseDatabase.getInstance().getReference("/FUI/" + memberEmail.replace(".", "_"));
        mDeviceAdapter = new FirebaseRecyclerAdapter<Device, MessageHolder>(
                Device.class,
                R.layout.listview_device_layout,
                MessageHolder.class,
                refDevice) {

            @Override
            public void populateViewHolder(MessageHolder holder, Device device, final int position) {
                if (device.getTopics_id() != null && device.getCompanyId() != null && device.getDevice() != null) {
                    FirebaseMessaging.getInstance().subscribeToTopic(device.getTopics_id());
                    if (device.getDeviceType().equals("主機")) {
                        holder.setDevice(device.getCompanyId() + "." + device.getDevice() + "." + "上線" + ":" + device.getDescription());
                    } else if (device.getConnection() != null) {
                        holder.setDevice(device.getCompanyId() + "." + device.getDevice() + "." + "上線" + ":" + device.getDescription());
                    } else {
                        holder.setDevice(device.getCompanyId() + "." + device.getDevice() + "." + "離線" + ":" + device.getDescription());
                    }
                    holder.setPhoto(device.getTopics_id());

                    if (device.getAlert().get("message") != null) {
                        Calendar timeStamp = Calendar.getInstance();
                        timeStamp.setTimeInMillis(Long.parseLong(device.getAlert().get("timeStamp").toString()));
                        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss MM/dd", Locale.TAIWAN);
                        holder.setMessage(device.getAlert().get("message").toString());
                        holder.setDeviceType(df.format(timeStamp.getTime()) + "  " + device.getDeviceType());
                    } else {
                        holder.setMessage("");
                        holder.setDeviceType(device.getDeviceType());
                    }
                }
            }
        };
        RV4.setAdapter(mDeviceAdapter);
        RV4.addOnItemTouchListener(new RecyclerViewTouchListener(getApplicationContext(), RV4, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                deviceId = mDeviceAdapter.getRef(position).getKey();
                mDeviceAdapter.getRef(position).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String deviceType = snapshot.child("deviceType").getValue().toString();
                        if (deviceType.equals("主機")) {
                            Intent intent = new Intent(Main2Activity.this, BossActivity.class);
                            intent.putExtra("deviceId", deviceId);
                            intent.putExtra("memberEmail", memberEmail);
                            if (snapshot.child("masterEmail").getValue().toString().equals(memberEmail)) {
                                intent.putExtra("master", true);
                            } else {
                                intent.putExtra("master", false);
                            }
                            startActivity(intent);
                        } else if (deviceType.equals("PLC監控機")) {
                            Intent intent = new Intent(Main2Activity.this, DevicePLCActivity.class);
                            intent.putExtra("deviceId", deviceId);
                            intent.putExtra("memberEmail", memberEmail);
                            if (snapshot.child("masterEmail").getValue().toString().equals(memberEmail)) {
                                intent.putExtra("master", true);
                            } else {
                                intent.putExtra("master", false);
                            }
                            startActivity(intent);
                        } else if (deviceType.equals("GPIO智慧機")) {
                            Intent intent = new Intent(Main2Activity.this, DeviceRPI3IOActivity.class);
                            intent.putExtra("deviceId", deviceId);
                            intent.putExtra("memberEmail", memberEmail);
                            if (snapshot.child("masterEmail").getValue().toString().equals(memberEmail)) {
                                intent.putExtra("master", true);
                            } else {
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

            @Override
            public void onLongClick(View view, int position) {
                //delDevice
                final String deviceId=mDeviceAdapter.getRef(position).getKey();
                String company_device=((TextView)view.findViewById(R.id.deviceName)).getText().toString();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Main2Activity.this);
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
            }
        }));

    }
}
