package com.digital5.service;

import com.digital5.models.RegisterModel;
import org.springframework.stereotype.Service;

@Service
public class PublicKeyService {

    public void registerPublicKeys(RegisterModel publishKeysModel) {

    }

    public boolean verifySignature(String uuid, String key, String signature){
        //ratelimit
        return true; //wenn passt
        //return false; //wenn nicht passt
    }
    //todo registerpublickey
}
