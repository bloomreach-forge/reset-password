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

import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import jakarta.servlet.http.Cookie;

import org.apache.commons.lang3.StringUtils;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;

import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import org.hippoecm.frontend.attributes.ClassAttribute;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.login.LoginHeaderItem;
import org.hippoecm.frontend.plugins.login.LoginPlugin;
import org.hippoecm.frontend.service.WicketFaviconService;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.hippoecm.frontend.session.PluginUserSession;
import org.hippoecm.frontend.usagestatistics.UsageStatisticsSettings;
import org.hippoecm.frontend.util.WebApplicationHelper;

import org.hippoecm.frontend.widgets.Pinger;
import org.onehippo.cms7.services.HippoServiceRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ResetPassword class for all Wicket based front-end code.
 * Calls @Link{ResetPasswordPanel} and @Link{SetPasswordPanel} as sub panels.
 *
 * Based on @Link{DefaultLoginPlugin} and @Link{LoginPlugin}.
 */
public class ResetPassword extends RenderPlugin {

    private static final Logger log = LoggerFactory.getLogger(ResetPassword.class);

    private static final String TERMS_AND_CONDITIONS_LINK = LoginPlugin.TERMS_AND_CONDITIONS_LINK;

    private static final String PARAM_CODE = "code";
    private static final String PARAM_UID = "uid";

    private static final String EDITION = "edition";
    private static final String AUTOCOMPLETE = "signin.form.autocomplete";

    private ResourceReference editionCss;

    private static final ResourceReference loginCss = new CssResourceReference(ResetPassword.class, "resetpassword.css");

    private final String configurationPath;

    /**
     * ResetPassword initializer.
     * @param context plugin context
     * @param config plugin config
     */
    public ResetPassword(final IPluginContext context, final IPluginConfig config) {
        super(context, config);

        configurationPath = config.getString("labels.location");

        add(ClassAttribute.append("login-plugin"));

        add(new Label("pageTitle", getString("page.title")));

        final WicketFaviconService wicketFaviconService = HippoServiceRegistry.getService(WicketFaviconService.class);
        final ResourceReference iconReference = wicketFaviconService.getFaviconResourceReference();
        add(new ResourceLink("faviconLink", iconReference));

        // In case of using a different edition, add extra CSS rules to show the required styling
        if (config.containsKey(EDITION)) {
            final String edition = config.getString(EDITION);
            if ("enterprise".equals(edition)) {
                editionCss = new CssResourceReference(ResetPassword.class, "login_enterprise.css");
            }
        }

        final IRequestParameters requestParameters = getRequest().getQueryParameters();
        final String code = requestParameters.getParameterValue(PARAM_CODE).toString();
        final String uid = requestParameters.getParameterValue(PARAM_UID).toString();
        final boolean hasParameters = StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(uid);

        final boolean autocomplete = getPluginConfig().getAsBoolean(AUTOCOMPLETE, true);

        final Configuration configuration = getConfiguration();
        final PanelInfo panelInfo = new PanelInfo(autocomplete, uid, configuration, context, config);
        final Panel resetPasswordForm = createResetPasswordPanel(panelInfo);
        resetPasswordForm.setVisible(!hasParameters);
        add(resetPasswordForm);

        final Panel setPasswordForm = createSetPasswordPanel(panelInfo, code, resetPasswordForm);
        setPasswordForm.setVisible(hasParameters);
        add(setPasswordForm);

        final ExternalLink termsAndConditions = new ExternalLink("termsAndConditions", TERMS_AND_CONDITIONS_LINK) {
            @Override
            public boolean isVisible() {
                return UsageStatisticsSettings.get().isEnabled();
            }
        };
        termsAndConditions.setOutputMarkupId(true);
        add(termsAndConditions);

        // Add a dummy pinger so a lingering pinger in an open CMS tab that sends an Ajax request to a freshly started
        // CMS instance will still find a pinger component on the current Wicket page (i.e. the login page). That
        // request will then make Wicket Ajax redirect to the login page.
        add(Pinger.dummy());
    }

    protected Panel createResetPasswordPanel(final PanelInfo panelInfo) {
        return new ResetPasswordPanel(panelInfo);
    }

    protected Panel createSetPasswordPanel(final PanelInfo panelInfo, final String code, final Panel resetPasswordForm) {
        return new SetPasswordPanel(panelInfo, code, resetPasswordForm);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        response.render(CssHeaderItem.forReference(loginCss));

        response.render(LoginHeaderItem.get());
        if (editionCss != null) {
            response.render(CssHeaderItem.forReference(editionCss));
        }
    }

    private Configuration getConfiguration() {
        final PluginUserSession userSession = PluginUserSession.get();
        final String cookieValue = getCookieValue(ResetPasswordConst.LOCALE_COOKIE);
        if (cookieValue != null) {
            userSession.setLocale(new Locale(cookieValue));
        }

        try {
            Session jcrSession = userSession.getJcrSession();

            final Node configNode = jcrSession.getNode(configurationPath);
            return new Configuration(configNode);
        } catch (final RepositoryException re) {
            log.error("Error trying to fetch configuration node {} from JCR session {}", configurationPath, ResetPasswordMain.USER_RESETPASSWORD, re);
            throw new IllegalStateException("Failed to read configuration " + configurationPath +
                    " from JCR session " + ResetPasswordMain.USER_RESETPASSWORD);
        }
    }

    protected String getCookieValue(final String cookieName) {
        Cookie[] cookies = WebApplicationHelper.retrieveWebRequest().getContainerRequest().getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
