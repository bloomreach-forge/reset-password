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

import org.apache.commons.mail2.core.EmailException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MailServiceImplTest {

    @Test
    void setMailSessionName_andGetMailSession_roundTrip() {
        MailServiceImpl service = new MailServiceImpl();
        service.setMailSessionName("mail/MySession");
        assertEquals("mail/MySession", service.getMailSession());
    }

    @Test
    void getMailSession_defaultsToNull() {
        assertNull(new MailServiceImpl().getMailSession());
    }

    @Test
    void sendMail_whenNoJndiContext_throwsEmailException() {
        MailServiceImpl service = new MailServiceImpl();
        MailMessage msg = new MailMessage();
        msg.setToMail("user@example.com", "User");
        msg.setFromMail("noreply@example.com", "System");
        msg.setSubject("Test");
        msg.setPlainTextBody("plain");
        msg.setHtmlTextBody("<p>html</p>");

        // No JNDI context in unit tests → getSession() returns null → EmailException thrown
        assertThrows(EmailException.class, () -> service.sendMail(msg));
    }

    @Test
    void sendMail_withCustomSessionName_throwsEmailException() {
        MailServiceImpl service = new MailServiceImpl();
        service.setMailSessionName("mail/Custom");
        MailMessage msg = new MailMessage();
        msg.setToMail("a@b.com", "A");
        msg.setFromMail("x@y.com", "X");
        msg.setSubject("S");
        msg.setPlainTextBody("p");
        msg.setHtmlTextBody("<p/>");
        assertThrows(EmailException.class, () -> service.sendMail(msg));
    }
}
