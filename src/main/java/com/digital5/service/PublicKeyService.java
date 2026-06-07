package com.digital5.service;

import com.digital5.data.models.RegisterModel;
import com.digital5.entity.AccountEntity;
import com.digital5.entity.PublicKeysEntity;
import com.digital5.repository.PublicKeysRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PublicKeyService {

    @Autowired
    private PublicKeysRepository publicKeysRepository;

    @Transactional
    public void registerPublicKeys(RegisterModel registerModel, String uuid) {
        PublicKeysEntity publicKeys = new PublicKeysEntity(
                uuid,
                registerModel.getIdentityKey(),
                registerModel.getPreKey(),
                registerModel.getPreKeySignature(),
                registerModel.getKemKey(),
                registerModel.getKeyKemSignature()
        );
        publicKeysRepository.save(publicKeys);
    }

    public boolean verifySignature(AccountEntity account, String data, String signature){
        //ratelimit
        return true; //wenn passt
        //return false; //wenn nicht passt
    }
}
