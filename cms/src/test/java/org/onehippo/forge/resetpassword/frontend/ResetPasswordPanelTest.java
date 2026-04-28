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

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResetPasswordPanelTest {

    @Test
    void buildUserDisplayName_returns_full_name_when_both_present() {
        assertEquals("John Smith", ResetPasswordPanel.buildUserDisplayName("John", "Smith"));
    }

    @Test
    void buildUserDisplayName_returns_first_name_only_when_last_is_null() {
        assertEquals("John", ResetPasswordPanel.buildUserDisplayName("John", null));
    }

    @Test
    void buildUserDisplayName_returns_last_name_only_when_first_is_null() {
        assertEquals("Smith", ResetPasswordPanel.buildUserDisplayName(null, "Smith"));
    }

    @Test
    void buildUserDisplayName_returns_empty_string_when_both_null() {
        assertEquals("", ResetPasswordPanel.buildUserDisplayName(null, null));
    }

    @Test
    void buildUserDisplayName_returns_empty_string_when_both_blank() {
        assertEquals("", ResetPasswordPanel.buildUserDisplayName("  ", "  "));
    }

    @Test
    void buildUserDisplayName_trims_blank_first_name() {
        assertEquals("Smith", ResetPasswordPanel.buildUserDisplayName("   ", "Smith"));
    }
}
