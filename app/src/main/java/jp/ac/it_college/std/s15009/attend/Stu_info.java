package jp.ac.it_college.std.s15009.attend;

/**
 * Created by s15009 on 17/02/15.
 */

public class Stu_info {
    private String num_stu;
    private String name_stu;
    private String attend_time;
    private String back_sc_time;

    public String getBack_sc_time() {
        return back_sc_time;
    }

    public void setBack_sc_time(String back_sc_time) {
        this.back_sc_time = back_sc_time;
    }

    public String getAttend_time() {
        return attend_time;
    }

    public void setAttend_time(String attend_time) {
        this.attend_time = attend_time;
    }

    public String getName_stu() {
        return name_stu;
    }

    public void setName_stu(String name_stu) {
        this.name_stu = name_stu;
    }

    public String getNum_stu() {
        return num_stu;
    }

    public void setNum_stu(String num_stu) {
        this.num_stu = num_stu;
    }

}
