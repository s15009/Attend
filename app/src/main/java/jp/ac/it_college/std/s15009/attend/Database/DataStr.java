package jp.ac.it_college.std.s15009.attend.Database;

/**
 * データベースにinsertするためのクラス
 */

public class DataStr {
    private String Student_id;
    private String Student_name;
    private String Card_id;

    public String getStudent_id() {
        return Student_id;
    }

    public void setStudent_id(String student_id) {
        Student_id = student_id;
    }

    public String getStudent_name() {
        return Student_name;
    }

    public void setStudent_name(String student_name) {
        Student_name = student_name;
    }

    public String getCard_id() {
        return Card_id;
    }

    public void setCard_id(String card_id) {
        Card_id = card_id;
    }
}
