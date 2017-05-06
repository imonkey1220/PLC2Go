package tw.imonkey.plc2go;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class DeviceRPI3IOActivity extends AppCompatActivity {
    public static final String devicePrefs = "devicePrefs";
    public static final String service="RPI3IO"; //GPIO智慧機 deviceType
    String deviceId, memberEmail;
    boolean master;
    ListView deviceView;
    ArrayList<String> friends = new ArrayList<>();
    DatabaseReference mFriends, mDevice ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_rpi3_io);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (master) {
            getMenuInflater().inflate(R.menu.menu, menu);
            return true;
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_friend:
                AlertDialog.Builder dialog = new AlertDialog.Builder(DeviceRPI3IOActivity.this);
                LayoutInflater inflater = LayoutInflater.from(DeviceRPI3IOActivity.this);
                final View v = inflater.inflate(R.layout.add_friend, deviceView, false);
                dialog.setTitle("邀請朋友加入服務");
                dialog.setView(v);
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final EditText editTextAddFriendEmail = (EditText) (v.findViewById(R.id.editTextAddFriendEmail));
                        if (!editTextAddFriendEmail.getText().toString().isEmpty()) {
                            DatabaseReference refDevice = FirebaseDatabase.getInstance().getReference("/DEVICE/" + deviceId);
                            refDevice.child("friend").push().setValue(editTextAddFriendEmail.getText().toString());
                            refDevice.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                        Device device = snapshot.getValue(Device.class);
                                        DatabaseReference mInvitation = FirebaseDatabase.getInstance().getReference("/FUI/" + editTextAddFriendEmail.getText().toString().replace(".", "_") + "/" + deviceId);
                                        mInvitation.setValue(device);
                                        Toast.makeText(DeviceRPI3IOActivity.this, "已寄出邀請函(有效時間10分鐘)", Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError firebaseError) {

                                }
                            });
                        }
                        dialog.cancel();
                    }
                });
                dialog.show();

                return true;

            case R.id.action_del_friend:
                AlertDialog.Builder dialog_list = new AlertDialog.Builder(DeviceRPI3IOActivity.this);
                dialog_list.setTitle("選擇要刪除的朋友");
                dialog_list.setItems(friends.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(DeviceRPI3IOActivity.this, "你要刪除是" + friends.get(which), Toast.LENGTH_SHORT).show();
                        mFriends.orderByValue().equalTo(friends.get(which)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                    childSnapshot.getRef().removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        friends.remove(which);
                    }
                });
                dialog_list.show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void init(){
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Taipei"));
        Bundle extras = getIntent().getExtras();
        deviceId = extras.getString("deviceId");
        memberEmail = extras.getString("memberEmail");
        master = extras.getBoolean("master");
        mDevice = FirebaseDatabase.getInstance().getReference("/FUI/" + memberEmail.replace(".", "_") + "/" + deviceId);
        mDevice.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot != null) {
                    if (snapshot.child("connection").getValue() != null) {
                        setTitle(snapshot.child("companyId").getValue().toString() + "." + snapshot.child("device").getValue().toString() + "." + "上線");
                    } else {
                        setTitle(snapshot.child("companyId").getValue().toString() + "." + snapshot.child("device").getValue().toString() + "." + "離線");
                        Toast.makeText(DeviceRPI3IOActivity.this, "PLC智慧機離線", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        mFriends=FirebaseDatabase.getInstance().getReference("/DEVICE/"+deviceId+"/friend/");
        mFriends.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                friends.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    friends.add(childSnapshot.getValue().toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
