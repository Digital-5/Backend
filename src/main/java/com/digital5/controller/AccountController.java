package com.digital5.controller;

import com.digital5.entity.AccountEntity;
import com.digital5.models.RegisterModel;
import com.digital5.service.PublicKeyService;
import com.digital5.service.StringService;
import com.digital5.service.AccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.digital5.service.StringService.cleanString;
import static com.digital5.service.StringService.sizeLimitString;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/account")
public class AccountController {

    //todo
    // for details check .drawio file


    @PostMapping("/publish_keys")
    public void publishKeys(@RequestBody RegisterModel publishKeysModel) {
        //publicKeyService.registerPublicKeys(publishKeysModel);
    }

    private static final short MAX_WAIT_LIST_SIZE = 50; //max number of users in waitlist (db
    @Autowired
    private AccountService accountService;


    // requestAccess sends the following things:
    // -uuid randomly generated in the backend and sent back if accepted,
    // -name of the user(will not be saved later on)
    // -identity publickey that the user generated
    // -the date when the user requested access in ms
    //
    @PostMapping("request_access")
    public String requestAccess(@RequestBody VerificationModel verificationModel) {

        String username = sizeLimitString(cleanString(
                        verificationModel.getFullname()
                ), 30
        ); //no special chars and size limit to 30 chars TODO

        long time = System.currentTimeMillis();

        /*
        if (!accountService.publicKeyExists(publicKey)) {

            AccountEntity user = new AccountEntity();

            user.setFull_name(username);
            user.setPublickey(publicKey);
            user.setDate(time);
            //and the uuid gets generated before its saved
            String uuid = accountService.saveUser(user); //temporary var to return the user his new uuid
            if (uuid != null) {
                return "200 " + uuid;
            }
        } else {
            log.warn("Failed to add User to the waitlist, check if he didnt already request access and if the number Users in the waitlist didnt reach the maximum");
        }
        return "500 Internal Server Error \n Please try again later";
        */
        return "500 Internal Server Error \n Please try again later";
    }

    //todo
    // api to view the acceptance status (ratelimited to once per minute per person(verify via privatekey signature and uuid))
    @PostMapping("viewStatus")
    public String viewStatus(@RequestBody x){
        return "0";
    }
}

