package com.digital5.service;

import com.digital5.entity.VerifyUserEntity;
import com.digital5.repository.VerifyUserRepository;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WaitlistDbService {

    @Autowired
    private VerifyUserRepository verifyUserRepository;

    public long waitlistSize() {
        try{
        return verifyUserRepository.count();
        } catch (Exception e) {
            log.error("Error sizing Database: "+e);
        }
        return -1; //error occurred
    }

    public Boolean publicKeyExists(String publickey) {

        try{
            return verifyUserRepository.findAll().contains(publickey);
        } catch (Exception e) {
            log.error("Error while searching Database: "+e);
        }
        return null;
    }

    public String saveUser(VerifyUserEntity User) {
        try{
            verifyUserRepository.save(User);
            return User.getUuid();
        } catch (Exception e) {
            log.error("Error while saving User in Database: "+e);
        }
        return null;
    }
}
