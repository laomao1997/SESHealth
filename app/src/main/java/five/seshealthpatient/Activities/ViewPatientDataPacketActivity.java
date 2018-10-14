package five.seshealthpatient.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import five.seshealthpatient.Model.DataPacket;
import five.seshealthpatient.R;

public class ViewPatientDataPacketActivity extends AppCompatActivity{

    private static final String TAG = "ViewPatientDataPacketAc";

    /**
     * add Firebase Database stuff
     */
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;
    private String patientID = ""; // ID of patient
    private String date = "";

    /**
     * ListView sub text
     */
    private SimpleAdapter simpleAdapter;


    /**
     * UI references
     */

    @BindView(R.id.input_text)
    EditText mInputText;
    @BindView(R.id.send)
    Button mBtnSend;
    @BindView(R.id.list_view)
    ListView listView;


    public ViewPatientDataPacketActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_patient_data_packet);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        patientID = intent.getStringExtra("TRANS_PATIENT_ID");

        mInputText.setEnabled(false);
        mBtnSend.setEnabled(false);


        //declare the database reference object. This is what we use to access the database.
        //NOTE: Unless you are signed in, this will not be useable.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * Get data from firebase and store in local string
     * @param dataSnapshot
     */
    private void showData(final DataSnapshot dataSnapshot) {
        // ArrayList<String> array  = new ArrayList<>();
        final List<Map<String, Object>> datas=new ArrayList<Map<String, Object>>();
        for(DataSnapshot ds : dataSnapshot.child("user").child(patientID).child("packet").getChildren()){
            DataPacket dPack = new DataPacket();
            dPack.setFile(ds.getValue(DataPacket.class).getFile());
            dPack.setGps(ds.getValue(DataPacket.class).getGps());
            dPack.setHeartrate(ds.getValue(DataPacket.class).getHeartrate());
            dPack.setText(ds.getValue(DataPacket.class).getText());
            if(ds.hasChild("comment")) {
                dPack.setComment(ds.getValue(DataPacket.class).getComment());
            }
            String record = "Date: " + ds.getKey() + "\n"
                    + "Text: " + dPack.getText() + "\n"
                    + "Heart rate: " + dPack.getHeartrate() + "\n"
                    + "GPS: " + dPack.getGps() + "\n"
                    + "File: " + dPack.getFile() + "\n"
                    + "comment: " + dPack.getComment();
            Log.d(TAG, "Value is: " + record);
            Map map = new HashMap();
            map.put("date", ds.getKey());
            map.put("text", dPack.getText());
            map.put("heart", dPack.getHeartrate());
            map.put("gps", dPack.getGps());
            map.put("file", dPack.getFile());
            map.put("comment", dPack.getComment());
            datas.add(map);
            // array.add(record);
        }

        String[] fields = {"date","text","heart","gps","file","comment"};
        int[] to = {R.id.dateTvInLv,R.id.textTvInLv,R.id.heartTvInLv,R.id.gpsTvInLv,R.id.fileTvInLv,R.id.commentTvInLv};
        listView.setAdapter(new SimpleAdapter(this, datas, R.layout.listview_data_packet, fields, to));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            // 第position项被单击时激发该方法。
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                date = datas.get(position).get("date").toString();
                toastMessage("Comment the packet on " + date);
                mInputText.setEnabled(true);
                mBtnSend.setEnabled(true);
                mInputText.requestFocus();
                mInputText.requestFocusFromTouch();


            }
        });
    }

    @OnClick(R.id.send)
    public void sendComment() {
        String comment = mInputText.getText().toString();
        if(!comment.equals("")) {
            myRef.child("user").child(patientID).child("packet").child(date).child("comment").setValue(comment);
            mInputText.setEnabled(false);
            mBtnSend.setEnabled(false);
            toastMessage("Comment successful.");
        }
        else{
            toastMessage("Comment cannot be empty.");
        }
    }


    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

}
