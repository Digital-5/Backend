package com.digital5.controller;

import com.digital5.data.models.SendMessageModel;
import com.digital5.entity.MessageEntity;
import com.digital5.repository.MessageRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody SendMessageModel sendMessageModel) {
        UUID uuid = UUID.randomUUID();
        MessageEntity messageEntity = new MessageEntity(
            uuid.toString(),
            "test",
            sendMessageModel.getRecipient(),
            sendMessageModel.getData(),
            Timestamp.from(Instant.now())
        );
        messageRepository.save(messageEntity);
        return ResponseEntity.ok(uuid.toString());
    }

    @PostMapping("/get")
    public void getMessages() {

    }
}
