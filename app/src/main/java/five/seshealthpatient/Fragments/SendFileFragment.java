package five.seshealthpatient.Fragments;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleAdapter;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import five.seshealthpatient.Activities.SendFile;
import five.seshealthpatient.Model.FilePacket;
import five.seshealthpatient.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SendFileFragment extends Fragment {

    private static final String TAG = "SendFileFragment";

    /**
     * add Firebase Database stuff
     */
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;

    FilePacket filePacket = new FilePacket();

    /**
     * ListView sub text
     */
    private SimpleAdapter simpleAdapter;

    /**
     * UI references
     */
    @BindView(R.id.btn_upload_file)
    Button btnUploadFile;
    @BindView(R.id.listView)
    SwipeMenuListView mListView;

    public SendFileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Send local file");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_send_file, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

        /**
         * Swipe Menu ListView Creator
         */
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {


                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(150);
                // set a icon
                deleteItem.setIcon(R.drawable.ic_action_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        mListView.setMenuCreator(creator);
    }

    private void showData(DataSnapshot dataSnapshot) {
        // ArrayList<String> array  = new ArrayList<>();
        final List<Map<String, Object>> datas=new ArrayList<Map<String, Object>>();
        for(DataSnapshot ds : dataSnapshot.child("user").child(userID).child("file").getChildren()){
            String date = ds.getKey();
            String fileName = "";
            String suffix = "";
            String link = "";
            for(DataSnapshot dss : ds.getChildren()) {
                fileName = dss.getKey();
                for(DataSnapshot dsss : dss.getChildren()) {
                    suffix = dsss.getKey();
                }
                link = dss.child(suffix).getValue(String.class);
            }
            Map map = new HashMap();
            map.put("date", date);
            map.put("fileName", fileName);
            map.put("suffix", suffix);
            map.put("link", link);
            datas.add(map);
            // array.add(record);
        }

        simpleAdapter=new SimpleAdapter(getContext(),datas,R.layout.listview_file,new String[]{"fileName", "date"},new int[]{R.id.fileTvInLv, R.id.dateTvInLv});
        mListView.setAdapter(simpleAdapter);

        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // Remove data
                        // Remove pack from online database
                        String date = datas.get(position).get("date").toString();
                        String fileName = datas.get(position).get("fileName").toString();
                        String suffix = datas.get(position).get("suffix").toString();
                        myRef.child("user").child(userID).child("file").child(date).child(fileName).child(suffix).setValue(null);

                        // Remove item from ListView
                        datas.remove(position);
                        simpleAdapter.notifyDataSetChanged();
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }

    @OnClick(R.id.btn_upload_file)
    public void uploadFile() {
        Intent intent = new Intent(getActivity(), SendFile.class);
        startActivity(intent);
    }

}