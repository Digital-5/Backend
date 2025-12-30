package com.digital5.controller;

import com.digital5.models.VerificationModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.digital5.service.StringService.cleanString;
import static com.digital5.service.StringService.sizeLimitString;

@RestController
@RequestMapping("/verification")
public class VerificationController {

    //todo
    // the message sent to us should be encrypted using the servers publickey (must be manually typed in)
    // AND signed using the users privatekey(->verify)
    // and from us encrypted via the users publickey(see db) and signed by the servers privatekey (->verify)

    //todo
    // api to view the acceptance status (ratelimited to once per minute per person(verify via privatekey signature))

    //todo
    // verification needs the following parameters before saving into the db:
    // -uuid randomly generated in the backend and sent back if accepted,
    // -name of the user(will not be saved later on)
    // -identity publickey that the user generated
    // -the date when the user requested access in ms

    final short MAX_WAIT_LIST_SIZE = 50; //max number of users in waitlist (db)
    long timeCachedDbSize = 0L; //ist null damit die db size beim ersten mal geholt wird
    int cachedDbSize = 0; //^^

    @PostMapping("request_access")
    public void requestAccess(@RequestBody VerificationModel verificationModel) {

        String username = sizeLimitString( cleanString(
                verificationModel.getFullname()
        ), 30
        ); //no special chars and size limit to 30 chars

        String publicKey = sizeLimitString( cleanString(
                verificationModel.getPublicKey() //Hexadecimal encoded public key
        ), 500
        ); //no special chars and sizelimit to 500 chars

        long time = System.currentTimeMillis();

        String uuid = String.valueOf(UUID.randomUUID());

        /** todo

         if time - timeCachedDbSize > 1 hour:
            cachedDbSize = get waitlist size from db
         timeCachedDbSize = time

         if public key doesnt already exists && db.size < limit:
            save to db with timestamp
         else
            log warning do nothing
         **/
    }
}
