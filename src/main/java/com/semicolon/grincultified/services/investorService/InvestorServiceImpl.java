package com.semicolon.grincultified.services.investorService;

import com.semicolon.grincultified.data.models.*;
import com.semicolon.grincultified.data.repositories.InvestorRepo;
import com.semicolon.grincultified.dtos.requests.InvestorRegistrationRequest;
import com.semicolon.grincultified.dtos.requests.CompleteRegistrationRequest;
import com.semicolon.grincultified.dtos.responses.*;
import com.semicolon.grincultified.exception.*;
import com.semicolon.grincultified.services.adminService.AdminService;
import com.semicolon.grincultified.services.mailService.MailService;
import com.semicolon.grincultified.services.temporaryUserService.TemporaryUserService;
import com.semicolon.grincultified.utilities.JwtUtility;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.semicolon.grincultified.data.models.Role.INVESTOR;
import static com.semicolon.grincultified.utilities.AppUtils.*;

@Service
@AllArgsConstructor
public class InvestorServiceImpl implements InvestorService {
    private final InvestorRepo investorRepo;
    private final AdminService adminService;
    private final JwtUtility jwtUtil;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final TemporaryUserService temporaryUserService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtility jwtUtility;


    @Override
    public ResponseEntity<GenericResponse<String>> initiateRegistration(InvestorRegistrationRequest investorRegistrationRequest) throws DuplicateInvestorException, AdminAlreadyExistException, InvalidEmailException {
        Optional<Investor> foundInvestor = investorRepo.findByUser_EmailAddressContainingIgnoreCase(investorRegistrationRequest.getEmailAddress());
        if (foundInvestor.isPresent()) throw new DuplicateInvestorException(INVESTOR_ALREADY_EXIST);
        temporaryUserService.deleteExistingTemporaryUser(investorRegistrationRequest.getEmailAddress());
        adminService.validateDuplicateExistence(investorRegistrationRequest.getEmailAddress());
        String encryptedEmail = jwtUtility.generateEncryptedLink(investorRegistrationRequest.getEmailAddress());
        String invitationLink = INVESTOR_REGISTRATION_PAGE_URL+encryptedEmail;
        sendEmailVerificationMail(investorRegistrationRequest, invitationLink);
        temporaryUserService.addUserTemporarily(investorRegistrationRequest);
        GenericResponse<String> genericResponse = new GenericResponse<>();
        genericResponse.setMessage(INVESTOR_INITIAL_REGISTRATION_RESPONSE);
        return ResponseEntity.ok().body(genericResponse);
    }

    @Override
    public ResponseEntity<Map<String, Object>> confirmRegistration(CompleteRegistrationRequest completeRegistrationRequest) throws TemporaryInvestorDoesNotExistException, InvestorRegistrationFailed {
        String email;
        try {
            email = jwtUtility.extractEmailFrom(completeRegistrationRequest.getEmailAddress());
        } catch (AuthenticationException e) {
            throw new InvestorRegistrationFailed(e.getMessage());
        }
        Optional<Investor> foundInvestor = investorRepo.findByUser_EmailAddressContainingIgnoreCase(email);
        Investor savedInvestor;
        InvestorRegistrationRequest investorRegistrationRequest = temporaryUserService.findByEmail(email);
        String password = investorRegistrationRequest.getPassword();
        if (foundInvestor.isEmpty()) savedInvestor = registerInvestor(investorRegistrationRequest);
        else savedInvestor = foundInvestor.get();
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authResult = authenticationManager.authenticate(authentication);
        String accessToken = jwtUtil.generateAccessToken(authResult.getAuthorities());
        Map<String, Object> responseData = new HashMap<>();
        responseData.put(ACCESS_TOKEN_VALUE, accessToken);
        responseData.put(USER, map(savedInvestor));
        return ResponseEntity.ok().body(responseData);
    }

    private Investor registerInvestor(InvestorRegistrationRequest investorRegistrationRequest) throws TemporaryInvestorDoesNotExistException {
        temporaryUserService.deleteTemporaryInvestor(investorRegistrationRequest);
        User user = modelMapper.map(investorRegistrationRequest, User.class);
        Address address = new Address();
        user.setAddress(address);
        user.setRoles(new HashSet<>());
        user.getRoles().add(INVESTOR);
        user.setPassword(passwordEncoder.encode(investorRegistrationRequest.getPassword()));
        Investor investor = new Investor();
        investor.setUser(user);
        Investor savedInvestor = investorRepo.save(investor);
        return savedInvestor;
    }

    private InvestorResponse map(Investor investor) {
        UserResponse userResponse = modelMapper.map(investor.getUser(), UserResponse.class);
        InvestorResponse investorResponse = modelMapper.map(investor, InvestorResponse.class);
        investorResponse.setUserResponse(userResponse);
        return investorResponse;
    }

    @Override
    public InvestorResponse findByEmail(String email) {
        Investor foundInvestor = investorRepo.findByUser_EmailAddressContainingIgnoreCase(email).get();
        return map(foundInvestor);
    }

    @Override
    public InvestorResponse findById(Long investorId) {
        return modelMapper.map(investorRepo.findById(investorId), InvestorResponse.class);
    }

    private void sendEmailVerificationMail(InvestorRegistrationRequest investorRegistrationRequest, String invitationLink) throws InvalidEmailException {
        String response = mailService.sendEmailVerificationMail(investorRegistrationRequest, invitationLink);
    }

    @Override
    public ResponseEntity<List<InvestorResponse>> getAllInvestors() {
        List<Investor> investors = investorRepo.findAll();
        List<InvestorResponse> investorResponses = investors.stream().map(this::map).collect(Collectors.toList());
        return ResponseEntity.ok().body(investorResponses);
    }

    @Override
    public void deleteAll() {
        investorRepo.deleteAll();
    }

    @Override
    public Long count() {
        return investorRepo.count();
    }

    @Override
    public String validateDuplicateExistence(String emailAddress) throws InvestorAlreadyExistException {
        Optional<Investor> investor = investorRepo.findByUser_EmailAddressContainingIgnoreCase(emailAddress);
        if (investor.isPresent()) throw new InvestorAlreadyExistException(INVESTOR_ALREADY_EXISTS);
        return INVESTOR_DOES_NOT_EXIST;
    }

}
