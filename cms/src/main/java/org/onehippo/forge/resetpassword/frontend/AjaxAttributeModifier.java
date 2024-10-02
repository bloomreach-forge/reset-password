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

import org.apache.commons.text.StringEscapeUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
/**
 * Exists as inner class of org.hippoecm.frontend.plugins.login.LoginPanel.
 * If ResetPassword becomes officially supported plugin it would make sense to create a class out of it for re-usability.
 */
public class AjaxAttributeModifier extends AttributeModifier {

    private String markupId;

    AjaxAttributeModifier(final String attribute, final IModel<String> replaceModel) {
        super(attribute, replaceModel);
    }

    @Override
    public void bind(final Component component) {
        markupId = component.getMarkupId();
    }

    void update(final AjaxRequestTarget target) {
        final String value = StringEscapeUtils.escapeEcmaScript((String) getReplaceModel().getObject());
        target.appendJavaScript(String.format("$('#%s').attr('%s', '%s');", markupId, getAttribute(), value));
    }
}
