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

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Per-username rate limiter for password reset requests.
 * State is in-memory and does not survive restarts, which is intentional:
 * the goal is to slow down automated abuse, not provide a hard guarantee.
 */
class ResetPasswordRateLimiter {

    private final Map<String, Instant> lastAttempt = new ConcurrentHashMap<>();
    private final Duration cooldown;
    private final Supplier<Instant> clock;

    ResetPasswordRateLimiter(final Duration cooldown) {
        this(cooldown, Instant::now);
    }

    ResetPasswordRateLimiter(final Duration cooldown, final Supplier<Instant> clock) {
        this.cooldown = cooldown;
        this.clock = clock;
    }

    boolean isAllowed(final String userId) {
        final Instant last = lastAttempt.get(userId);
        if (last == null) {
            return true;
        }
        return clock.get().isAfter(last.plus(cooldown));
    }

    void recordAttempt(final String userId) {
        lastAttempt.put(userId, clock.get());
    }
}
