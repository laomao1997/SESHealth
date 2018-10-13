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
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
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

    /**
     * ListView sub text
     */
    private SimpleAdapter simpleAdapter;


    /**
     * UI references
     */
    @BindView(R.id.tvDataPack)
    TextView mTextViewDataPack;
    @BindView(R.id.listView)
    SwipeMenuListView mListView;
    @BindView(R.id.editTextComment)
    EditText mEditTextComment;

    public ViewPatientDataPacketActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_patient_data_packet);

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

        /**
         * Swipe Menu ListView Creator
         */
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "comment" item
                SwipeMenuItem selectItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                selectItem.setBackground(new ColorDrawable(Color.rgb(0x00,
                        0x99, 0x33)));
                // set item width
                selectItem.setWidth(170);
                // set a icon
                selectItem.setIcon(R.drawable.ic_action_select);
                // add to menu
                menu.addMenuItem(selectItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(170);
                // set a icon
                deleteItem.setIcon(R.drawable.ic_action_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        mListView.setMenuCreator(creator);

        Intent intent = getIntent();
        patientID = intent.getStringExtra("patientID");
    }

    /**
     * Get data from firebase and store in local string
     * @param dataSnapshot
     */
    private void showData(DataSnapshot dataSnapshot) {
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
            // map.put("gps", dPack.getGps());
            try{
                map.put("gps", getAddressFromLocation(dPack.getGps()));
            }catch (IOException e){
                Log.d(TAG, "showData: " + e.getMessage());
            }
            map.put("file", dPack.getFile());
            map.put("comment", dPack.getComment());
            datas.add(map);
            // array.add(record);
        }

        simpleAdapter=new SimpleAdapter(this,datas,R.layout.listview_data_packet,new String[]{"date","text","heart","gps","file","comment"},new int[]{R.id.dateTvInLv,R.id.textTvInLv,R.id.heartTvInLv,R.id.gpsTvInLv,R.id.fileTvInLv,R.id.commentTvInLv});
        mListView.setAdapter(simpleAdapter);

        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                String date;
                switch (index) {
                    case 0:
                        date = datas.get(position).get("date").toString();
                        myRef.child("user").child(patientID).child("packet").child(date).child("comment").setValue(getComment());
                        simpleAdapter.notifyDataSetChanged();
                        simpleAdapter.notifyDataSetChanged();
                        break;
                    case 1:
                        // Remove data
                        // Remove pack from online database
                        date = datas.get(position).get("date").toString();
                        myRef.child("user").child(patientID).child("packet").child(date).child("file").setValue(null);
                        myRef.child("user").child(patientID).child("packet").child(date).child("gps").setValue(null);
                        myRef.child("user").child(patientID).child("packet").child(date).child("heartrate").setValue(null);
                        myRef.child("user").child(patientID).child("packet").child(date).child("text").setValue(null);
                        myRef.child("user").child(patientID).child("packet").child(date).child("comment").setValue(null);

                        // Remove item from ListView
                        datas.remove(position);
                        simpleAdapter.notifyDataSetChanged();
                        simpleAdapter.notifyDataSetChanged();
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }

    private String getAddressFromLocation(String location) throws IOException {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        boolean falg = geocoder.isPresent();
        String addressName = "";
        double latitude;
        double longitude;
        location = location.substring(1,location.length()-1);
        latitude = Double.parseDouble(location.substring(0, location.indexOf(",")));
        longitude = Double.parseDouble(location.substring(location.indexOf(",")+1, location.length()-1));
        try{
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            StringBuilder stringBuilder = new StringBuilder();
            if(addresses.size() > 0){
                Address address = addresses.get(0);
                for(int i = 0; i < address.getMaxAddressLineIndex()+1; i++) {
                    stringBuilder.append(address.getAddressLine(i)).append(", ");
                }
                //stringBuilder.append(address.getLocality()).append("_");
                //stringBuilder.append(address.getPostalCode()).append("_");
                //stringBuilder.append(address.getCountryCode()).append("_");
                //stringBuilder.append(address.getCountryName()).append("_");
                stringBuilder
                        .append(address.getLocality()).append(", ")
                        .append(address.getAdminArea()).append(", ")
                        .append(address.getCountryName());
                addressName = stringBuilder.toString();
            }
        } catch (IOException e) {
            // Log.d(TAG, "getAddressFromLocation: 经度:" + latitude);
        }
        Log.d(TAG, "getAddressFromLocation: 经度:" + latitude);
        Log.d(TAG, "getAddressFromLocation: 纬度:" + longitude);
        Log.d(TAG, "getAddressFromLocation: " + addressName);
        return addressName;
    }

    private String getComment() {
        return mEditTextComment.getText().toString();
    }

    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

}