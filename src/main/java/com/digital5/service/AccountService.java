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
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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

    public String registerNewUser(@NonNull RegisterModel registerModel) throws DigitalException {

        if (!registerModel.getUsername().toLowerCase().matches("^[a-z0-9]{4,30}$")) {
            throw new DigitalException(HttpStatus.BAD_REQUEST, "Invalid Username, should be only alphanumeric between 4-30 characters.");
        }

        UUID uuid = UUID.randomUUID();
        try{
            AccountEntity user = new AccountEntity(
                uuid.toString(),
                registerModel.getUsername(),
                    AccountStatus.UNVERIFIED.toShort(),
                System.currentTimeMillis()
            );
            //todo check if this is really needed bc it wasnt in my branch
            publicKeyService.registerPublicKeys(registerModel, uuid);
          
            accountRepository.save(user);
            return uuid.toString();
        } catch (Exception e) {
            Logger.logError(e);
            throw new DigitalException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not register new account.");
        }
    }

    public AccountEntity authenticateUser(String jwt) throws DigitalException {
        String uuid = jwtService.verifyJWT(jwt);
        return getUserFromUUID(uuid);
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
