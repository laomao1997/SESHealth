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

public class AddNewPair extends AppCompatActivity {

    private static final String TAG = "AddNewPair";

    private List<UserInformation> userList = new ArrayList<>();

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;
    private String userName;

    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private LinearLayoutManager mLayoutManager;
    private TextView addUserTxt;
    private Button addUserBtn;
    private DataSnapshot dsTest;
    private String btnStateText;
    private List<String> btnStateTextList = new ArrayList<>();
    private int listItemPosition;

    private UserInformation userInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_pair);

        findID();
        initialization();
        showAllUsers();

        addUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnStateTextList.get(listItemPosition).equals("Waiting")){
                    toastMessage("You have already sent request to the user, please wait for the user's response.");
                    return;
                }
                if(btnStateTextList.get(listItemPosition).equals("Delete")){
                    deleteUser();
                    toastMessage("You have successfully deleted this user from your list.");
                }
                if(btnStateTextList.get(listItemPosition).equals("Requested")){
                    agreeRequest();
                    toastMessage("You have agreed to the user's application.");
                }
                if(btnStateTextList.get(listItemPosition).equals("Add")){
                    addNewUser();
                    toastMessage("Request sent successfully!");
                }
            }
        });

    }

    private void findID(){
        addUserTxt = (TextView) findViewById(R.id.addUserTxt);
        addUserBtn = (Button) findViewById(R.id.addUserBtn);
    }

    private void initialization(){
        mToolbar = (Toolbar) findViewById(R.id.pair_toolbar_new);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setTitle("Add New Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLayoutManager = new LinearLayoutManager(this);

        mUsersList = (RecyclerView) findViewById(R.id.users_list_new);
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
                dsTest = dataSnapshot;
                getAllUsers(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getAllUsers(DataSnapshot dataSnapshot) {

        for(DataSnapshot ds : dataSnapshot.child("user").getChildren()) {
            if(ds.getKey().equals(userID)){
                continue;
            }
            UserInformation uInfo = new UserInformation();
            uInfo.setName(ds.getValue(UserInformation.class).getName()); //set the name
            uInfo.setGroup(ds.getValue(UserInformation.class).getGroup());
            String UID = ds.getKey();
            uInfo.setUID(UID); //set the email
            userList.add(uInfo);
        }
    }

    private void showAllUsers(){
        AddNewPair.UserAdapter adapter = new UserAdapter(userList);
        mUsersList.setAdapter(adapter);
    }

    public  class UserAdapter extends RecyclerView.Adapter<AddNewPair.UserAdapter.ViewHolder>{

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
        public AddNewPair.UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);
            final AddNewPair.UserAdapter.ViewHolder holder = new AddNewPair.UserAdapter.ViewHolder(view);
            holder.userView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    listItemPosition = position;
                    userInformation = userInformations.get(position);
                    userName = userInformation.getName();
                    addUserTxt.setText(btnStateTextList.get(position)+": "+userName);
                    Log.d(TAG, "onClick: test111: " + btnStateTextList.get(position));
                    Toast.makeText(v.getContext(),"you clicked user: "+ userInformation.getName(),Toast.LENGTH_SHORT).show();
                }
            });
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    listItemPosition = position;
                    userInformation = userInformations.get(position);
                    userName = userInformation.getName();
                    addUserTxt.setText(btnStateTextList.get(position)+": "+userName);
                    Log.d(TAG, "onClick: test112: " + btnStateTextList.get(position));
                    Toast.makeText(v.getContext(),"you clicked user: "+ userInformation.getName(),Toast.LENGTH_SHORT).show();
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull AddNewPair.UserAdapter.ViewHolder holder, int position) {
            UserInformation user = userInformations.get(position);
            holder.userName.setText(user.getName());
            holder.userDescription.setText(user.getGroup()+": Self-introduction");
            btnStateText = btnState(user);
            btnStateTextList.add(btnStateText);
            holder.btn.setText(btnStateText);
            Log.d(TAG, "onBindViewHolder: test1: "+btnStateText);
        }

        @Override
        public int getItemCount() {
            return userInformations.size();
        }
    }

    private String btnState(UserInformation user){
        if(user==null){
            toastMessage("Please select a user.");
            return null;
        }
        Boolean hasRequest = false;
        Boolean hasPass = false;
        Boolean isWait = false;
        if(dsTest.child("user").child(userID).child("pair").child(user.getUID()).exists()){
            hasRequest = dsTest.child("user").child(userID).child("pair").child(user.getUID()).getValue().equals("requested");
            hasPass = dsTest.child("user").child(userID).child("pair").child(user.getUID()).getValue().equals("passed");
            isWait = dsTest.child("user").child(userID).child("pair").child(user.getUID()).getValue().equals("waiting");
        }
        Log.d(TAG, "addNewUser: test11"+ hasRequest);
        if(hasRequest){
            return "Requested";
        }
        if(hasPass){
            return "Delete";
        }
        if(isWait){
            return "Waiting";
        }
        return "Add";
    }

    public void addNewUser(){
        if(userInformation==null){
            toastMessage("Please select a user.");
            return;
        }
        Boolean hasRequest = false;
        Boolean hasPass = false;
        Boolean isWait = false;
        if(dsTest.child("user").child(userInformation.getUID()).child("pair").child(userID).exists()){
            hasRequest = dsTest.child("user").child(userInformation.getUID()).child("pair").child(userID).getValue().equals("requested");
            hasPass = dsTest.child("user").child(userInformation.getUID()).child("pair").child(userID).getValue().equals("passed");
            isWait = dsTest.child("user").child(userInformation.getUID()).child("pair").child(userID).getValue().equals("waiting");
        }
        Log.d(TAG, "addNewUser: test11"+ hasRequest);
        if(hasRequest){
            toastMessage("You have already sent request to the user, please wait for the user's response.");
            return;
        }
        if(hasPass){
            toastMessage("You have already added the user.");
            return;
        }
        if(isWait){
            toastMessage("You have already received the request from this user, please confirm it.");
            return;
        }
        if((userInformation!=null)&& !hasRequest&&!hasPass&&!isWait){
            myRef.child("user").child(userInformation.getUID()).child("pair").child(userID).setValue("requested");
            myRef.child("user").child(userID).child("pair").child(userInformation.getUID()).setValue("waiting");
            //toastMessage("Request sent successfully!");
        }
    }

    public void agreeRequest(){
        if(userInformation!=null){
            myRef.child("user").child(userInformation.getUID()).child("pair").child(userID).setValue("passed");
            myRef.child("user").child(userID).child("pair").child(userInformation.getUID()).setValue("passed");
            //toastMessage("You have agreed to the user's application.");
        }
    }

    public void deleteUser(){
        if(userInformation!=null){
            myRef.child("user").child(userInformation.getUID()).child("pair").child(userID).setValue(null);
            myRef.child("user").child(userID).child("pair").child(userInformation.getUID()).setValue(null);
            //toastMessage("You have successfully deleted this user from your list.");
        }

    }

    /**
     * customizable toast
     */
    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}
