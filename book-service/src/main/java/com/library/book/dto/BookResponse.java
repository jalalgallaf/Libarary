package com.library.book.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private Integer publicationYear;
    private String categoryName;
    private Long categoryId;
}
