package five.seshealthpatient.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import five.seshealthpatient.Activities.SendFile;
import five.seshealthpatient.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SendFileFragment extends Fragment {


    @BindView(R.id.btn_upload_file)
    Button btnUploadFile;

    public SendFileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_send_file, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @OnClick(R.id.btn_upload_file)
    public void uploadFile() {
        Intent intent = new Intent(getActivity(), SendFile.class);
        startActivity(intent);
    }

}
