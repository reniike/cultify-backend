package com.semicolon.grincultified.services.investorService;

import com.semicolon.grincultified.dtos.requests.InvestorRegistrationRequest;
import com.semicolon.grincultified.dtos.requests.OtpVerificationRequest;
import com.semicolon.grincultified.dtos.responses.InvestorRegistrationResponse;
import com.semicolon.grincultified.dtos.responses.InvestorResponse;
import com.semicolon.grincultified.exception.DuplicateInvestorException;
import com.semicolon.grincultified.exception.TemporaryInvestorDoesNotExistException;
import org.springframework.http.ResponseEntity;

public interface InvestorService {
    ResponseEntity<InvestorRegistrationResponse> initiateRegistration(InvestorRegistrationRequest investorRegistrationRequest) throws DuplicateInvestorException;
    InvestorRegistrationResponse confirmRegistration(OtpVerificationRequest otpVerificationRequest) throws TemporaryInvestorDoesNotExistException;

    InvestorResponse findByEmail(String email);
}
