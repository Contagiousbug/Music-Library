package com.musiclibrary.controller;

import com.musiclibrary.model.User;
import com.musiclibrary.service.PlaylistService;
import com.musiclibrary.service.SongService;
import com.musiclibrary.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final SongService songService;
    private final PlaylistService playlistService;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User currentUser = userService.getCurrentUser();

        model.addAttribute("user", currentUser);
        model.addAttribute("playlists", playlistService.findByOwner(currentUser.getId()));
        model.addAttribute("topSongs", songService.findTopSongs(5));
        model.addAttribute("recentSongs", songService.findAll(
                org.springframework.data.domain.PageRequest.of(0, 6)).getContent());
        model.addAttribute("playlistCount", playlistService.countByOwner(currentUser.getId()));
        model.addAttribute("genres", songService.findAllGenres());

        return "dashboard";
    }
}
