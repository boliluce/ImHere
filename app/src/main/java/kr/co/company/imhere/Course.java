package kr.co.company.imhere;

/**
 * Created by 경란 on 2017-10-17.
 */

public class Course {

    String courseTitle;
    String courseDay;
    String courseStart;
    String courseEnd;
    String courseID;
    String secID;
    String semester;
    String year;

    public Course(String courseTitle, String courseDay, String courseStart, String courseEnd, String courseID, String secID, String semester, String year) {
        this.courseTitle = courseTitle;
        this.courseDay = courseDay;
        this.courseStart = courseStart;
        this.courseEnd = courseEnd;
        this.courseID = courseID;
        this.secID = secID;
        this.semester = semester;
        this.year = year;
    }


    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getCourseDay() {
        return courseDay;
    }

    public void setCourseDay(String courseDay) {
        this.courseDay = courseDay;
    }

    public String getCourseStart() {
        return courseStart;
    }

    public void setCourseStart(String courseStart) {
        this.courseStart = courseStart;
    }

    public String getCourseEnd() {
        return courseEnd;
    }

    public void setCourseEnd(String courseEnd) {
        this.courseEnd = courseEnd;
    }

    public String getCourseID() { return courseID; }

    public void setCourseID(String courseID) { this.courseID = courseID; }

    public String getSecID() { return secID; }

    public void setSecID(String secID) { this.secID = secID; }

    public String getSemester() { return semester; }

    public void setSemester(String semester) { this.semester = semester; }

    public String getYear() { return year; }

    public void setYear(String year) { this.year = year; }

}
