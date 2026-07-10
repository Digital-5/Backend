package com.digital5.data.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RegisterModel {

    @NotNull
    @NotBlank
    private String username;

    @NotNull
    @NotBlank
    private String identityKey;

    @NotNull
    @NotBlank
    private String preKey;

    @NotNull
    @NotBlank
    private String preKeySignature;

    @NotNull
    @NotBlank
    private String kemKey;

    @NotNull
    @NotBlank
    private String keyKemSignature;

}
