package com.musiclibrary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlaylistDto {

    private Long id;

    @NotBlank(message = "Playlist name is required")
    @Size(min = 1, max = 200, message = "Name must be 1-200 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private String coverImageUrl;

    private boolean isPublic = false;
}
