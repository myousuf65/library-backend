package com.StudentLibrary.Studentlibrary.Model;

import javax.persistence.*;

@Entity(name = "notifications")
public class Notifications {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long Id;

    @ManyToOne
    @JoinColumn
    private Student student;

    String message;

    Boolean read;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    @Override
    public String toString() {
        return "Notifications{" +
                "Id=" + Id +
                ", student=" + student +
                ", message='" + message + '\'' +
                ", read=" + read +
                '}';
    }
}
