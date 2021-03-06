package five.seshealthpatient.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.text.SimpleDateFormat;
import java.util.Date;

import five.seshealthpatient.R;

public class RegisterInformation extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{

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
    private String age;
    private boolean gender;
    private String group = "patient";
    private String DOB;
    private String height;
    private String weight;
    private String medicalCondition;
    private String occupation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_information);

        findID();
        firebaseInfor();
        getEmailFromRegisterActivity();

        //genderInfor.setOnCheckedChangeListener(this);
        //groupInfor.setOnCheckedChangeListener(this);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!(nameInfor.getText().toString().equals(""))){
                    name = nameInfor.getText().toString();
                    Log.d(TAG, "getInforInput: not null"+ nameInfor.getText().toString());
                }else{
                    Toast.makeText(RegisterInformation.this, "Empty name!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "getInforInput: null");
                    return;
                }

                if(!(ageInfor.getText().toString().equals(""))){
                    age = ageInfor.getText().toString();
                }else{
                    Toast.makeText(RegisterInformation.this, "Empty age!", Toast.LENGTH_SHORT).show();
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

                if(!(birthdayInfor.getText().toString().equals(""))){
                    DOB = birthdayInfor.getText().toString();
                }else{
                    Toast.makeText(RegisterInformation.this, "Empty Date of Birth!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!(heightInfor.getText().toString().equals(""))){
                    height = heightInfor.getText().toString();
                }else{
                    Toast.makeText(RegisterInformation.this, "Empty height!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!(weightInfor.getText().toString().equals(""))){
                    weight = weightInfor.getText().toString();
                }else{
                    Toast.makeText(RegisterInformation.this, "Empty weight!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!(medicalConditionInfor.getText().toString().equals(""))){
                    medicalCondition = medicalConditionInfor.getText().toString();
                }else{
                    Toast.makeText(RegisterInformation.this, "Empty Medical Condition!", Toast.LENGTH_SHORT).show();
                    return;
                }

                occupation = " ";
                uploadInfor();
                Toast.makeText(RegisterInformation.this, "Register Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setClass(RegisterInformation.this,MainActivity.class);
                RegisterInformation.this.startActivity(intent);
            }
        });



    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()){
            case R.id.genderInfor:
                if(compoundButton.isChecked()) gender = true;
                else gender = false;
                break;
        }
    }

    private void findID(){
        nameInfor = (EditText) findViewById(R.id.nameInfor);
        emailInfor = (TextView) findViewById(R.id.emailInfor);
        ageInfor = (EditText) findViewById(R.id.ageInfor);
        genderInfor = (Switch) findViewById(R.id.genderInfor);
        birthdayInfor = (EditText) findViewById(R.id.birthdayInfor);
        heightInfor = (EditText) findViewById(R.id.heightInfor);
        weightInfor = (EditText) findViewById(R.id.weightInfor);
        medicalConditionInfor = (EditText) findViewById(R.id.medicalConditionInfor);
        submitBtn = (Button) findViewById(R.id.submitBtn);
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
}
