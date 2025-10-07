package com.chethan.PasswordMailOTP.service;


import com.chethan.PasswordMailOTP.entity.Podcast;
import com.chethan.PasswordMailOTP.entity.User;
import com.chethan.PasswordMailOTP.repository.PodcastRepo;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PodcastService {

    @Autowired
    private PodcastRepo podcastRepo;

    public List<Podcast> getAllPodcasts() {
        return podcastRepo.findAll();
    }

    public Optional<Podcast> getPodcast(Long id) {
        return podcastRepo.findById(id);
    }

    @Transactional
    public Optional<Podcast> fetchAndSave(String url) {
        if(url == null || url.isBlank())
            return Optional.empty();
        try {
            Podcast podcast = podcastRepo.findByRssUrl(url).orElseGet(Podcast::new);
            podcast.setRssUrl(url);
            podcast.setLastUpdated(LocalDateTime.now());

            if (isYouTubeUrl(url)) {
                // --- Handle YouTube link using oEmbed ---
                podcast.setSourceType("YOUTUBE");
                podcast.setCategory("YouTube");
                JSONObject oembed = fetchYouTubeMetadata(url);
                if (oembed != null) {
                    podcast.setTitle(oembed.optString("title", "YouTube Video"));
                    podcast.setAuthor(oembed.optString("author_name", "YouTube"));
                    podcast.setImageUrl(oembed.optString("thumbnail_url", null));
                    podcast.setDescription("Embedded YouTube video");
                } else {
                    podcast.setTitle("YouTube Video");
                    podcast.setAuthor("YouTube");
                    podcast.setImageUrl(null);
                    podcast.setDescription("Embedded YouTube video");
                }
            } else {
                // --- Handle RSS feed ---
                URL feedSource = new URL(url);

                // Step 1: Read as plain text
                String xml = new String(feedSource.openStream().readAllBytes(), StandardCharsets.UTF_8);

                // Step 2: Remove <!DOCTYPE ...>
                xml = xml.replaceAll("<!DOCTYPE[^>]*>", "");

                // Step 3: Parse sanitized XML with Rome
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(new URL(url)));


                podcast.setSourceType("RSS");
                podcast.setTitle(feed.getTitle());
                podcast.setDescription(feed.getDescription());
                podcast.setAuthor(feed.getAuthor());
                podcast.setImageUrl(feed.getImage() != null ? feed.getImage().getUrl() : null);
                if (feed.getCategories() != null && !feed.getCategories().isEmpty()) {
                    podcast.setCategory(feed.getCategories().get(0).getName());
                }
            }

            podcast = podcastRepo.save(podcast);
            return Optional.of(podcast);

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // --- Helper Methods ---

    private boolean isYouTubeUrl(String url) {

        return url.contains("youtube.com") || url.contains("youtu.be");
    }

    private JSONObject fetchYouTubeMetadata(String videoUrl) {
        try {
            String oembedUrl = "https://www.youtube.com/oembed?url=" + videoUrl + "&format=json";
            HttpURLConnection connection = (HttpURLConnection) new URL(oembedUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() == 200) {
                String jsonText = new String(connection.getInputStream().readAllBytes());
                return new JSONObject(jsonText);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Podcast> searchPodcasts(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of(); // return empty list instead of null
        }
        return podcastRepo.search(keyword);
    }

    @Transactional
    public Optional<Podcast> incrementViews(Long id){
        Optional<Podcast> optionalPodcast = podcastRepo.findById(id);
        if(optionalPodcast.isPresent()){
            Podcast podcast = optionalPodcast.get();
            podcast.incrementViews();
            podcastRepo.save(podcast);
            return Optional.of(podcast);
        }
        return Optional.empty();
    }
}
