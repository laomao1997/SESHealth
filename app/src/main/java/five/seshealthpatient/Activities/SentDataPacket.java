package five.seshealthpatient.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import five.seshealthpatient.R;

public class SentDataPacket extends AppCompatActivity {
    private EditText packetName;
    private TextView fullName;
    private TextView gender;
    private TextView age;
    private TextView height;
    private TextView weight;
    private TextView medicalCondition;
    private TextView heartRate;
    private Button heartRateBtn;
    private TextView currentLocation;
    private Button currentLocationBtn;
    private EditText relevantText;
    private Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sent_data_packet);
        findID();
    }

    private void findID() {
        packetName = (EditText)findViewById(R.id.packetName);
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
}
