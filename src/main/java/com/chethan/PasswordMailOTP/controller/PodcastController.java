package com.chethan.PasswordMailOTP.controller;

import com.chethan.PasswordMailOTP.entity.Podcast;
import com.chethan.PasswordMailOTP.entity.User;
import com.chethan.PasswordMailOTP.repository.PodcastRepo;
import com.chethan.PasswordMailOTP.service.PodcastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/podcasts")
public class PodcastController {

    @Autowired
    private PodcastService podcastService;

    @Autowired
    private PodcastRepo podcastRepo;


    @GetMapping
    public ResponseEntity<List<Podcast>> getAllPodcasts(){
        List<Podcast> podcasts = podcastService.getAllPodcasts();
        return ResponseEntity.ok(podcasts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPodcastById(@PathVariable Long id){
        Optional<Podcast> optional = podcastService.getPodcast(id);
        return optional.map(ResponseEntity::ok).orElseGet(()->ResponseEntity.status(404).build());
    }

    @PostMapping("/fetch")
    public ResponseEntity<?> fetchPodcast(@RequestBody Map<String, String> body){
        String rssUrl = body.get("rssUrl");
        if(rssUrl == null || rssUrl.isBlank()){
            return  ResponseEntity.badRequest().body("RssUrl is Required in body");
        }
        Optional<Podcast> saved = podcastService.fetchAndSave(rssUrl);
        return saved.map(ResponseEntity::ok).orElseGet(()->ResponseEntity.status(404).build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Podcast>> search(@RequestParam String keyword) {
        List<Podcast> results = podcastService.searchPodcasts(keyword);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/play/{id}")
    public ResponseEntity<?> playPodcast(@PathVariable Long id){
        Optional<Podcast> viewPodcast = podcastService.incrementViews(id);
        return viewPodcast
                .map(ResponseEntity::ok)
                .orElseGet(()->ResponseEntity.status(404).build());
    }

    @GetMapping("/trending")
    public ResponseEntity<List<Podcast>> getTrendingPodcast(){
        List<Podcast> trending = podcastRepo.findTop10ByOrderByViewsDesc();
        return ResponseEntity.ok(trending);
    }
}
