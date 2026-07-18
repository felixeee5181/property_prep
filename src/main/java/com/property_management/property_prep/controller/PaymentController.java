package com.property_management.property_prep.controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.property_management.property_prep.entity.Payment;
import com.property_management.property_prep.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    private final PaymentRepository paymentRepository;

    @PostMapping("/create-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestParam Long paymentId) throws StripeException {
        Stripe.apiKey = stripeApiKey;

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Stripe needs amount in cents (smallest currency unit)
        long amountCents = (long) (payment.getAmount() * 100);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountCents)
                .setCurrency("usd") // or "kes" for Kenyan Shillings
                .putMetadata("paymentId", String.valueOf(paymentId))
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", intent.getClientSecret());
        response.put("paymentId", String.valueOf(paymentId));
        return ResponseEntity.ok(response);
    }
    // Stripe Webhook endpoint (no security, public)
    @PostMapping("/stripe-webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload) {
        // In a real production app, you must verify the Stripe signature here
        System.out.println("Stripe Webhook received: " + payload);

        try {
            // Simple JSON parsing (you can use Jackson for a proper parser)
            // Assuming the webhook sends the payment ID in metadata
            if (payload.contains("payment_intent.succeeded")) {
                // This is where you would parse the metadata to get the ID
                // For this simple demo, let's assume we find it manually or log it.
                // Example logic to update DB:
                // String paymentId = ... extracted from payload;
                // Payment payment = paymentRepository.findById(Long.parseLong(paymentId)).orElseThrow();
                // payment.setStatus("PAID");
                // paymentRepository.save(payment);

                System.out.println("Payment successful! Database would update to PAID.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Webhook error");
        }
        return ResponseEntity.ok("Webhook received");
    }
}