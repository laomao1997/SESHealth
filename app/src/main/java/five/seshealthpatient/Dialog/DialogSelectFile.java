package five.seshealthpatient.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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
import five.seshealthpatient.Activities.ChangePhotoDialog;
import five.seshealthpatient.Activities.SendDataPacket;
import five.seshealthpatient.Model.DataPacket;
import five.seshealthpatient.Model.FilePacket;
import five.seshealthpatient.R;

public class DialogSelectFile extends Dialog {

    private static final String TAG = "DialogSelectFile";

    /**
     * add Firebase Database stuff
     */
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;

    /**
     * ListView sub text
     */
    private SimpleAdapter simpleAdapter;

    /**
     * UI reference
     */
    @BindView(R.id.listView)
    SwipeMenuListView mListView;
    @BindView(R.id.tvFileChoosed)
    TextView tvFileChoosed;

    FilePacket filePacket = new FilePacket();






    public DialogSelectFile(Context context) {
        super(context);
    }

    public DialogSelectFile(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected DialogSelectFile(Context context, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.dialog_select_file, null);
        setContentView(view);

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

                // create "select" item
                SwipeMenuItem selectItem = new SwipeMenuItem(
                        getContext());
                // set item background
                selectItem.setBackground(new ColorDrawable(Color.rgb(0x00,
                        0x99, 0x33)));
                // set item width
                selectItem.setWidth(120);
                // set a icon
                selectItem.setIcon(R.drawable.ic_action_select);
                // add to menu
                menu.addMenuItem(selectItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(120);
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
                        filePacket.setFileName(datas.get(position).get("fileName").toString());
                        filePacket.setDate(datas.get(position).get("date").toString());
                        filePacket.setLink(datas.get(position).get("link").toString());
                        Log.d(TAG, "onMenuItemClick: " + filePacket.getFileName());
                        tvFileChoosed.setText("File: " + filePacket.getFileName());

                        break;
                    case 1:
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


    @OnClick(R.id.submitBtn)
    public void submit() {
        Intent intent = new Intent();
        intent.putExtra("file", filePacket.getFileName()+"\n"+filePacket.getDate());
        intent.setClass(getContext(), SendDataPacket.class);
        getContext().startActivity(intent);
        Log.d(TAG, "submit: " + filePacket.toString());

        dismiss();
    }

}
