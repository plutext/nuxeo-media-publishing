/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *      Nelson Silva
 */

package org.nuxeo.ecm.media.publishing;

import com.google.api.client.auth.oauth2.Credential;
import org.nuxeo.ecm.media.publishing.adapter.PublishableMedia;
import org.nuxeo.ecm.platform.oauth2.providers.NuxeoOAuth2ServiceProvider;
import org.nuxeo.ecm.platform.oauth2.providers.OAuth2ServiceProvider;
import org.nuxeo.ecm.platform.oauth2.providers.OAuth2ServiceProviderRegistry;
import org.nuxeo.runtime.api.Framework;

/**
 * Abstract Media Publishing Provider using OAuth2
 *
 * @since 7.3
 */
public abstract class OAuth2MediaPublishingProvider implements MediaPublishingProvider {

    private final String providerName;

    public OAuth2MediaPublishingProvider(String providerName) {
        this.providerName = providerName;
    }

    protected OAuth2ServiceProvider getOAuth2ServiceProvider() {
        OAuth2ServiceProviderRegistry oAuth2ProviderRegistry = Framework.getLocalService(OAuth2ServiceProviderRegistry.class);
        return oAuth2ProviderRegistry.getProvider(providerName);
    }

    protected Credential getCredential(String account) {
        return getOAuth2ServiceProvider() != null ? getOAuth2ServiceProvider().loadCredential(account) : null;
    }

    @Override
    public boolean isAvailable() {
        NuxeoOAuth2ServiceProvider serviceProvider = (NuxeoOAuth2ServiceProvider) getOAuth2ServiceProvider();
        return serviceProvider != null && serviceProvider.isEnabled() &&
            serviceProvider.getClientSecret() != null && serviceProvider.getClientId() != null;
    }

    @Override
    public boolean isMediaAvailable(PublishableMedia media) {
        NuxeoOAuth2ServiceProvider serviceProvider = (NuxeoOAuth2ServiceProvider) getOAuth2ServiceProvider();
        String account = media.getAccount(providerName);
        return isAvailable() && serviceProvider != null && getCredential(account) != null;
    }
}
