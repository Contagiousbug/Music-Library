package com.musiclibrary.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SongDto {

    private Long id;

    @NotBlank(message = "Song title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Artist name is required")
    @Size(max = 150)
    private String artist;

    @Size(max = 150)
    private String album;

    @Size(max = 50)
    private String genre;

    @Positive(message = "Duration must be a positive number")
    private Integer durationSeconds;

    @Min(value = 1900, message = "Release year must be after 1900")
    @Max(value = 2100, message = "Release year must be valid")
    private Integer releaseYear;

    private String coverImageUrl;
    private String audioUrl;
    private String lyrics;
}
