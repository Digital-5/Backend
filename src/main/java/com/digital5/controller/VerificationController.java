package com.digital5.controller;

import com.digital5.models.VerificationModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.digital5.service.StringService.cleanString;
import static com.digital5.service.StringService.sizeLimitString;

@RestController
@RequestMapping("/verification")
public class VerificationController {


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
