package com.walhay.dto;

import java.math.BigDecimal;

public record PublisherInfo(String publisherName, long totalGames, long totalSold, BigDecimal totalRevenue) {
}
