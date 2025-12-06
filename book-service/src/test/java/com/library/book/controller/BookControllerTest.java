package com.library.book.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.book.dto.BookRequest;
import com.library.book.dto.BookResponse;
import com.library.book.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBook_ShouldReturnCreatedBook() throws Exception {
        BookRequest request = BookRequest.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("978-0132350884")
                .publicationYear(2008)
                .categoryName("Programming")
                .build();

        BookResponse response = BookResponse.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("978-0132350884")
                .publicationYear(2008)
                .categoryName("Programming")
                .categoryId(10L)
                .build();

        when(bookService.createBook(any(BookRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.categoryName").value("Programming"));
    }

    @Test
    void createBook_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        BookRequest request = BookRequest.builder()
                .title("") // Invalid: blank
                .author("") // Invalid: blank
                .isbn("") // Invalid: blank
                .publicationYear(-100) // Invalid: negative
                .build();

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Title is required"))
                .andExpect(jsonPath("$.author").value("Author is required"))
                .andExpect(jsonPath("$.isbn").value("ISBN is required"))
                .andExpect(jsonPath("$.publicationYear").value("Publication year must be positive"));
    }
}
