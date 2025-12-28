package com.walhay.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "users", schema = "library")
@Getter
@Setter
@ToString
public class User {

    @Id
    @Column(name = "login", nullable = false)
    private String login;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @ManyToMany(mappedBy = "users")
    @ToString.Exclude
    private Set<Game> games;
}
