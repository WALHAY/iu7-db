package com.walhay.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SaleInfo {
    @Column(name = "value")
    private Double value;
    @Column(name = "end")
    private Date end;
    @Column(name = "start")
    private Date start;
}
