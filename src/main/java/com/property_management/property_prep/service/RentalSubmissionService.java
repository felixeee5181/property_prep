package com.property_management.property_prep.service;

import com.property_management.property_prep.dto.RentalSubmissionDTO;
import com.property_management.property_prep.entity.RentalRecord;
import com.property_management.property_prep.repository.RentalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalSubmissionService {
    private final EmailService emailService;
    private final RentalRecordRepository repository;

    public void processSubmission(RentalSubmissionDTO dto) {
        RentalRecord record = new RentalRecord();
        record.setHouseNumber(dto.getHouseNumber());
        record.setTenantName(dto.getTenantName());
        record.setTenantEmail(dto.getTenantEmail());
        record.setTenantPhone(dto.getTenantPhone());
        record.setPaymentStatus(dto.getPaymentStatus());
        repository.save(record);

        emailService.sendPaymentUpdateToLandlord(
                dto.getHouseNumber(),
                dto.getTenantName(),
                dto.getPaymentStatus().name()
        );
    }

    public Map<String, Object> getPaymentStats() {
        List<RentalRecord> all = repository.findAll();
        long paid = all.stream()
                .filter(r -> r.getPaymentStatus() == RentalSubmissionDTO.PaymentStatus.PAID)
                .count();
        long notPaid = all.size() - paid;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSubmissions", all.size());
        stats.put("paidCount", paid);
        stats.put("notPaidCount", notPaid);

        List<RentalRecord> latest = all.stream()
                .sorted((a, b) -> b.getSubmittedAt().compareTo(a.getSubmittedAt()))
                .limit(5)
                .collect(Collectors.toList());
        stats.put("latestSubmissions", latest);

        return stats;
    }
}