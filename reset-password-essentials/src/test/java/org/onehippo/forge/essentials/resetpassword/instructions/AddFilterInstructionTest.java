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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onehippo.cms7.essentials.sdk.api.install.Instruction;
import org.onehippo.cms7.essentials.sdk.api.model.Module;
import org.onehippo.cms7.essentials.sdk.api.service.WebXmlService;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddFilterInstructionTest {

    @Mock private WebXmlService webXmlService;
    @InjectMocks private AddFilterInstruction instruction;

    @Test
    void execute_whenAllServiceCallsSucceed_returnsSuccess() {
        when(webXmlService.addFilter(eq(Module.CMS), eq("ResetPassword"), anyString(), any())).thenReturn(true);
        when(webXmlService.insertFilterMapping(eq(Module.CMS), eq("ResetPassword"), any(), eq("CMS"))).thenReturn(true);
        when(webXmlService.addDispatchersToFilterMapping(eq(Module.CMS), eq("ResetPassword"), any())).thenReturn(true);
        when(webXmlService.addDispatchersToFilterMapping(eq(Module.CMS), eq("CMS"), any())).thenReturn(true);

        assertEquals(Instruction.Status.SUCCESS, instruction.execute(Collections.emptyMap()));
    }

    @Test
    void execute_whenAddFilterFails_returnsFailed() {
        when(webXmlService.addFilter(any(), any(), any(), any())).thenReturn(false);

        assertEquals(Instruction.Status.FAILED, instruction.execute(Collections.emptyMap()));
    }

    @Test
    void execute_whenInsertFilterMappingFails_returnsFailed() {
        when(webXmlService.addFilter(any(), any(), any(), any())).thenReturn(true);
        when(webXmlService.insertFilterMapping(any(), any(), any(), any())).thenReturn(false);

        assertEquals(Instruction.Status.FAILED, instruction.execute(Collections.emptyMap()));
    }

    @Test
    void populateChangeMessages_emitsExecuteMessage() {
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
