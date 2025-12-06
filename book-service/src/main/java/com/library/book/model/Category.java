package com.library.book.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    // One category can have multiple books (optional bidirectional mapping, 
    // but usually better to keep it simple or just mapped by book)
    // For this example, we will focus on the Book owning the relationship.
}
