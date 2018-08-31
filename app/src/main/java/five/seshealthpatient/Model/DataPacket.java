package five.seshealthpatient.Model;

/**
 * Created by Jinghao Zhang in 31st August, 2018
 */

public class DataPacket {
    private String text;
    private String gps;
    private String file;
    private String heartrate;

    public DataPacket() {

    }

    public static DataPacket newInstance() {
        return newInstance();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getGps() {
        return gps;
    }

    public void setGps(String gps) {
        this.gps = gps;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getHeartrate() {
        return heartrate;
    }

    public void setHeartrate(String heartrate) {
        this.heartrate = heartrate;
    }
}
