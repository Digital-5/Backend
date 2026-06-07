package com.digital5.controller;

import com.digital5.data.models.OneTimeKeyModel;
import com.digital5.entity.AccountEntity;
import com.digital5.exception.DigitalException;
import com.digital5.data.models.RegisterModel;
import com.digital5.service.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        AccountEntity account = (AccountEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (account != null) {
            return ResponseEntity.ok(String.valueOf(account.getStatus()));
        }else {
            throw new DigitalException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }

    @PostMapping("/add_onetimes")
    public ResponseEntity<String> addOneTimes(@RequestBody OneTimeKeyModel oneTimeKeyModel) throws DigitalException {
        AccountEntity account = (AccountEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (account != null) {
            String responseUUID = accountService.addNewOneTimeKeys(account, oneTimeKeyModel);
            return ResponseEntity.ok(responseUUID);
        }else {
            throw new DigitalException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }
}
