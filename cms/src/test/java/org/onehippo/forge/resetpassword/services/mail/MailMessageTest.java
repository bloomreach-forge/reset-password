/*
 * Copyright 2025 Bloomreach Inc. (https://www.bloomreach.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.resetpassword.services.mail;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MailMessageTest {

    @Test
    void setAndGetToMail_roundTrips() {
        MailMessage msg = new MailMessage();
        msg.setToMail("user@example.com");
        assertEquals("user@example.com", msg.getToMail());
    }

    @Test
    void setToMailWithName_setsToMailAndToName() {
        MailMessage msg = new MailMessage();
        msg.setToMail("user@example.com", "John Doe");
        assertEquals("user@example.com", msg.getToMail());
        assertEquals("John Doe", msg.getToName());
    }

    @Test
    void setAndGetToName_roundTrips() {
        MailMessage msg = new MailMessage();
        msg.setToName("Jane Smith");
        assertEquals("Jane Smith", msg.getToName());
    }

    @Test
    void setAndGetFromMail_roundTrips() {
        MailMessage msg = new MailMessage();
        msg.setFromMail("noreply@example.com");
        assertEquals("noreply@example.com", msg.getFromMail());
    }

    @Test
    void setFromMailWithName_setsFromMailAndFromName() {
        MailMessage msg = new MailMessage();
        msg.setFromMail("noreply@example.com", "System");
        assertEquals("noreply@example.com", msg.getFromMail());
        assertEquals("System", msg.getFromName());
    }

    @Test
    void setAndGetFromName_roundTrips() {
        MailMessage msg = new MailMessage();
        msg.setFromName("Admin");
        assertEquals("Admin", msg.getFromName());
    }

    @Test
    void setAndGetSubject_roundTrips() {
        MailMessage msg = new MailMessage();
        msg.setSubject("Reset your password");
        assertEquals("Reset your password", msg.getSubject());
    }

    @Test
    void setAndGetPlainTextBody_roundTrips() {
        MailMessage msg = new MailMessage();
        msg.setPlainTextBody("Please click the link.");
        assertEquals("Please click the link.", msg.getPlainTextBody());
    }

    @Test
    void setAndGetHtmlTextBody_roundTrips() {
        MailMessage msg = new MailMessage();
        msg.setHtmlTextBody("<p>Please <a href='...'>click here</a>.</p>");
        assertEquals("<p>Please <a href='...'>click here</a>.</p>", msg.getHtmlTextBody());
    }

    @Test
    void defaultState_allFieldsAreNull() {
        MailMessage msg = new MailMessage();
        assertNull(msg.getToMail());
        assertNull(msg.getToName());
        assertNull(msg.getFromMail());
        assertNull(msg.getFromName());
        assertNull(msg.getSubject());
        assertNull(msg.getPlainTextBody());
        assertNull(msg.getHtmlTextBody());
    }
}
