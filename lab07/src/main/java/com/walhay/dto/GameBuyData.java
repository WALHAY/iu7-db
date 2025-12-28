package com.walhay.dto;

import java.math.BigDecimal;

public record GameBuyData(
        Long id,
        String name,
        Long players,
        BigDecimal revenue)
{}
