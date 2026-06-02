package com.digital5.controller;

import com.digital5.entity.AccountEntity;
import com.digital5.exception.DigitalException;
import com.digital5.data.models.RegisterModel;
import com.digital5.service.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/account")
public class AccountController {

    private AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<String> requestAccess(@RequestBody RegisterModel registerModel) throws DigitalException {
        String uuid = accountService.registerNewUser(registerModel);
        return ResponseEntity.ok(uuid);
    }

    @GetMapping("/status")
    public ResponseEntity<String> viewStatus() throws DigitalException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthorized");
        }

        AccountEntity account = (AccountEntity) auth.getPrincipal();

        return ResponseEntity.ok(String.valueOf(account.getStatus()));
    }

    @GetMapping("/add_onetimes")
    public ResponseEntity<String> addOneTimes() throws DigitalException {
        return null;
    }
}
