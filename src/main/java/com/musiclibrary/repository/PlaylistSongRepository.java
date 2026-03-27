package com.musiclibrary.repository;

import com.musiclibrary.model.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    Optional<PlaylistSong> findByPlaylistIdAndSongId(Long playlistId, Long songId);

    void deleteByPlaylistIdAndSongId(Long playlistId, Long songId);

    @Query("SELECT COALESCE(MAX(ps.position), 0) FROM PlaylistSong ps WHERE ps.playlist.id = :playlistId")
    Integer findMaxPositionByPlaylistId(@Param("playlistId") Long playlistId);

    @Modifying
    @Query("UPDATE PlaylistSong ps SET ps.position = ps.position - 1 " +
           "WHERE ps.playlist.id = :playlistId AND ps.position > :position")
    void shiftPositionsDown(@Param("playlistId") Long playlistId, @Param("position") Integer position);
}
