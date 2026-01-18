package com.harding.meals.service.auth;

import com.harding.meals.repository.AppJwtTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Scheduled service to clean up expired JWT tokens from the database.
 *
 * This service runs automatically on a scheduled basis to delete expired
 * refresh tokens that are no longer needed, keeping the database clean
 * and performant.
 */
@Service
public class TokenCleanupService {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupService.class);

    private final AppJwtTokenRepository tokenRepository;

    public TokenCleanupService(AppJwtTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Runs daily at 2 AM to delete expired tokens.
     * Tokens that expired more than 1 day ago are removed.
     *
     * Schedule: 0 0 2 * * ? = At 02:00:00am every day
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting expired token cleanup");

        try {
            // Delete tokens that expired more than 1 day ago
            OffsetDateTime cutoff = OffsetDateTime.now().minusDays(1);
            int deletedCount = tokenRepository.deleteExpiredTokens(cutoff);

            log.info("Expired token cleanup completed. Deleted {} tokens", deletedCount);
        } catch (Exception e) {
            log.error("Error during token cleanup: {}", e.getMessage(), e);
        }
    }
}
