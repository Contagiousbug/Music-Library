package com.musiclibrary.service.impl;

import com.musiclibrary.dto.PlaylistDto;
import com.musiclibrary.exception.AccessDeniedException;
import com.musiclibrary.exception.ResourceNotFoundException;
import com.musiclibrary.model.Playlist;
import com.musiclibrary.model.PlaylistSong;
import com.musiclibrary.model.Song;
import com.musiclibrary.model.User;
import com.musiclibrary.repository.PlaylistRepository;
import com.musiclibrary.repository.PlaylistSongRepository;
import com.musiclibrary.repository.SongRepository;
import com.musiclibrary.repository.UserRepository;
import com.musiclibrary.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;

    @Override
    public Playlist createPlaylist(PlaylistDto dto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Playlist playlist = new Playlist();
        playlist.setName(dto.getName());
        playlist.setDescription(dto.getDescription());
        playlist.setCoverImageUrl(dto.getCoverImageUrl());
        playlist.setPublic(dto.isPublic());
        playlist.setOwner(owner);

        Playlist saved = playlistRepository.save(playlist);
        log.info("Playlist '{}' created by user: {}", saved.getName(), owner.getUsername());
        return saved;
    }

    @Override
    public Playlist updatePlaylist(Long id, PlaylistDto dto, Long ownerId) {
        Playlist playlist = playlistRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new AccessDeniedException("Playlist not found or access denied"));

        playlist.setName(dto.getName());
        playlist.setDescription(dto.getDescription());
        playlist.setCoverImageUrl(dto.getCoverImageUrl());
        playlist.setPublic(dto.isPublic());

        return playlistRepository.save(playlist);
    }

    @Override
    public void deletePlaylist(Long id, Long ownerId) {
        Playlist playlist = playlistRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new AccessDeniedException("Playlist not found or access denied"));
        playlistRepository.delete(playlist);
        log.info("Playlist {} deleted by user {}", id, ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Playlist> findById(Long id) {
        return playlistRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Playlist> findByOwner(Long ownerId) {
        return playlistRepository.findByOwnerIdWithSongs(ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Playlist> findPublicPlaylists() {
        return playlistRepository.findPublicPlaylists(PageRequest.of(0, 20)).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Playlist> searchByOwner(Long ownerId, String query) {
        return playlistRepository.searchByOwner(ownerId, query);
    }

    @Override
    public Playlist addSongToPlaylist(Long playlistId, Long songId, Long ownerId) {
        Playlist playlist = playlistRepository.findByIdAndOwnerId(playlistId, ownerId)
                .orElseThrow(() -> new AccessDeniedException("Playlist not found or access denied"));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found"));

        // Prevent duplicates
        boolean alreadyExists = playlistRepository.existsSongInPlaylist(playlistId, songId);
        if (alreadyExists) {
            log.info("Song {} already in playlist {}", songId, playlistId);
            return playlist;
        }

        Integer maxPos = playlistSongRepository.findMaxPositionByPlaylistId(playlistId);
        int nextPos = (maxPos == null ? 0 : maxPos) + 1;

        PlaylistSong ps = new PlaylistSong();
        ps.setPlaylist(playlist);
        ps.setSong(song);
        ps.setPosition(nextPos);
        playlistSongRepository.save(ps);

        log.info("Song '{}' added to playlist '{}'", song.getTitle(), playlist.getName());
        return playlistRepository.findById(playlistId).orElse(playlist);
    }

    @Override
    public Playlist removeSongFromPlaylist(Long playlistId, Long songId, Long ownerId) {
        Playlist playlist = playlistRepository.findByIdAndOwnerId(playlistId, ownerId)
                .orElseThrow(() -> new AccessDeniedException("Playlist not found or access denied"));

        PlaylistSong ps = playlistSongRepository.findByPlaylistIdAndSongId(playlistId, songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found in this playlist"));

        Integer removedPos = ps.getPosition();
        playlistSongRepository.delete(ps);

        // Shift remaining songs down
        if (removedPos != null) {
            playlistSongRepository.shiftPositionsDown(playlistId, removedPos);
        }

        return playlistRepository.findById(playlistId).orElse(playlist);
    }

    @Override
    public void reorderSong(Long playlistId, Long songId, int newPosition, Long ownerId) {
        playlistRepository.findByIdAndOwnerId(playlistId, ownerId)
                .orElseThrow(() -> new AccessDeniedException("Playlist not found or access denied"));

        PlaylistSong ps = playlistSongRepository.findByPlaylistIdAndSongId(playlistId, songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not in playlist"));

        ps.setPosition(newPosition);
        playlistSongRepository.save(ps);
    }

    @Override
    public PlaylistDto toDto(Playlist playlist) {
        PlaylistDto dto = new PlaylistDto();
        dto.setId(playlist.getId());
        dto.setName(playlist.getName());
        dto.setDescription(playlist.getDescription());
        dto.setCoverImageUrl(playlist.getCoverImageUrl());
        dto.setPublic(playlist.isPublic());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public long countByOwner(Long ownerId) {
        return playlistRepository.countByOwnerId(ownerId);
    }
}
