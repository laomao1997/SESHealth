package five.seshealthpatient.Fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import five.seshealthpatient.Activities.DoctorActivity;
import five.seshealthpatient.Activities.ViewPatientDataPacketActivity;
import five.seshealthpatient.Model.DataPacket;
import five.seshealthpatient.Model.UserInformation;
import five.seshealthpatient.R;

public class DoctorDataPacketFragment extends Fragment {

    private static final String TAG = "DoctorDataPacketFragmen";

    /**
     * add Firebase Database stuff
     */
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;
    private String patientID;

    /**
     * ListView sub text
     */
    private SimpleAdapter simpleAdapter;


    /**
     * UI references
     */
    @BindView(R.id.list_view)
    ListView listView;

    public DoctorDataPacketFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: Instead of hardcoding the title perhaps take the user name from somewhere?
        // Note the use of getActivity() to reference the Activity holding this fragment
        getActivity().setTitle("Data packet");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_doctor_data_packet, container, false);

        // Note how we are telling butter knife to bind during the on create view method
        ButterKnife.bind(this, v);

        return v;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
    private void showData(DataSnapshot dataSnapshot) {
        final List<Map<String, Object>> datas=new ArrayList<Map<String, Object>>();
        for(DataSnapshot ds : dataSnapshot.child("user").child(userID).child("pair").getChildren())
        {
            UserInformation uInfo = new UserInformation();
            //uInfo.setName(ds.child(userID).child("pair").getValue(UserInformation.class).getName()); //set the name
            String UID = "";
            String userName = null;
            long numDataPack = 0;
            if(ds.getValue().equals("passed")){
                UID = ds.getKey();
                uInfo.setUID(UID);
                Log.d(TAG, "getPairedUsers: test111UID :"+UID);
                for(DataSnapshot ds1 : dataSnapshot.child("user").getChildren()){
                    if(ds1.getKey().equals(UID)){
                        userName = ds1.child("name").getValue(String.class);
                        numDataPack = ds1.child("packet").getChildrenCount();

                        Map map = new HashMap();
                        map.put("patientID", UID);
                        map.put("patientName", userName);
                        map.put("numberOfDataPack", numDataPack);
                        datas.add(map);
                    }
                }

            }
        }
        simpleAdapter = new SimpleAdapter(getContext(), datas, R.layout.listview_doctor_patients,
                new String[]{"patientID", "patientName", "numberOfDataPack"},
                new int[]{R.id.tvPatientID, R.id.tvPatientName, R.id.tvPatientPackNumber});
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            // 第position项被单击时激发该方法。
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                //toastMessage(datas.get(position).get("patientName").toString());
                String choosePatientID = datas.get(position).get("patientID").toString();
                Intent intent = new Intent(getActivity(), ViewPatientDataPacketActivity.class);
                intent.putExtra("TRANS_PATIENT_ID", choosePatientID);
                startActivity(intent);
            }
        });
    }

    private void toastMessage(String message){
        Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
    }

}