package com.digital5.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity(name="keys")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class PublicKeysEntity {

    @Id
    @Column(name="username", unique = true, nullable = false)
    private String username;
    @Column(name="identity_key", nullable = false)
    private String identityKey;
    @Column(name="prekey", nullable = false)
    private String preKey;
    @Column(name="prekey_signature", nullable = false)
    private String preKeySignature;
    @Column(name="last_resort_kemkey", nullable = false)
    private String kemKey;
    @Column(name="kemkey_signature", nullable = false)
    private String kemKeySignature;

}
