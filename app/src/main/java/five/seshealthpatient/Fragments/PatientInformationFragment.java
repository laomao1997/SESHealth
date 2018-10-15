package five.seshealthpatient.Fragments;


import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import five.seshealthpatient.Activities.DoctorActivity;
import five.seshealthpatient.Activities.LoginActivity;
import five.seshealthpatient.Activities.MainActivity;
import five.seshealthpatient.Activities.RegisterInformation;
import five.seshealthpatient.Activities.RegisterInformationDoctor;
import five.seshealthpatient.Activities.UsersActivity;
import five.seshealthpatient.Model.UserInformation;
import five.seshealthpatient.R;

import static android.content.ContentValues.TAG;

/**
 * Class: PatientInformationFragment
 * Extends: {@link Fragment}
 * Author: Carlos Tirado < Carlos.TiradoCorts@uts.edu.au> and YOU!
 * Description:
 * <p>
 * This fragment's job will be that to display patients information, and be able to edit that
 * information (either edit it in this fragment or a new fragment, up to you!)
 * <p>

 */
public class PatientInformationFragment extends Fragment {

    private static final String TAG = "MainActivity";

    /**
     * add Firebase Database stuff
     */
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;
    private UserInformation uInfo;
    /**
     * UI references
     */
    @BindView(R.id.tvName) TextView mName;
    @BindView(R.id.tvEmail) TextView mEmail;
    @BindView(R.id.tvAge) TextView mAge;
    @BindView(R.id.tvGender) TextView mGender;
    @BindView(R.id.tvGroup) TextView mGroup;
    @BindView(R.id.tvBirth) TextView mBirthday;
    @BindView(R.id.tvHeight) TextView mHeight;
    @BindView(R.id.tvWeight) TextView mWeight;
    @BindView(R.id.tvCondition) TextView mCondition;
    @BindView(R.id.btnEdit) Button mButtonEdit;

    public PatientInformationFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Note the use of getActivity() to reference the Activity holding this fragment
        getActivity().setTitle("Username Information");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_patient_information, container, false);

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
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    toastMessage("Successfully signed in with: " + user.getEmail());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    toastMessage("Successfully signed out.");
                }
                // ...
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value + add exception
                Log.w(TAG, "Failed to read value.");
            }
        });
    }

    /**
     * Get data from firebase and store in local string
     * @param dataSnapshot
     */
    private void showData(DataSnapshot dataSnapshot) {
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            uInfo = new UserInformation();
            uInfo.setName(ds.child(userID).getValue(UserInformation.class).getName()); //set the name
            uInfo.setEmail(ds.child(userID).getValue(UserInformation.class).getEmail()); //set the email
            uInfo.setAge(ds.child(userID).getValue(UserInformation.class).getAge()); //set the age
            uInfo.setGender(ds.child(userID).getValue(UserInformation.class).isGender()); //set the gender
            uInfo.setBirthday(ds.child(userID).getValue(UserInformation.class).getBirthday()); //set the birthday
            uInfo.setGroup(ds.child(userID).getValue(UserInformation.class).getGroup()); //set the group
            uInfo.setHeight(ds.child(userID).getValue(UserInformation.class).getHeight()); //set the height
            uInfo.setWeight(ds.child(userID).getValue(UserInformation.class).getWeight()); //set the weight
            uInfo.setCondition(ds.child(userID).getValue(UserInformation.class).getCondition()); //set the group

            mName.setText("Welcome, " + uInfo.getName());
            mEmail.setText("Email: " + uInfo.getEmail());
            mAge.setText("Age: " + uInfo.getAge());
            mGender.setText("Gender: " + (uInfo.isGender()?"male":"female"));
            mBirthday.setText("D.O.B: " + uInfo.getBirthday());
            mGroup.setText("Group: " + uInfo.getGroup());
            mHeight.setText("Height: " + uInfo.getHeight());
            mWeight.setText("Weight: " + uInfo.getWeight());
            mCondition.setText("Medical condition: " + uInfo.getCondition());

        }
    }

    @OnClick(R.id.btnEdit)
    public void editInfo(){
        EditInformationFragment editFrag= new EditInformationFragment();
        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editFrag,"findThisFragment")
                .addToBackStack(null)
                .commit();
    }
    @OnClick(R.id.btnPair)
    public void doPair() {
        Intent intentToPair = new Intent(getContext(), UsersActivity.class);
        startActivity(intentToPair);
    }




    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
    }
}


