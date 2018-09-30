package five.seshealthpatient.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import five.seshealthpatient.Model.UserInformation;
import five.seshealthpatient.R;

public class UsersActivity extends AppCompatActivity {
    private static final String TAG = "UsersActivity";

    private List<UserInformation> userList = new ArrayList<>();

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;

    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private LinearLayoutManager mLayoutManager;
    private Button addNew, btn;
    private TextView pairedSum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        findID();
        initialization();
        //showPairedUsers();

        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addNew = new Intent(UsersActivity.this, AddNewPair.class);
                startActivity(addNew);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void findID(){
        addNew = (Button) findViewById(R.id.addNew);
        btn = (Button) findViewById(R.id.btn);
        pairedSum = (TextView) findViewById(R.id.pairedSum);
    }

    private void initialization(){
        mToolbar = (Toolbar) findViewById(R.id.pair_toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setTitle("Paired Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLayoutManager = new LinearLayoutManager(this);

        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(mLayoutManager);

        setUser();
    }

    private void setUser(){
        //Set User
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
                Log.d(TAG, "onDataChange:");
                userList.clear();
                getPairedUsers(dataSnapshot);
                showPairedUsers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getPairedUsers(DataSnapshot dataSnapshot) {
        int PairedSum = 0;
        for(DataSnapshot ds : dataSnapshot.child("user").child(userID).child("pair").getChildren()) {
            UserInformation uInfo = new UserInformation();
            //uInfo.setName(ds.child(userID).child("pair").getValue(UserInformation.class).getName()); //set the name
            String UID = null;
            if(ds.getValue().equals("passed")){
                PairedSum++;
                UID = ds.getKey();
                uInfo.setUID(UID);
                Log.d(TAG, "getPairedUsers: test111UID :"+UID);
                String userName = null;
                String group = null;
                for(DataSnapshot ds1 : dataSnapshot.child("user").getChildren()){
                    if(ds1.getKey().equals(UID)){
                        userName = ds1.child("name").getValue(String.class);
                        group = ds1.child("group").getValue(String.class);
                        //uInfo.setName(ds1.getValue(UserInformation.class).getName());
                        Log.d(TAG, "getPairedUsers: testName :"+userName);
                    }
                }
                uInfo.setName(userName);
                uInfo.setGroup(group);
                pairedSum.setText("Paired Sum: "+PairedSum);
                userList.add(uInfo);
            }
        }
    }

    private void showPairedUsers(){
        UserAdapter adapter = new UserAdapter(userList);
        mUsersList.setAdapter(adapter);
    }

    public static class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

        private List<UserInformation> userInformations;

        class ViewHolder extends RecyclerView.ViewHolder{
            View userView;
            TextView userName, userDescription;
            Button btn;

            public ViewHolder(View itemView) {
                super(itemView);
                userView = itemView;
                userName = (TextView) itemView.findViewById(R.id.user_single_name);
                userDescription = (TextView) itemView.findViewById(R.id.user_single_status);
                btn = (Button) itemView.findViewById(R.id.btn);
            }
        }

        public UserAdapter(List<UserInformation> userList){
            userInformations = userList;
        }

        @NonNull
        @Override
        public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);
            final ViewHolder holder = new ViewHolder(view);
            holder.userView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    UserInformation userInformation = userInformations.get(position);
                    Toast.makeText(v.getContext(),"you clicked user: "+ userInformation.getName(),Toast.LENGTH_SHORT).show();
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
            UserInformation user = userInformations.get(position);
            holder.userName.setText(user.getName());
            holder.userDescription.setText(user.getGroup()+": Self-introduction");
            holder.btn.setVisibility(View.INVISIBLE);
        }

        @Override
        public int getItemCount() {
            return userInformations.size();
        }
    }

}
