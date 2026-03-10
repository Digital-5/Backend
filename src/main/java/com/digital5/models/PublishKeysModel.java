package com.digital5.models;

import java.util.HashMap;

public class PublishKeysModel {

    private String username;
    private String identityKey;
    private String preKey;
    private String preKeySignature;
    private String kemKey;
    private String keyKemSignature;
    private HashMap<String, String[]> oneTimeKeyPairs;

}
