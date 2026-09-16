package com.eldersphere.dtos.Payment;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class EarningsResponse {
    private Long caretakerId;
    private BigDecimal totalEarned;
    private long succeededPaymentCount;
    private List<MonthlyEarning> monthlyBreakdown;

    @Data
    @Builder
    public static class MonthlyEarning {
        private LocalDate month;
        private BigDecimal total;
    }
}
