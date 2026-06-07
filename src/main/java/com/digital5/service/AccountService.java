package com.digital5.service;

import com.digital5.data.AccountStatus;
import com.digital5.data.models.OneTimeKeyModel;
import com.digital5.entity.AccountEntity;
import com.digital5.entity.OneTimesEntity;
import com.digital5.exception.DigitalException;
import com.digital5.logger.LogLevel;
import com.digital5.logger.Logger;
import com.digital5.data.models.RegisterModel;
import com.digital5.repository.AccountRepository;
import com.digital5.repository.OneTimeKeyRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private OneTimeKeyRepository oneTimeKeyRepository;

    private PublicKeyService publicKeyService;

    @Transactional
    public String registerNewUser(RegisterModel registerModel) throws DigitalException {

        if (!registerModel.getUsername().toLowerCase().matches("^[a-z0-9]{4,30}$")) {
            throw new DigitalException(HttpStatus.BAD_REQUEST, "Invalid Username, should be only alphanumeric between 4-30 characters.");
        }

        //TODO: Verify all signatures!
        //TODO: verify if the keys are not already in use

        String uuid = UUID.randomUUID().toString();
        try{
            AccountEntity User = new AccountEntity(
                uuid,
                registerModel.getUsername(),
                    AccountStatus.UNVERIFIED.toShort(),
                System.currentTimeMillis()
            );
            accountRepository.save(User);
            publicKeyService.registerPublicKeys(registerModel, uuid);
            return uuid;
        } catch (Exception e) {
            Logger.logError(e);
            throw new DigitalException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not register new account.");
        }
    }

    public AccountEntity getUserFromUUID(String uuid) {
        AccountEntity account =  accountRepository.findById(uuid).orElse(null);
        if (account == null) {
            Logger.log(LogLevel.WARN, "User with UUID: " + uuid + " was requested, but not found.");
        }
        return account;
    }

    @Transactional
    public String addNewOneTimeKeys(AccountEntity account, OneTimeKeyModel oneTimeKeyModel) throws DigitalException {
        if (publicKeyService.verifySignature(account, oneTimeKeyModel.getOneTimeKemKey(), oneTimeKeyModel.getOneTimeKemSignature())) {
            UUID uuid = UUID.randomUUID();
            OneTimesEntity oneTimes = new OneTimesEntity(
                uuid.toString(),
                account.getUuid(),
                oneTimeKeyModel.getOneTimeCurveKey(),
                oneTimeKeyModel.getOneTimeKemKey(),
                oneTimeKeyModel.getOneTimeKemSignature()
            );
            oneTimeKeyRepository.save(oneTimes);
            return uuid.toString();
        }else {
            throw new DigitalException(HttpStatus.BAD_REQUEST, "Invalid signature for one time key.");
        }
    }
}
