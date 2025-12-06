package com.library.book.service;

import com.library.book.dto.BookRequest;
import com.library.book.dto.BookResponse;
import com.library.book.model.Book;
import com.library.book.model.Category;
import com.library.book.repository.BookRepository;
import com.library.book.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public BookResponse createBook(BookRequest request) {
        Category category;
        
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
        } else if (request.getCategoryName() != null) {
            category = categoryRepository.findByName(request.getCategoryName())
                    .orElseGet(() -> categoryRepository.save(Category.builder()
                            .name(request.getCategoryName())
                            .description("Auto-created category")
                            .build()));
        } else {
            throw new IllegalArgumentException("Category ID or Name must be provided");
        }

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .publicationYear(request.getPublicationYear())
                .category(category)
                .build();

        Book savedBook = bookRepository.save(book);

        return mapToResponse(savedBook);
    }

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BookResponse getBookById(Long id) {
        return bookRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }

    private BookResponse mapToResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .publicationYear(book.getPublicationYear())
                .categoryId(book.getCategory() != null ? book.getCategory().getId() : null)
                .categoryName(book.getCategory() != null ? book.getCategory().getName() : null)
                .build();
    }
}
