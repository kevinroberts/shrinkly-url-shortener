package com.vinberts.shrinkly.web.controllers;

import com.google.common.primitives.Longs;
import com.vinberts.shrinkly.email.ShrinklyMailClient;
import com.vinberts.shrinkly.email.mailPojos.MailLink;
import com.vinberts.shrinkly.persistence.model.Invitation;
import com.vinberts.shrinkly.persistence.model.User;
import com.vinberts.shrinkly.security.AutoLoginService;
import com.vinberts.shrinkly.service.IInvitationService;
import com.vinberts.shrinkly.web.dto.InvitationAcceptDto;
import com.vinberts.shrinkly.web.errors.InvitationException;
import com.vinberts.shrinkly.web.errors.UserAlreadyExistException;
import com.vinberts.shrinkly.web.util.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Admin-only invitation endpoints plus the public token-gated accept flow.
 */
@Controller
@Slf4j
public class InvitationController {

    private final IInvitationService invitationService;
    private final ShrinklyMailClient mailClient;
    private final AutoLoginService autoLoginService;

    @Value("${shrinkly.base.url}")
    private String serverUrl;

    public InvitationController(final IInvitationService invitationService,
                                final ShrinklyMailClient mailClient,
                                final AutoLoginService autoLoginService) {
        this.invitationService = invitationService;
        this.mailClient = mailClient;
        this.autoLoginService = autoLoginService;
    }

    @PostMapping("/admin/invite")
    @ResponseBody
    public ResponseEntity<Object> invite(@RequestBody final Map<String, Object> payload) {
        if (!payload.containsKey("email")) {
            return new ResponseEntity<>(new GenericResponse("Please enter an email address.", "", "email"), HttpStatus.BAD_REQUEST);
        }
        final String email = payload.get("email").toString().trim();
        if (!EmailValidator.getInstance().isValid(email)) {
            return new ResponseEntity<>(new GenericResponse("Please enter a valid email address.", "", "email"), HttpStatus.BAD_REQUEST);
        }
        try {
            final Invitation invitation = invitationService.createInvitation(email);
            final String acceptUrl = serverUrl + "/invite/accept?token=" + invitation.getToken();

            final HashMap<String, Object> emailContext = new HashMap<>();
            emailContext.put("userGreeting", "Hello,");
            emailContext.put("mailBody1", "You've been invited to create an account on Shrinkly.");
            emailContext.put("ctaLink", new MailLink(acceptUrl, "Create your account"));
            emailContext.put("plainText", "You've been invited to Shrinkly. Create your account: " + acceptUrl);
            emailContext.put("mailBody3", "This invitation link will expire in 24 hours.<br>Thanks,<br>The Shrinkly Team");
            mailClient.prepareAndSend(email, "You're invited to Shrinkly", emailContext);

            return new ResponseEntity<>(new GenericResponse("Invitation sent to " + email), HttpStatus.CREATED);
        } catch (InvitationException e) {
            return new ResponseEntity<>(new GenericResponse(e.getMessage(), "", "email"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/admin/invite/revoke")
    @ResponseBody
    public ResponseEntity<Object> revoke(@RequestBody final Map<String, Object> payload) {
        if (!payload.containsKey("id")) {
            return new ResponseEntity<>(new GenericResponse("Please pass a valid id as a parameter.", "", "id"), HttpStatus.BAD_REQUEST);
        }
        final Long id = Longs.tryParse(payload.get("id").toString(), 10);
        if (id == null) {
            return new ResponseEntity<>(new GenericResponse("Please pass a valid id as a parameter.", "", "id"), HttpStatus.BAD_REQUEST);
        }
        invitationService.revokeInvitation(id);
        return new ResponseEntity<>(new GenericResponse("Invitation revoked."), HttpStatus.OK);
    }

    @GetMapping("/invite/accept")
    public ModelAndView acceptForm(@RequestParam("token") final String token, final Map<String, Object> model) {
        final Optional<Invitation> invitation = invitationService.getValidInvitation(token);
        if (invitation.isEmpty()) {
            model.put("pageTitle", "Shrinkly - Invitation");
            return new ModelAndView("inviteInvalid", model);
        }
        model.put("pageTitle", "Shrinkly - Accept Invitation");
        model.put("email", invitation.get().getEmail());
        model.put("token", token);
        return new ModelAndView("invite", model);
    }

    @PostMapping("/invite/accept")
    public ModelAndView accept(@Valid final InvitationAcceptDto dto, final BindingResult result,
                               final HttpServletRequest request, final HttpServletResponse response,
                               final Locale locale) {
        final Optional<Invitation> invitation = invitationService.getValidInvitation(dto.getToken());
        if (invitation.isEmpty()) {
            return new ModelAndView("inviteInvalid");
        }
        if (result.hasErrors()) {
            return acceptFormWithError(invitation.get().getEmail(), dto.getToken(),
                    result.getAllErrors().get(0).getDefaultMessage());
        }
        try {
            final User user = invitationService.acceptInvitation(dto.getToken(), dto.getUsername(), dto.getPassword());
            autoLoginService.loginWithoutPassword(user, request, response);
            request.getSession().setAttribute("message", "Welcome to Shrinkly! Your account is ready.");
            return new ModelAndView("redirect:/?lang=" + locale.getLanguage());
        } catch (UserAlreadyExistException e) {
            return acceptFormWithError(invitation.get().getEmail(), dto.getToken(), e.getMessage());
        } catch (InvitationException e) {
            return new ModelAndView("inviteInvalid");
        }
    }

    private ModelAndView acceptFormWithError(final String email, final String token, final String error) {
        final ModelAndView mv = new ModelAndView("invite");
        mv.addObject("pageTitle", "Shrinkly - Accept Invitation");
        mv.addObject("email", email);
        mv.addObject("token", token);
        mv.addObject("validationError", error);
        return mv;
    }
}
