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

import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
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
class PanelInfoTest {

    @Mock private IPluginContext context;
    @Mock private IPluginConfig config;
    @Mock private Node configNode;
    @Mock private NodeIterator nodeIterator;

    private Configuration buildEmptyConfig() throws RepositoryException {
        when(configNode.getNodes("selection:listitem")).thenReturn(nodeIterator);
        when(nodeIterator.hasNext()).thenReturn(false);
        // All 18 label lookups will throw IllegalArgumentException; wrap in a config with empty data
        // Instead, build a config via a node that has all 18 keys
        return null; // skip — test PanelInfo with null config
    }

    @Test
    void getAutoComplete_returnsConstructorValue() {
        PanelInfo info = new PanelInfo(true, "admin", null, context, config);
        assertTrue(info.isAutoComplete());
    }

    @Test
    void getAutoComplete_falseValue() {
        PanelInfo info = new PanelInfo(false, "user", null, context, config);
        assertFalse(info.isAutoComplete());
    }

    @Test
    void getUserId_returnsConstructorValue() {
        PanelInfo info = new PanelInfo(false, "john", null, context, config);
        assertEquals("john", info.getUserId());
    }

    @Test
    void getContext_returnsConstructorValue() {
        PanelInfo info = new PanelInfo(false, "u", null, context, config);
        assertSame(context, info.getContext());
    }

    @Test
    void getConfig_returnsConstructorValue() {
        PanelInfo info = new PanelInfo(false, "u", null, context, config);
        assertSame(config, info.getConfig());
    }

    @Test
    void getConfiguration_returnsConstructorValue() {
        PanelInfo info = new PanelInfo(false, "u", null, context, config);
        assertNull(info.getConfiguration());
    }
}
