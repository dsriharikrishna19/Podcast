package com.chethan.PasswordMailOTP.repository;

import com.chethan.PasswordMailOTP.entity.Podcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PodcastRepo extends JpaRepository<Podcast, Long> {
    Optional<Podcast> findByRssUrl(String rssUrl);

    @Query("SELECT p FROM Podcast p " +
            "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(p.author) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            " OR LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%'))" )
    List<Podcast> search(@Param("keyword") String keyword);

    List<Podcast> findTop10ByOrderByViewsDesc();
}