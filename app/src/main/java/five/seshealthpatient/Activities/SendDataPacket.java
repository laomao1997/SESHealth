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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import five.seshealthpatient.R;

public class SendDataPacket extends AppCompatActivity {
    private static final String TAG = "SendDataPacket";

    @BindView(R.id.heartRate) TextView heartRate;
    @BindView(R.id.heartRateBtn) Button heartRateBtn;
    @BindView(R.id.currentLocation) TextView currentLocation;
    @BindView(R.id.currentLocationBtn) Button currentLocationBtn;
    @BindView(R.id.relevantText) EditText relevantText;
    @BindView(R.id.submitBtn) Button submitBtn;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private  String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data_packet);
        ButterKnife.bind(this);

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
    }

    @OnClick(R.id.submitBtn)
    public void SubmitDataPacket() {
        Log.d(TAG, "onClick: Attempting to add object to database.");
        FirebaseUser user = mAuth.getCurrentUser();
        String userID = user.getUid();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = df.format(new Date());
        myRef.child("user").child(userID).child("packet").child(time).child("time").setValue(time);
        String heart = "heartTest";
        myRef.child("user").child(userID).child("packet").child(time).child("heart").setValue(heart);
        String longitude = "longitudeTest";
        myRef.child("user").child(userID).child("packet").child(time).child("gps").setValue("[123.1234, -23.9807]");
        String latitude = "latitudeTest";
        String userRelevantText = relevantText.getText().toString();
        if(!userRelevantText.equals("")){
            myRef.child("user").child(userID).child("packet").child(time).child("text").setValue(userRelevantText);
            toastMessage("Adding relevant information to database...");
            relevantText.setText("");
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
