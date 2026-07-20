package com.property_management.property_prep.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

@Service
public class DarajaService {

    @Value("${daraja.consumer.key}")
    private String consumerKey;

    @Value("${daraja.consumer.secret}")
    private String consumerSecret;

    @Value("${daraja.passkey}")
    private String passkey;

    @Value("${daraja.shortcode}")
    private String shortcode;

    @Value("${daraja.callback.url}")
    private String callbackUrl;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // 1. Get OAuth Token from Safaricom
    public String getAccessToken() throws IOException {
        String auth = consumerKey + ":" + consumerSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        Request request = new Request.Builder()
                .url("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
                .header("Authorization", "Basic " + encodedAuth)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Failed to get token");
            String json = response.body().string();
            JsonNode node = mapper.readTree(json);
            return node.get("access_token").asText();
        }
    }

    // 2. Initiate STK Push (the pop-up on the tenant's phone)
    public String stkPush(String phoneNumber, Long amount, String accountReference) throws IOException {
        String token = getAccessToken();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String password = Base64.getEncoder().encodeToString((shortcode + passkey + timestamp).getBytes());

        String jsonBody = String.format(
                "{" +
                        "\"BusinessShortCode\":\"%s\"," +
                        "\"Password\":\"%s\"," +
                        "\"Timestamp\":\"%s\"," +
                        "\"TransactionType\":\"CustomerPayBillOnline\"," +
                        "\"Amount\":\"%d\"," +
                        "\"PartyA\":\"%s\"," +
                        "\"PartyB\":\"%s\"," +
                        "\"PhoneNumber\":\"%s\"," +
                        "\"CallBackURL\":\"%s\"," +
                        "\"AccountReference\":\"%s\"," +
                        "\"TransactionDesc\":\"Rent Payment\"" +
                        "}",
                shortcode, password, timestamp, amount, phoneNumber, shortcode, phoneNumber,
                callbackUrl, accountReference
        );

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest")
                .header("Authorization", "Bearer " + token)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("STK push failed");
            return response.body().string();
        }
    }
}