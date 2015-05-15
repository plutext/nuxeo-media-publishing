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
 *      André Justo
 */

package org.nuxeo.ecm.media.publishing.wistia;

import com.google.api.client.auth.oauth2.Credential;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.media.publishing.wistia.model.Media;
import org.nuxeo.ecm.platform.oauth2.providers.OAuth2ServiceProvider;
import org.nuxeo.ecm.platform.oauth2.providers.OAuth2ServiceProviderRegistry;
import org.nuxeo.ecm.media.publishing.MediaPublishingProvider;
import org.nuxeo.ecm.media.publishing.adapter.PublishableMedia;
import org.nuxeo.ecm.media.publishing.upload.MediaPublishingProgressListener;
import org.nuxeo.ecm.media.publishing.wistia.model.Stats;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Wistia Media Publishing Provider Service
 *
 * @since 7.3
 */
public class WistiaService implements MediaPublishingProvider {

    private static final Log log = LogFactory.getLog(WistiaService.class);

    public static final String PROVIDER = "Wistia";

    private OAuth2ServiceProvider oauth2Provider;

    protected OAuth2ServiceProvider getOAuth2ServiceProvider() throws ClientException {
        if (oauth2Provider == null) {
            OAuth2ServiceProviderRegistry oAuth2ProviderRegistry = Framework.getLocalService(
                OAuth2ServiceProviderRegistry.class);
            oauth2Provider = oAuth2ProviderRegistry.getProvider(PROVIDER);
        }
        return oauth2Provider;
    }

    public WistiaClient getWistiaClient(String account) throws ClientException {
        WistiaClient wistiaClient;
        Credential credential = null;

        // Use system wide OAuth2 provider
        if (getOAuth2ServiceProvider() != null) {
            try {
                credential = getOAuth2ServiceProvider().loadCredential(account);
                if (credential != null) {
                    // Refresh access token if needed (based on com.google.api.client.auth.oauth.Credential.intercept())
                    // TODO: rely on Google Oauth aware client instead
                    Long expiresIn = credential.getExpiresInSeconds();
                    // check if token will expire in a minute
                    if (credential.getAccessToken() == null || expiresIn != null && expiresIn <= 60) {
                        credential.refreshToken();
                        if (credential.getAccessToken() == null) {
                            // nothing we can do without an access token
                            throw new ClientException("Failed to refresh access token");
                        }
                    }
                }
                else {
                    throw new ClientException("Failed to get Wistia credentials");
                }
            } catch (IOException e) {
                throw new ClientException(e.getMessage());
            }
        }

        wistiaClient = new WistiaClient(credential.getAccessToken());
        return wistiaClient;
    }

    @Override
    public String upload(PublishableMedia media, MediaPublishingProgressListener progressListener, String account, Map<String, String> options) throws IOException {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        params.putSingle("name", media.getTitle());
        params.putSingle("description", media.getDescription());

        for (Entry<String, String> entry : options.entrySet()) {
            if (entry.getValue() != null && entry.getValue().length() > 0) {
                params.putSingle(entry.getKey(), entry.getValue());
            }
        }

        // upload original video
        Blob blob = media.getBlob();

        Media video = getWistiaClient(account).upload(blob.getFilename(), blob.getStream(), params);

        return video.getHashedId();
    }

    @Override
    public String getPublishedUrl(String mediaId, String account) {
        return getWistiaClient(account).getAccount().getUrl() + "/medias/" + mediaId;
    }

    @Override
    public String getEmbedCode(String mediaId, String account) {
        return getWistiaClient(account).getEmbedCode(getPublishedUrl(mediaId, account));
    }

    @Override
    public Map<String, String> getStats(String mediaId, String account) {
        Stats stats = getWistiaClient(account).getMediaStats(mediaId);
        if (stats == null) {
            return null;
        }

        Map<String, String> map = new HashMap<>();
        map.put("Visitors", Integer.toString(stats.getVisitors()));
        map.put("Plays", Integer.toString(stats.getPlays()));
        map.put("Average % Watched", Integer.toString(stats.getAveragePercentWatched()));
        map.put("Page Loads", Integer.toString(stats.getPageLoads()));
        map.put("% of visitors clicking play", Integer.toString(stats.getPercentOfVisitorsClickingPlay()));
        return map;
    }

    public List getProjects(String account) {
        return getWistiaClient(account).getProjects();
    }
}
