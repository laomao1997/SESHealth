package five.seshealthpatient.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import five.seshealthpatient.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class HeartRateFragment extends Fragment {

    TextView ageEditText;

    LinearLayout rhrLinearLayout;
    TextView rhrTextView;

    LinearLayout thrLinearLayout;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;
    private String userHeartRate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        //TODO: Instead of hardcoding the title perhaps take the user name from somewhere?
        // Note the use of getActivity() to reference the Activity holding this fragment
        getActivity().setTitle("Heartrate Home");



    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.heartrate_measure, container, false);

        // Note how we are telling butter knife to bind during the on create view method
        ButterKnife.bind(this, v);

        return v;
    }


    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //setContentView(R.layout.heartrate_measure);

        final String TAG = "SendHeartRate";
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

        setUpPermissions();

        ageEditText = (TextView) getView().findViewById(R.id.et_age);
        rhrLinearLayout = (LinearLayout) getView().findViewById(R.id.ll_restingHeartRate);
        rhrTextView = (TextView) getView().findViewById(R.id.tv_restingHeartRate);
        thrLinearLayout = (LinearLayout) getView().findViewById(R.id.ll_targetHeartRate);

        if(savedInstanceState!=null){
            ageEditText.setText(savedInstanceState.getString("storedAge"));
            rhrTextView.setText(savedInstanceState.getString("storedRHR"));
        }

        rhrLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getRestingHeartRate();
            }
        });
        thrLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateTargetHeartRate();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String currentAge = ageEditText.getText().toString();
        outState.putString("storedAge", currentAge);
        String currentRHR = rhrTextView.getText().toString();
        outState.putString("storedRHR", currentRHR);
    }

    private void calculateTargetHeartRate() {
        String age = ageEditText.getText().toString();
        if(age.matches("")){
            Toast.makeText(getActivity(), "Please enter your age!", Toast.LENGTH_SHORT).show();
            return;
        }

        String restingHeartRate = rhrTextView.getText().toString();
        if(restingHeartRate.matches("")){
            Toast.makeText(getActivity(), "Please calculate your RHR first!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intentToCalculateTHR = new Intent(getActivity(), HeartRate_MainActivity.class);

        intentToCalculateTHR.putExtra("age", age);
        intentToCalculateTHR.putExtra("restingHeartRate", restingHeartRate);

        startActivity(intentToCalculateTHR);
    }

    public void getRestingHeartRate(){
        Intent intentToGetRHR = new Intent(getActivity(), HeartRate_MainActivity.class);
        startActivityForResult(intentToGetRHR, 1);
    }

    private void setUpPermissions() {
        if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                String[] permissionsWeNeed = new String[]{Manifest.permission.CAMERA};
                requestPermissions(permissionsWeNeed, 88);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 88: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // The permission was granted!
                    //set up whatever required the permissions

                } else {
                    Toast.makeText(getActivity(), "Permission for camera not granted. HeartBeat Monitor can't run.", Toast.LENGTH_LONG).show();
                    getActivity().finish();

                    // The permission was denied, so we can show a message why we can't run the app
                    // and then close the app.
                }
            }
            // Other permissions could go down here

        }
    }

    private void showData(DataSnapshot dataSnapshot) {
        String age = "NUL";
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            age = ds.child(userID).child("age").getValue(String.class); //set the age
        }
        ageEditText.setText(age);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1 && resultCode == RESULT_OK){
            String restingHeartRate = data.getStringExtra("restingHeartRate");
            rhrTextView.setText(restingHeartRate);

            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setCancelable(true);
            builder.setTitle("Warning");
            builder.setMessage("Would you like to save this heart rate of: " + rhrTextView.getText() + " BMP?");
            builder.setPositiveButton("Confirm",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String time = df.format(new Date());
                            myRef.child("user").child(userID).child("heartrate").child(time).setValue(rhrTextView.getText());
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        }








        //HERE I SEND THE DATA TO THE FIREBASE
    }
}
