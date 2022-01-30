package com.bobocode.entity;

import com.bobocode.bibernateorm.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by Shelupets Denys on 05.12.2021.
 */

@Table(name = "person")
@Getter
@Setter
@ToString
public class Person {
    @Id
    @GeneratedValue(type = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;

}
