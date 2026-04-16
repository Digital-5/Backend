package com.digital5.controller;

import com.digital5.exception.DigitalException;
import com.digital5.data.models.RegisterModel;
import com.digital5.service.AccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public String requestAccess(@RequestBody RegisterModel registerModel) throws DigitalException {
        accountService.registerNewUser(registerModel);
        return "temp";
    }

    //todo
    // api to view the acceptance status (ratelimited to once per minute per person(verify via privatekey signature and uuid))
    @GetMapping("/status")
    public String viewStatus(@RequestBody RegisterModel registerModel){
        //Prekey=registerModel.getPreKey();
        //publicKeyService.verifySignature(,prekey,);

        return "0";
    }
}
