package com.digital5.service;

import com.digital5.entity.AccountEntity;
import com.digital5.models.RegisterModel;
import com.digital5.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public String registerNewUser(RegisterModel registerModel) {

        if (!registerModel.getUsername().toLowerCase().matches("^[a-z]{4,30}$")) {

        }

        UUID uuid = UUID.randomUUID();
        try{
            AccountEntity User = new AccountEntity(
                uuid.toString(),
                registerModel.getUsername(),
                registerModel.getIdentityKey(),
                System.currentTimeMillis()
            );
            accountRepository.save(User);
            return uuid.toString();
        } catch (Exception e) {
            log.error("Error while saving User in Database: "+e);
        }
        return null;
    }

    public AccountEntity getUserByUUID(String uuid) {
        AccountEntity account =  accountRepository.findById(uuid).orElse(null);
        if (account == null) {
            log.error("User with uuid "+uuid+" not found in database");
        }
        return account;
    }
}
