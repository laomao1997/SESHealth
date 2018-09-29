package five.seshealthpatient.Activities;

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

    private static final String TAG = "RegisterInformation";

    private String username;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private StorageReference mStorageRef;
    private  String userID;

    private TextView emailInfor;
    private EditText nameInfor, ageInfor, birthdayInfor, heightInfor, weightInfor, medicalConditionInfor;
    private Button submitBtn;
    private Switch genderInfor, groupInfor;

    private String name;
    private String email;
    private boolean gender;
    private String group = "doctor";
    private String occupation;

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
        emailInfor.setText("Email: "+username);
        email = username;
    }

    private void getInforInput(){

    }

    private void uploadInfor(){
        myRef.child("user").child(userID).child("name").setValue(name);
        myRef.child("user").child(userID).child("email").setValue(email);
        myRef.child("user").child(userID).child("gender").setValue(gender);
        myRef.child("user").child(userID).child("group").setValue(group);
        //update occupation
        //myRef.child("user").child(userID).child("occupation").setValue(occupation);

    }

    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.submitBtn)
        public void registerDoctor(){
        if(!(nameInfor.getText().toString().equals(""))){
            name = nameInfor.getText().toString();
            Log.d(TAG, "getInforInput: not null"+ nameInfor.getText().toString());
        }else{
            Toast.makeText(RegisterInformationDoctor.this, "Empty name!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "getInforInput: null");
            return;
        }


                /*if(genderInfor.getText().toString()!=null){
                    age = ageInfor.getText().toString();
                }else{
                    Toast.makeText(RegisterInformation.this, "Empty age!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(groupInfor.getText().toString()!=null){
                    age = ageInfor.getText().toString();
                }else{
                    Toast.makeText(RegisterInformation.this, "Empty age!", Toast.LENGTH_SHORT).show();
                    return;
                }*/

        uploadInfor();
        Toast.makeText(RegisterInformationDoctor.this, "Register Successful!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setClass(RegisterInformationDoctor.this,MainActivity.class);
        RegisterInformationDoctor.this.startActivity(intent);
    }
}
