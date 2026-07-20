package com.property_management.property_prep.controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.property_management.property_prep.entity.Payment;
import com.property_management.property_prep.repository.PaymentRepository;
import com.property_management.property_prep.service.DarajaService;  // <-- NEW IMPORT
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;  // <-- NEW IMPORT
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    // Existing Stripe repository...
    private final PaymentRepository paymentRepository;

    // ✅ NEW: Inject the DarajaService
    private final DarajaService darajaService;

    // --- STRIPE ENDPOINTS (Unchanged) ---
    @PostMapping("/create-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestParam Long paymentId) throws StripeException {
        Stripe.apiKey = stripeApiKey;

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        long amountCents = (long) (payment.getAmount() * 100);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountCents)
                .setCurrency("usd")
                .putMetadata("paymentId", String.valueOf(paymentId))
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", intent.getClientSecret());
        response.put("paymentId", String.valueOf(paymentId));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stripe-webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload) {
        System.out.println("Stripe Webhook received: " + payload);
        try {
            if (payload.contains("payment_intent.succeeded")) {
                System.out.println("Payment successful! Database would update to PAID.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Webhook error");
        }
        return ResponseEntity.ok("Webhook received");
    }

    // ==========================================
    // ✅ NEW: M-PESA (DARAGA) ENDPOINTS START HERE
    // ==========================================

    // 1. Trigger the STK Push (Tenant clicks "Pay with M-Pesa")
    @PostMapping("/mpesa/stk-push")
    public ResponseEntity<String> initiateMpesaPayment(@RequestParam Long paymentId, @RequestParam String phoneNumber) throws IOException {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        String response = darajaService.stkPush(
                phoneNumber,
                payment.getAmount().longValue(),
                "Lease-" + payment.getLease().getId()
        );
        return ResponseEntity.ok(response);
    }

    // 2. The Callback Webhook (Safaricom calls this automatically)
    @PostMapping("/mpesa/callback")
    public ResponseEntity<String> mpesaCallback(@RequestBody String callbackData) {
        System.out.println("M-Pesa callback received: " + callbackData);
        // TODO: Parse the JSON to check if the payment was successful.
        // If successful, find the payment by AccountReference and set status to "PAID".
        return ResponseEntity.ok("Success");
    }
}