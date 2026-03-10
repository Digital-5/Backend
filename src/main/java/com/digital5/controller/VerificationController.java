package com.digital5.controller;

import com.digital5.entity.VerifyUserEntity;
import com.digital5.models.VerificationModel;
import com.digital5.service.StringService;
import com.digital5.service.WaitlistDbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.digital5.service.StringService.cleanString;
import static com.digital5.service.StringService.sizeLimitString;

@Slf4j
@RestController
@RequestMapping("/verification")
public class VerificationController {

    //todo
    // for details check .drawio file

    private static final short MAX_WAIT_LIST_SIZE = 50; //max number of users in waitlist (db)
    private long lastTimeCachedDbSize = System.currentTimeMillis();
    @Autowired
    private WaitlistDbService waitlistDbService;
    private long cachedDbSize = waitlistDbService.waitlistSize();


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
        ); //no special chars and size limit to 30 chars

        String publicKey = sizeLimitString(cleanString(StringService.hexToString(

                        verificationModel.getPublicKey()
                )
            ), 500
        ); //convert key to hex, then remove special chars and at last limit the size of the String to 500 chars


        long time = System.currentTimeMillis();

        if (cachedDbSize > MAX_WAIT_LIST_SIZE && time - lastTimeCachedDbSize > (1000 * 60 * 10)) {
            //if cached dbsize too big and the last refresh was more than 10min ago
            cachedDbSize = waitlistDbService.waitlistSize();
            lastTimeCachedDbSize = time;
        }


        if (!waitlistDbService.publicKeyExists(publicKey) && cachedDbSize < MAX_WAIT_LIST_SIZE) {

            VerifyUserEntity user = new VerifyUserEntity();

            user.setFull_name(username);
            user.setPublickey(publicKey);
            user.setDate(time);
            //and the uuid gets generated before its saved
            String uuid = waitlistDbService.saveUser(user); //temporary var to return the user his new uuid
            if (uuid != null) {
                return "200 " + uuid;
            }
        } else {
            log.warn("Failed to add User to the waitlist, check if he didnt already request access and if the number Users in the waitlist didnt reach the maximum");
        }
        return "500 Internal Server Error \n Please try again later";
    }

    //todo
    // api to view the acceptance status (ratelimited to once per minute per person(verify via privatekey signature and uuid))
    @PostMapping("viewStatus")
    public String viewStatus(@RequestBody x){
        return "0";
    }
}

