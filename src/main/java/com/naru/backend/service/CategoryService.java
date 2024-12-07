package com.naru.backend.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.naru.backend.dto.CategoryDTO;
import com.naru.backend.model.Category;
import com.naru.backend.model.Post;
import com.naru.backend.model.User;
import com.naru.backend.repository.CategoryRepository;
import com.naru.backend.repository.PostRepository;
import com.naru.backend.repository.UserRepository;
import com.naru.backend.security.UserPrincipal;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    public List<CategoryDTO> getAllCategories() {
        // 1. 모든 카테고리 조회
        List<Category> categories = categoryRepository.findAll();

        // 2. 모든 카테고리 ID 추출
        List<Long> categoryIds = categories.stream()
                .map(Category::getId)
                .toList();

        // 3. 한 번의 쿼리로 모든 포스트 조회
        Map<Long, List<Post>> postsByCategory = postRepository.findByCategoryIdIn(categoryIds)
                .stream()
                .collect(Collectors.groupingBy(Post::getCategoryId));

        // 4. CategoryDto 생성 시 매핑된 포스트 할당
        return categories.stream()
                .map(category -> {
                    CategoryDTO dto = new CategoryDTO(category);
                    dto.setPosts(postsByCategory.getOrDefault(category.getId(), Collections.emptyList()));
                    return dto;
                }).toList();
    }

    public CategoryDTO createCategory(CategoryDTO categoryDto, UserPrincipal userPrincipal) {
        Category category = new Category();

        // User ID를 통해 User 엔티티 조회
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(
                        () -> new UsernameNotFoundException("User not found with ID: " + userPrincipal.getId()));

        // Category 엔티티에 User 설정
        category.setName(categoryDto.getName());
        category.setUser(user);
        category.setPosts(Collections.emptyList());

        Category newCategory = categoryRepository.save(category);
        return new CategoryDTO(newCategory);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}