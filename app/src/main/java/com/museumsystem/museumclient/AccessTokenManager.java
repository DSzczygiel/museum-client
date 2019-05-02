package com.museumsystem.museumclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.auth0.android.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.museumsystem.museumclient.dto.TokenResponseDto;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class AccessTokenManager {

    String accesToken;

    /**
     * Get difference berween two dates in minutes
     * @param date1
     * @param date2
     * @return
     */
    private static Long getDateDifference(Date date1, Date date2){
        Long minutes;
        minutes = date2.getTime() - date1.getTime();

        return minutes;
    }

    /**
     * Interface to return the result to the calling method
     */
    public interface Callback{
        void onSuccess(String result);
    }

    /**
     * Check if token is going to expire
     * @param accessToken JWT token
     * @return true if token is expired or will expire in 5 minutes, otherwise false
     */
    public static Boolean isTokenExpired(String accessToken){
        JWT token = new JWT(accessToken);
        Date expirationDate = token.getExpiresAt();
        Date now = new Date();

        Long diff = getDateDifference(now, expirationDate);

        if(diff <= 5)
            return true;
        else
            return false;
    }

    /**
     * Try to obtain new Access token using existing Refresh token
     * @param context Application context
     * @param refreshToken  Refresh token used to obtain new Access token
     * @param callback  Volley callback to read response from outside of this class
     */
    public static void obtainNewAccessToken(Context context, String refreshToken, final Callback callback){
        final HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("grant_type", "refresh_token");
        requestBody.put("client_id", ServerValues.CLIENT_USERNAME);
        requestBody.put("client_password", ServerValues.CLIENT_PASSWORD);
        requestBody.put("refresh_token", refreshToken);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ServerValues.SERVER_LOGIN_URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                ObjectMapper objectMapper = new ObjectMapper();
                TokenResponseDto responseDto;
                try {
                    responseDto = objectMapper.readValue(response, TokenResponseDto.class);
                    String accessToken = responseDto.getAccessToken();
                    callback.onSuccess(accessToken);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("https", "Response: " + response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onSuccess("null");
            }
        }) {
            @Override
            public HashMap<String, String> getHeaders() {
                HashMap<String, String> params = new HashMap<String, String>();

                String creds = String.format("%s:%s", ServerValues.CLIENT_USERNAME, ServerValues.CLIENT_PASSWORD);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);
                params.put("Authorization", auth);

                return params;
            }

            @Override
            protected HashMap<String, String> getParams() throws AuthFailureError {
                return requestBody;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };

        RequestManager.getInstance(context).addToRequestQueue(stringRequest);
    }

    public static void validateTokens(Context context, final Callback callback){
        final SharedPreferences prefs = context.getSharedPreferences(ServerValues.TOKEN_PREFS, MODE_PRIVATE);
        final String accessToken = prefs.getString("access_token", null);

        if(accessToken == null || isTokenExpired(accessToken)){
            if(!validateRefreshToken(context)){
               callback.onSuccess("error_no_token");
               return;
            }

            final String refreshToken = prefs.getString("refresh_token", null);
            Log.d("ticket token", "expired");
           obtainNewAccessToken(context, refreshToken, new AccessTokenManager.Callback() {
                @Override
                public void onSuccess(String result) {
                    if(result == null){
                        callback.onSuccess("err_unknown");
                        return;
                    }else {
                        Log.d("ticket_token result", result);
                        prefs.edit().putString("access_token", result).apply();
                        //prefs.edit().apply();
                        callback.onSuccess("true");
                    }
                }
            });
        }else{
            callback.onSuccess("true");
        }
    }

    public static boolean validateRefreshToken(Context context){
        final SharedPreferences prefs = context.getSharedPreferences(ServerValues.TOKEN_PREFS, MODE_PRIVATE);

        if(prefs.contains("refresh_token")){
            String refreshToken = prefs.getString("refresh_token", null);
            if(refreshToken == null)
                return  false;
            else
                return !isTokenExpired(refreshToken);
        }else{
            return false;
        }
    }

}
