/*
 *  Copyright 2024 Bloomreach Inc. (https://www.bloomreach.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.forge.resetpassword.frontend;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResetPasswordRateLimiterTest {

    @Test
    void first_request_for_any_user_is_allowed() {
        final var limiter = new ResetPasswordRateLimiter(Duration.ofMinutes(1));
        assertTrue(limiter.isAllowed("alice"));
    }

    @Test
    void request_within_cooldown_window_is_blocked() {
        final Instant now = Instant.now();
        final var limiter = new ResetPasswordRateLimiter(Duration.ofMinutes(1), () -> now);
        limiter.recordAttempt("alice");
        assertFalse(limiter.isAllowed("alice"));
    }

    @Test
    void request_at_exactly_cooldown_boundary_is_blocked() {
        final AtomicReference<Instant> clock = new AtomicReference<>(Instant.now());
        final var limiter = new ResetPasswordRateLimiter(Duration.ofMinutes(1), clock::get);
        limiter.recordAttempt("alice");
        clock.set(clock.get().plus(Duration.ofMinutes(1)));
        // isAfter is strict: now == expiry means NOT yet past cooldown
        assertFalse(limiter.isAllowed("alice"));
    }

    @Test
    void request_after_cooldown_is_allowed() {
        final AtomicReference<Instant> clock = new AtomicReference<>(Instant.now());
        final var limiter = new ResetPasswordRateLimiter(Duration.ofMinutes(1), clock::get);
        limiter.recordAttempt("alice");
        clock.set(clock.get().plus(Duration.ofMinutes(1)).plusSeconds(1));
        assertTrue(limiter.isAllowed("alice"));
    }

    @Test
    void different_users_have_independent_limits() {
        final Instant now = Instant.now();
        final var limiter = new ResetPasswordRateLimiter(Duration.ofMinutes(1), () -> now);
        limiter.recordAttempt("alice");
        assertTrue(limiter.isAllowed("bob"));
    }

    @Test
    void second_attempt_resets_cooldown_timer() {
        final AtomicReference<Instant> clock = new AtomicReference<>(Instant.now());
        final var limiter = new ResetPasswordRateLimiter(Duration.ofMinutes(1), clock::get);
        limiter.recordAttempt("alice");
        // advance past cooldown so a second attempt is recorded
        clock.set(clock.get().plus(Duration.ofMinutes(2)));
        limiter.recordAttempt("alice");
        // now back within cooldown window of the second attempt
        clock.set(clock.get().minus(Duration.ofSeconds(30)));
        assertFalse(limiter.isAllowed("alice"));
    }
}
