package tw.imonkey.plc2go;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class DevicePLCActivity extends AppCompatActivity  {
    public static final String devicePrefs = "devicePrefs";
    public static final String service="PLC"; //PLC監控機 deviceType
    //*******PLC****************
    //set serialport protocol parameters
    String STX=new String(new char[]{0x02});
    String ETX=new String(new char[]{0x03});
    String ENQ=new String(new char[]{0x05});
    String newLine=new String(new char[]{0x0D,0x0A});

    String[] cmd={"","","","",""};

    String deviceId, memberEmail;
    boolean master;
    ListView deviceView;
    ArrayList<String> friends = new ArrayList<>();
    ArrayList<String> CMDs = new ArrayList<>();
    DatabaseReference mFriends,mDevice,mRespond,mRequest,mCMDDel, mCMDSave;

    EditText ETCMDTest;
    Spinner PLC_Protocol,PLC_Mode,PLC_No,PLC_Register,Register_Block;
    TextView TVRX;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_plc);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();
//
        respondRX();

    }

    private void respondRX(){
        TVRX =(TextView)findViewById(R.id.textViewRX);
        mRespond= FirebaseDatabase.getInstance().getReference("/LOG/RS232/"+deviceId+"/RX/");
        mRespond.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.child("message").getValue()!= null) {
                    TVRX.setText(dataSnapshot.child("message").getValue().toString());
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void onClickTEST(View v){
        String CMDTest= ETCMDTest.getText().toString().trim();
        if (!TextUtils.isEmpty(CMDTest)){
            mRequest= FirebaseDatabase.getInstance().getReference("/LOG/RS232/"+deviceId+"/TX/");
            Map<String, Object> CMD = new HashMap<>();
            CMD.clear();
            CMD.put("message",CMDTest);
            CMD.put("memberEmail",memberEmail);
            CMD.put("timeStamp", ServerValue.TIMESTAMP);
            mRequest.push().setValue(CMD);
            Toast.makeText(DevicePLCActivity.this, "Test CMD:"+CMDTest, Toast.LENGTH_LONG).show();
        }
    }

    public void onClickSAVE(View v){
        String CMDTest= ETCMDTest.getText().toString().trim();
        if (!TextUtils.isEmpty(CMDTest)){
            mCMDSave= FirebaseDatabase.getInstance().getReference("/DEVICE/"+deviceId+"/SETTINGS/CMD/");
            mCMDSave.push().setValue(CMDTest);
            Toast.makeText(DevicePLCActivity.this, "Save CMD:"+CMDTest, Toast.LENGTH_LONG).show();
        }
    }

    public void onClickDEL(View v){
        mCMDDel=FirebaseDatabase.getInstance().getReference("/DEVICE/"+deviceId+"/SETTINGS/CMD/");
        mCMDDel.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                CMDs.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    CMDs.add(childSnapshot.getValue().toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        AlertDialog.Builder dialog_list = new AlertDialog.Builder(DevicePLCActivity.this);
        dialog_list.setTitle("選擇要刪除的CMD");
        dialog_list.setItems(CMDs.toArray(new String[0]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(DevicePLCActivity.this, "DEL CMD:" + CMDs.get(which), Toast.LENGTH_SHORT).show();
                mCMDDel.orderByValue().equalTo(CMDs.get(which)).addValueEventListener(new ValueEventListener() {
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
                CMDs.remove(which);
            }
        });
        dialog_list.show();
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
                AlertDialog.Builder dialog = new AlertDialog.Builder(DevicePLCActivity.this);
                LayoutInflater inflater = LayoutInflater.from(DevicePLCActivity.this);
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
                                        Toast.makeText(DevicePLCActivity.this, "已寄出邀請函(有效時間10分鐘)", Toast.LENGTH_LONG).show();
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
                AlertDialog.Builder dialog_list = new AlertDialog.Builder(DevicePLCActivity.this);
                dialog_list.setTitle("選擇要刪除的朋友");
                dialog_list.setItems(friends.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(DevicePLCActivity.this, "你要刪除是" + friends.get(which), Toast.LENGTH_SHORT).show();
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
        ETCMDTest =(EditText) findViewById(R.id.editTextCMDTest);
        mDevice = FirebaseDatabase.getInstance().getReference("/FUI/" + memberEmail.replace(".", "_") + "/" + deviceId);
        mDevice.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot != null) {
                    if (snapshot.child("connection").getValue() != null) {
                        setTitle(snapshot.child("companyId").getValue().toString() + "." + snapshot.child("device").getValue().toString() + "." + "上線");
                    } else {
                        setTitle(snapshot.child("companyId").getValue().toString() + "." + snapshot.child("device").getValue().toString() + "." + "離線");
                        Toast.makeText(DevicePLCActivity.this, "PLC智慧機離線", Toast.LENGTH_LONG).show();
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

        PLC_Protocol();
        PLC_No();
        PLC_Mode();
        PLC_Delay();
        PLC_Register();
        Register_Block();
    }
    private void PLC_No(){
        // Spinner element
        PLC_No = (Spinner) findViewById(R.id.spinnerPLCNo);
        // Spinner Drop down elements
        final List<String> items = new ArrayList<>();
        items.add("0000");
        items.add("00FF");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        PLC_No.setAdapter(dataAdapter);
        PLC_No.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(DevicePLCActivity.this, "你選的是" + items.get(position), Toast.LENGTH_SHORT).show();
                cmd[0]=items.get(position);
                ETCMDTest.setText(cmd[0]+cmd[1]+cmd[2]+cmd[3]+cmd[4]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void PLC_Mode(){
        // Spinner element
        PLC_Mode = (Spinner) findViewById(R.id.spinnerMode);
        // Spinner Drop down elements
        final List<String> items = new ArrayList<>();
        items.add("BR"); // read bit
        items.add("WR"); // read word
        items.add("BW"); // write bit
        items.add("WW"); // write word
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        PLC_Mode.setAdapter(dataAdapter);
        PLC_Mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(DevicePLCActivity.this, "你選的是" + items.get(position), Toast.LENGTH_SHORT).show();
                cmd[1]=items.get(position);
                ETCMDTest.setText(cmd[0]+cmd[1]+cmd[2]+cmd[3]+cmd[4]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void PLC_Delay(){
        // Spinner element
        PLC_Mode = (Spinner) findViewById(R.id.spinnerDelayTime);
        // Spinner Drop down elements
        final List<String> items = new ArrayList<>();
        items.add("0");
        items.add("5");
        items.add("A");
        items.add("E");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        PLC_Mode.setAdapter(dataAdapter);
        PLC_Mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(DevicePLCActivity.this, "你選的是" + items.get(position), Toast.LENGTH_SHORT).show();
                cmd[2]=items.get(position);
                ETCMDTest.setText(cmd[0]+cmd[1]+cmd[2]+cmd[3]+cmd[4]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }




    private void PLC_Register(){
        // Spinner element
        PLC_Register = (Spinner) findViewById(R.id.spinnerRegister);
        // Spinner Drop down elements
        final List<String> items = new ArrayList<>();
        items.add("M0000");// bit register
        items.add("M0010");// bit register
        items.add("M0020");// bit register
        items.add("M0030");// bit register
        items.add("D0000");// word register
        items.add("D0010");// word register
        items.add("D0020");// word register
        items.add("D0030");// word register
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        PLC_Register.setAdapter(dataAdapter);
        PLC_Register.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(DevicePLCActivity.this, "你選的是" + items.get(position), Toast.LENGTH_SHORT).show();
                cmd[3]=items.get(position);
                ETCMDTest.setText(cmd[0]+cmd[1]+cmd[2]+cmd[3]+cmd[4]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }
    private void Register_Block(){
        // Spinner element
        Register_Block = (Spinner) findViewById(R.id.spinnerBlock);
        // Spinner Drop down elements
        final List<String> items = new ArrayList<>();
        items.add("01");
        items.add("02");
        items.add("03");
        items.add("04");
        items.add("05");
        items.add("06");
        items.add("07");
        items.add("08");
        items.add("0A");
        items.add("0B");
        items.add("0C");
        items.add("0D");
        items.add("0E");
        items.add("0F");
        items.add("10");
        items.add("20");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        Register_Block.setAdapter(dataAdapter);
        Register_Block.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(DevicePLCActivity.this, "你選的是" + items.get(position), Toast.LENGTH_SHORT).show();
                cmd[4]=items.get(position);
                ETCMDTest.setText(cmd[0]+cmd[1]+cmd[2]+cmd[3]+cmd[4]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void PLC_Protocol(){

        // Spinner element
        PLC_Protocol = (Spinner) findViewById(R.id.spinnerProtocol);
        // Spinner Drop down elements
        final List<String> items = new ArrayList<>();
        items.add("No protocol");
        items.add("ModBus");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        PLC_Protocol.setAdapter(dataAdapter);
        PLC_Protocol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(DevicePLCActivity.this, "你選的是" + items.get(position), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
