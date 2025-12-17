package com.digital5.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name="onetimes")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class OneTimesEntity {

    @Id
    @Column(name="id",unique = true, nullable = false)
    private String id;
    @Column(name="username", nullable = false)
    private String username;
    @Column(name="onetimecurve", nullable = false)
    private String oneTimeCurve;
    @Column(name="onetimekem", nullable = false)
    private String oneTimeKem;
    @Column(name="kem_signature", nullable = false)
    private String kemSignature;

}
