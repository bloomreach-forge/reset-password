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

/**
 * Validates user IDs before they are used to construct JCR paths.
 *
 * The CMS itself imposes no character restriction on usernames beyond a minimum
 * length of 2. This validator blocks only the characters that enable JCR path
 * traversal: the path separator '/', the parent-navigation sequence '..', and
 * null bytes. All other characters — including Unicode and '+' in email-style
 * names — are left to the JCR layer to accept or reject.
 */
public final class UserIdValidator {

    private UserIdValidator() {
    }

    /**
     * Returns true if the userId cannot be used for JCR path traversal.
     * Rejects null, strings shorter than 2 characters, and strings containing
     * '/', '..', or null bytes.
     */
    public static boolean isValid(final String userId) {
        return userId != null
                && userId.length() >= 2
                && !userId.contains("/")
                && !userId.contains("..")
                && !userId.contains("\0");
    }
}
