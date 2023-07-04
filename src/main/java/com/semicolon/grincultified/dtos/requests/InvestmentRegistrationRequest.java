package com.semicolon.grincultified.dtos.requests;

import com.semicolon.grincultified.data.models.InvestmentReturnType;
import com.semicolon.grincultified.data.models.InvestmentStatus;
import com.semicolon.grincultified.data.models.RedemptionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Setter
@Getter
public class InvestmentRegistrationRequest {
    private Long farmProjectId;
    private Long investorId;
    private BigDecimal amount;
    private InvestmentReturnType returnType;

}