package com.naru.backend.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.naru.backend.dto.LoginDTO;
import com.naru.backend.dto.UserRequestDTO;
import com.naru.backend.dto.UserResponseDTO;
import com.naru.backend.exception.EmailNotVerifiedException;
import com.naru.backend.model.User;
import com.naru.backend.repository.UserRepository;
import com.naru.backend.security.UserPrincipal;
import com.naru.backend.util.CookieUtil;
import com.naru.backend.util.JwtUtil;
import com.naru.backend.util.TokenUtil;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final MailService mailService;
    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;
    private final CookieUtil cookieUtil;

    private static final List<String> ownerAuthorities = Arrays.asList("OWNER");
    private static final List<String> guestAuthorities = Arrays.asList("GUEST");

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
            AuthenticationManager authenticationManager, MailService mailService, TokenService tokenService,
            UserDetailsService userDetailsService, CookieUtil cookieUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.mailService = mailService;
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
        this.cookieUtil = cookieUtil;
    }

    @Transactional
    public UserResponseDTO registerUser(UserRequestDTO userRequestDTO) {
        if (userRepository.findByEmail(userRequestDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(userRequestDTO.getUsername());
        user.setEmail(userRequestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        user.setEmailVerificationToken(TokenUtil.generateTokenWithTimestamp());

        // 오너 권한 설정
        if (userRequestDTO.getEmail().equals("kwonnaru@kakao.com")) {
            user.setAuthorities(ownerAuthorities);
        } else {
            // 게스트 권한 설정
            user.setAuthorities(guestAuthorities);
        }

        mailService.sendVerificationEmail(user.getEmail(), user.getEmailVerificationToken());
        return new UserResponseDTO(userRepository.save(user));
    }

    public User authenticateUser(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));
        UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public UserResponseDTO convertToUserResponseDTO(String email) {
        User user = findByEmail(email);
        return new UserResponseDTO(user);
    }

    public Map<String, String> generateTokens(User user) {
        try {

            if (!user.isEmailVerified()) {
                throw new EmailNotVerifiedException("Email not verified");
            }

            UserPrincipal userPrincipal = UserPrincipal.create(user);
            Map<String, String> tokens = cookieUtil.generateTokens(userPrincipal, user.getEmail());
            tokenService.saveAccessToken(user.getEmail(), tokens.get("NID_AUTH"));
            tokenService.saveRefreshToken(user.getEmail(), tokens.get("refreshToken"));
            return tokens;
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

    public Map<String, String> refreshAccessToken(String refreshToken) {
        String email = jwtUtil.extractUsername(refreshToken);

        if (tokenService.validateRefreshToken(email, refreshToken)) {
            UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(email);
            Map<String, String> tokens = cookieUtil.generateTokens(userPrincipal, email);
            tokenService.saveAccessToken(email, tokens.get("NID_AUTH"));
            tokenService.saveRefreshToken(email, tokens.get("refreshToken"));
            return tokens;
        }

        throw new RuntimeException("Invalid refresh token");
    }
}
