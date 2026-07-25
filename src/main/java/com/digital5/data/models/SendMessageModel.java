package com.digital5.data.models;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class SendMessageModel {

    @NonNull String signedString;
    @NonNull String encryptedHeader;
    @NonNull String messageBody;


}
