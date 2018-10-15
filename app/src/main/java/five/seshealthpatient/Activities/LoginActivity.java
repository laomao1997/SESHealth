package five.seshealthpatient.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
import five.seshealthpatient.Model.UserInformation;
import five.seshealthpatient.R;

/**
 * Class: LoginActivity
 * Extends: {@link AppCompatActivity}
 * Author: Carlos Tirado < Carlos.TiradoCorts@uts.edu.au> and YOU!
 * Description:
 * <p>
 * Welcome to the first class in the project. I will be leaving some comments like this through all
 * the classes I write in order to help you get a hold on the project. Here I took the liberty of
 * creating an empty Log In activity for you to fill in the details of how your log in is
 * gonna work. Please, Modify Accordingly!
 * <p>
 */
public class LoginActivity extends AppCompatActivity {


    /**
     * Use the @BindView annotation so Butter Knife can search for that view, and cast it for you
     * (in this case it will get casted to Edit Text)
     */
    @BindView(R.id.usernameET)
    EditText usernameEditText;

    /**
     * If you want to know more about Butter Knife, please, see the link I left at the build.gradle
     * file.
     */
    @BindView(R.id.passwordET)
    EditText passwordEditText;

    /**
     * It is helpful to create a tag for every activity/fragment. It will be easier to understand
     * log messages by having different tags on different places.
     */
    private static String TAG = "LoginActivity";
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private StorageReference mStorageRef;
    private UserInformation uInfo;
    private String group;
    private String userID;
    private boolean loginSuccess = false;
    private String doctor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // You need this line on your activity so Butter Knife knows what Activity-View we are referencing
        ButterKnife.bind(this);

        // A reference to the toolbar, that way we can modify it as we please
        Toolbar toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);

        // A reference to the progress bar
        progressDialog = new ProgressDialog(this);

        //Initialise firebase auth
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();


        // Please try to use more String resources (values -> strings.xml) vs hardcoded Strings.
        setTitle(R.string.login_activity_title);

        //if (mAuth.getCurrentUser() != null) {
            //startActivity(new Intent(LoginActivity.this, MainActivity.class));
            //finish();
        //}
        mAuth = FirebaseAuth.getInstance();
    }




    /**
     * See how Butter Knife also lets us add an on click event by adding this annotation before the
     * declaration of the function, making our life way easier.
     */
    @OnClick(R.id.login_btn)
    public void LogIn() {
        final String username = usernameEditText.getText().toString();
        final String password = passwordEditText.getText().toString();

        // Having a tag, and the name of the function on the console message helps allot in
        // knowing where the message should appear.
        Log.d(TAG, "LogIn: username: " + username + " password: " + password);

        //Check username is entered
        if (TextUtils.isEmpty(username)){
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }
        //Check password is entered
        if (TextUtils.isEmpty(password)){
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        //authenticate user
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            // there was an error
                            if (password.length() < 6) {
                                passwordEditText.setError(getString(R.string.minimum_password));
                            } else {
                                Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            FirebaseUser user = mAuth.getCurrentUser();
                            userID = user.getUid();
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // This method is called once with the initial value and again
                                    // whenever data at this location is updated.
                                    showData(dataSnapshot);
                                    if(doctor.equals("doctor")) {
                                        Intent intent = new Intent(LoginActivity.this, DoctorActivity.class);
                                        startActivity(intent);
                                    }
                                    else{
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                    //toastMessage("Welcome. "+doctor+".");
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });



                        }
                    }
                });

    }

    private void showData(DataSnapshot dataSnapshot) {
        for(DataSnapshot ds : dataSnapshot.getChildren()) {
            UserInformation uInfo = new UserInformation();
            uInfo.setGroup(ds.child(userID).getValue(UserInformation.class).getGroup());
            doctor = uInfo.getGroup();
        }
    }

    @OnClick(R.id.create_account_btn)
    public void registerUser(){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.reset_password_btn)
    public void resetPassword(){
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        startActivity(intent);
    }

    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }




}
