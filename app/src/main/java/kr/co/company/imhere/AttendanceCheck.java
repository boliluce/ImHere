package kr.co.company.imhere;


/**
 * Created by user on 2017-10-22.
 */

public class AttendanceCheck {
    String Name;
    String StudentID;
    String Week;
    String OX;

    public AttendanceCheck(String name, String studentID, String week, String ox)
    {
        this.Name = name;
        this.StudentID = studentID;
        this.Week = week;
        this.OX = ox;
    }
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getStudentID() {
        return StudentID;
    }

    public void setStudentID(String StudentID) {
        this.StudentID = StudentID;
    }

    public String getWeek() {
        return Week;
    }

    public void setWeek(String week) {
        this.Week = week;
    }

    public String getOX() { return OX; }

    public void setOX(String ox) {
        this.OX = ox;
    }


}

