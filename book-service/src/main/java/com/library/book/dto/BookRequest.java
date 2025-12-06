package com.library.book.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookRequest {
    private String title;
    private String author;
    private String isbn;
    private Integer publicationYear;
    private Long categoryId; 
    // Optionally allow creating a category by name if it doesn't exist, 
    // but for strict microservices usually we pass ID. 
    // I'll add categoryName for convenience in this demo.
    private String categoryName;
}
