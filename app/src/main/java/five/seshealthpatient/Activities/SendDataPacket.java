package five.seshealthpatient.Activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

import five.seshealthpatient.R;

public class SendDataPacket extends AppCompatActivity {
    private static final String TAG = "SendDataPacket";

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private  String userID;

    private TextView fullName, gender, age, height, weight, medicalCondition, heartRate, currentLocation;
    private EditText relevantText;
    private Button heartRateBtn, currentLocationBtn, submitBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data_packet);
        findID();

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
                    //toastMessage("Successfully signed in with: " + user.getEmail());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    //toastMessage("Successfully signed out.");
                }
                // ...
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

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Attempting to add object to database.");
                FirebaseUser user = mAuth.getCurrentUser();
                String userID = user.getUid();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = df.format(new Date());
                myRef.child("user").child(userID).child("packet").child(time).child("time").setValue(time);
                String heart = "heartTest";
                myRef.child("user").child(userID).child("packet").child(time).child("heart").setValue(heart);
                String longitude = "longitudeTest";
                myRef.child("user").child(userID).child("packet").child(time).child("gps").child("0").setValue(longitude);
                String latitude = "latitudeTest";
                myRef.child("user").child(userID).child("packet").child(time).child("gps").child("1").setValue(latitude);
                String userRelevantText = relevantText.getText().toString();
                if(!userRelevantText.equals("")){
                    myRef.child("user").child(userID).child("packet").child(time).child("text").setValue(userRelevantText);
                    toastMessage("Adding relevant information to database...");
                    relevantText.setText("");
                }
            }
        });
    }

    private void findID() {
        fullName = (TextView) findViewById(R.id.fullName);
        gender = (TextView) findViewById(R.id.gender);
        age = (TextView) findViewById(R.id.age);
        height = (TextView) findViewById(R.id.height);
        weight = (TextView) findViewById(R.id.weight);
        medicalCondition = (TextView) findViewById(R.id.medicalCondition);
        heartRate = (TextView) findViewById(R.id.heartRate);
        heartRateBtn = (Button) findViewById(R.id.heartRateBtn);
        currentLocation = (TextView) findViewById(R.id.currentLocation);
        currentLocationBtn = (Button) findViewById(R.id.currentLocationBtn);
        relevantText = (EditText)findViewById(R.id.relevantText);
        submitBtn = (Button) findViewById(R.id.submitBtn);
    }
    //private TextView fullName, gender, age, height, weight, medicalCondition, heartRate, currentLocation;
    private void showData(DataSnapshot dataSnapshot) {
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            //String name = ds.child(userID).child("name").getValue(String.class);
            //uInfo.setName(name);
            String nameRead = ds.child(userID).child("name").getValue(String.class); //Read the name
            boolean genderRead = ds.child(userID).child("gender").getValue(boolean.class); //Read the email
            String ageRead = ds.child(userID).child("age").getValue(String.class);
            String heightRead = ds.child(userID).child("height").getValue(String.class);
            String weightRead = ds.child(userID).child("weight").getValue(String.class);
            String medicalConditionRead = ds.child(userID).child("condition").getValue(String.class);
            //uInfo.setGender(ds.child(userID).child("gender").equalTo(true).getValue(String.class));
            //String genderReceive = ds.child(userID).child("gender").child("female").getValue(String.class);
           /* if(genderReceive.equals("false"))
            {
                genderReceive = "male";
            }
            if(genderReceive.equals("true"))
            {
                genderReceive = "female";
            }*/

            //display all the information
            Log.d(TAG, "showData: name: " + nameRead);

            /*ArrayList<String> array  = new ArrayList<>();
            array.add(uInfo.getName());
            array.add(uInfo.getEmail());
            array.add(uInfo.getPhone_num());
            ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,array);
            mListView.setAdapter(adapter);*/

            fullName.setText(nameRead);
            gender.setText(genderRead?"male":"female");
            age.setText(ageRead);
            height.setText(heightRead);
            weight.setText(weightRead);
            medicalCondition.setText(medicalConditionRead);


        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}
