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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserIdValidatorTest {

    // --- rejection of traversal vectors ---

    @Test
    void null_is_invalid() {
        assertFalse(UserIdValidator.isValid(null));
    }

    @Test
    void empty_string_is_invalid() {
        assertFalse(UserIdValidator.isValid(""));
    }

    @Test
    void single_character_is_invalid() {
        assertFalse(UserIdValidator.isValid("a"));
    }

    @Test
    void forward_slash_is_invalid() {
        assertFalse(UserIdValidator.isValid("ad/min"));
    }

    @Test
    void path_traversal_with_double_dot_is_invalid() {
        assertFalse(UserIdValidator.isValid("../hippo:groups"));
    }

    @Test
    void double_dot_without_slash_is_invalid() {
        assertFalse(UserIdValidator.isValid("..admin"));
    }

    @Test
    void leading_slash_is_invalid() {
        assertFalse(UserIdValidator.isValid("/admin"));
    }

    @Test
    void deep_traversal_is_invalid() {
        assertFalse(UserIdValidator.isValid("../../../../jcr:system"));
    }

    @Test
    void null_byte_is_invalid() {
        assertFalse(UserIdValidator.isValid("admin\0extra"));
    }

    // --- characters the CMS legitimately allows ---

    @Test
    void simple_username_is_valid() {
        assertTrue(UserIdValidator.isValid("admin"));
    }

    @Test
    void minimum_two_character_username_is_valid() {
        assertTrue(UserIdValidator.isValid("ab"));
    }

    @Test
    void username_with_dots_dashes_underscores_is_valid() {
        assertTrue(UserIdValidator.isValid("john.doe-123_test"));
    }

    @Test
    void email_style_username_is_valid() {
        assertTrue(UserIdValidator.isValid("john.doe@example.com"));
    }

    @Test
    void email_with_plus_tag_is_valid() {
        assertTrue(UserIdValidator.isValid("user+tag@company.com"));
    }

    @Test
    void unicode_username_is_valid() {
        assertTrue(UserIdValidator.isValid("André"));
    }

    @Test
    void jcr_colon_without_slash_is_valid() {
        // ':' is a JCR namespace separator but not a path separator;
        // JCR itself will reject nodes with invalid namespaces, so we allow it here.
        assertTrue(UserIdValidator.isValid("hippo:admin"));
    }

    @Test
    void single_dot_is_valid() {
        // a single dot is not a traversal sequence
        assertTrue(UserIdValidator.isValid("a.b"));
    }
}
