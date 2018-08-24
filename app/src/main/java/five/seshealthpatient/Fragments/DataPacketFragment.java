package five.seshealthpatient.Fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import five.seshealthpatient.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class DataPacketFragment extends Fragment {
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

    public DataPacketFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        findID();
        return inflater.inflate(R.layout.fragment_data_packet, container, false);
    }

    private void findID()
    {
        //packetName = (EditText)findViewById(R.id.packetName);
    }
}
