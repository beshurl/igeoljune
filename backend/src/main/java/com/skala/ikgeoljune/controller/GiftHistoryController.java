package com.skala.ikgeoljune.controller;

import com.skala.ikgeoljune.domain.GiftHistory;
import com.skala.ikgeoljune.service.GiftHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// SCR-HISTORY-001 · UC14
@RestController
@RequestMapping("/api/gift-histories")
@RequiredArgsConstructor
public class GiftHistoryController {

    private final GiftHistoryService giftHistoryService;

    @GetMapping
    public List<GiftHistory> list(@RequestParam(required = false) Long recipientId) {
        return giftHistoryService.findByRecipient(recipientId);
    }
}
