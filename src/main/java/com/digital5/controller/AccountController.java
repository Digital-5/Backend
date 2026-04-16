package com.digital5.controller;

import com.digital5.exception.DigitalException;
import com.digital5.data.models.RegisterModel;
import com.digital5.service.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/account")
public class AccountController {

    private AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<String> requestAccess(@RequestBody RegisterModel registerModel) throws DigitalException {
        accountService.registerNewUser(registerModel);
        return ResponseEntity.ok("Account registered successfully");
    }

    //todo
    // api to view the acceptance status (verify via jwt)
    @GetMapping("/status")
    public String viewStatus(@RequestBody RegisterModel registerModel){
        //Prekey=registerModel.getPreKey();
        //publicKeyService.verifySignature(,prekey,);

        return "0";
    }
}
