package five.seshealthpatient.Activities;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

    @BindView(R.id.reset_password_btn)
    Button resetPasswordButton;

    @BindView(R.id.create_account_btn)
    Button createAccountButton;

    /**
     * It is helpful to create a tag for every activity/fragment. It will be easier to understand
     * log messages by having different tags on different places.
     */
    private static String TAG = "LoginActivity";

    /**
     * Firebase Instance
     */
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // You need this line on your activity so Butter Knife knows what Activity-View we are referencing
        ButterKnife.bind(this);

        // A reference to the toolbar, that way we can modify it as we please
        Toolbar toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);

        // Please try to use more String resources (values -> strings.xml) vs hardcoded Strings.
        setTitle(R.string.login_activity_title);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in" + user.getUid());
                    toastMessage("Successfully signed in with: " + user.getEmail());
                    startLog();
                }
                else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    toastMessage("Successfully signed out.");
                }
            }
        };
    }


    /**
     * See how Butter Knife also lets us add an on click event by adding this annotation before the
     * declaration of the function, making our life way easier.
     */
    @OnClick(R.id.login_btn)
    public void LogIn() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // TODO: For now, the login button will simply print on the console the username/password and let you in
        // TODO: It is up to you guys to implement a proper login system

        // Having a tag, and the name of the function on the console message helps allot in
        // knowing where the message should appear.
        if(!username.equals("")&&!password.equals("")){
            Log.d(TAG, "LogIn: username: " + username + " password: " + password);

            mAuth.signInWithEmailAndPassword(username, password);
        }


    }

    @OnClick(R.id.reset_password_btn)
    public void ResetPassword() {
        usernameEditText.setText("");
        passwordEditText.setText("");
        Intent intentResetPass = new Intent(LoginActivity.this, ResetPasswordActivity.class);
        intentResetPass.putExtra("resetPass", "0");
        startActivity(intentResetPass);
    }

    @OnClick(R.id.create_account_btn)
    public void createAccount() {
        usernameEditText.setText("");
        passwordEditText.setText("");
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * Start a new activity
     */
    public void startLog() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * add a toast to show when successfully signed in
     * @param message
     */
    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
