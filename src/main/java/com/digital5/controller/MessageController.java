package com.digital5.controller;

import com.digital5.data.models.JWTModel;
import com.digital5.data.models.SendMessageModel;
import com.digital5.entity.AccountEntity;
import com.digital5.service.JWTService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/messages")
public class MessageController {


    @PostMapping("/send")
    public void sendMessage(@RequestBody SendMessageModel sendMessageModel) {
//Xed25519 wird angenommen
    }

    @PostMapping("/get")
    public void getMessages(@RequestBody JWTModel jwtModel) {
    }
}
