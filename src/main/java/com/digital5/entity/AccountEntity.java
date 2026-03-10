package com.digital5.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name="account")
@Table(name="account")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String uuid;
    @Column(nullable = false)
    String full_name;
    @Column(nullable = false,unique = true)
    String publickey;

    long date;
}
