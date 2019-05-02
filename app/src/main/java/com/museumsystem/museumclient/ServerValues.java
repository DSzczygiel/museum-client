package com.museumsystem.museumclient;

public final class ServerValues {
    public static final String SERVER_PROTOCOL = "https://";
    public static final String SERVER_IP = "145.239.93.41";
    public static final String SERVER_PORT = "8443";
    public static final String SERVER_URL =  SERVER_PROTOCOL + SERVER_IP + ":" + SERVER_PORT;
    public static final String SERVER_REGISTER_URL = SERVER_URL + "/user/add";
    public static final String SERVER_LOGIN_URL = SERVER_URL + "/oauth/token";
    public static final String SERVER_ACTIVATE_ACCOUNT_URL = SERVER_URL + "/account/activate";
    public static final String SERVER_TICKET_PURCHASE_URL = SERVER_URL + "/customer/ticket";
    public static final String SERVER_GET_USER_TICKETS_URL = SERVER_URL + "/customer/ticket/user/";
    public static final String SERVER_MAKE_PAYMENT = SERVER_URL + "/payment/new";
    public static final String SERVER_ARTWORK = SERVER_URL + "/artwork";
    public static final String SERVER_ARTIST = SERVER_URL + "/artist";
    public static final String SERVER_CUSTOMER = SERVER_URL + "/customer/";
    public static final String SERVER_NEWS = SERVER_URL + "/news/";
    public static final  String SERVER_VALIDATE_TICKET = SERVER_URL + "/employee/ticket/validate/";
    //TODO secure password storage
    public static final String CLIENT_USERNAME = "museum-client-app";
    public static final String CLIENT_PASSWORD = "secret";

    public static final String TOKEN_PREFS = "tokens";
}
