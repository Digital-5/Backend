package com.digital5.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name="messages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageEntity {

    @Id
    @Column(name="id", nullable = false)
    private String Id;
    @Column(name="sender", nullable = false)
    private String sender;
    @Column(name="recipient", nullable = false)
    private String recipient;
    @Column(name="data", nullable = false)
    private String data;
    @Column(name="rachetparams", nullable = false)
    private String rachetparams;

}
