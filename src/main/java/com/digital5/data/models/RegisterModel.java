package com.digital5.data.models;

import lombok.Getter;

@Getter
public class RegisterModel {

    private String username;
    private String identityKey;
    private String preKey;
    private String preKeySignature;
    private String kemKey;
    private String keyKemSignature;

}
