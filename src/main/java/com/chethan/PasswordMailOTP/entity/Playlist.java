package com.chethan.PasswordMailOTP.entity;

import jakarta.persistence.*;

import java.util.List;

public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany
    @JoinTable(
            name = "playlist_podcast",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "podcast_id")
    )
    private List<Podcast> podcasts;
}
