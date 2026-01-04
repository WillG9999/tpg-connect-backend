package com.tpg.connect.common.constants;

public class ConnectApiEndpointConstants {

    public ConnectApiEndpointConstants() {
        throw new UnsupportedOperationException("This is a utility class, instantiation is not possible");
    }

    public static final String REGISTER_USER_ENDPOINT = "/v1/auth/register" ;
    public static final String SEND_VERIFICATION_ENDPOINT = "/v1/auth/send-verification-code" ;
    public static final String VERIFY_EMAIL_CODE_ENDPOINT = "/v1/auth/verify-email-code" ;
    public static final String LOGIN_ENDPOINT = "/v1/auth/login" ;




}
