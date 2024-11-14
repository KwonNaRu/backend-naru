package com.naru.backend.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.naru.backend.dto.UserDto;
import com.naru.backend.exception.EmailNotVerifiedException;
import com.naru.backend.model.Login;
import com.naru.backend.model.User;
import com.naru.backend.repository.UserRepository;
import com.naru.backend.security.JwtUtil;
import com.naru.backend.security.UserPrincipal;
import com.naru.backend.util.TokenUtil;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final MailService mailService;

    private static final List<String> ownerAuthorities = Arrays.asList("OWNER");
    private static final List<String> guestAuthorities = Arrays.asList("GUEST");

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
            AuthenticationManager authenticationManager, MailService mailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.mailService = mailService;
    }

    @Transactional
    public UserDto registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setEmailVerificationToken(TokenUtil.generateTokenWithTimestamp());

        // 오너 권한 설정
        if (user.getEmail().equals("kwonnaru@kakao.com")) {
            newUser.setAuthorities(ownerAuthorities);
        } else {
            // 게스트 권한 설정
            newUser.setAuthorities(guestAuthorities);
        }

        mailService.sendVerificationEmail(newUser.getEmail(), newUser.getEmailVerificationToken());
        return new UserDto(userRepository.save(newUser));
    }

    @Transactional
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public String authenticateUser(Login login) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword()));
            UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (!user.isEmailVerified()) {
                throw new EmailNotVerifiedException("Email not verified");
            }
            return jwtUtil.generateToken(userDetails);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Wrong password");
        }
    }

    public boolean verifyEmail(String email, String token) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Assuming you also want to check if the token matches before verifying the
            // email
            if (token.equals(user.getEmailVerificationToken())) {
                user.setEmailVerified(true);
                user.setEmailVerificationToken(null);
                userRepository.save(user);
                return true;
            }
        }

        return false;
    }
}
