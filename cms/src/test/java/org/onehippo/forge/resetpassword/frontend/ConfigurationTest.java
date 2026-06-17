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
package org.onehippo.forge.resetpassword.frontend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationTest {

    @Mock private Node configNode;
    @Mock private Node listItemNode;
    @Mock private NodeIterator nodeIterator;
    @Mock private Property keyProp;
    @Mock private Property labelProp;

    @Test
    void getLabel_withMatchingKey_returnsLabel() throws RepositoryException {
        when(configNode.getNodes("selection:listitem")).thenReturn(nodeIterator);
        when(nodeIterator.hasNext()).thenReturn(true, false);
        when(nodeIterator.nextNode()).thenReturn(listItemNode);
        when(listItemNode.getProperty("selection:key")).thenReturn(keyProp);
        when(listItemNode.getProperty("selection:label")).thenReturn(labelProp);
        when(keyProp.getString()).thenReturn("email.sent");
        when(labelProp.getString()).thenReturn("Email sent successfully.");

        assertEquals("Email sent successfully.", Configuration.getLabel(configNode, "email.sent"));
    }

    @Test
    void getLabel_withMissingKey_throwsIllegalArgumentException() throws RepositoryException {
        when(configNode.getNodes("selection:listitem")).thenReturn(nodeIterator);
        when(nodeIterator.hasNext()).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> Configuration.getLabel(configNode, "missing.key"));
    }

    @Test
    void getLabel_withEmptyLabel_throwsIllegalArgumentException() throws RepositoryException {
        when(configNode.getNodes("selection:listitem")).thenReturn(nodeIterator);
        when(nodeIterator.hasNext()).thenReturn(true, false);
        when(nodeIterator.nextNode()).thenReturn(listItemNode);
        when(listItemNode.getProperty("selection:key")).thenReturn(keyProp);
        when(listItemNode.getProperty("selection:label")).thenReturn(labelProp);
        when(keyProp.getString()).thenReturn("email.sent");
        when(labelProp.getString()).thenReturn("");

        assertThrows(IllegalArgumentException.class, () -> Configuration.getLabel(configNode, "email.sent"));
    }

    // Build a NodeIterator that freshly walks all keys on each invocation
    private static NodeIterator buildIteratorForKeys(String[] keys) throws RepositoryException {
        NodeIterator iter = mock(NodeIterator.class);
        java.util.concurrent.atomic.AtomicInteger idx = new java.util.concurrent.atomic.AtomicInteger(0);
        when(iter.hasNext()).thenAnswer(inv -> idx.get() < keys.length);
        when(iter.nextNode()).thenAnswer(inv -> {
            String key = keys[idx.getAndIncrement()];
            Node item = mock(Node.class);
            Property kp = mock(Property.class);
            Property lp = mock(Property.class);
            when(item.getProperty("selection:key")).thenReturn(kp);
            when(item.getProperty("selection:label")).thenReturn(lp);
            when(kp.getString()).thenReturn(key);
            String val = key.equals(Configuration.URL_VALIDITY_IN_MINUTES) ? "60"
                       : key.equals(Configuration.DAYS_BEFORE_PASSWORD_EXPIRES) ? "90"
                       : "value-for-" + key;
            when(lp.getString()).thenReturn(val);
            return item;
        });
        return iter;
    }

    private Node buildFullConfigNode() throws RepositoryException {
        String[] keys = {
            Configuration.EMAIL_SENT, Configuration.INFORMATION_INCOMPLETE,
            Configuration.RESET_LINK_EXPIRED, Configuration.REQUEST_NEW_LINK_TEXT,
            Configuration.PASSWORDS_DO_NOT_MATCH, Configuration.SYSTEM_ERROR,
            Configuration.PASSWORD_RESET_DONE, Configuration.EMAIL_FROM_NAME,
            Configuration.EMAIL_FROM_EMAIL, Configuration.EMAIL_SUBJECT_RESET,
            Configuration.EMAIL_SUBJECT_EXPIRE, Configuration.EMAIL_TEXT_RESET,
            Configuration.EMAIL_TEXT_EXPIRE, Configuration.RESET_INTRO_TEXT,
            Configuration.SET_PW_INTRO_TEXT, Configuration.LOGIN_LINK_TEXT,
            Configuration.URL_VALIDITY_IN_MINUTES, Configuration.DAYS_BEFORE_PASSWORD_EXPIRES
        };
        Node node = mock(Node.class);
        // Each call to getNodes("selection:listitem") must return a fresh iterator
        when(node.getNodes("selection:listitem")).thenAnswer(inv -> buildIteratorForKeys(keys));
        return node;
    }

    @Test
    void constructor_withFullConfigNode_populatesLabelMap() throws RepositoryException {
        Node node = buildFullConfigNode();
        Configuration config = new Configuration(node);
        assertFalse(config.getLabelMap().isEmpty());
        assertEquals("value-for-" + Configuration.EMAIL_SENT, config.getLabelMap().get(Configuration.EMAIL_SENT));
    }

    @Test
    void constructor_withFullConfigNode_populatesDurationsMap() throws RepositoryException {
        Node node = buildFullConfigNode();
        Configuration config = new Configuration(node);
        assertEquals(60, config.getDurationsMap().get(Configuration.URL_VALIDITY_IN_MINUTES));
        assertEquals(90, config.getDurationsMap().get(Configuration.DAYS_BEFORE_PASSWORD_EXPIRES));
    }

    @Test
    void getLabelMap_returnsUnmodifiableMap() throws RepositoryException {
        Node node = buildFullConfigNode();
        Configuration config = new Configuration(node);
        assertThrows(UnsupportedOperationException.class,
                () -> config.getLabelMap().put("key", "value"));
    }

    @Test
    void getDurationsMap_returnsUnmodifiableMap() throws RepositoryException {
        Node node = buildFullConfigNode();
        Configuration config = new Configuration(node);
        assertThrows(UnsupportedOperationException.class,
                () -> config.getDurationsMap().put("key", 1));
    }
}
