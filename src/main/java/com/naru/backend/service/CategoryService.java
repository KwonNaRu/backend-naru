package com.naru.backend.service;

import com.naru.backend.dto.CategoryDto;
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

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryDto::new).toList();
    }

    public CategoryDto createCategory(CategoryDto categoryDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Category category = new Category();
        if (authentication != null && authentication.isAuthenticated()) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // User ID를 통해 User 엔티티 조회
            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(
                            () -> new UsernameNotFoundException("User not found with ID: " + userPrincipal.getId()));

            // Category 엔티티에 User 설정

            category.setName(categoryDto.getName());
            category.setUser(user);
        }
        return new CategoryDto(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}