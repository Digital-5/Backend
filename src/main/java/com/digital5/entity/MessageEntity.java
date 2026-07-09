package com.digital5.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity(name="messages")
@Table(name="messages")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class MessageEntity {

    @Id
    @Column(name="id", nullable = false, unique = true)
    private String id;
    @Column(name="sender", nullable = false)
    private String sender;
    @Column(name="recipient", nullable = false)
    private String recipient;
    @Column(name="data", nullable = false)
    private String data;
    @Column(name="timestamp", nullable = false)
    private Timestamp timestamp;
}
