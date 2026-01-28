package com.aeris2.controller;
//
import com.aeris2.model.Category;
import com.aeris2.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin // allow frontend access
public class CategoryController {

    private final CategoryRepository categoryRepo;

    public CategoryController(CategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    // -----------------------------------------------------------
    // GET ALL CATEGORIES
    // -----------------------------------------------------------
    @GetMapping
    public List<Category> getAll() {
        return categoryRepo.findAll();
    }

    // -----------------------------------------------------------
    // GET CATEGORY BY ID
    // -----------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable Long id) {
        return categoryRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // -----------------------------------------------------------
    // CREATE CATEGORY (ADMIN)
    // -----------------------------------------------------------
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Category c) {

        if (c.getName() == null || c.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Category name is required");
        }

        // prevent duplicate names
        if (categoryRepo.existsByNameIgnoreCase(c.getName().trim())) {
            return ResponseEntity.badRequest().body("Category already exists");
        }

        Category saved = categoryRepo.save(c);
        return ResponseEntity.ok(saved);
    }

    // -----------------------------------------------------------
    // UPDATE CATEGORY (ADMIN)
    // -----------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Category c) {
        return categoryRepo.findById(id).map(existing -> {

            if (c.getName() != null && !c.getName().trim().isEmpty()) {
                existing.setName(c.getName().trim());
            }

            Category updated = categoryRepo.save(existing);
            return ResponseEntity.ok(updated);

        }).orElse(ResponseEntity.notFound().build());
    }

    // -----------------------------------------------------------
    // DELETE CATEGORY (ADMIN)
    // -----------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!categoryRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        categoryRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
