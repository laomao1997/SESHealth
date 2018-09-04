package five.seshealthpatient.Activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Environment;


/**
 * A utility class for getting folders and files for your phone. If permission is granted, you can get a list of files for any path on your phone
 */
public class GetFilesUtils {

    public static final String FILE_TYPE_FOLDER="wFl2d";

    public static final String FILE_INFO_NAME="fName";
    public static final String FILE_INFO_ISFOLDER="fIsDir";
    public static final String FILE_INFO_TYPE="fFileType";
    public static final String FILE_INFO_NUM_SONDIRS="fSonDirs";
    public static final String FILE_INFO_NUM_SONFILES="fSonFiles";
    public static final String FILE_INFO_PATH="fPath";

    private static GetFilesUtils gfu;

    private GetFilesUtils(){

    }

    /**
     * Get GetFilesUtils instance
     * @return GetFilesUtils
     **/
    public static synchronized GetFilesUtils getInstance(){
        if(gfu==null){
            gfu=new GetFilesUtils();
        }
        return gfu;
    }

    /**
     * Gets the file list under the file path folder
     * @see #getSonNode(String)
     * @param path Folders on the phone
     * @return The file list information under the path folder is stored in the Map, and the list of Map key is as follows: ：
     * 		   FILE_INFO_NAME : String file name
     * 		   FILE_INFO_ISFOLDER: boolean isFolder
     * 		   FILE_INFO_TYPE: string suffix
     * 		   FILE_INFO_NUM_SONDIRS : int the tumber of subfolders
     * 		   FILE_INFO_NUM_SONFILES: int the tumber of subfiles
     * 		   FILE_INFO_PATH : String the absolute path of the file
     **/
    public List<Map<String, Object>> getSonNode(File path){
        if(path.isDirectory()){
            List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
            File[] files=path.listFiles();
            if(files!=null){

                for(int i=0;i<files.length;i++){
                    Map<String, Object> fileInfo=new HashMap<String, Object>();
                    fileInfo.put(FILE_INFO_NAME, files[i].getName());
                    if(files[i].isDirectory()){
                        fileInfo.put(FILE_INFO_ISFOLDER, true);
                        File[] bFiles=files[i].listFiles();
                        if(bFiles==null){
                            fileInfo.put(FILE_INFO_NUM_SONDIRS, 0);
                            fileInfo.put(FILE_INFO_NUM_SONFILES, 0);
                        }else{
                            int getNumOfDir=0;
                            for(int j=0;j<bFiles.length;j++){
                                if(bFiles[j].isDirectory()){
                                    getNumOfDir++;
                                }
                            }
                            fileInfo.put(FILE_INFO_NUM_SONDIRS, getNumOfDir);
                            fileInfo.put(FILE_INFO_NUM_SONFILES, bFiles.length-getNumOfDir);
                        }
                        fileInfo.put(FILE_INFO_TYPE, FILE_TYPE_FOLDER);
                    }else{
                        fileInfo.put(FILE_INFO_ISFOLDER, false);
                        fileInfo.put(FILE_INFO_NUM_SONDIRS, 0);
                        fileInfo.put(FILE_INFO_NUM_SONFILES, 0);
                        fileInfo.put(FILE_INFO_TYPE, getFileType(files[i].getName()));
                    }
                    fileInfo.put(FILE_INFO_PATH, files[i].getAbsoluteFile());
                    list.add(fileInfo);
                }
                return list;
            }else{
                return null;
            }
        }else{
            return null;
        }
    }
    /**
     * get the file list under the pathStr folder
     * @see #getSonNode(File)
     * @param pathStr absolute path to the folder on the phone
     * @return pathStrthe information of the file list under the folder is stored in the Map. The list of Map key is as follows：
     * 		   FILE_INFO_NAME : String String fileName
     * 		   FILE_INFO_ISFOLDER: boolean isFolder
     * 		   FILE_INFO_TYPE: string suffix
     * 		   FILE_INFO_NUM_SONDIRS : int theNumberOfSubfolders
     * 		   FILE_INFO_NUM_SONFILES: int theNumberOfSubfiles
     * 		   FILE_INFO_PATH : String the absolute path of the file
     **/


    public List<Map<String, Object>> getSonNode(String pathStr){
        File path=new File(pathStr);
        return getSonNode(path);
    }

    /**
     * Gets a list of sibling node files for the file path file or folder
     * @see #getBrotherNode(String)
     * @param path Folders on the phone
     * @return path The information of the file list under the folder is stored in the Map. The list of Map key is as follows:
     * 		   FILE_INFO_NAME : String fileName
     * 		   FILE_INFO_ISFOLDER: boolean isFolder
     * 		   FILE_INFO_TYPE: string suffix
     * 		   FILE_INFO_NUM_SONDIRS : int theNumberOfSubfolders
     * 		   FILE_INFO_NUM_SONFILES: int theNumberOfSubfiles
     * 		   FILE_INFO_PATH : String the absolute path of the file
     **/
    public List<Map<String, Object>> getBrotherNode(File path){
        if(path.getParentFile()!=null){
            return getSonNode(path.getParentFile());
        }else{
            return null;
        }
    }
    /**
     * Gets a list of sibling node files for the file path file or folder
     * @see #getBrotherNode(File)
     * @param   Folders on the phone
     * @return path The information of the file list under the folder is stored in the Map. The list of Map key is as follows:
     * 		   FILE_INFO_NAME : String fileName
     * 		   FILE_INFO_ISFOLDER: boolean isFolder
     * 		   FILE_INFO_TYPE: string suffix
     * 		   FILE_INFO_NUM_SONDIRS : int theNumberOfSubfolders
     * 		   FILE_INFO_NUM_SONFILES: int theNumberOfSubfiles
     * 		   FILE_INFO_PATH : String the absolute path of the file
     **/
    public List<Map<String, Object>> getBrotherNode(String pathStr){
        File path=new File(pathStr);
        return getBrotherNode(path);
    }

    /**
     * Gets the parent path of a file or folder
     * @param  path file or folder
     * @return String path super path
     **/
    public String getParentPath(File path){
        if(path.getParentFile()==null){
            return null;
        }else{
            return path.getParent();
        }
    }
    /**
     * Gets the parent path of a file or file
     * @param  pathStr file or folder path
     * @return String pathStr super path
     **/
    public String getParentPath(String pathStr){
        File path=new File(pathStr);
        if(path.getParentFile()==null){
            return null;
        }else{
            return path.getParent();
        }
    }

    /**
     * Get the absolute path of the sd card
     * @return String If the sd card exists, return the absolute path of the sd card, otherwise return null
     **/
    public String getSDPath(){
        String sdcard=Environment.getExternalStorageState();
        if(sdcard.equals(Environment.MEDIA_MOUNTED)){
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }else{
            return null;
        }
    }

    /**
     * Get a basic path that general applications can use to store application data
     * @return String If the SD card exists, return the absolute path of the SD card, and if the SD card does not exist, return the absolute path of the Android data directory
     **/
    public String getBasePath(){
        String basePath=getSDPath();
        if(basePath==null){
            return Environment.getDataDirectory().getAbsolutePath();
        }else{
            return basePath;
        }
    }

    /**
     * get the size of the file path
     * @return String path size
    public String getFileSize(File path) throws IOException{
        if(path.exists()){
            DecimalFormat df = new DecimalFormat("#.00");
            String sizeStr="";
            FileInputStream fis=new FileInputStream(path);
            long size=fis.available();
            fis.close();
            if(size<1024){
                sizeStr=size+"B";
            }else if(size<1048576){
                sizeStr=df.format(size/(double)1024)+"KB";
            }else if(size<1073741824){
                sizeStr=df.format(size/(double)1048576)+"MB";
            }else{
                sizeStr=df.format(size/(double)1073741824)+"GB";
            }
            return sizeStr;
        }else{
            return null;
        }
    }

    /**
     * get the size of the file fpath
     * @return String path size
     **/
    public String getFileSize(String fpath){
        File path=new File(fpath);
        if(path.exists()){
            DecimalFormat df = new DecimalFormat("#.00");
            String sizeStr="";
            long size=0;
            try {
                FileInputStream fis = new FileInputStream(path);
                size=fis.available();
                fis.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "Unknown size";
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "Unknown size";
            }
            if(size<1024){
                sizeStr=size+"B";
            }else if(size<1048576){
                sizeStr=df.format(size/(double)1024)+"KB";
            }else if(size<1073741824){
                sizeStr=df.format(size/(double)1048576)+"MB";
            }else{
                sizeStr=df.format(size/(double)1073741824)+"GB";
            }
            return sizeStr;
        }else{
            return "Unknown size";
        }
    }

    /**
     * Gets the type of file fileName based on the suffix
     * @return String the type of the file
     **/
    public String getFileType(String fileName){
        if(fileName!=""&&fileName.length()>3){
            int dot=fileName.lastIndexOf(".");
            if(dot>0){
                return fileName.substring(dot+1);
            }else{
                return "";
            }
        }
        return "";
    }

    public Comparator<Map<String, Object>> defaultOrder() {

        final String orderBy0=FILE_INFO_ISFOLDER;
        final String orderBy1=FILE_INFO_TYPE;
        final String orderBy2=FILE_INFO_NAME;

        Comparator<Map<String, Object>> order=new Comparator<Map<String,Object>>() {

            @Override
            public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
                // TODO Auto-generated method stub
                int left0=lhs.get(orderBy0).equals(true)?0:1;
                int right0=rhs.get(orderBy0).equals(true)?0:1;
                if(left0==right0){
                    String left1=lhs.get(orderBy1).toString();
                    String right1=rhs.get(orderBy1).toString();
                    if(left1.compareTo(right1)==0){
                        String left2=lhs.get(orderBy2).toString();
                        String right2=rhs.get(orderBy2).toString();
                        return left2.compareTo(right2);
                    }else{
                        return left1.compareTo(right1);
                    }
                }else{
                    return left0-right0;
                }
            }
        };

        return order;
    }

}
