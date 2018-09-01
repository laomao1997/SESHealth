package five.seshealthpatient.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import five.seshealthpatient.R;

public class SendDataPacket extends AppCompatActivity implements ChangePhotoDialog.OnPhotoReceivedListener{

    private static final String TAG = "SendDataPacket";

    @Override
    public void getImagePath(Uri imagePath) {
        if( !imagePath.toString().equals("")){
            mSelectedImageBitmap = null;
            mSelectedImageUri = imagePath;
            Log.d(TAG, "getImagePath: got the image uri: " + mSelectedImageUri);
            Log.d(TAG, "getImage got the image PathTest: 1");
            imageAdded.setImageURI(imagePath);
        }
    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        if(bitmap != null){
            mSelectedImageUri = null;
            mSelectedImageBitmap = bitmap;
            Log.d(TAG, "getImageBitmap: got the image bitmap: " + mSelectedImageBitmap);
            imageAdded.setImageBitmap(bitmap);
        }
    }

    //firebase
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private StorageReference mySto;
    private  String userID;

    private TextView fullName, gender, age, height, weight, medicalCondition, heartRate, currentLocation;
    private EditText relevantText, imageName;
    private Button heartRateBtn, currentLocationBtn, submitBtn, addImage;
    private ImageView imageAdded;
    String time;
    String mImageName;

    private static final int REQUEST_CODE = 1234;
    private static final double MB_THRESHHOLD = 5.0;
    private static final double MB = 1000000.0;

    private ProgressBar mProgressBar;
    private boolean mStoragePermissions;
    private Uri mSelectedImageUri;
    private Bitmap mSelectedImageBitmap;
    private byte[] mBytes;
    private double progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data_packet);
        findID();

        //declare the database reference object. NOTE: Unless you are signed in, this will not be useable.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    //toastMessage("Successfully signed in with: " + user.getEmail());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    //toastMessage("Successfully signed out.");
                }
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showData(dataSnapshot);// This method is called once with the initial value and again, whenever data at this location is updated.
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: First step, load the image and get the path.");
                verifyStoragePermissions();
                init();
            }

        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Attempting to add object to database.");
                FirebaseUser user = mAuth.getCurrentUser();
                String userID = user.getUid();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                time = df.format(new Date());
                myRef.child("user").child(userID).child("packet").child(time).child("time").setValue(time);
                String heart = "heartTest";
                myRef.child("user").child(userID).child("packet").child(time).child("heart").setValue(heart);
                String longitude = "longitudeTest";
                myRef.child("user").child(userID).child("packet").child(time).child("gps").child("0").setValue(longitude);
                String latitude = "latitudeTest";
                myRef.child("user").child(userID).child("packet").child(time).child("gps").child("1").setValue(latitude);
                String userRelevantText = relevantText.getText().toString();
                if(!userRelevantText.equals("")){
                    myRef.child("user").child(userID).child("packet").child(time).child("text").setValue(userRelevantText);
                    toastMessage("Adding relevant information to database...");
                    relevantText.setText("");
                }

                 //Upload the New Phot
                if(mSelectedImageUri != null){
                    mImageName = imageName.getText().toString();
                    if(mImageName.equals("")){
                        mImageName = "Unnamed";
                    }
                    uploadNewPhoto(mSelectedImageUri);
                }else if(mSelectedImageBitmap  != null){
                    mImageName = imageName.getText().toString();
                    if(mImageName.equals("")){
                        mImageName = "Unnamed";
                    }
                    Log.d(TAG, "onClick: test 555");
                    uploadNewPhoto(mSelectedImageBitmap);
                }
            }
        });
    }

    /**
     * Uploads a new profile photo to Firebase Storage using an imageUri
     */
    public void uploadNewPhoto(Uri imageUri){
        //upload a new profile photo to firebase storage.
        Log.d(TAG, "uploadNewPhoto: uploading new profile photo to firebase storage.");
        //Only accept image sizes that are compressed to under 5MB, or do not allow image to be uploaded
        BackgroundImageResize resize = new BackgroundImageResize(null);
        resize.execute(imageUri);
    }

    /**
     * Uploads a new profile photo to Firebase Storage using an imageBitmap
     */
    public void uploadNewPhoto(Bitmap imageBitmap){
        //upload a new profile photo to firebase storage.
        Log.d(TAG, "uploadNewPhoto: uploading new profile photo to firebase storage.");
        //Only accept image sizes that are compressed to under 5MB, or do not allow image to be uploaded
        BackgroundImageResize resize = new BackgroundImageResize(imageBitmap);
        Uri uri = null;
        resize.execute(uri);
    }

    /**
     * 1) doinBackground takes an imageUri and returns the byte array after compression
     * 2) onPostExecute will print the % compression to the log once finished
     */
    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {
        Bitmap mBitmap;
        public BackgroundImageResize(Bitmap bm) {
            if(bm != null){
                mBitmap = bm;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog();
            Toast.makeText(SendDataPacket.this, "compressing image", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected byte[] doInBackground(Uri... params ) {
            Log.d(TAG, "doInBackground: started.");
            if(mBitmap == null){
                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(SendDataPacket.this.getContentResolver(), params[0]);
                    Log.d(TAG, "doInBackground: bitmap size: megabytes: " + mBitmap.getByteCount()/MB + " MB");
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: IOException: ", e.getCause());
                }
            }

            byte[] bytes = null;
            for (int i = 1; i < 11; i++){
                if(i == 10){
                    Toast.makeText(SendDataPacket.this, "That image is too large.", Toast.LENGTH_SHORT).show();
                    break;
                }
                bytes = getBytesFromBitmap(mBitmap,100/i);
                Log.d(TAG, "doInBackground: megabytes: (" + (11-i) + "0%) "  + bytes.length/MB + " MB");
                if(bytes.length/MB  < MB_THRESHHOLD){
                    return bytes;
                }
            }
            return bytes;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            hideDialog();
            mBytes = bytes;
            executeUploadTask();//execute the upload
        }
    }

    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * convert from bitmap to byte array.
     */
    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void executeUploadTask(){
        showDialog();//specify where the photo will be stored
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/users/" + FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+time+"/" + mImageName); //just replace the old image with the new one
        if(mBytes.length/MB < MB_THRESHHOLD) {// Create file metadata including the content type.
            StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpg").setContentLanguage("en").build();
            UploadTask uploadTask = null;//if the image size is valid then I can submit to database
            uploadTask = storageReference.putBytes(mBytes, metadata);
            //uploadTask = storageReference.putBytes(mBytes); //without metadata

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Now insert the download url into the firebase database
                    //Task<Uri> firebaseURL = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                    //Task<Uri> firebaseURL = mySto.getDownloadUrl();
                    String firebaseURL = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                    Toast.makeText(SendDataPacket.this, "Upload Success", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: firebase download url : " + firebaseURL.toString());
                    FirebaseDatabase.getInstance().getReference()
                            .child("user")
                            .child(userID)
                            .child("packet")
                            .child(time)
                            .child("imageUri")
                            .setValue(firebaseURL.toString());
                    hideDialog();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(SendDataPacket.this, "could not upload photo", Toast.LENGTH_SHORT).show();
                    hideDialog();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if(currentProgress > (progress + 15)){
                        progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: Upload is " + progress + "% done");
                        Toast.makeText(SendDataPacket.this, progress + "%", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            Toast.makeText(this, "Image is too Large", Toast.LENGTH_SHORT).show();
        }
    }

    private void findID() {

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

        imageAdded = (ImageView) findViewById(R.id.imageAdded);
        imageName = (EditText) findViewById(R.id.imageName);
        addImage = (Button) findViewById(R.id.addImage);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        relevantText = (EditText)findViewById(R.id.relevantText);
        submitBtn = (Button) findViewById(R.id.submitBtn);
    }

    private void showData(DataSnapshot dataSnapshot) {
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            //String name = ds.child(userID).child("name").getValue(String.class);
            //uInfo.setName(name);
            String nameRead = ds.child(userID).child("name").getValue(String.class); //Read the name
            boolean genderRead = ds.child(userID).child("gender").getValue(boolean.class); //Read the email
            String ageRead = ds.child(userID).child("age").getValue(String.class);
            String heightRead = ds.child(userID).child("height").getValue(String.class);
            String weightRead = ds.child(userID).child("weight").getValue(String.class);
            String medicalConditionRead = ds.child(userID).child("condition").getValue(String.class);
            //uInfo.setGender(ds.child(userID).child("gender").equalTo(true).getValue(String.class));
            //String genderReceive = ds.child(userID).child("gender").child("female").getValue(String.class);

            Log.d(TAG, "showData: name: " + nameRead);

            fullName.setText(nameRead);
            gender.setText(genderRead?"male":"female");
            age.setText(ageRead);
            height.setText(heightRead);
            weight.setText(weightRead);
            medicalCondition.setText(medicalConditionRead);
        }
    }

    /**
     * Generalized method for asking permission. Can pass any array of permissions
     */
    public void verifyStoragePermissions(){
        Log.d(TAG, "verifyPermissions: asking user for permissions.");
        String[] permissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2] ) == PackageManager.PERMISSION_GRANTED) {
            mStoragePermissions = true;
        } else {
            ActivityCompat.requestPermissions(
                    SendDataPacket.this,
                    permissions,
                    REQUEST_CODE
            );
        }
    }

    private void init(){
        if(mStoragePermissions){
            ChangePhotoDialog dialog = new ChangePhotoDialog();
            dialog.show(getSupportFragmentManager(), "ChangePhotoDialog");
        }else{
            verifyStoragePermissions();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * customizable toast
     */
    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}
