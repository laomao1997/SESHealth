package five.seshealthpatient.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import five.seshealthpatient.R;

public class RegisterInformationDoctor extends AppCompatActivity {

    private static final String TAG = "RegisterInformationDoc";

    private String username;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private StorageReference mStorageRef;
    private  String userID;


    private String name;
    private String email;
    private boolean gender;
    private String group = "doctor";
    private String occupation;

    private String age;
    private String DOB;
    private String height;
    private String weight;
    private String medicalCondition;


    @BindView(R.id.nameET)
    EditText nameEditText;
    @BindView(R.id.emailET)
    EditText emailEditText;
    @BindView(R.id.genderET)
    EditText genderEditText;
    @BindView(R.id.occupationET)
    EditText occupationEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_information_doctor);
        ButterKnife.bind(this);
        firebaseInfor();
        getEmailFromRegisterActivity();

    }

    private void firebaseInfor(){
        mStorageRef = FirebaseStorage.getInstance().getReference();
        //declare the database reference object. NOTE: Unless you are signed in, this will not be useable.
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
                    //toastMessage("Successfully signed in with: " + user.getEmail());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    //toastMessage("Successfully signed out.");
                }
            }
        };
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getEmailFromRegisterActivity(){
        Intent intent = getIntent(); //Get filepath from FolderActivity.
        username = intent.getStringExtra("username");
        email = username;
    }

    private void getInforInput(){

    }

    private void uploadInfor(){
        myRef.child("user").child(userID).child("name").setValue(name);
        myRef.child("user").child(userID).child("email").setValue(email);
        myRef.child("user").child(userID).child("age").setValue(age);
        myRef.child("user").child(userID).child("gender").setValue(gender);
        myRef.child("user").child(userID).child("group").setValue(group);
        myRef.child("user").child(userID).child("birthday").setValue(DOB);
        myRef.child("user").child(userID).child("height").setValue(height);
        myRef.child("user").child(userID).child("weight").setValue(weight);
        myRef.child("user").child(userID).child("height").setValue(height);
        myRef.child("user").child(userID).child("condition").setValue(medicalCondition);
        //myRef.child("user").child(userID).child("occupation").setValue(occupation);

    }

    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.submitBtn)
        public void registerDoctor(){
        if(!(nameEditText.getText().toString().isEmpty())){
            name = nameEditText.getText().toString();
            Log.d(TAG, "getInforInput: not null"+ nameEditText.getText().toString());
        }else{
            Toast.makeText(RegisterInformationDoctor.this, "Empty name!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "getInforInput: null");
            return;
        }


        if(genderEditText.getText().toString().toLowerCase().equals("male")){
            gender = true;
        }
        else if(genderEditText.getText().toString().toLowerCase().equals("female")){
            gender = false;
        }
        else{
            Toast.makeText(RegisterInformationDoctor.this, "Empty gender!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(occupationEditText.getText().toString()!=null){
            occupation = occupationEditText.getText().toString();
        }
        else{
            Toast.makeText(RegisterInformationDoctor.this, "Empty occupation!", Toast.LENGTH_SHORT).show();
            return;
        }
        age = " ";
        DOB = " ";
        height = " ";
        weight = " ";
        medicalCondition = " ";
        uploadInfor();
        Toast.makeText(RegisterInformationDoctor.this, "Register Successful!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setClass(RegisterInformationDoctor.this,DoctorActivity.class);
        RegisterInformationDoctor.this.startActivity(intent);
    }
}
