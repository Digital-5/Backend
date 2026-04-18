package com.digital5.service;

import com.digital5.entity.AccountEntity;
import com.digital5.exception.DigitalException;
import com.digital5.logger.LogLevel;
import com.digital5.logger.Logger;
import com.digital5.data.models.RegisterModel;
import com.digital5.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;
    private JWTService jwtService;

    public String registerNewUser(RegisterModel registerModel) throws DigitalException {

        if (!registerModel.getUsername().toLowerCase().matches("^[a-z0-9]{4,30}$")) {
            throw new DigitalException(HttpStatus.BAD_REQUEST, "Invalid Username, should be only alphanumeric between 4-30 characters.");
        }

        //TODO: Verify all signatures!
        //TODO: verify if the keys are not already in use

        UUID uuid = UUID.randomUUID();
        try{
            AccountEntity User = new AccountEntity(
                uuid.toString(),
                registerModel.getUsername(),
                (short) 0,
                System.currentTimeMillis()
            );
            accountRepository.save(User);
            return uuid.toString();
        } catch (Exception e) {
            Logger.logError(e);
            throw new DigitalException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not register new account.");
        }
    }

    public AccountEntity authenticateUser(String jwt) {
        JsonNode userNode = jwtService.verifyJWT(jwt);
        String uuid = userNode.get("uuid").toString();
        return getUserFromUUID(uuid);
    }

    public AccountEntity getUserFromUUID(String uuid) {
        AccountEntity account =  accountRepository.findById(uuid).orElse(null);
        if (account == null) {
            Logger.log(LogLevel.WARN, "User with UUID: " + uuid + " was requested, but not found.");
        }
        return account;
    }
}
