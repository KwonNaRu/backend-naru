package com.naru.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.naru.backend.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 사용자 이름으로 사용자 정보를 찾기 위한 메소드
    Optional<User> findByUsername(String username);

}