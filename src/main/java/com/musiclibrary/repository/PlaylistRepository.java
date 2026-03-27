package com.musiclibrary.repository;

import com.musiclibrary.model.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    // Get all playlists by owner with song count (avoids N+1 with JOIN FETCH)
    @Query("SELECT DISTINCT p FROM Playlist p LEFT JOIN FETCH p.playlistSongs ps " +
           "LEFT JOIN FETCH ps.song WHERE p.owner.id = :ownerId ORDER BY p.createdAt DESC")
    List<Playlist> findByOwnerIdWithSongs(@Param("ownerId") Long ownerId);

    // Simple list without songs for overview
    List<Playlist> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    // Public playlists
    @Query("SELECT p FROM Playlist p WHERE p.isPublic = true ORDER BY p.createdAt DESC")
    Page<Playlist> findPublicPlaylists(Pageable pageable);

    // Find playlist by id and verify ownership
    Optional<Playlist> findByIdAndOwnerId(Long id, Long ownerId);

    // Search playlists by name (within owner's playlists)
    @Query("SELECT p FROM Playlist p WHERE p.owner.id = :ownerId AND " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Playlist> searchByOwner(@Param("ownerId") Long ownerId, @Param("query") String query);

    // Count playlists per user
    long countByOwnerId(Long ownerId);

    // Check if a song exists in a playlist
    @Query("SELECT COUNT(ps) > 0 FROM PlaylistSong ps WHERE ps.playlist.id = :playlistId AND ps.song.id = :songId")
    boolean existsSongInPlaylist(@Param("playlistId") Long playlistId, @Param("songId") Long songId);
}
