package com.learnwiremock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    private Long movie_id;
    private String name;
    private String cast;
    private LocalDate release_date;
    private Integer year;
}
