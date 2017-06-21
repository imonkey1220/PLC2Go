package tw.imonkey.plc2go;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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
import com.google.firebase.storage.StorageReference;

import java.util.TimeZone;

public class Main2Activity extends AppCompatActivity {
    FirebaseRecyclerAdapter mAdapter;

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
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

    private void getDevices(){
        RecyclerView RV4 = (RecyclerView) findViewById(R.id.RV4);
        RV4.setLayoutManager(new LinearLayoutManager(this));
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/FUI/"+memberEmail.replace(".", "_"));

        mAdapter = new FirebaseRecyclerAdapter<Message, MessageHolder>(
                Message.class,
                R.layout.listview_device_layout,
                MessageHolder.class,
                ref) {

            @Override
            public void populateViewHolder(MessageHolder holder, Message message, final int position) {
                holder.setName(message.getmemberEmail());
                holder.setMessage(message.getMessage());
            }

        };
        RV4.setAdapter(mAdapter);
        RV4.addOnItemTouchListener(new RecyclerItemClickListener(this,RV4,new RecyclerItemClickListener.OnItemClickListener() {
            @Override public void onItemClick(View view, int position) {
                // do whatever
            }

            @Override public void onLongItemClick(View view, int position) {
                // do whatever
            }
        }));

    }
}
