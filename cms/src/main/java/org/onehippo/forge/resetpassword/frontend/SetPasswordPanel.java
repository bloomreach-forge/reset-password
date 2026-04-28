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

import java.time.Instant;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;

import org.hippoecm.frontend.attributes.ClassAttribute;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugin.config.impl.JcrPluginConfig;
import org.hippoecm.frontend.plugins.cms.admin.password.validation.IPasswordValidationService;
import org.hippoecm.frontend.plugins.cms.admin.password.validation.PasswordValidationServiceImpl;
import org.hippoecm.frontend.plugins.cms.admin.password.validation.PasswordValidationStatus;
import org.hippoecm.frontend.plugins.cms.admin.users.User;
import org.hippoecm.frontend.plugins.login.LoginResourceModel;
import org.hippoecm.frontend.session.PluginUserSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.onehippo.forge.resetpassword.frontend.ResetPasswordConst.HIPPO_USERS_PATH;
import static org.onehippo.forge.resetpassword.frontend.ResetPasswordConst.PASSWORDVALIDATION_LOCATION;
import static org.onehippo.forge.resetpassword.frontend.ResetPasswordConst.PASSWORD_RESET_KEY;
import static org.onehippo.forge.resetpassword.frontend.ResetPasswordConst.PASSWORD_RESET_TIMESTAMP;

/**
 * SetPasswordPanel
 * Panel on ResetPasswordFrame, showing two fields for entering the new password twice
 */
public class SetPasswordPanel extends Panel {

    private static final Logger log = LoggerFactory.getLogger(SetPasswordPanel.class);

    private final IPasswordValidationService passwordValidationService;

    private final Map<String, String> labelMap;

    private final int urlValidity;

    /**
     * Constructor
     *
     * @param panelInfo          information used for configuring this panel
     * @param code               check for valid password reset
     * @param resetPasswordPanel original panel, reference used to set the proper panel visible
     */
    public SetPasswordPanel(final PanelInfo panelInfo, final String code, final Panel resetPasswordPanel) {
        super("setPasswordForm");
        labelMap = panelInfo.getConfiguration().getLabelMap();
        urlValidity = panelInfo.getConfiguration().getDurationsMap().get(Configuration.URL_VALIDITY_IN_MINUTES).intValue();

        add(ClassAttribute.append("hippo-login-panel-center"));
        add(new SetPasswordForm(panelInfo, code, resetPasswordPanel));
        String passwordValidationLocation = panelInfo.getConfig().get(PASSWORDVALIDATION_LOCATION).toString();
        JcrPluginConfig pluginConfig = new JcrPluginConfig(new JcrNodeModel(passwordValidationLocation));
        passwordValidationService = new PasswordValidationServiceImpl(panelInfo.getContext(), pluginConfig);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        response.render(OnDomReadyHeaderItem.forScript("$('#password-verification').bind(\"cut copy paste\",function(e) {" +
                "  e.preventDefault();" +
                "});"));
    }

    /**
     * Returns true if the submitted code matches the persisted code on the user node.
     */
    static boolean isCodeMatch(final String submittedCode, final String persistedCode) {
        return submittedCode.equals(persistedCode);
    }

    /**
     * Returns true if the link has expired (current time is strictly after timestamp + urlValidityMinutes).
     */
    static boolean isLinkExpired(final Calendar persistedTimestamp, final int urlValidityMinutes, final Instant now) {
        final Calendar expiry = (Calendar) persistedTimestamp.clone();
        expiry.add(Calendar.MINUTE, urlValidityMinutes);
        return now.toEpochMilli() > expiry.getTimeInMillis();
    }

    /**
     * Validates the reset code on the user node; throws if missing or mismatched.
     */
    static void validateCode(final String code, final String uid, final Node userNode)
            throws RepositoryException, ResetPasswordException {
        if (!userNode.hasProperty(PASSWORD_RESET_KEY)) {
            throw new ResetPasswordException("No reset key property on node: " + uid);
        }
        final String persistedCode = userNode.getProperty(PASSWORD_RESET_KEY).getString();
        if (!isCodeMatch(code, persistedCode)) {
            throw new ResetPasswordException("Verification code mismatch for: " + uid);
        }
    }

    /**
     * Validates the reset timestamp on the user node; throws if missing or expired.
     */
    static void validateTimestamp(final String uid, final Node userNode, final int urlValidityMinutes)
            throws RepositoryException, ResetPasswordException, ResetPasswordLinkExpiredException {
        if (!userNode.hasProperty(PASSWORD_RESET_TIMESTAMP)) {
            throw new ResetPasswordException("No reset timestamp property on node: " + uid);
        }
        final Calendar persistedTimestamp = userNode.getProperty(PASSWORD_RESET_TIMESTAMP).getDate();
        if (isLinkExpired(persistedTimestamp, urlValidityMinutes, Instant.now())) {
            throw new ResetPasswordLinkExpiredException("The link has expired");
        }
    }

    protected class SetPasswordForm extends Form<Void> {

        private final String code;
        private final String uid;

        private final FeedbackPanel feedback;
        private TextField<String> passwordField;
        private IModel<String> passwordPlaceholder;
        private TextField<String> passwordVerificationField;
        private IModel<String> passwordVerificationPlaceholder;

        private String password;
        private String passwordVerification;

        private final WebMarkupContainer setPasswordFormTable = new WebMarkupContainer("setPasswordFormTable");
        private final WebMarkupContainer loginLink;

        public SetPasswordForm(final PanelInfo panelInfo, final String code, final Panel resetPanel) {
            super("setPasswordForm");
            this.code = code;
            uid = panelInfo.getUserId();

            createPasswordFormTable(panelInfo.isAutoComplete());
            add(setPasswordFormTable);

            feedback = new FeedbackPanel("feedback");
            feedback.setOutputMarkupId(true);
            add(feedback);

            loginLink = new WebMarkupContainer("loginLink");
            loginLink.add(new Label("login.link.text", labelMap.get(Configuration.LOGIN_LINK_TEXT)));
            loginLink.setVisible(false);
            add(loginLink);

            final Link<Void> resetLink = new Link<Void>("resetLink") {

                @Override
                public void onClick() {
                    resetPanel.setVisible(true);
                    setVisible(false);
                }
            };
            resetLink.add(new Label("reset.link.text", labelMap.get(Configuration.REQUEST_NEW_LINK_TEXT)));
            resetLink.setVisible(false);
            setPasswordFormTable.add(resetLink);

            final boolean hasParameters = StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(uid);
            if (hasParameters) {
                final boolean isValid = validateForm(code, uid);
                if (!isValid) {
                    setPasswordFormTable.setVisible(false);
                    resetLink.setVisible(true);
                }
            }
        }

        private void createPasswordFormTable(final boolean autocomplete) {
            setPasswordFormTable.add(new Label("intro.text", labelMap.get(Configuration.SET_PW_INTRO_TEXT)));

            setPasswordFormTable.add(passwordField = new PasswordTextField("new-password",
                    new PropertyModel<>(this, "password")));
            setPasswordFormTable.add(passwordVerificationField = new PasswordTextField("repeat-password",
                    new PropertyModel<>(this, "passwordVerification")));
            passwordVerificationField.setMarkupId("password-verification");
            passwordPlaceholder = new LoginResourceModel("password-label", ResetPassword.class);
            passwordVerificationPlaceholder = new LoginResourceModel("password-verification-label", ResetPassword.class);
            addAjaxAttributeModifier(passwordField, "placeholder", passwordPlaceholder);
            addAjaxAttributeModifier(passwordVerificationField, "placeholder", passwordVerificationPlaceholder);
            setPasswordFormTable.add(new AttributeModifier("autocomplete", new Model<>(autocomplete ? "on" : "off")));
            setPasswordFormTable.add(new Button("submit", new ResourceModel("submit-label")));
        }

        @Override
        protected final void onValidate() {
            super.onValidate();

            if (!passwordField.getValue().equals(passwordVerificationField.getValue())) {
                error(labelMap.get(Configuration.PASSWORDS_DO_NOT_MATCH));
            }

            try {
                final Session jcrSession = PluginUserSession.get().getJcrSession();

                validateUid(code, uid, jcrSession);

                final User user = new User(uid);

                final List<PasswordValidationStatus> statuses =
                        passwordValidationService.checkPassword(passwordField.getValue(), user);
                for (final PasswordValidationStatus status : statuses) {
                    if (!status.accepted()) {
                        error(status.getMessage());
                    }
                }
            } catch (final RepositoryException re) {
                log.error("Error validating SetPasswordForm", re);
                error(labelMap.get(Configuration.SYSTEM_ERROR));
            }
        }

        @Override
        protected final void onSubmit() {
            super.onSubmit();

            try {
                final Session jcrSession = PluginUserSession.get().getJcrSession();

                final Node userNode = jcrSession.getNode(HIPPO_USERS_PATH + uid);

                // Remove the reset token before saving the new password so a
                // failed savePassword call cannot be retried with the same link.
                if (userNode.hasProperty(PASSWORD_RESET_TIMESTAMP)) {
                    userNode.getProperty(PASSWORD_RESET_TIMESTAMP).remove();
                }
                if (userNode.hasProperty(PASSWORD_RESET_KEY)) {
                    userNode.getProperty(PASSWORD_RESET_KEY).remove();
                }
                jcrSession.save();

                final User user = new User(uid);
                user.savePassword(password);

                info(labelMap.get(Configuration.PASSWORD_RESET_DONE));
                setPasswordFormTable.setVisible(false);
                loginLink.setVisible(true);

            } catch (final RepositoryException re) {
                log.error("Error saving password SetPasswordForm", re);
                error(labelMap.get(Configuration.SYSTEM_ERROR));
            }
        }

        /**
         * Returns true if the code+uid parameters are valid (form should be shown).
         * Returns false if invalid (form should be hidden, reset link shown).
         */
        private boolean validateForm(final String code, final String uid) {
            try {
                final Session jcrSession = PluginUserSession.get().getJcrSession();
                return validateUid(code, uid, jcrSession);
            } catch (final RepositoryException e) {
                log.error("Error validating SetPasswordForm", e);
                error(labelMap.get(Configuration.SYSTEM_ERROR));
                return false;
            }
        }

        /**
         * Returns true if the uid and code are valid; false if any check fails (errors added to form).
         */
        private boolean validateUid(final String code, final String uid, final Session session) throws RepositoryException {
            if (!UserIdValidator.isValid(uid)) {
                error(labelMap.get(Configuration.INFORMATION_INCOMPLETE));
                return false;
            }
            try {
                final Node userNode = session.getNode(HIPPO_USERS_PATH + uid);

                validateCode(code, uid, userNode);
                validateTimestamp(uid, userNode, urlValidity);

                return true;

            } catch (final PathNotFoundException pnfe) {
                log.error("Unknown username during password reset", pnfe);
                error(labelMap.get(Configuration.INFORMATION_INCOMPLETE));
                return false;
            } catch (final ResetPasswordException rpe) {
                log.info("Invalid reset parameters: {}", rpe.getMessage());
                error(labelMap.get(Configuration.INFORMATION_INCOMPLETE));
                return false;
            } catch (final ResetPasswordLinkExpiredException rpplee) {
                log.info("Expired reset link used");
                error(labelMap.get(Configuration.RESET_LINK_EXPIRED));
                return false;
            }
        }

        private void addAjaxAttributeModifier(final Component component, final String name, final IModel<String> value) {
            final AjaxAttributeModifier modifier = new AjaxAttributeModifier(name, value);
            component.add(modifier);
        }
    }
}
