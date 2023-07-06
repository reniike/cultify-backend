package com.semicolon.grincultified.services.investorService;

import com.semicolon.grincultified.data.models.*;
import com.semicolon.grincultified.data.repositories.InvestorRepo;
import com.semicolon.grincultified.dtos.requests.InvestmentRegistrationRequest;
import com.semicolon.grincultified.dtos.requests.InvestorRegistrationRequest;
import com.semicolon.grincultified.dtos.requests.OtpVerificationRequest;
import com.semicolon.grincultified.dtos.requests.SendMailRequest;
import com.semicolon.grincultified.dtos.responses.GenericResponse;
import com.semicolon.grincultified.dtos.responses.InvestmentResponse;
import com.semicolon.grincultified.dtos.responses.InvestorResponse;
import com.semicolon.grincultified.dtos.responses.UserResponse;
import com.semicolon.grincultified.exception.DuplicateInvestorException;
import com.semicolon.grincultified.exception.TemporaryInvestorDoesNotExistException;
import com.semicolon.grincultified.services.mailService.MailService;
import com.semicolon.grincultified.services.otpService.OtpService;
import com.semicolon.grincultified.services.temporaryUserService.TemporaryUserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.semicolon.grincultified.utilities.AppUtils.*;

@Service
@AllArgsConstructor
public class InvestorServiceImpl implements InvestorService {
    private final InvestorRepo investorRepo;
    private final ModelMapper modelMapper;
    private final TemporaryUserService temporaryUserService;
    private final MailService mailService;
    private final OtpService otpService;

    @Override
    public ResponseEntity<GenericResponse<String>> initiateRegistration(InvestorRegistrationRequest investorRegistrationRequest) throws DuplicateInvestorException {
        Optional<Investor> foundInvestor = investorRepo.findByUser_EmailAddressContainingIgnoreCase(investorRegistrationRequest.getEmailAddress());
        if (foundInvestor.isPresent()) throw new DuplicateInvestorException(EMAIL_ALREADY_EXIST);
        Otp otp = otpService.generateOtp();
        investorRegistrationRequest.setOtp(otp);
        temporaryUserService.addUserTemporarily(investorRegistrationRequest);
        sendOtp(investorRegistrationRequest);
        GenericResponse<String> genericResponse = new GenericResponse<>();
        genericResponse.setMessage(CHECK_YOUR_MAIL_FOR_YOUR_OTP);
        genericResponse.setData(otp.getOtpToken());
        return ResponseEntity.ok().body(genericResponse);
    }

    @Override
    public InvestorResponse confirmRegistration(OtpVerificationRequest otpVerificationRequest) throws TemporaryInvestorDoesNotExistException {
        InvestorRegistrationRequest investorRegistrationRequest = otpService.verifyOtp(otpVerificationRequest);
        User user = modelMapper.map(investorRegistrationRequest, User.class);
        Address address = new Address();
        user.setAddress(address);
        Investor investor = new Investor();
        investor.setUser(user);
        Investor savedInvestor = investorRepo.save(investor);
        temporaryUserService.deleteTemporaryInvestor(investorRegistrationRequest);
        UserResponse userResponse = modelMapper.map(savedInvestor.getUser(), UserResponse.class);
        InvestorResponse investorResponse = modelMapper.map(savedInvestor, InvestorResponse.class);
        investorResponse.setUserResponse(userResponse);
        return investorResponse;
    }

    @Override
    public InvestorResponse findByEmail(String email) {
        Investor foundInvestor = investorRepo.findByUser_EmailAddressContainingIgnoreCase(email).get();
        UserResponse userResponse = modelMapper.map(foundInvestor.getUser(), UserResponse.class);
        InvestorResponse investorResponse = modelMapper.map(foundInvestor, InvestorResponse.class);
        investorResponse.setUserResponse(userResponse);
        return investorResponse;
    }

    @Override
    public InvestorResponse findById(Long investorId) {
        return modelMapper.map(investorRepo.findById(investorId), InvestorResponse.class);
    }

    @Override
    public void saveInvestmentFor(InvestmentRegistrationRequest investmentRegistrationRequest) {
        Investment investment = modelMapper.map(investmentRegistrationRequest, Investment.class);
        Investor investor = investorRepo.findById(investmentRegistrationRequest.getInvestorId()).get();
        List<Investment> investments = investor.getInvestments();
        investments.add(investment);
        investor.setInvestments(investments);
        investorRepo.save(investor);
    }


    private String sendOtp(InvestorRegistrationRequest investorRegistrationRequest) {
        SendMailRequest sendMailRequest = new SendMailRequest();
        sendMailRequest.setFrom(SYSTEM_MAIL);
        sendMailRequest.setTo(investorRegistrationRequest.getEmailAddress());
        sendMailRequest.setSubject(REGISTRATION_OTP);
        String text = String.format(OTP_TOKEN, investorRegistrationRequest.getOtp().getOtpToken());
        sendMailRequest.setText(text);
        mailService.sendMail(sendMailRequest);
        return sendMailRequest.getText();
    }
}
