package tw.imonkey.plc2go;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class DeviceRPI3IOActivity extends AppCompatActivity {
    public static final String devicePrefs = "devicePrefs";
    public static final String service="RPI3IO"; //GPIO智慧機 deviceType
    String deviceId, memberEmail;
    boolean master;
    ArrayList<String> friends = new ArrayList<>();
    Map<String, Object> cmd = new HashMap<>();
    Map<String, Object> log = new HashMap<>();
    DatabaseReference mFriends,mDevice,mLog,mXINPUT,mYOUTPUT,mSETTINGS;
    FirebaseListAdapter mAdapter;
    ListView deviceView ,logView;
    Switch Y00,Y01,Y02,Y03,Y04,Y05,Y06,Y07;
    TextView X00,X01,X02,X03,X04,X05,X06,X07;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_rpi3_io);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        X00=(TextView) findViewById(R.id.textViewX00);
        X01=(TextView) findViewById(R.id.textViewX01);
        X02=(TextView) findViewById(R.id.textViewX02);
        X03=(TextView) findViewById(R.id.textViewX03);
        X04=(TextView) findViewById(R.id.textViewX04);
        X05=(TextView) findViewById(R.id.textViewX05);
        X06=(TextView) findViewById(R.id.textViewX06);
        X07=(TextView) findViewById(R.id.textViewX07);

        Y00=(Switch) findViewById(R.id.switchY00);
        Y01=(Switch) findViewById(R.id.switchY01);
        Y02=(Switch) findViewById(R.id.switchY02);
        Y03=(Switch) findViewById(R.id.switchY03);
        Y04=(Switch) findViewById(R.id.switchY04);
        Y05=(Switch) findViewById(R.id.switchY05);
        Y06=(Switch) findViewById(R.id.switchY06);
        Y07=(Switch) findViewById(R.id.switchY07);

        init();
        SETTINGS();

        mLog=FirebaseDatabase.getInstance().getReference("/LOG/GPIO/" + deviceId+"/LOG/");
        Query refDevice = FirebaseDatabase.getInstance().getReference("/LOG/GPIO/" + deviceId+"/LOG/").limitToLast(25);
        logView = (ListView) findViewById(R.id.listViewLog);
        mAdapter= new FirebaseListAdapter<Message>(this, Message.class, android.R.layout.two_line_list_item, refDevice) {
            @Override
            public Message getItem(int position) {
                return super.getItem(getCount() - (position + 1)); //反轉排序
            }

            @Override
            protected void populateView(View view, Message message, int position) {
                Calendar timeStamp= Calendar.getInstance();
                timeStamp.setTimeInMillis(message.getTimeStamp());
                SimpleDateFormat df = new SimpleDateFormat(" HH:mm:ss MM/dd", Locale.TAIWAN);
                if (position%2==0) {
                    ((TextView) view.findViewById(android.R.id.text1)).setText(message.getMessage());
                    ((TextView) view.findViewById(android.R.id.text1)).setTextColor(Color.BLUE);
                }else{
                    ((TextView) view.findViewById(android.R.id.text1)).setText(message.getMessage());
                    ((TextView) view.findViewById(android.R.id.text1)).setTextColor(Color.RED);
                }
                ((TextView)view.findViewById(android.R.id.text2)).setText((df.format(timeStamp.getTime())));

            }
        };
        logView.setAdapter(mAdapter);

        mYOUTPUT=FirebaseDatabase.getInstance().getReference("/LOG/GPIO/" + deviceId+"/Y/");
        Y00.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Y00.isChecked()) {
                    cmd.clear();
                    cmd.put("Y00",true);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp", ServerValue.TIMESTAMP);
                    mYOUTPUT.child("Y00").push().setValue(cmd);
                    log("Y_input:"+memberEmail+"->Y00=true");
                }else{
                    cmd.clear();
                    cmd.put("Y00",false);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mYOUTPUT.child("Y00").push().setValue(cmd);
                    log("Y_input:"+memberEmail+"->Y00=false");

                }
            }
        });
        Y01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Y01.isChecked()) {
                    cmd.clear();
                    cmd.put("Y01",true);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mYOUTPUT.child("Y01").push().setValue(cmd);
                    log("Y_input:"+memberEmail+"->Y01=true");

                }else{
                    cmd.clear();
                    cmd.put("Y01",false);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mYOUTPUT.child("Y01").push().setValue(cmd);
                    log("Y_input:"+memberEmail+"->Y01=false");

                }
            }
        });

        Y02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Y02.isChecked()) {
                    cmd.clear();
                    cmd.put("Y02",true);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mYOUTPUT.child("Y02").push().setValue(cmd);
                    log("Y_input:"+memberEmail+"->Y02=true");

                }else{
                    cmd.clear();
                    cmd.put("Y02",false);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mYOUTPUT.child("Y02").push().setValue(cmd);
                    log("Y_input:"+memberEmail+"->Y02=false");

                }
            }
        });

        Y03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Y03.isChecked()) {
                    cmd.clear();
                    cmd.put("Y03",true);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mYOUTPUT.child("Y03").push().setValue(cmd);
                    log("Y_input:"+memberEmail+"->Y03=true");

                }else{
                    cmd.clear();
                    cmd.put("Y03",false);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mYOUTPUT.child("Y03").push().setValue(cmd);
                    log("Y_input:"+memberEmail+"->Y03=false");
                }
            }
        });

        Y04.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Y04.isChecked()) {
                    cmd.clear();
                    cmd.put("Y04",true);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mYOUTPUT.child("Y04").push().setValue(cmd);
                    log("Y_input:"+memberEmail+"->Y04=true");
                }else{
                    cmd.clear();
                    cmd.put("Y04",false);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mYOUTPUT.child("Y04").push().setValue(cmd);
                    log("Y_input:"+memberEmail+"->Y04=false");

                }
            }
        });
        Y05.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Y05.isChecked()) {
                    cmd.clear();
                    cmd.put("Y05",true);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mYOUTPUT.child("Y05").push().setValue(cmd);
                    log("Y_input:"+memberEmail+"->Y05=true");

                }else{
                    cmd.clear();
                    cmd.put("Y05",false);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mYOUTPUT.child("Y05").push().setValue(cmd);
                    log("Y_input:"+memberEmail+"->Y05=false");

                }
            }
        });

        Y06.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Y06.isChecked()) {
                    cmd.clear();
                    cmd.put("Y06",true);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mYOUTPUT.child("Y06").push().setValue(cmd);
                    log("Y_input:"+memberEmail+"->Y06=true");

                }else{
                    cmd.clear();
                    cmd.put("Y06",false);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mYOUTPUT.child("Y06").push().setValue(cmd);
                    log("Y_input:"+memberEmail+"->Y06=false");

                }
            }
        });

        Y07.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Y07.isChecked()) {
                    cmd.clear();
                    cmd.put("Y07",true);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mYOUTPUT.child("Y07").push().setValue(cmd);
                    log("Y_input:"+memberEmail+"->Y07=true");

                }else{
                    cmd.clear();
                    cmd.put("Y07",false);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mYOUTPUT.child("Y07").push().setValue(cmd);
                    log("Y_input:"+memberEmail+"->Y07=false");
                }
            }
        });

        mYOUTPUT.child("Y00").limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("Y00").getValue()!=null) {
                    if(dataSnapshot.child("Y00").getValue().equals(true)) {
                        Y00.setChecked(true);
                    }else{
                        Y00.setChecked(false);
                    }
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

        mYOUTPUT.child("Y01").limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("Y01").getValue()!=null) {
                    if(dataSnapshot.child("Y01").getValue().equals(true)) {
                        Y01.setChecked(true);
                    }else{
                        Y01.setChecked(false);
                    }
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

        mYOUTPUT.child("Y02").limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("Y02").getValue()!=null) {
                    if(dataSnapshot.child("Y02").getValue().equals(true)) {
                        Y02.setChecked(true);
                    }else{
                        Y02.setChecked(false);
                    }
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

        mYOUTPUT.child("Y03").limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("Y03").getValue()!=null) {
                    if(dataSnapshot.child("Y03").getValue().equals(true)) {
                        Y03.setChecked(true);
                    }else{
                        Y03.setChecked(false);
                    }
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

        mYOUTPUT.child("Y04").limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("Y04").getValue()!=null) {
                    if(dataSnapshot.child("Y04").getValue().equals(true)) {
                        Y04.setChecked(true);
                    }else{
                        Y04.setChecked(false);
                    }
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

        mYOUTPUT.child("Y05").limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("Y05").getValue()!=null) {
                    if(dataSnapshot.child("Y05").getValue().equals(true)) {
                        Y05.setChecked(true);
                    }else{
                        Y05.setChecked(false);
                    }
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

        mYOUTPUT.child("Y06").limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("Y06").getValue()!=null) {
                    if(dataSnapshot.child("Y06").getValue().equals(true)) {
                        Y06.setChecked(true);
                    }else{
                        Y06.setChecked(false);
                    }
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

        mYOUTPUT.child("Y07").limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("Y07").getValue()!=null) {
                    if(dataSnapshot.child("Y07").getValue().equals(true)) {
                        Y07.setChecked(true);
                    }else{
                        Y07.setChecked(false);
                    }
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


        mXINPUT= FirebaseDatabase.getInstance().getReference("/LOG/GPIO/" + deviceId+"/X/");
        mXINPUT.child("X00").limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("X00").getValue()!=null) {
                    if(dataSnapshot.child("X00").getValue().equals(true)) {
                        X00.setBackgroundColor(Color.RED);
                    }else{
                        X00.setBackgroundColor(Color.BLUE);
                    }
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mXINPUT.child("X01").limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("X01").getValue()!=null) {
                    if(dataSnapshot.child("X01").getValue().equals(true)) {
                        X01.setBackgroundColor(Color.RED);
                    }else{
                        X01.setBackgroundColor(Color.BLUE);
                    }
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mXINPUT.child("X02").limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("X02").getValue()!=null) {
                    if(dataSnapshot.child("X02").getValue().equals(true)) {
                        X02.setBackgroundColor(Color.RED);
                    }else{
                        X02.setBackgroundColor(Color.BLUE);
                    }
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mXINPUT.child("X03").limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("X03").getValue()!=null) {
                    if(dataSnapshot.child("X03").getValue().equals(true)) {
                        X03.setBackgroundColor(Color.RED);
                    }else{
                        X03.setBackgroundColor(Color.BLUE);
                    }
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mXINPUT.child("X04").limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("X04").getValue()!=null) {
                    if(dataSnapshot.child("X04").getValue().equals(true)) {
                        X04.setBackgroundColor(Color.RED);
                    }else{
                        X04.setBackgroundColor(Color.BLUE);
                    }
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mXINPUT.child("X05").limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("X05").getValue()!=null) {
                    if(dataSnapshot.child("X05").getValue().equals(true)) {
                        X05.setBackgroundColor(Color.RED);
                    }else{
                        X05.setBackgroundColor(Color.BLUE);
                    }
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mXINPUT.child("X06").limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("X06").getValue()!=null) {
                    if(dataSnapshot.child("X06").getValue().equals(true)) {
                        X06.setBackgroundColor(Color.RED);
                    }else{
                        X06.setBackgroundColor(Color.BLUE);
                    }
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mXINPUT.child("X07").limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("X07").getValue()!=null) {
                    if(dataSnapshot.child("X07").getValue().equals(true)) {
                        X07.setBackgroundColor(Color.RED);
                    }else{
                        X07.setBackgroundColor(Color.BLUE);
                    }
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mFriends=FirebaseDatabase.getInstance().getReference("/DEVICE/"+memberEmail.replace(".","_")+"/"+deviceId+"/"+"friend");
        mFriends.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                friends.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    friends.add(childSnapshot.getValue().toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });


    }

    private void SETTINGS() {
        mSETTINGS = FirebaseDatabase.getInstance().getReference("/DEVICES/" + deviceId + "/SETTINGS");
        mSETTINGS.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    X00.setText(snapshot.child("X00").getValue().toString());
                    X01.setText(snapshot.child("X01").getValue().toString());
                    X02.setText(snapshot.child("X02").getValue().toString());
                    X03.setText(snapshot.child("X03").getValue().toString());
                    X04.setText(snapshot.child("X04").getValue().toString());
                    X05.setText(snapshot.child("X05").getValue().toString());
                    X06.setText(snapshot.child("X06").getValue().toString());
                    X07.setText(snapshot.child("X07").getValue().toString());
                    Y00.setText(snapshot.child("Y00").getValue().toString());
                    Y01.setText(snapshot.child("Y01").getValue().toString());
                    Y02.setText(snapshot.child("Y02").getValue().toString());
                    Y03.setText(snapshot.child("Y03").getValue().toString());
                    Y04.setText(snapshot.child("Y04").getValue().toString());
                    Y05.setText(snapshot.child("Y05").getValue().toString());
                    Y06.setText(snapshot.child("Y06").getValue().toString());
                    Y07.setText(snapshot.child("Y07").getValue().toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        X00.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                showDialog("X00");
                return true;
            }
        });
        X01.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                showDialog("X01");
                return true;
            }
        });
        X02.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                showDialog("X02");
                return true;
            }
        });
        X03.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                showDialog("X03");
                return true;
            }
        });
        X04.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                showDialog("X04");
                return true;
            }
        });
        X05.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                showDialog("X05");
                return true;
            }
        });
        X06.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                showDialog("X06");
                return true;
            }
        });
        X07.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                showDialog("X07");
                return true;
            }
        });

        Y00.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                showDialog("Y00");
                return true;
            }
        });
        Y01.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                showDialog("Y01");
                return true;
            }
        });
        Y02.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                showDialog("Y02");
                return true;
            }
        });
        Y03.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                showDialog("Y03");
                return true;
            }
        });
        Y04.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                showDialog("Y04");
                return true;
            }
        });
        Y05.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                showDialog("Y05");
                return true;
            }
        });
        Y06.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                showDialog("Y06");
                return true;
            }
        });
        Y07.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                showDialog("Y07");
                return true;
            }
        });
    }

    private void showDialog(final String PINOUT) {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle(PINOUT)
                .setMessage("請輸入"+PINOUT+"功能")
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mSETTINGS.child(PINOUT).setValue(input.getText());
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,Main2Activity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
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
                        Toast.makeText(DeviceRPI3IOActivity.this, "GPIO智慧機離線", Toast.LENGTH_LONG).show();
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


    private void log(String message) {
        log.clear();
        log.put("message", message);
        log.put("memberEmail", memberEmail);
        log.put("timeStamp", ServerValue.TIMESTAMP);
        mLog.push().setValue(log);
    }

    public void buttonSendMessageOnClick(View view){
        EditText editTextTalk=(EditText)findViewById(R.id.editTextTalk);
        DatabaseReference mTalk=FirebaseDatabase.getInstance().getReference("/LOG/GPIO/" + deviceId+"/LOG/");
        if(TextUtils.isEmpty(editTextTalk.getText().toString().trim())){
            Map<String, Object> addMessage = new HashMap<>();
            addMessage.put("memberEmail",memberEmail);
            addMessage.put("message","Gotcha:"+memberEmail);
            addMessage.put("timeStamp", ServerValue.TIMESTAMP);
            mTalk.push().setValue(addMessage);
            Toast.makeText(DeviceRPI3IOActivity.this, "Gotcha!", Toast.LENGTH_LONG).show();
        }else{
            Map<String, Object> addMessage = new HashMap<>();
            addMessage.put("memberEmail",memberEmail);
            addMessage.put("message","Gotcha:"+memberEmail+"->"+editTextTalk.getText().toString().trim());
            addMessage.put("timeStamp", ServerValue.TIMESTAMP);
            mTalk.push().setValue(addMessage);
            Toast.makeText(DeviceRPI3IOActivity.this,editTextTalk.getText().toString().trim(), Toast.LENGTH_LONG).show();
            editTextTalk.setText("");
        }

    }
}

