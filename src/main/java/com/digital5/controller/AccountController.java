package com.digital5.controller;

import com.digital5.data.DataResponse;
import com.digital5.data.models.AuthenticationModel;
import com.digital5.entity.AccountEntity;
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
    public ResponseEntity<String> viewStatus(@RequestBody AuthenticationModel authenticationModel) throws DigitalException {
        String jwt = authenticationModel.getJwt();
        AccountEntity user = accountService.authenticateUser(jwt);
        DataResponse response = new DataResponse();
        response.addData("account_status", String.valueOf(user.getStatus()));
        return ResponseEntity.ok(response.toJsonString());
    }
}
