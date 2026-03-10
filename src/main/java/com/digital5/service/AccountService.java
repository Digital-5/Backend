package com.digital5.service;

import com.digital5.entity.AccountEntity;
import com.digital5.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public long waitlistSize() {
        try{
        return accountRepository.count();
        } catch (Exception e) {
            log.error("Error sizing Database: "+e);
        }
        return -1; //error occurred
    }

    public Boolean publicKeyExists(String publickey) {
        try{
            return accountRepository.findAll().contains(publickey);
        } catch (Exception e) {
            log.error("Error while searching Database: "+e);
        }
        return null;
    }

    public String saveUser(AccountEntity User) {
        try{
            accountRepository.save(User);
            return User.getUuid();
        } catch (Exception e) {
            log.error("Error while saving User in Database: "+e);
        }
        return null;
    }
}
