package com.ugent.pidgeon.postgre.models;


import com.ugent.pidgeon.postgre.models.types.UserRole;
import jakarta.persistence.*;

import java.sql.Timestamp;

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
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "microsoft_token")
    private String microsoftToken;

    @Column(name = "created_at")
    private Timestamp createdAt;

    public UserEntity(String name, String surname, String email, UserRole role, String microsoftToken) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.role = role;
        this.microsoftToken = microsoftToken;
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
        return role;
    }

    public String getMicrosoftToken() {
        return microsoftToken;
    }

    public void setMicrosoftToken(String microsoftToken) {
        this.microsoftToken = microsoftToken;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

