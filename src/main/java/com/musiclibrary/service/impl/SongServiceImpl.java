package com.musiclibrary.service.impl;

import com.musiclibrary.dto.SongDto;
import com.musiclibrary.exception.ResourceNotFoundException;
import com.musiclibrary.model.Song;
import com.musiclibrary.repository.SongRepository;
import com.musiclibrary.service.SongService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;

    @Override
    public Song createSong(SongDto dto) {
        Song song = fromDto(dto);
        song.setPlayCount(0L);
        Song saved = songRepository.save(song);
        log.info("Song created: {} by {}", saved.getTitle(), saved.getArtist());
        return saved;
    }

    @Override
    public Song updateSong(Long id, SongDto dto) {
        Song existing = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + id));

        existing.setTitle(dto.getTitle());
        existing.setArtist(dto.getArtist());
        existing.setAlbum(dto.getAlbum());
        existing.setGenre(dto.getGenre());
        existing.setDurationSeconds(dto.getDurationSeconds());
        existing.setReleaseYear(dto.getReleaseYear());
        existing.setCoverImageUrl(dto.getCoverImageUrl());
        existing.setAudioUrl(dto.getAudioUrl());
        existing.setLyrics(dto.getLyrics());

        return songRepository.save(existing);
    }

    @Override
    public void deleteSong(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + id));
        songRepository.delete(song);
        log.info("Song deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Song> findById(Long id) {
        return songRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Song> findAll(Pageable pageable) {
        return songRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Song> searchSongs(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return songRepository.findAll(pageable);
        }
        return songRepository.searchSongs(query.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Song> findByGenre(String genre, Pageable pageable) {
        return songRepository.findByGenreIgnoreCase(genre, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Song> findByArtist(String artist) {
        return songRepository.findByArtistIgnoreCaseOrderByTitleAsc(artist);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Song> findTopSongs(int limit) {
        return songRepository.findTopSongs(PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAllGenres() {
        return songRepository.findAllGenres();
    }

    @Override
    public void incrementPlayCount(Long id) {
        songRepository.incrementPlayCount(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Song> findSongsNotInPlaylist(Long playlistId) {
        return songRepository.findSongsNotInPlaylist(playlistId);
    }

    @Override
    public SongDto toDto(Song song) {
        SongDto dto = new SongDto();
        dto.setId(song.getId());
        dto.setTitle(song.getTitle());
        dto.setArtist(song.getArtist());
        dto.setAlbum(song.getAlbum());
        dto.setGenre(song.getGenre());
        dto.setDurationSeconds(song.getDurationSeconds());
        dto.setReleaseYear(song.getReleaseYear());
        dto.setCoverImageUrl(song.getCoverImageUrl());
        dto.setAudioUrl(song.getAudioUrl());
        dto.setLyrics(song.getLyrics());
        return dto;
    }

    @Override
    public Song fromDto(SongDto dto) {
        Song song = new Song();
        song.setTitle(dto.getTitle());
        song.setArtist(dto.getArtist());
        song.setAlbum(dto.getAlbum());
        song.setGenre(dto.getGenre());
        song.setDurationSeconds(dto.getDurationSeconds());
        song.setReleaseYear(dto.getReleaseYear());
        song.setCoverImageUrl(dto.getCoverImageUrl());
        song.setAudioUrl(dto.getAudioUrl());
        song.setLyrics(dto.getLyrics());
        return song;
    }
}
