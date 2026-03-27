package com.musiclibrary.service;

import com.musiclibrary.dto.SongDto;
import com.musiclibrary.model.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SongService {
    Song createSong(SongDto dto);
    Song updateSong(Long id, SongDto dto);
    void deleteSong(Long id);
    Optional<Song> findById(Long id);
    Page<Song> findAll(Pageable pageable);
    Page<Song> searchSongs(String query, Pageable pageable);
    Page<Song> findByGenre(String genre, Pageable pageable);
    List<Song> findByArtist(String artist);
    List<Song> findTopSongs(int limit);
    List<String> findAllGenres();
    void incrementPlayCount(Long id);
    List<Song> findSongsNotInPlaylist(Long playlistId);
    SongDto toDto(Song song);
    Song fromDto(SongDto dto);
}
