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
import org.mockito.Mockito;

import javax.jcr.Node;
import javax.jcr.Property;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.onehippo.forge.resetpassword.frontend.ResetPasswordConst.PASSWORD_RESET_KEY;
import static org.onehippo.forge.resetpassword.frontend.ResetPasswordConst.PASSWORD_RESET_TIMESTAMP;

class SetPasswordPanelTest {

    // --- isCodeMatch ---

    @Test
    void isCodeMatch_returns_true_for_identical_codes() {
        assertTrue(SetPasswordPanel.isCodeMatch("abc-123", "abc-123"));
    }

    @Test
    void isCodeMatch_returns_false_for_different_codes() {
        assertFalse(SetPasswordPanel.isCodeMatch("abc-123", "xyz-456"));
    }

    @Test
    void isCodeMatch_is_case_sensitive() {
        assertFalse(SetPasswordPanel.isCodeMatch("ABC-123", "abc-123"));
    }

    // --- isLinkExpired ---

    @Test
    void isLinkExpired_returns_false_when_within_validity_window() {
        final Instant now = Instant.now();
        final Calendar timestamp = calendarAt(now.minus(30, ChronoUnit.MINUTES));
        assertFalse(SetPasswordPanel.isLinkExpired(timestamp, 60, now));
    }

    @Test
    void isLinkExpired_returns_true_when_past_validity_window() {
        final Instant now = Instant.now();
        final Calendar timestamp = calendarAt(now.minus(90, ChronoUnit.MINUTES));
        assertTrue(SetPasswordPanel.isLinkExpired(timestamp, 60, now));
    }

    @Test
    void isLinkExpired_returns_false_at_exact_expiry_boundary() {
        final Instant now = Instant.now();
        // timestamp is exactly urlValidity minutes ago → expiry == now → not after → not expired
        final Calendar timestamp = calendarAt(now.minus(60, ChronoUnit.MINUTES));
        assertFalse(SetPasswordPanel.isLinkExpired(timestamp, 60, now));
    }

    @Test
    void isLinkExpired_returns_true_one_second_past_expiry() {
        final Instant now = Instant.now();
        final Calendar timestamp = calendarAt(now.minus(60, ChronoUnit.MINUTES).minus(1, ChronoUnit.SECONDS));
        assertTrue(SetPasswordPanel.isLinkExpired(timestamp, 60, now));
    }

    // --- validateCode (JCR-touching, uses Mockito) ---

    @Test
    void validateCode_throws_ResetPasswordException_when_key_property_missing() throws Exception {
        final Node node = Mockito.mock(Node.class);
        when(node.hasProperty(PASSWORD_RESET_KEY)).thenReturn(false);

        assertThrows(ResetPasswordException.class, () ->
                SetPasswordPanel.validateCode("any-code", "alice", node));
    }

    @Test
    void validateCode_throws_ResetPasswordException_when_codes_differ() throws Exception {
        final Node node = Mockito.mock(Node.class);
        final Property prop = Mockito.mock(Property.class);
        when(node.hasProperty(PASSWORD_RESET_KEY)).thenReturn(true);
        when(node.getProperty(PASSWORD_RESET_KEY)).thenReturn(prop);
        when(prop.getString()).thenReturn("stored-code");

        assertThrows(ResetPasswordException.class, () ->
                SetPasswordPanel.validateCode("different-code", "alice", node));
    }

    @Test
    void validateCode_does_not_throw_when_codes_match() throws Exception {
        final Node node = Mockito.mock(Node.class);
        final Property prop = Mockito.mock(Property.class);
        when(node.hasProperty(PASSWORD_RESET_KEY)).thenReturn(true);
        when(node.getProperty(PASSWORD_RESET_KEY)).thenReturn(prop);
        when(prop.getString()).thenReturn("correct-code");

        assertDoesNotThrow(() -> SetPasswordPanel.validateCode("correct-code", "alice", node));
    }

    // --- validateTimestamp (JCR-touching, uses Mockito) ---

    @Test
    void validateTimestamp_throws_ResetPasswordException_when_timestamp_property_missing() throws Exception {
        final Node node = Mockito.mock(Node.class);
        when(node.hasProperty(PASSWORD_RESET_TIMESTAMP)).thenReturn(false);

        assertThrows(ResetPasswordException.class, () ->
                SetPasswordPanel.validateTimestamp("alice", node, 60));
    }

    @Test
    void validateTimestamp_throws_ResetPasswordLinkExpiredException_when_link_expired() throws Exception {
        final Node node = Mockito.mock(Node.class);
        final Property prop = Mockito.mock(Property.class);
        final Calendar oldTimestamp = calendarAt(Instant.now().minus(2, ChronoUnit.HOURS));
        when(node.hasProperty(PASSWORD_RESET_TIMESTAMP)).thenReturn(true);
        when(node.getProperty(PASSWORD_RESET_TIMESTAMP)).thenReturn(prop);
        when(prop.getDate()).thenReturn(oldTimestamp);

        assertThrows(ResetPasswordLinkExpiredException.class, () ->
                SetPasswordPanel.validateTimestamp("alice", node, 60));
    }

    @Test
    void validateTimestamp_does_not_throw_for_fresh_link() throws Exception {
        final Node node = Mockito.mock(Node.class);
        final Property prop = Mockito.mock(Property.class);
        final Calendar recentTimestamp = calendarAt(Instant.now().minus(5, ChronoUnit.MINUTES));
        when(node.hasProperty(PASSWORD_RESET_TIMESTAMP)).thenReturn(true);
        when(node.getProperty(PASSWORD_RESET_TIMESTAMP)).thenReturn(prop);
        when(prop.getDate()).thenReturn(recentTimestamp);

        assertDoesNotThrow(() -> SetPasswordPanel.validateTimestamp("alice", node, 60));
    }

    private static Calendar calendarAt(final Instant instant) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(instant.toEpochMilli());
        return cal;
    }
}
