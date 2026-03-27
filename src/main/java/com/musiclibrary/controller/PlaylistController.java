package com.musiclibrary.controller;

import com.musiclibrary.dto.PlaylistDto;
import com.musiclibrary.exception.ResourceNotFoundException;
import com.musiclibrary.model.Playlist;
import com.musiclibrary.model.User;
import com.musiclibrary.service.PlaylistService;
import com.musiclibrary.service.SongService;
import com.musiclibrary.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;
    private final UserService userService;
    private final SongService songService;

    @GetMapping
    public String listPlaylists(@RequestParam(defaultValue = "") String query, Model model) {
        User currentUser = userService.getCurrentUser();
        var playlists = query.isBlank()
                ? playlistService.findByOwner(currentUser.getId())
                : playlistService.searchByOwner(currentUser.getId(), query);

        model.addAttribute("playlists", playlists);
        model.addAttribute("query", query);
        model.addAttribute("user", currentUser);
        return "playlist/list";
    }

    @GetMapping("/{id}")
    public String viewPlaylist(@PathVariable Long id, Model model) {
        User currentUser = userService.getCurrentUser();
        Playlist playlist = playlistService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

        // Allow viewing if public or owner
        boolean isOwner = playlist.getOwner().getId().equals(currentUser.getId());
        if (!playlist.isPublic() && !isOwner) {
            return "redirect:/playlists";
        }

        model.addAttribute("playlist", playlist);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("availableSongs", isOwner ? songService.findSongsNotInPlaylist(id) : null);
        return "playlist/view";
    }

    @GetMapping("/new")
    public String newPlaylistForm(Model model) {
        model.addAttribute("playlistDto", new PlaylistDto());
        return "playlist/form";
    }

    @PostMapping("/new")
    public String createPlaylist(@Valid @ModelAttribute("playlistDto") PlaylistDto dto,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "playlist/form";
        }
        User currentUser = userService.getCurrentUser();
        Playlist playlist = playlistService.createPlaylist(dto, currentUser.getId());
        redirectAttributes.addFlashAttribute("successMessage",
                "Playlist '" + playlist.getName() + "' created!");
        return "redirect:/playlists/" + playlist.getId();
    }

    @GetMapping("/{id}/edit")
    public String editPlaylistForm(@PathVariable Long id, Model model) {
        User currentUser = userService.getCurrentUser();
        Playlist playlist = playlistService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

        if (!playlist.getOwner().getId().equals(currentUser.getId())) {
            return "redirect:/playlists";
        }

        model.addAttribute("playlistDto", playlistService.toDto(playlist));
        model.addAttribute("playlistId", id);
        return "playlist/form";
    }

    @PostMapping("/{id}/edit")
    public String updatePlaylist(@PathVariable Long id,
                                  @Valid @ModelAttribute("playlistDto") PlaylistDto dto,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "playlist/form";
        }
        User currentUser = userService.getCurrentUser();
        Playlist updated = playlistService.updatePlaylist(id, dto, currentUser.getId());
        redirectAttributes.addFlashAttribute("successMessage",
                "Playlist '" + updated.getName() + "' updated!");
        return "redirect:/playlists/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deletePlaylist(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        playlistService.deletePlaylist(id, currentUser.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Playlist deleted.");
        return "redirect:/playlists";
    }

    // ── Song Management ──────────────────────────────────────────

    @PostMapping("/{playlistId}/songs/add")
    public String addSong(@PathVariable Long playlistId,
                           @RequestParam Long songId,
                           RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        playlistService.addSongToPlaylist(playlistId, songId, currentUser.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Song added to playlist!");
        return "redirect:/playlists/" + playlistId;
    }

    @PostMapping("/{playlistId}/songs/{songId}/remove")
    public String removeSong(@PathVariable Long playlistId,
                              @PathVariable Long songId,
                              RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        playlistService.removeSongFromPlaylist(playlistId, songId, currentUser.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Song removed from playlist.");
        return "redirect:/playlists/" + playlistId;
    }

    @GetMapping("/public")
    public String publicPlaylists(Model model) {
        model.addAttribute("playlists", playlistService.findPublicPlaylists());
        return "playlist/public";
    }
}
