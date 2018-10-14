package five.seshealthpatient.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import five.seshealthpatient.Dialog.DialogSelectFile;
import five.seshealthpatient.Fragments.PatientInformationFragment;
import five.seshealthpatient.Model.DataPacket;
import five.seshealthpatient.Model.FilePacket;
import five.seshealthpatient.Model.UserInformation;
import five.seshealthpatient.R;

public class SendDataPacket extends AppCompatActivity {
    private static final String TAG = "SendDataPacket";

    String file = "";

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


    /**
     * GPS location set
     */
    String GPS;
    private LocationManager lm;// Location management
    private FusedLocationProviderClient mFusedLocationClient;

    FilePacket filePacket = new FilePacket();

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
        file += intent.getStringExtra("file");
        Log.d(TAG, "onCreate: file: " + file);

        textViewChooseFile.setText(file);


    }

    private void showData(DataSnapshot dataSnapshot) {
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            tvName.setText("User name: " + ds.child(userID).getValue(UserInformation.class).getName());

        }

        if(dataSnapshot.child("user").child(userID).child("heartrate").hasChildren()) {
            int countOfHeartRate = (int) dataSnapshot.child("user").child(userID).child("heartrate").getChildrenCount();
            int i = 0;
            for(DataSnapshot dataSnapshot1 : dataSnapshot.child("user").child(userID).child("heartrate").getChildren()) {
                i++;
                if(i == countOfHeartRate) {
                    heartRate.setText(dataSnapshot1.getValue(String.class));
                }
            }
        }
    }

    @OnClick(R.id.submitBtn)
    public void SubmitDataPacket() {
        if(!textViewChooseFile.equals("null")) {
            try{
                filePacket.setDate(file.substring(file.lastIndexOf("\n")+1));
                filePacket.setFileName(file.substring(0, file.lastIndexOf("\n")));
            }catch (Exception e)
            {
                toastMessage(file);
            }
        }
        toastMessage("Adding relevant information to database...");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = df.format(new Date());
        String userRelevantText;
        if(relevantText.getText().toString().isEmpty())
            userRelevantText = "Adding relevant information to database...";
        else
            userRelevantText = relevantText.getText().toString();
        try{
            DataPacket dPack =
                    new DataPacket(
                            userRelevantText,
                            currentLocation.getText().toString(),
                            filePacket.getFileName(),
                            heartRate.getText().toString());
            myRef.child("user").child(userID).child("packet").child(time).child("file").setValue(dPack.getFile());
            myRef.child("user").child(userID).child("packet").child(time).child("heartrate").setValue(dPack.getHeartrate());
            myRef.child("user").child(userID).child("packet").child(time).child("gps").setValue(dPack.getGps());
            myRef.child("user").child(userID).child("packet").child(time).child("text").setValue(dPack.getText());
            toastMessage("Complete");
            relevantText.setText("");
        }catch (Exception e) {

        }

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
                                    try{
                                        currentLocation.setText(getAddressFromLocation(GPS));
                                    }catch (Exception e) {

                                    }
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
                    stringBuilder.append(address.getAddressLine(i)).append(" ");
                }
                //stringBuilder.append(address.getLocality()).append("_");
                //stringBuilder.append(address.getPostalCode()).append("_");
                //stringBuilder.append(address.getCountryCode()).append("_");
                //stringBuilder.append(address.getCountryName()).append("_");

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
