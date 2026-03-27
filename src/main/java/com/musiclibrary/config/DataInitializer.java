package com.musiclibrary.config;

import com.musiclibrary.model.Role;
import com.musiclibrary.model.Song;
import com.musiclibrary.model.User;
import com.musiclibrary.repository.RoleRepository;
import com.musiclibrary.repository.SongRepository;
import com.musiclibrary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedUsers();
        seedSongs();
        log.info("=== Data initialization complete ===");
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(null, Role.ERole.ROLE_USER));
            roleRepository.save(new Role(null, Role.ERole.ROLE_ADMIN));
            log.info("Roles seeded.");
        }
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            // Admin user
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@musiclibrary.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setDisplayName("Administrator");
            admin.setActive(true);
            Set<Role> adminRoles = new HashSet<>();
            roleRepository.findByName(Role.ERole.ROLE_ADMIN).ifPresent(adminRoles::add);
            roleRepository.findByName(Role.ERole.ROLE_USER).ifPresent(adminRoles::add);
            admin.setRoles(adminRoles);
            userRepository.save(admin);

            // Regular demo user
            User demo = new User();
            demo.setUsername("demo");
            demo.setEmail("demo@musiclibrary.com");
            demo.setPassword(passwordEncoder.encode("demo123"));
            demo.setDisplayName("Demo User");
            demo.setActive(true);
            Set<Role> userRoles = new HashSet<>();
            roleRepository.findByName(Role.ERole.ROLE_USER).ifPresent(userRoles::add);
            demo.setRoles(userRoles);
            userRepository.save(demo);

            log.info("Demo users seeded. admin/admin123  |  demo/demo123");
        }
    }

    private void seedSongs() {
        if (songRepository.count() == 0) {
            List<Song> songs = List.of(
                createSong("Bohemian Rhapsody",    "Queen",           "A Night at the Opera",  "Rock",      354, 1975),
                createSong("Hotel California",     "Eagles",          "Hotel California",       "Rock",      391, 1977),
                createSong("Stairway to Heaven",   "Led Zeppelin",    "Led Zeppelin IV",        "Rock",      482, 1971),
                createSong("Imagine",              "John Lennon",     "Imagine",                "Pop",       187, 1971),
                createSong("Billie Jean",          "Michael Jackson", "Thriller",               "Pop",       294, 1982),
                createSong("Smells Like Teen Spirit","Nirvana",       "Nevermind",              "Grunge",    301, 1991),
                createSong("Purple Rain",          "Prince",          "Purple Rain",            "R&B",       520, 1984),
                createSong("Like a Rolling Stone", "Bob Dylan",       "Highway 61 Revisited",   "Folk Rock", 369, 1965),
                createSong("Johnny B. Goode",      "Chuck Berry",     "Single",                 "Rock",      162, 1958),
                createSong("What's Going On",      "Marvin Gaye",     "What's Going On",        "Soul",      235, 1971),
                createSong("Superstition",         "Stevie Wonder",   "Talking Book",           "Funk",      245, 1972),
                createSong("Thriller",             "Michael Jackson", "Thriller",               "Pop",       357, 1982),
                createSong("Sweet Child O' Mine",  "Guns N' Roses",   "Appetite for Destruction","Rock",    356, 1987),
                createSong("Lose Yourself",        "Eminem",          "8 Mile",                 "Hip-Hop",   326, 2002),
                createSong("Blinding Lights",      "The Weeknd",      "After Hours",            "Synth-pop", 200, 2019),
                createSong("Shape of You",         "Ed Sheeran",      "÷ (Divide)",             "Pop",       234, 2017),
                createSong("Rolling in the Deep",  "Adele",           "21",                     "Soul",      228, 2010),
                createSong("Bad Guy",              "Billie Eilish",   "When We All Fall Asleep", "Electropop",194, 2019),
                createSong("Uptown Funk",          "Bruno Mars",      "Uptown Special",         "Funk",      270, 2014),
                createSong("Despacito",            "Luis Fonsi",      "Vida",                   "Reggaeton", 229, 2017)
            );
            songRepository.saveAll(songs);
            log.info("Sample songs seeded: {} songs", songs.size());
        }
    }

    private Song createSong(String title, String artist, String album, String genre,
                             int durationSec, int year) {
        Song s = new Song();
        s.setTitle(title);
        s.setArtist(artist);
        s.setAlbum(album);
        s.setGenre(genre);
        s.setDurationSeconds(durationSec);
        s.setReleaseYear(year);
        s.setPlayCount((long)(Math.random() * 10000));
        return s;
    }
}
