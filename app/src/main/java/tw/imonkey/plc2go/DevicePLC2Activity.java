package tw.imonkey.plc2go;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class DevicePLC2Activity extends AppCompatActivity {
    public static final String devicePrefs = "devicePrefs";
    public static final String service="PLC"; //PLC監控機 deviceType
    //*******PLC****************
    //set serialport protocol parameters
    String STX=new String(new char[]{0x02});
    String ETX=new String(new char[]{0x03});
    String ENQ=new String(new char[]{0x05});
    String newLine=new String(new char[]{0x0D,0x0A});

    String deviceId, memberEmail;
    boolean master;
    ListView userView;
    ArrayList<String> users = new ArrayList<>();
    DatabaseReference  mUsers,mDevice,mRegister;
    FirebaseRecyclerAdapter mRegisterAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_plc2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "設定PLC UART", Snackbar.LENGTH_LONG)
                        .setAction("Go",new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(DevicePLC2Activity.this, DevicePLCActivity.class);
                                intent.putExtra("deviceId", deviceId);
                                intent.putExtra("memberEmail", memberEmail);
                                startActivity(intent);
                                finish();
                            }
                        }).show();
            }
        });
        init();
        RegisterView();
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
                AlertDialog.Builder dialog = new AlertDialog.Builder(DevicePLC2Activity.this);
                LayoutInflater inflater = LayoutInflater.from(DevicePLC2Activity.this);
                final View v = inflater.inflate(R.layout.add_friend, userView, false);
                dialog.setTitle("邀請朋友加入服務");
                dialog.setView(v);
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final EditText editTextAddFriendEmail = (EditText) (v.findViewById(R.id.editTextAddFriendEmail));
                        if (!editTextAddFriendEmail.getText().toString().isEmpty()) {
                            DatabaseReference mAddfriend = FirebaseDatabase.getInstance().getReference("/DEVICE/" + deviceId);
                            mAddfriend.child("/users/" + editTextAddFriendEmail.getText().toString().replace(".", "_")).setValue(editTextAddFriendEmail.getText().toString());
                            Toast.makeText(DevicePLC2Activity.this, "已寄出邀請函(有效時間10分鐘)", Toast.LENGTH_LONG).show();
                        }
                        dialog.cancel();
                    }
                });
                dialog.show();

                return true;

            case R.id.action_del_friend:
                AlertDialog.Builder dialog_list = new AlertDialog.Builder(DevicePLC2Activity.this);
                dialog_list.setTitle("選擇要刪除的朋友");
                dialog_list.setItems(users.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(DevicePLC2Activity.this, "你要刪除是" + users.get(which), Toast.LENGTH_SHORT).show();
                        mUsers.orderByValue().equalTo(users.get(which)).addListenerForSingleValueEvent(new ValueEventListener() {
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
                        users.remove(which);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRegisterAdapter.cleanup();
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,Main2Activity.class);
        startActivity(intent);
        finish();
    }


    private void init(){
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Taipei"));
        Bundle extras = getIntent().getExtras();
        deviceId = extras.getString("deviceId");
        memberEmail = extras.getString("memberEmail");
        master = extras.getBoolean("master");
        mDevice = FirebaseDatabase.getInstance().getReference("/DEVICE/"+ deviceId);
        mDevice.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot != null) {
                    if (snapshot.child("connection").getValue() != null) {
                        setTitle(snapshot.child("companyId").getValue().toString() + "." + snapshot.child("device").getValue().toString() + "." + "上線");
                    } else {
                        setTitle(snapshot.child("companyId").getValue().toString() + "." + snapshot.child("device").getValue().toString() + "." + "離線");
                        Toast.makeText(DevicePLC2Activity.this, "PLC智慧機離線", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //Device's Users
        mUsers= FirebaseDatabase.getInstance().getReference("/DEVICE/"+deviceId+"/users/");
        mUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    users.add(childSnapshot.getValue().toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void RegisterView() {
        mRegister = FirebaseDatabase.getInstance().getReference("/DEVICE/"+deviceId+"/REGISTER/");
        RecyclerView RV5 = (RecyclerView) findViewById(R.id.RV5);
        RV5.setLayoutManager(new LinearLayoutManager(this));
        mRegisterAdapter = new FirebaseRecyclerAdapter<RegisterPLC, RegisterHolder>(
                RegisterPLC.class,
                android.R.layout.two_line_list_item,
                RegisterHolder.class,
                mRegister){
            @Override
            public void populateViewHolder(RegisterHolder holder, RegisterPLC register, final int position) {
                holder.setName(register.getName());
                holder.setMessage(register.getMessage());
            }
        };
        RV5.setAdapter(mRegisterAdapter);
        RV5.addOnItemTouchListener(new RecyclerViewTouchListener(getApplicationContext(), RV5, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                // TODO
            }
            @Override
            public void onLongClick(View view, int position) {
                showDialog(mRegisterAdapter.getRef(position).getKey());
            }
        }));
    }


    private void showDialog(final String address) {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle(address)
                .setMessage("請輸入暫存器功能")
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mRegister.child("name").setValue(input.getText());
                    }
                })
                .show();
    }

}
