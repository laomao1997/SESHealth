package five.seshealthpatient.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import five.seshealthpatient.Dialog.DialogSelectFile;
import five.seshealthpatient.Fragments.PatientInformationFragment;
import five.seshealthpatient.Model.DataPacket;
import five.seshealthpatient.Model.UserInformation;
import five.seshealthpatient.R;

public class SendDataPacket extends AppCompatActivity {
    private static final String TAG = "SendDataPacket";

    @BindView(R.id.fullName) TextView tvName;
    @BindView(R.id.heartRate) TextView heartRate;
    @BindView(R.id.currentLocation) TextView currentLocation;
    @BindView(R.id.relevantText) EditText relevantText;
    @BindView(R.id.submitBtn) Button submitBtn;
    @BindView(R.id.textViewChooseFile) TextView textViewChooseFile;

    /**
     * FireBase set
     */
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;
    private String userLocation;

    /**
     * GPS location set
     */
    String GPS;
    private LocationManager lm;// Location management
    private FusedLocationProviderClient mFusedLocationClient;

    String filePath;

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

        initGPS();

        Intent intent = getIntent();
        filePath = intent.getStringExtra("name");
        textViewChooseFile.setText(filePath);
    }

    private void showData(DataSnapshot dataSnapshot) {
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            tvName.setText("User name: " + ds.child(userID).getValue(UserInformation.class).getName());
        }
    }

    @OnClick(R.id.submitBtn)
    public void SubmitDataPacket() {
        toastMessage("Adding relevant information to database...");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = df.format(new Date());
        String userRelevantText;
        if(relevantText.getText().toString().isEmpty())
            userRelevantText = "Adding relevant information to database...";
        else
            userRelevantText = relevantText.getText().toString();
        DataPacket dPack = new DataPacket(userRelevantText, GPS, filePath, "65");
        myRef.child("user").child(userID).child("packet").child(time).child("file").setValue(dPack.getFile());
        myRef.child("user").child(userID).child("packet").child(time).child("heartrate").setValue(dPack.getHeartrate());
        myRef.child("user").child(userID).child("packet").child(time).child("gps").setValue(dPack.getGps());
        myRef.child("user").child(userID).child("packet").child(time).child("text").setValue(dPack.getText());
        toastMessage("Complete");
        relevantText.setText("");
    }

    @OnClick(R.id.textViewChooseFile)
    public void ChooseFile() {
        DialogSelectFile dialogSelectFile = new DialogSelectFile(this);
        dialogSelectFile.show();
    }

    private void initGPS() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        lm = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (ok) // GPS service is OK
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // No permission
                Toast.makeText(this, "No GPS Permission", Toast.LENGTH_SHORT).show();

            } else {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    // Logic to handle location object
                                    GPS = "["+location.getLatitude()+","+location.getLongitude()+"]";
                                    currentLocation.setText("Current Location: " + GPS);
                                }
                            }
                        });
            }
        } else {
            Toast.makeText(this, "Please open the GPS service", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 1315);
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
