package five.seshealthpatient.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import five.seshealthpatient.R;

public class SelectUserActivity extends AppCompatActivity {

    private static final String TAG = "SelectUser";
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);
        ButterKnife.bind(this);
        getEmailFromRegisterActivity();
    }

    @OnClick(R.id.doctorBtn)
    public void docotorInfo(){
        Intent intent = new Intent();
        intent.putExtra("username" ,username); //Pass the key value filePath, and the value is the string filePath.
        intent.setClass(SelectUserActivity.this,RegisterInformationDoctor.class);
        SelectUserActivity.this.startActivity(intent);
        Log.d(TAG, "Starting Doc Info");
        //finish();
    }

    @OnClick(R.id.patientBtn)
    public void patientInfo(){
        Intent intent = new Intent();
        intent.putExtra("username" ,username); //Pass the key value filePath, and the value is the string filePath.
        intent.setClass(SelectUserActivity.this,RegisterInformation.class);
        SelectUserActivity.this.startActivity(intent);
        //finish();
    }

    private void getEmailFromRegisterActivity(){
        Intent intent = getIntent(); //Get filepath from FolderActivity.
        username = intent.getStringExtra("username");
    }
}
