package five.seshealthpatient.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import five.seshealthpatient.R;

public class RegisterActivity extends AppCompatActivity {

    private static String TAG = "RegisterActivity";
    @BindView(R.id.usernameET)
    EditText usernameEditText;

    @BindView(R.id.passwordET)
    EditText passwordEditText;

    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        // A reference to the toolbar, that way we can modify it as we please
        Toolbar toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        //Set title
        setTitle(R.string.register_activity_title);

        //Get instance
        firebaseAuth = FirebaseAuth.getInstance();

    }

    @OnClick(R.id.register_btn)
    public void register(){
        final String username = usernameEditText.getText().toString();
        final String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(username)){
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }
        //Check password is entered
        if (TextUtils.isEmpty(password)){
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }
        //Check length
        if(password.length() < 6){
            Toast.makeText(getApplicationContext(), "Password too short, 6 characters minimum!", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Toast.makeText(RegisterActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                // If sign in fails, display a message to the user.
                if (!task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Authentication failed." + task.getException(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("username" ,username); //Pass the key value filePath, and the value is the string filePath.
                    intent.setClass(RegisterActivity.this,RegisterInformation.class);
                    RegisterActivity.this.startActivity(intent);
                    //startActivity(new Intent(RegisterActivity.this, RegisterInformation.class));
                    //startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                }
            }
        });
    }

    @OnClick(R.id.back_btn)
    public void goBack(){
        Intent intent = new Intent(this, LoginActivity.class);
        finish();
    }
}
