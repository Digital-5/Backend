package com.digital5.controller;

import com.digital5.models.JWTModel;
import com.digital5.models.PublishKeysModel;
import com.digital5.models.SendMessageModel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pqxdh")
public class MessageController {

    @PostMapping("/publish_keys")
    public void publishKeys(@RequestBody PublishKeysModel publishKeysModel) {

    }

    @PostMapping("/send_message")
    public void sendMessage(@RequestBody SendMessageModel sendMessageModel) {

    }

    @PostMapping("/get_messages")
    public void getMessages(@RequestBody JWTModel jwtModel) {

    }

    @PostMapping("/get_keys")
    public void getKeys(@RequestBody JWTModel jwtModel, @RequestParam String username) {

    }

}
