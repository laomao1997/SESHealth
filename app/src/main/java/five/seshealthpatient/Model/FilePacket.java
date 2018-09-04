package five.seshealthpatient.Model;

public class FilePacket {

    String fileName;
    String link;
    String date;

    public FilePacket() {
    }

    public FilePacket(String fileName, String suffixName, String link, String date) {
        this.fileName = fileName;
        this.link = link;
        this.date = date;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSuffixName() {
        return fileName.substring(fileName.lastIndexOf(".")+1);
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean hasFileName(String name) {
        return fileName.equals(name);
    }

    @Override
    public String toString() {
        return getFileName() + getDate() + getLink();
    }
}
