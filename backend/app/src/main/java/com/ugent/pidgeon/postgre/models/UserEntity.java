package com.ugent.pidgeon.postgre.models;


import com.ugent.pidgeon.postgre.models.types.UserRole;
import jakarta.persistence.*;

import java.time.OffsetDateTime;


@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private long id;

    @Column(name = "name", nullable=false)
    private String name;

    @Column(name = "surname", nullable=false)
    private String surname;

    @Column(name = "email", nullable=false)
    private String email;

    @Column(name = "role")
    private String role;

    @Column(name = "azure_id")
    private String azureId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "studentnumber")
    private String studentNumber;

    public UserEntity(String name, String surname, String email, UserRole role, String azureId,
        String studentNumber) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.role = role.toString();
        this.azureId = azureId;
        this.studentNumber = studentNumber;
    }

    public UserEntity() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return switch (role) {
            case "student" -> UserRole.student;
            case "teacher" -> UserRole.teacher;
            case "admin" -> UserRole.admin;
            default -> throw new IllegalStateException("Unexpected value: " + role);
        };
    }

    public void setRole(UserRole role) {
        this.role = role.toString();
    }

    public String getAzureId() {
        return azureId;
    }


    public void setAzureId(String azureId) {
        this.azureId = azureId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }
}

