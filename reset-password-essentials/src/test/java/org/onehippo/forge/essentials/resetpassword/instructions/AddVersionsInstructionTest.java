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
package org.onehippo.forge.essentials.resetpassword.instructions;

import org.junit.jupiter.api.Test;
import org.onehippo.cms7.essentials.sdk.api.install.Instruction;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AddVersionsInstructionTest {

    @Test
    void populateChangeMessages_emitsExecuteMessage() {
        AddVersionsInstruction instruction = new AddVersionsInstruction();
        AtomicReference<Instruction.Type> capturedType = new AtomicReference<>();
        AtomicReference<String> capturedMsg = new AtomicReference<>();

        instruction.populateChangeMessages((type, msg) -> {
            capturedType.set(type);
            capturedMsg.set(msg);
        });

        assertEquals(Instruction.Type.EXECUTE, capturedType.get());
        assertNotNull(capturedMsg.get());
        assertFalse(capturedMsg.get().isBlank());
    }
}
