package five.seshealthpatient.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import five.seshealthpatient.Model.UserInformation;
import five.seshealthpatient.R;

import android.os.Bundle;
import android.app.Fragment;
import android.os.health.UidHealthStats;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import five.seshealthpatient.Model.UserInformation;
import five.seshealthpatient.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditInformationFragment extends Fragment {
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
    private boolean prevGender;
    /**
     * UI references
     */
    @BindView(R.id.nameET) EditText mName;
    @BindView(R.id.emailET) EditText mEmail;
    @BindView(R.id.ageET) EditText mAge;
    @BindView(R.id.genderET) EditText mGender;
    //@BindView(R.id.genderET) TextView mGroup;
    @BindView(R.id.birthET) EditText mBirthday;
    @BindView(R.id.heightET) EditText mHeight;
    @BindView(R.id.weightET) EditText mWeight;
    @BindView(R.id.conditionET) EditText mCondition;


    public EditInformationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Note the use of getActivity() to reference the Activity holding this fragment
        getActivity().setTitle("Edit Information");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_information, container, false);

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
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    uInfo = new UserInformation();
                    uInfo.setName(ds.child(userID).getValue(UserInformation.class).getName()); //set the name
                    mName.setText(uInfo.getName());
                    uInfo.setEmail(ds.child(userID).getValue(UserInformation.class).getEmail()); //set the email
                    mEmail.setText(uInfo.getEmail());
                    uInfo.setAge(ds.child(userID).getValue(UserInformation.class).getAge()); //set the age
                    mAge.setText(uInfo.getAge());
                    uInfo.setGender(ds.child(userID).getValue(UserInformation.class).isGender()); //set the gender
                    if(uInfo.isGender()) {
                        mGender.setText("Male");
                    }else{
                        mGender.setText("Female");
                    }
                    //store previous gender value in variable
                    //prevGender = ds.child(userID).getValue(UserInformation.class).isGender();

                    uInfo.setBirthday(ds.child(userID).getValue(UserInformation.class).getBirthday()); //set the birthday
                    mBirthday.setText(uInfo.getBirthday());
                    uInfo.setGroup(ds.child(userID).getValue(UserInformation.class).getGroup()); //set the group
                    //mGroup.setText(uInfo.getGroup());
                    uInfo.setHeight(ds.child(userID).getValue(UserInformation.class).getHeight()); //set the height
                    mHeight.setText(uInfo.getHeight());
                    uInfo.setWeight(ds.child(userID).getValue(UserInformation.class).getWeight()); //set the weight
                    mWeight.setText(uInfo.getWeight());
                    uInfo.setCondition(ds.child(userID).getValue(UserInformation.class).getCondition()); //set the group
                    mCondition.setText(uInfo.getCondition());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value + add exception
                Log.w(TAG, "Failed to read value.");
            }
        });
    }
    // Update user information on confirmation click
    @OnClick(R.id.btnConfirm)
    public void update(){
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            myRef = mFirebaseDatabase.getReference("user");
            FirebaseUser user = mAuth.getCurrentUser();
            userID = user.getUid();
            updateInfo();
            myRef.child(userID).child("name").setValue(uInfo.getName());
            myRef.child(userID).child("email").setValue(uInfo.getEmail());
            myRef.child(userID).child("age").setValue(uInfo.getAge());
            myRef.child(userID).child("gender").setValue(mGender.getText().toString());
            myRef.child(userID).child("birthday").setValue(uInfo.getBirthday());
            myRef.child(userID).child("height").setValue(uInfo.getHeight());
            myRef.child(userID).child("weight").setValue(uInfo.getWeight());
            myRef.child(userID).child("condition").setValue(uInfo.getCondition());



            getActivity().getFragmentManager().popBackStack();
    }

    @OnClick(R.id.btnExit)
    public void exit(){
        getActivity().getFragmentManager().popBackStack();
    }
    //Check text fields and update to user information
    private void updateInfo(){
        if(!mName.getText().toString().isEmpty()) {
            uInfo.setName(mName.getText().toString());
        }
        if(!mEmail.getText().toString().isEmpty()) {
            uInfo.setEmail(mEmail.getText().toString());
        }
        if(!mAge.getText().toString().isEmpty()) {
            uInfo.setAge(mAge.getText().toString());
        }
        if(!mBirthday.getText().toString().isEmpty()) {
            uInfo.setBirthday(mBirthday.getText().toString());
        }
        if(!mGender.getText().toString().isEmpty()) {
            uInfo.setGender(getGender());
        }
        if(!mHeight.getText().toString().isEmpty()) {
            uInfo.setHeight(mHeight.getText().toString());
        }
        if(!mWeight.getText().toString().isEmpty()) {
            uInfo.setWeight(mWeight.getText().toString());
        }
        if(!mCondition.getText().toString().isEmpty()) {
            uInfo.setCondition(mCondition.getText().toString());
        }
        toastMessage("Information Updated Successfully");
    }

    private void toastMessage(String message){
        Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
    }
    //Check gender is male or female and return value
    private boolean getGender(){
        if(mGender.getText().toString().toLowerCase().equals("male")){
            return true;
        }
        else if(mGender.getText().toString().toLowerCase().equals("female")) {
            return false;
        }
        else
            return prevGender;
    }

}