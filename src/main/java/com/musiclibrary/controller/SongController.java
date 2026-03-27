package com.musiclibrary.controller;

import com.musiclibrary.dto.SongDto;
import com.musiclibrary.model.Song;
import com.musiclibrary.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @GetMapping
    public String listSongs(@RequestParam(defaultValue = "") String query,
                             @RequestParam(defaultValue = "") String genre,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "title") String sort,
                             Model model) {
        PageRequest pageable = PageRequest.of(page, 12, Sort.by(sort).ascending());
        Page<Song> songPage;

        if (!query.isBlank()) {
            songPage = songService.searchSongs(query, pageable);
        } else if (!genre.isBlank()) {
            songPage = songService.findByGenre(genre, pageable);
        } else {
            songPage = songService.findAll(pageable);
        }

        model.addAttribute("songs", songPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", songPage.getTotalPages());
        model.addAttribute("totalSongs", songPage.getTotalElements());
        model.addAttribute("query", query);
        model.addAttribute("selectedGenre", genre);
        model.addAttribute("genres", songService.findAllGenres());
        model.addAttribute("sort", sort);
        return "song/list";
    }

    @GetMapping("/{id}")
    public String viewSong(@PathVariable Long id, Model model) {
        Song song = songService.findById(id)
                .orElseThrow(() -> new com.musiclibrary.exception.ResourceNotFoundException("Song not found"));
        model.addAttribute("song", song);
        return "song/view";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newSongForm(Model model) {
        model.addAttribute("songDto", new SongDto());
        model.addAttribute("genres", songService.findAllGenres());
        return "song/form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String createSong(@Valid @ModelAttribute("songDto") SongDto dto,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("genres", songService.findAllGenres());
            return "song/form";
        }
        Song song = songService.createSong(dto);
        redirectAttributes.addFlashAttribute("successMessage",
                "Song '" + song.getTitle() + "' added successfully!");
        return "redirect:/songs";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editSongForm(@PathVariable Long id, Model model) {
        Song song = songService.findById(id)
                .orElseThrow(() -> new com.musiclibrary.exception.ResourceNotFoundException("Song not found"));
        model.addAttribute("songDto", songService.toDto(song));
        model.addAttribute("songId", id);
        model.addAttribute("genres", songService.findAllGenres());
        return "song/form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateSong(@PathVariable Long id,
                              @Valid @ModelAttribute("songDto") SongDto dto,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("songId", id);
            model.addAttribute("genres", songService.findAllGenres());
            return "song/form";
        }
        Song updated = songService.updateSong(id, dto);
        redirectAttributes.addFlashAttribute("successMessage",
                "Song '" + updated.getTitle() + "' updated successfully!");
        return "redirect:/songs/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteSong(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        songService.deleteSong(id);
        redirectAttributes.addFlashAttribute("successMessage", "Song deleted successfully.");
        return "redirect:/songs";
    }

    @PostMapping("/{id}/play")
    public String playSong(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        songService.incrementPlayCount(id);
        redirectAttributes.addFlashAttribute("infoMessage", "Now playing!");
        return "redirect:/songs/" + id;
    }
}
