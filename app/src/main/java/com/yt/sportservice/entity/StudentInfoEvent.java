package com.yt.sportservice.entity;

/**
 * Created by jianqin on 2018/7/27.
 */

public class StudentInfoEvent {
    public String studentName;
    public String studentNumber;
    public String studentGroup;

    public StudentInfoEvent(String studentName, String studentNumber, String studentGroup) {
        this.studentName = studentName;
        this.studentNumber = studentNumber;
        this.studentGroup = studentGroup;
    }

    @Override
    public String toString() {
        return "StudentInfoEvent{" +
                "studentName='" + studentName + '\'' +
                ", studentNumber='" + studentNumber + '\'' +
                ", studentGroup='" + studentGroup + '\'' +
                '}';
    }
}
