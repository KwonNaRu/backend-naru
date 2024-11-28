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
import com.naru.backend.security.JwtUtil;
import com.naru.backend.security.UserPrincipal;
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

    private static final List<String> ownerAuthorities = Arrays.asList("OWNER");
    private static final List<String> guestAuthorities = Arrays.asList("GUEST");

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
            AuthenticationManager authenticationManager, MailService mailService, TokenService tokenService,
            UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.mailService = mailService;
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
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

    @Transactional
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public Map<String, String> authenticateUser(LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));
            UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (!user.isEmailVerified()) {
                throw new EmailNotVerifiedException("Email not verified");
            }

            UserPrincipal userPrincipal = UserPrincipal.create(user);
            String accessToken = jwtUtil.generateToken(userPrincipal);
            String refreshToken = jwtUtil.generateRefreshToken(userPrincipal);

            // Redis에 토큰 저장
            tokenService.saveAccessToken(user.getEmail(), accessToken);
            tokenService.saveRefreshToken(user.getEmail(), refreshToken);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);

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
            String newAccessToken = jwtUtil.generateToken(userPrincipal);

            tokenService.saveAccessToken(email, newAccessToken);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", newAccessToken);
            return tokens;
        }

        throw new RuntimeException("Invalid refresh token");
    }
}
