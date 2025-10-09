package com.chethan.PasswordMailOTP.service;

import com.chethan.PasswordMailOTP.entity.Podcast;
import com.chethan.PasswordMailOTP.repository.PodcastRepo;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.transaction.annotation.Transactional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class PodcastService {

    @Autowired
    private PodcastRepo podcastRepo;

    /**
     * Get all podcasts with pagination support
     * @param pageable pagination information (page number, page size, sorting)
     * @return Page containing the list of podcasts and pagination information
     */
    public Page<Podcast> getAllPodcasts(Pageable pageable) {
        return podcastRepo.findAll(pageable);
    }
    
    /**
     * Get all podcasts without pagination
     * @return List of all podcasts
     */
    public List<Podcast> getAllPodcastsList() {
        return podcastRepo.findAll();
    }

    public Optional<Podcast> getPodcast(Long id) {
        return podcastRepo.findById(id);
    }

    private boolean isYouTubeUrl(String url) {
        return url != null && (url.contains("youtube.com") || url.contains("youtu.be"));
    }

    @Transactional
    public Optional<Podcast> fetchAndSave(String url) {
        if (url == null || url.isBlank()) {
            return Optional.empty();
        }
        
        try {
            Podcast podcast = podcastRepo.findByRssUrl(url).orElseGet(Podcast::new);
            podcast.setRssUrl(url);
            podcast.setLastUpdated(LocalDateTime.now());

            if (isYouTubeUrl(url)) {
                // Handle YouTube link using oEmbed
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
                // Handle RSS feed
                try {
                    SyndFeedInput input = new SyndFeedInput();
                    SyndFeed feed = input.build(new XmlReader(URI.create(url).toURL()));
                    
                    podcast.setSourceType("RSS");
                    podcast.setTitle(feed.getTitle());
                    podcast.setDescription(feed.getDescription() != null ? feed.getDescription() : "");
                    podcast.setAuthor(feed.getAuthor());
                    // Set other fields as needed
                    
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse RSS feed: " + e.getMessage(), e);
                }
            }
            
            return Optional.of(podcastRepo.save(podcast));
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch and save podcast: " + e.getMessage(), e);
        }
    }

    private JSONObject fetchYouTubeMetadata(String videoUrl) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.youtube.com/oembed?url=" + videoUrl + "&format=json"))
                .header("Accept", "application/json")
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch YouTube metadata. HTTP error code: " + response.statusCode());
            }
            
            return new JSONObject(response.body());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch YouTube metadata: " + e.getMessage(), e);
        }
    }

    /**
     * Search podcasts by keyword with pagination support
     * @param keyword search term
     * @param pageable pagination information
     * @return Page containing matching podcasts and pagination info
     */
    public Page<Podcast> searchPodcasts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return Page.empty(pageable);
        }
        return podcastRepo.search(keyword, pageable);
    }
    
    /**
     * Search podcasts by keyword without pagination
     * @param keyword search term
     * @return List of matching podcasts
     */
    public List<Podcast> searchPodcastsList(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return podcastRepo.search(keyword);
    }

    @Transactional
    public Optional<Podcast> incrementViews(Long id) {
        return podcastRepo.findById(id).map(podcast -> {
            podcast.incrementViews();
            return podcastRepo.save(podcast);
        });
    }
    
    /**
     * Get podcasts by category with pagination
     * @param category category to filter by
     * @param pageable pagination information
     * @return Page of podcasts in the specified category
     */
    @Transactional(readOnly = true)
    public Page<Podcast> getPodcastsByCategory(String category, Pageable pageable) {
        if (category == null || category.isBlank()) {
            return Page.empty(pageable);
        }
        return podcastRepo.findByCategoryIgnoreCase(category, pageable);
    }
    
    /**
     * Get trending podcasts ordered by view count in descending order
     * @param limit maximum number of podcasts to return
     * @return List of trending podcasts
     */
    @Transactional(readOnly = true)
    public List<Podcast> getTrendingPodcasts(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        // Use the existing repository method if the limit is 10
        if (limit == 10) {
            return podcastRepo.findTop10ByOrderByViewsDesc();
        }
        // For other limits, use a custom query with Pageable
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        return podcastRepo.findTopNByOrderByViewsDesc(pageable);
    }
}
