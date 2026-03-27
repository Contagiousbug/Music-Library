package com.musiclibrary.controller;

import com.musiclibrary.model.User;
import com.musiclibrary.service.PlaylistService;
import com.musiclibrary.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PlaylistService playlistService;

    @GetMapping("/profile")
    public String profile(Model model) {
        User currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        model.addAttribute("playlists", playlistService.findByOwner(currentUser.getId()));
        model.addAttribute("playlistCount", playlistService.countByOwner(currentUser.getId()));
        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String displayName,
                                 @RequestParam String email,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        try {
            userService.updateProfile(currentUser.getId(), displayName, email);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                  @RequestParam String newPassword,
                                  @RequestParam String confirmNewPassword,
                                  RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmNewPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match.");
            return "redirect:/users/profile";
        }
        User currentUser = userService.getCurrentUser();
        try {
            userService.changePassword(currentUser.getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users/profile";
    }

    // ── Admin ─────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String listUsers(@RequestParam(defaultValue = "") String query, Model model) {
        var users = query.isBlank()
                ? userService.findAllUsers()
                : userService.searchUsers(query);
        model.addAttribute("users", users);
        model.addAttribute("query", query);
        model.addAttribute("totalActive", userService.countActiveUsers());
        return "user/list";
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public String deactivateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.deactivateUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "User deactivated.");
        return "redirect:/users";
    }
}
