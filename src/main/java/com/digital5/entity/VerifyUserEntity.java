package com.digital5.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

//this will be sent to the db dedicated for the user waitlist
@Getter
@Setter
@Entity
@Table(name="waitlist_entries")
public class VerifyUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String uuid;

    @Column(nullable = false)
    String full_name;
    @Column(nullable = false,unique = true)
    String publickey;

    long date;
}
