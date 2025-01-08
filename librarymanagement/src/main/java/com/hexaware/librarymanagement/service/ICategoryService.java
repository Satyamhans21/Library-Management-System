package com.hexaware.librarymanagement.service;

import com.hexaware.librarymanagement.dto.CategoryDTO;

import java.util.List;

public interface ICategoryService {
    CategoryDTO addCategory(CategoryDTO categoryDTO); // Accept and return CategoryDTO for abstraction
    List<CategoryDTO> getAllCategories();            // Return a list of CategoryDTOs
    CategoryDTO getCategoryById(int categoryId);     // Return a CategoryDTO for a specific ID


}
