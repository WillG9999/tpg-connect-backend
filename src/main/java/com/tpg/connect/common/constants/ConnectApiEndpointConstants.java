package com.tpg.connect.common.constants;

public class ConnectApiEndpointConstants {

    public ConnectApiEndpointConstants() {
        throw new UnsupportedOperationException("This is a utility class, instantiation is not possible");
    }

    public static final String REGISTER_USER_ENDPOINT = "/v1/auth/register" ;
    public static final String SEND_VERIFICATION_ENDPOINT = "/v1/auth/send-verification-code" ;
    public static final String VERIFY_EMAIL_CODE_ENDPOINT = "/v1/auth/verify-email-code" ;
    public static final String LOGIN_ENDPOINT = "/v1/auth/login" ;
    public static final String LOGOUT_ENDPOINT = "/v1/auth/logout" ;
    public static final String REFRESH_TOKEN_ENDPOINT = "/v1/auth/refresh" ;
    public static final String APPLICATION_SUBMIT_ENDPOINT = "/v1/application/submit" ;
    public static final String APPLICATION_STATUS_ENDPOINT = "/v1/application/status" ;
    public static final String FORGOT_PASSWORD_ENDPOINT = "/v1/auth/forgot-password" ;
    public static final String RESET_PASSWORD_ENDPOINT = "/v1/auth/reset-password" ;
    public static final String VERIFY_RESET_TOKEN_ENDPOINT = "/v1/auth/verify-reset-token" ;
    public static final String CHANGE_PASSWORD_ENDPOINT = "/v1/auth/change-password" ;

    public static final String ADMIN_APPLICATIONS_ALL = "/v1/admin/applications";
    public static final String ADMIN_APPLICATIONS_PENDING = "/v1/admin/applications/pending";
    public static final String ADMIN_APPLICATIONS_BY_STATUS = "/v1/admin/applications/status/{status}";
    public static final String ADMIN_APPLICATIONS_BY_ID = "/v1/admin/applications/{applicationId}";
    public static final String ADMIN_APPLICATIONS_APPROVE = "/v1/admin/applications/{applicationId}/approve";
    public static final String ADMIN_APPLICATIONS_REJECT = "/v1/admin/applications/{applicationId}/reject";

    public static final String ADMIN_USERS_ALL = "/v1/admin/users";
    public static final String ADMIN_USER_BY_ID = "/v1/admin/users/{connectId}";
    public static final String ADMIN_DEMOGRAPHICS_STATS = "/v1/admin/demographics/stats";

    public static final String PROFILE_GET_ENDPOINT = "/v1/profile";
    public static final String PROFILE_UPDATE_ENDPOINT = "/v1/profile";
    public static final String PROFILE_PHOTO_ADD = "/v1/profile/photos";
    public static final String PROFILE_PHOTO_REMOVE = "/v1/profile/photos";

}
