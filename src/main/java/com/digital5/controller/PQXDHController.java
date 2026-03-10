package com.digital5.controller;

import com.digital5.models.JWTModel;
import com.digital5.models.PublishKeysModel;
import com.digital5.models.SendMessageModel;
import com.digital5.service.PublicKeyService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/pqxdh")
public class PQXDHController {

    private final PublicKeyService publicKeyService;

    @PostMapping("/publish_keys")
    public void publishKeys(@RequestBody PublishKeysModel publishKeysModel) {
        publicKeyService.registerPublicKeys(publishKeysModel);
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
