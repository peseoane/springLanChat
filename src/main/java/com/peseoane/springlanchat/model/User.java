package com.peseoane.springlanchat.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@Entity
@Table(name = "users", schema = "public", indexes = {
        @Index(name = "users_username_key", columnList = "username", unique = true)
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "avatar", nullable = false, columnDefinition = "varchar(255) not null default 'avatar/default.svg'")
    private String avatar = "avatar/default.png";

    @Column(name = "pin", nullable = false, length = 4)
    private String pin;

    @OneToMany(mappedBy = "sender",fetch = FetchType.EAGER)
    private Set<Message> messages = new LinkedHashSet<>();

}