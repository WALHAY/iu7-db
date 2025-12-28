package com.walhay.dto;

import java.time.LocalDate;

public record GamePublished(Long id, String name, LocalDate publishDate) {
}
