package five.seshealthpatient.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import five.seshealthpatient.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FolderActivity extends Activity implements OnItemClickListener,OnClickListener {

    private static final String TAG = "FolderActivity";

    private ListView folderLv;
    private TextView foldernowTv;
    private SimpleAdapter sAdapter;
    private List<Map<String, Object>> aList;
    private String baseFile;

    private TextView titleTv;

    // Declare an array to store all permissions that need to be applied dynamically
    private String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    // Declare a collection that is used later in the code to store the user's right to deny authorization
    private List<String> mPermissionList = new ArrayList<>();


    private void checkPermission() {
        mPermissionList.clear();
//        /**
//         * Determine which permissions are not granted so that you can reapply if necessary
//         */
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(FolderActivity.this, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        }
        /**
         * Determines whether the set of permissions granted by the storage committee is empty
         */
        if (!mPermissionList.isEmpty()) {
            String [] permissions1 = mPermissionList.toArray(new String[mPermissionList.size()]);
            ActivityCompat.requestPermissions(FolderActivity.this, permissions1, 1);
        } else {// ungranted permissions are empty, meaning all of them are granted
// follow-up operations...
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folder_layout);
//        if(ContextCompat.checkSelfPermission(FolderActivity.this, Manifest.
//        permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        checkPermission();
        baseFile=GetFilesUtils.getInstance().getBasePath();
        Log.d(TAG, "onCreate: folder1"+baseFile);
//        titleTv=(TextView) findViewById(R.id.title_text);
//        titleTv.setText("Local file");
        folderLv=(ListView) findViewById(R.id.folder_list);
        foldernowTv=(TextView) findViewById(R.id.folder_now);
        Drawable drawable1 = getResources().getDrawable(R.drawable.folder_backupimg);
        drawable1.setBounds(0,0,80,80);
        foldernowTv.setCompoundDrawables(drawable1, null, null, null);
        foldernowTv.setText(baseFile);
        foldernowTv.setOnClickListener(this);
        aList=new ArrayList<Map<String,Object>>();
        sAdapter=new SimpleAdapter(this, aList,R.layout.listitem_folder, new String[]{"fImg","fName","fInfo"},
                new int[]{R.id.folder_img,R.id.folder_name,R.id.folder_info});
        folderLv.setAdapter(sAdapter);
        folderLv.setOnItemClickListener(this);
        try {
            loadFolderList(baseFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void loadFolderList(String file) throws IOException{
        List<Map<String, Object>> list=GetFilesUtils.getInstance().getSonNode(file);
        if(list!=null){
            Collections.sort(list, GetFilesUtils.getInstance().defaultOrder());
            aList.clear();
            for(Map<String, Object> map:list){
                String fileType=(String) map.get(GetFilesUtils.FILE_INFO_TYPE);
                String fName = map.get(GetFilesUtils.FILE_INFO_NAME).toString();
                if(fName.substring(0,1).equals(".")){
                    continue;
                }
                Map<String,Object> gMap=new HashMap<String, Object>();
                if(map.get(GetFilesUtils.FILE_INFO_ISFOLDER).equals(true)){
                    gMap.put("fIsDir", true);
                    gMap.put("fImg",R.drawable.filetype_folder );
                    gMap.put("fInfo", map.get(GetFilesUtils.FILE_INFO_NUM_SONDIRS)+" folders and "+
                            map.get(GetFilesUtils.FILE_INFO_NUM_SONFILES)+" files");
                }else{
                    gMap.put("fIsDir", false);
                    if(fileType.equals("txt")||fileType.equals("text")){
                        gMap.put("fImg", R.drawable.filetype_txt);
                    }else{
                        gMap.put("fImg", R.drawable.filetype_unknow);
                    }
                    gMap.put("fInfo","The file size:"+GetFilesUtils.getInstance().getFileSize(
                            map.get(GetFilesUtils.FILE_INFO_PATH).toString()));
                }
                gMap.put("fName", map.get(GetFilesUtils.FILE_INFO_NAME));
                gMap.put("fPath", map.get(GetFilesUtils.FILE_INFO_PATH));
                aList.add(gMap);
            }
        }else{
            aList.clear();
        }
        sAdapter.notifyDataSetChanged();
        foldernowTv.setText(file);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // TODO Auto-generated method stub
        try {
            if(aList.get(position).get("fIsDir").equals(true)){
                loadFolderList(aList.get(position).get("fPath").toString());
            }else{
                String filePath = aList.get(position).get("fPath").toString();
                Intent intent = new Intent();
                intent.putExtra("filePath" ,filePath); //Pass the key value filePath, and the value is the string filePath.
                intent.setClass(FolderActivity.this,SendFile.class);
                FolderActivity.this.startActivity(intent);
                //doSearch(aList.get(position).get("fPath").toString());
                //Toast.makeText(this, "Functions need to be added", Toast.LENGTH_SHORT).show();
                /*Toast.makeText(this, "" + position, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onItemClick: position:"+position+". view:"+ view+". id:"+id+". PathtoString:"+aList.get(position).get("fPath")+". fIsDir:"+aList.get(position).get("fIsDir"));
                Log.d(TAG, "onItemClick: position:"+position+". view:"+ view+". id:"+id+". PathtoString:"+aList.get(position).get("fPath").toString()+". fIsDir:"+aList.get(position).get("fIsDir").toString());*/
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if(v.getId()==R.id.folder_now){
            try {
                String folder=GetFilesUtils.getInstance().getParentPath(foldernowTv.getText().toString());
                Log.d(TAG, "onClick: folder1"+folder);
                //if(folder==null){
                if(folder.equals("/storage/emulated")){
                    Intent SendFile = new Intent(FolderActivity.this, SendFile.class);
                    startActivityForResult(SendFile, 7);
                }
                else{
                    loadFolderList(folder);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    boolean mShowRequestPermission = true;//Users can exit by mShowRequestPermission if they have permission denied

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if(grantResults.length>0){
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            //Ask if check it again after the user clicks "no"
//                            boolean showRequestPermission = ActivityCompat.
//                                    shouldShowRequestPermissionRationale(FolderActivity.this,
//                                            permissions[i]);
//                            if (showRequestPermission) {
//                                // When true, the dialog box shows the permission decision and ask the user to selects whether to apply for the permission again
//                            } else {
//                                // Follow-up operation...
//                            }
                            Toast.makeText(this, "Will exit if permission is denied!", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                }else {
                    Toast.makeText(this, "Unknown error!", Toast.LENGTH_SHORT).show();
                    finish();
                }

                //Follow-up operations after the end of authorization...
                break;
            default:
                break;
        }
    }
}