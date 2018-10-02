package five.seshealthpatient.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import five.seshealthpatient.R;

public class SendFile extends AppCompatActivity implements ChangePhotoDialog.OnPhotoReceivedListener{

    private static final String TAG = "SendFile";

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
    private StorageReference mStorageRef;
    private  String userID;
    Task<Uri> urlTask;

    private TextView fileAddress;
    private EditText imageName, fileName;
    private Button submitBtn, addImage, addFile;
    private ImageView imageAdded;
    String time;
    String mImageName;
    String mFileName;
    String filePath;
    SimpleDateFormat df;
    String fileNameFile;

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
        setContentView(R.layout.activity_send_file);
        mStorageRef = FirebaseStorage.getInstance().getReference();
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
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        Intent intent = getIntent(); //Get filepath from FolderActivity.
        filePath = intent.getStringExtra("filePath");
        fileAddress.setText(filePath);

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: First step, load the image and get the path.");
                verifyStoragePermissions();
                init();
            }

        });

        addFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent SendFile = new Intent(SendFile.this, FolderActivity.class);
                    startActivityForResult(SendFile, 7);
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                time = df.format(new Date());
                //Upload the New Photo
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
                mFileName = fileName.getText().toString();
                if(mFileName.equals("")){
                    mFileName = "Unnamed";
                }
                if(filePath!=null) uploadfile(filePath);
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
        SendFile.BackgroundImageResize resize = new SendFile.BackgroundImageResize(null);
        resize.execute(imageUri);
    }

    /**
     * Uploads a new profile photo to Firebase Storage using an imageBitmap
     */
    public void uploadNewPhoto(Bitmap imageBitmap){
        //upload a new profile photo to firebase storage.
        Log.d(TAG, "uploadNewPhoto: uploading new profile photo to firebase storage.");
        //Only accept image sizes that are compressed to under 5MB, or do not allow image to be uploaded
        SendFile.BackgroundImageResize resize = new SendFile.BackgroundImageResize(imageBitmap);
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
            Toast.makeText(SendFile.this, "compressing image", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected byte[] doInBackground(Uri... params ) {
            Log.d(TAG, "doInBackground: started.");
            if(mBitmap == null){
                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(SendFile.this.getContentResolver(), params[0]);
                    Log.d(TAG, "doInBackground: bitmap size: megabytes: " + mBitmap.getByteCount()/MB + " MB");
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: IOException: ", e.getCause());
                }
            }

            byte[] bytes = null;
            for (int i = 1; i < 11; i++){
                if(i == 10){
                    Toast.makeText(SendFile.this, "That image is too large.", Toast.LENGTH_SHORT).show();
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
        final StorageReference storageReference1 = FirebaseStorage.getInstance().getReference().child("images/users/" + FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+time+"/" + mImageName); //just replace the old image with the new one
        if(mBytes.length/MB < MB_THRESHHOLD) {// Create file metadata including the content type.
            StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpg").setContentLanguage("en").build();
            UploadTask uploadTask = null;//if the image size is valid then I can submit to database
            uploadTask = storageReference1.putBytes(mBytes, metadata);
            //uploadTask = storageReference.putBytes(mBytes); //without metadata

            urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference1.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {

                        Uri downloadUri = task.getResult();

                        FirebaseDatabase.getInstance().getReference()
                                .child("user")
                                .child(userID)
                                .child("file")
                                .child(time)
                                .child(time)
                                .child("jpg")
                                .setValue(downloadUri.toString());
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Now insert the download url into the firebase database
                    //Task<Uri> firebaseURL = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                    //Task<Uri> firebaseURL = mySto.getDownloadUrl();
                    String firebaseURL = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                    Toast.makeText(SendFile.this, "Upload Success", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: test firebase download url : " + taskSnapshot.getMetadata().getReference().getDownloadUrl());
                  /*  FirebaseDatabase.getInstance().getReference()
                            .child("user")
                            .child(userID)
                            .child("file")
                            .child(time)
                            .child("link")
                            .setValue(firebaseURL);*/
/*                    FirebaseDatabase.getInstance().getReference()
                            .child("user")
                            .child(userID)
                            .child("file")
                            .child(time)
                            .child(mImageName)
                            .child("jpg")
                            .setValue(firebaseURL);*/
                    hideDialog();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(SendFile.this, "could not upload photo", Toast.LENGTH_SHORT).show();
                    hideDialog();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if(currentProgress > (progress + 15)){
                        progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: Upload is " + progress + "% done");
                        Toast.makeText(SendFile.this, progress + "%", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            Toast.makeText(this, "Image is too Large", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Unused in this code currently.
     * @return
     */
    public String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);//Determine if an sd card exists
        if(sdCardExist)
        {
            sdDir = Environment.getExternalStorageDirectory();//Get the root directory
            Log.d(TAG, "getSDPath: test true");
        }
        return sdDir.toString();
    }

    private void uploadfile(String path) {
        File newFile = new File(path);
        String fileName = newFile.getName();
        fileNameFile = fileName.substring(0,fileName.lastIndexOf("."));
        final String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);

        final Uri file = Uri.fromFile(newFile);
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("fill/users/" + FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+mFileName+"."+suffix);
        //StorageReference riversRef = storageRef.child("file/"+file.getLastPathSegment());
        UploadTask uploadTask = null;//if the image size is valid then I can submit to database
        uploadTask = storageReference.putFile(file);

        urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.SECOND, 1);
                    String time = df.format(c.getTime());
                    Uri downloadUri = task.getResult();
                    FirebaseDatabase.getInstance().getReference()
                            .child("user")
                            .child(userID)
                            .child("file")
                            .child(time)
                            .child(fileNameFile)
                            .child(suffix)
                            .setValue(downloadUri.toString());
                } else {
                    // Handle failures
                    // ...
                }
            }
        });

// Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                if(file!=null) {
                    Log.d(TAG, "onFailure: Test, upload fail, not null");
                }
                if(file == null) {
                    Log.d(TAG, "onFailure: Test, upload fail, null");
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: Test, upload success");
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                /*FirebaseDatabase.getInstance().getReference()
                        .child("user")
                        .child(userID)
                        .child("file")
                        .child(time)
                        .setValue(urlTask.toString());*/
            }
        });
    }

    private void findID() {
        imageAdded = (ImageView) findViewById(R.id.imageAdded);
        imageName = (EditText) findViewById(R.id.imageName);
        addImage = (Button) findViewById(R.id.addImage);

        fileName = (EditText) findViewById(R.id.fileName);
        fileAddress = (TextView) findViewById(R.id.fileAddress);
        addFile = (Button) findViewById(R.id.addFile);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        submitBtn = (Button) findViewById(R.id.submitBtn);
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
                    SendFile.this,
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

