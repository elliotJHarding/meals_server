package com.harding.meals.controller;

import com.harding.meals.dto.IngestReceiptRequest;
import com.harding.meals.dto.ReceiptDto;
import com.harding.meals.dto.ReceiptIngestionResultDto;
import com.harding.meals.entity.user.AppUser;
import com.harding.meals.mapping.ReceiptMapper;
import com.harding.meals.repository.ReceiptRepository;
import com.harding.meals.service.receipt.ReceiptIngestionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class ReceiptController {

    private final ReceiptIngestionService receiptIngestionService;
    private final ReceiptRepository receiptRepository;
    private final ReceiptMapper receiptMapper;

    public ReceiptController(ReceiptIngestionService receiptIngestionService,
                             ReceiptRepository receiptRepository,
                             ReceiptMapper receiptMapper) {
        this.receiptIngestionService = receiptIngestionService;
        this.receiptRepository = receiptRepository;
        this.receiptMapper = receiptMapper;
    }

    @PostMapping("/receipts")
    public ReceiptIngestionResultDto ingest(@RequestBody IngestReceiptRequest request,
                                            @AuthenticationPrincipal AppUser user) {
        if (request.getRawContent() == null || request.getRawContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receipt content is empty");
        }
        try {
            return receiptIngestionService.ingest(user, request);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/receipts")
    public List<ReceiptDto> all(@AuthenticationPrincipal AppUser user) {
        return receiptRepository.findByFamilyGroup(user).stream()
                .map(receiptMapper::toDto)
                .toList();
    }
}
