package com.naru.backend.service;

import com.naru.backend.model.Category;
import com.naru.backend.model.User;
import com.naru.backend.repository.CategoryRepository;
import com.naru.backend.repository.UserRepository;
import com.naru.backend.security.UserPrincipal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category createCategory(Category category) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // User ID를 통해 User 엔티티 조회
            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(
                            () -> new UsernameNotFoundException("User not found with ID: " + userPrincipal.getId()));

            // Category 엔티티에 User 설정
            category.setUser(user);
        }
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}