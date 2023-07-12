package com.semicolon.grincultified.services.adminInvitationService;

import com.semicolon.grincultified.data.models.AdminInvitation;
import com.semicolon.grincultified.exception.AdminInvitationNotFoundException;

import java.util.Optional;

public interface AdminInvitationService {
    Optional<AdminInvitation> findByEmail(String emailAddress);

    String registerInvitation(AdminInvitation invitation);

    void verifyInvitationForRegistration(String emailAddress) throws AdminInvitationNotFoundException;

    void deleteAll();

}