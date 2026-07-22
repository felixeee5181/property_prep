package com.property_management.property_prep.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.property_management.property_prep.entity.LeaseAgreement;
import com.property_management.property_prep.entity.Payment;
import com.property_management.property_prep.entity.Property;
import com.property_management.property_prep.entity.User;
import com.property_management.property_prep.enums.RoleType;
import com.property_management.property_prep.repository.LeaseRepository;
import com.property_management.property_prep.repository.PaymentRepository;
import com.property_management.property_prep.repository.PropertyRepository;
import com.property_management.property_prep.service.DarajaService;
import com.property_management.property_prep.service.EmailService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    private final PaymentRepository paymentRepository;
    private final LeaseRepository leaseRepository;
    private final PropertyRepository propertyRepository;
    private final DarajaService darajaService;
    private final EmailService emailService;

    // ==========================================
    // 1. STRIPE ENDPOINT (Create Payment Intent)
    // ==========================================
    @PostMapping("/create-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(
            @RequestParam Long leaseId,
            @AuthenticationPrincipal User tenant) throws StripeException {
        Stripe.apiKey = stripeApiKey;

        LeaseAgreement lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new RuntimeException("Lease not found"));

        // Verify the tenant is the one who owns the lease
        if (!lease.getTenant().getId().equals(tenant.getId())) {
            throw new RuntimeException("You are not the tenant for this lease");
        }

        // Create a new payment record
        Payment payment = new Payment();
        payment.setLease(lease);
        payment.setAmount(lease.getProperty().getMonthlyRent());
        payment.setDueDate(LocalDate.now().plusDays(30));
        payment.setStatus("PENDING");
        payment.setPaymentMethod("STRIPE");
        payment = paymentRepository.save(payment);

        long amountCents = (long) (payment.getAmount() * 100);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountCents)
                .setCurrency("usd")
                .putMetadata("paymentId", String.valueOf(payment.getId()))
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", intent.getClientSecret());
        response.put("paymentId", String.valueOf(payment.getId()));
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 2. STRIPE WEBHOOK (Called by Stripe)
    // ==========================================
    @PostMapping("/stripe-webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload) {
        try {
            // Parse the payload to check for payment_intent.succeeded
            if (payload.contains("payment_intent.succeeded")) {
                // Extract the paymentId from metadata (in a real impl, use JSON parsing)
                // For demo, we'll just log it
                System.out.println("Stripe payment succeeded!");
                // TODO: Parse JSON to get metadata.paymentId and update status to PAID
            }
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Webhook error");
        }
        return ResponseEntity.ok("Webhook received");
    }

    // ==========================================
    // 3. M-PESA (DARAJ) STK PUSH
    // ==========================================
    @PostMapping("/mpesa/stk-push")
    public ResponseEntity<String> initiateMpesaPayment(
            @RequestParam Long leaseId,
            @RequestParam String phoneNumber,
            @AuthenticationPrincipal User tenant) throws IOException {

        LeaseAgreement lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new RuntimeException("Lease not found"));

        if (!lease.getTenant().getId().equals(tenant.getId())) {
            throw new RuntimeException("You are not the tenant for this lease");
        }

        // Create a new payment record
        Payment payment = new Payment();
        payment.setLease(lease);
        payment.setAmount(lease.getProperty().getMonthlyRent());
        payment.setDueDate(LocalDate.now().plusDays(30));
        payment.setStatus("PENDING");
        payment.setPaymentMethod("MPESA");
        payment = paymentRepository.save(payment);

        String response = darajaService.stkPush(
                phoneNumber,
                payment.getAmount().longValue(),
                "Lease-" + lease.getId()
        );
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 4. CASH PAYMENT REQUEST (Tenant)
    // ==========================================
    @PostMapping("/create-cash")
    public ResponseEntity<Map<String, Object>> createCashPayment(
            @RequestParam Long leaseId,
            @AuthenticationPrincipal User tenant) {

        LeaseAgreement lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new RuntimeException("Lease not found"));

        if (!lease.getTenant().getId().equals(tenant.getId())) {
            throw new RuntimeException("You are not the tenant for this lease");
        }

        Payment payment = new Payment();
        payment.setLease(lease);
        payment.setAmount(lease.getProperty().getMonthlyRent());
        payment.setDueDate(LocalDate.now().plusDays(30));
        payment.setStatus("PENDING");
        payment.setPaymentMethod("CASH");
        payment = paymentRepository.save(payment);

        // Notify manager via email
        User manager = lease.getProperty().getManager();
        String subject = "💰 Cash Payment Request from " + tenant.getUsername();
        String body = String.format(
                "Dear Manager,\n\nTenant %s has requested to pay rent in cash.\n\n" +
                        "Property: %s\nAmount: KES %.2f\n\n" +
                        "Please log in to your dashboard to confirm when you receive the payment.",
                tenant.getUsername(), lease.getProperty().getAddress(), payment.getAmount()
        );
        emailService.sendSimpleEmail(manager.getEmail(), subject, body);

        Map<String, Object> response = new HashMap<>();
        response.put("paymentId", payment.getId());
        response.put("amount", payment.getAmount());
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 5. CONFIRM CASH PAYMENT (Manager)
    // ==========================================
    @PostMapping("/cash/{paymentId}")
    public ResponseEntity<String> confirmCashPayment(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal User manager) {

        // Verify manager has proper role
        if (manager.getRole() != RoleType.LANDLORD) {
            throw new RuntimeException("Access Denied: Only landlords can confirm cash payments.");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Verify the manager owns the property
        Property property = payment.getLease().getProperty();
        if (!property.getManager().getId().equals(manager.getId())) {
            throw new RuntimeException("You are not the manager for this property");
        }

        // Update status
        payment.setStatus("PAID");
        payment.setPaidDate(LocalDate.now());
        paymentRepository.save(payment);

        // Send receipts
        String tenantEmail = payment.getLease().getTenant().getEmail();
        String managerEmail = manager.getEmail();
        String subject = "🧾 Cash Payment Confirmed";
        String tenantBody = String.format(
                "Dear %s,\n\nYour rent payment of KES %.2f has been marked as PAID by your landlord.\n\nThank you.",
                payment.getLease().getTenant().getUsername(), payment.getAmount()
        );
        String managerBody = String.format(
                "Dear Manager,\n\nYou have confirmed cash payment of KES %.2f from tenant %s.",
                payment.getAmount(), payment.getLease().getTenant().getUsername()
        );

        emailService.sendSimpleEmail(tenantEmail, subject, tenantBody);
        emailService.sendSimpleEmail(managerEmail, subject, managerBody);

        return ResponseEntity.ok("Cash payment confirmed and receipts sent.");
    }

    // ==========================================
    // 6. GET PENDING CASH PAYMENTS (Manager)
    // ==========================================
    @GetMapping("/cash-pending")
    public ResponseEntity<List<Payment>> getPendingCashPayments(@AuthenticationPrincipal User manager) {
        if (manager.getRole() != RoleType.LANDLORD) {
            throw new RuntimeException("Access Denied: Only landlords can view pending cash payments.");
        }

        List<Property> myProps = propertyRepository.findByManager(manager);
        List<LeaseAgreement> myLeases = new ArrayList<>();
        for (Property prop : myProps) {
            myLeases.addAll(leaseRepository.findByProperty(prop));
        }

        List<Payment> cashPayments = new ArrayList<>();
        for (LeaseAgreement lease : myLeases) {
            cashPayments.addAll(
                    paymentRepository.findByLeaseAndPaymentMethodAndStatus(lease, "CASH", "PENDING")
            );
        }
        return ResponseEntity.ok(cashPayments);
    }
    // Inside PaymentController.java
    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    @GetMapping("/config/stripe-publishable-key")
    public ResponseEntity<String> getStripePublishableKey() {
        return ResponseEntity.ok(stripePublishableKey);
    }
}