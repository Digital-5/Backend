package com.digital5.data;

public enum AccountStatus {

    UNVERIFIED,
    VERIFIED,
    DELETED;

    public short toShort() {
        return (short) this.ordinal();
    }

    public static AccountStatus fromShort(short value) {
        for (AccountStatus status : AccountStatus.values()) {
            if (status.ordinal() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown AccountStatus value: " + value);
    }
}
