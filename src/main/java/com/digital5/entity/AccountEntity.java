package com.digital5.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;

@Entity(name="account")
@Table(name="account")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class AccountEntity {

    @Id
    @Column( name = "uuid", nullable = false, unique = true)
    String uuid;
    @Column(name = "username", nullable = false, unique = true)
    String username;
    @Column(name = "identity_key", nullable = false,unique = true)
    String identityKey;
    @Column(name = "created_at", nullable = false)
    long createdAt;
}
