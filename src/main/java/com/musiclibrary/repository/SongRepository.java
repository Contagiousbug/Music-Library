package com.musiclibrary.repository;

import com.musiclibrary.model.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    // Full-text style search across title, artist, album
    @Query("SELECT s FROM Song s WHERE " +
           "LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.artist) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.album) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Song> searchSongs(@Param("query") String query, Pageable pageable);

    // Filter by genre
    Page<Song> findByGenreIgnoreCase(String genre, Pageable pageable);

    // Find by artist
    List<Song> findByArtistIgnoreCaseOrderByTitleAsc(String artist);

    // Top played songs (optimized projection)
    @Query("SELECT s FROM Song s ORDER BY s.playCount DESC")
    List<Song> findTopSongs(Pageable pageable);

    // Distinct genres for filter dropdown
    @Query("SELECT DISTINCT s.genre FROM Song s WHERE s.genre IS NOT NULL ORDER BY s.genre")
    List<String> findAllGenres();

    // Increment play count efficiently without loading the entity
    @Modifying
    @Query("UPDATE Song s SET s.playCount = s.playCount + 1 WHERE s.id = :id")
    void incrementPlayCount(@Param("id") Long id);

    // Songs NOT in a specific playlist
    @Query("SELECT s FROM Song s WHERE s.id NOT IN " +
           "(SELECT ps.song.id FROM PlaylistSong ps WHERE ps.playlist.id = :playlistId)")
    List<Song> findSongsNotInPlaylist(@Param("playlistId") Long playlistId);

    // Songs in a playlist (via join)
    @Query("SELECT ps.song FROM PlaylistSong ps WHERE ps.playlist.id = :playlistId ORDER BY ps.position")
    List<Song> findSongsByPlaylistId(@Param("playlistId") Long playlistId);

    long countByArtistIgnoreCase(String artist);
}
