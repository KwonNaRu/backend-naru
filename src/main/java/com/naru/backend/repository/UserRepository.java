package com.naru.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.naru.backend.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    
    Optional<User> findByEmail(String email);

    // 사용자 이름으로 사용자 정보를 찾기 위한 메소드
    Optional<User> findByUsername(String email);

    // 회원가입 메소드
    @Modifying
    @Query("INSERT INTO User (username, email, password, authorities) VALUES (:username, :email, :password, :authorities)")
    void saveUser (@Param("username") String username, @Param("email") String email, @Param("password") String password, @Param("authorities") List<String> authorities);

    User findByEmailVerificationToken(String token);

}