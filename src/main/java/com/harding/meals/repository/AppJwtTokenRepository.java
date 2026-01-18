package com.harding.meals.repository;

import com.harding.meals.entity.user.token.AppJwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface AppJwtTokenRepository extends JpaRepository<AppJwtToken, Long> {

    /**
     * Find a token by its JWT ID (jti claim).
     *
     * @param jti the JWT ID
     * @return the token if found
     */
    Optional<AppJwtToken> findByJti(String jti);

    /**
     * Revoke all non-revoked tokens for a specific user.
     * Used during logout to invalidate all active sessions.
     *
     * @param userId the user ID
     * @param revokedAt when the tokens were revoked
     */
    @Modifying
    @Query("UPDATE AppJwtToken t SET t.revoked = true, t.revokedAt = :revokedAt " +
           "WHERE t.user.id = :userId AND t.revoked = false")
    void revokeAllForUser(@Param("userId") Long userId, @Param("revokedAt") OffsetDateTime revokedAt);

    /**
     * Convenience method to revoke all tokens for a user with current timestamp.
     *
     * @param userId the user ID
     */
    default void revokeAllForUser(Long userId) {
        revokeAllForUser(userId, OffsetDateTime.now());
    }

    /**
     * Delete expired tokens older than the specified date.
     * Used by cleanup service to remove old tokens from database.
     *
     * @param before tokens that expired before this date will be deleted
     * @return number of deleted records
     */
    @Modifying
    @Query("DELETE FROM AppJwtToken t WHERE t.expiresAt < :before")
    int deleteExpiredTokens(@Param("before") OffsetDateTime before);
}
