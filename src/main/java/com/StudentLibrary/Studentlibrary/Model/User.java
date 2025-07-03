package com.StudentLibrary.Studentlibrary.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private List<String> roles = new ArrayList<>();

    @Column(name = "moodle_id")
    private String moodleId;

    // For student users, link to their student entity
    @OneToOne(mappedBy = "user")
    private Student student;

    // No-args constructor
    public User() {}

    // All-args constructor
    public User(int id, String username, String password, List<String> roles, String moodleId, Student student) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.moodleId = moodleId;
        this.student = student;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public List<String> getRoles() {
        return roles;
    }
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    public String getMoodleId() {
        return moodleId;
    }
    public void setMoodleId(String moodleId) {
        this.moodleId = moodleId;
    }
    public Student getStudent() {
        return student;
    }
    public void setStudent(Student student) {
        this.student = student;
    }
}
