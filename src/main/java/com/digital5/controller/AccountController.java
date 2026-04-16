package com.digital5.controller;

import com.digital5.exception.DigitalException;
import com.digital5.data.models.RegisterModel;
import com.digital5.service.AccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/account")
public class AccountController {

    //todo
    // for details check .drawio file

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
