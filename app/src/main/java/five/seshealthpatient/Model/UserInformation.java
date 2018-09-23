package five.seshealthpatient.Model;

/**
 * Created by Jinghao Zhang on 31 Aug 2018.
 */

public class UserInformation {

    private String name;
    private String email;
    private String age;
    private String birthday;
    private String height;
    private String weight;
    private String condition;
    private String group;
    private boolean gender;
    private String UID;

    public UserInformation() {
    }

    public UserInformation(String name, String email, String age, String birthday, String height, String weight, String condition, String group, boolean gender, String UID) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.birthday = birthday;
        this.height = height;
        this.weight = weight;
        this.condition = condition;
        this.group = group;
        this.gender = gender;
        this.UID = UID;
    }

    public static UserInformation newInstance() {
        return newInstance();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }
}