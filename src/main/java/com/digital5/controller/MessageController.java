package com.digital5.controller;

import com.digital5.data.models.SendMessageModel;
import com.digital5.exception.DigitalException;
import com.digital5.repository.MessageRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody SendMessageModel sendMessageModel) throws DigitalException {
        throw new DigitalException(HttpStatus.NOT_IMPLEMENTED, "Not implemented yet.");
    }

    @PostMapping("/get")
    public void getMessages() throws DigitalException {
        throw new DigitalException(HttpStatus.NOT_IMPLEMENTED, "Not implemented yet.");
    }
}
