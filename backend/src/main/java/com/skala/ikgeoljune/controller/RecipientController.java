package com.skala.ikgeoljune.controller;

import com.skala.ikgeoljune.dto.request.RecipientRequest;
import com.skala.ikgeoljune.dto.response.RecipientResponse;
import com.skala.ikgeoljune.service.RecipientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// SCR-RECIPIENT-001 · UC2
@RestController
@RequestMapping("/api/recipients")
@RequiredArgsConstructor
public class RecipientController {

    private final RecipientService recipientService;

    @GetMapping
    public List<RecipientResponse> list(@RequestParam(required = false) Long userId) {
        return recipientService.findAll(userId);
    }

    @PostMapping
    public RecipientResponse create(@RequestParam(required = false) Long userId,
                                     @Valid @RequestBody RecipientRequest request) {
        return recipientService.create(userId, request);
    }

    @PutMapping("/{recipientId}")
    public RecipientResponse update(@PathVariable Long recipientId,
                                     @Valid @RequestBody RecipientRequest request) {
        return recipientService.update(recipientId, request);
    }

    @DeleteMapping("/{recipientId}")
    public void delete(@PathVariable Long recipientId) {
        recipientService.delete(recipientId);
    }
}
