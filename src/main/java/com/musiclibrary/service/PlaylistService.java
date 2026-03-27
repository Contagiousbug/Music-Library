package com.musiclibrary.service;

import com.musiclibrary.dto.PlaylistDto;
import com.musiclibrary.model.Playlist;

import java.util.List;
import java.util.Optional;

public interface PlaylistService {
    Playlist createPlaylist(PlaylistDto dto, Long ownerId);
    Playlist updatePlaylist(Long id, PlaylistDto dto, Long ownerId);
    void deletePlaylist(Long id, Long ownerId);
    Optional<Playlist> findById(Long id);
    List<Playlist> findByOwner(Long ownerId);
    List<Playlist> findPublicPlaylists();
    List<Playlist> searchByOwner(Long ownerId, String query);

    // Song management within playlists
    Playlist addSongToPlaylist(Long playlistId, Long songId, Long ownerId);
    Playlist removeSongFromPlaylist(Long playlistId, Long songId, Long ownerId);
    void reorderSong(Long playlistId, Long songId, int newPosition, Long ownerId);

    PlaylistDto toDto(Playlist playlist);
    long countByOwner(Long ownerId);
}
