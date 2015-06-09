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
import org.nuxeo.common.utils.i18n.I18NUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.schema.types.constraints.Constraint;
import org.nuxeo.ecm.media.publishing.wistia.model.Media;
import org.nuxeo.ecm.media.publishing.OAuth2MediaPublishingProvider;
import org.nuxeo.ecm.media.publishing.adapter.PublishableMedia;
import org.nuxeo.ecm.media.publishing.upload.MediaPublishingProgressListener;
import org.nuxeo.ecm.media.publishing.wistia.model.Project;
import org.nuxeo.ecm.media.publishing.wistia.model.Stats;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Wistia Media Publishing Provider Service
 *
 * @since 7.3
 */
public class WistiaService extends OAuth2MediaPublishingProvider {

    private static final Log log = LogFactory.getLog(WistiaService.class);

    public static final String PROVIDER = "Wistia";

    public WistiaService() {
        super(PROVIDER);
    }

    public WistiaClient getWistiaClient(String account) throws ClientException {
        Credential credential = getCredential(account);
        if (credential == null) {
            return null;
        }
        try {
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
        } catch (IOException e) {
            throw new ClientException(e.getMessage());
        }

        return new WistiaClient(credential.getAccessToken());
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
        WistiaClient client = getWistiaClient(account);
        return client == null ? null : client.getAccount().getUrl() + "/medias/" + mediaId;
    }

    @Override
    public String getEmbedCode(String mediaId, String account) {
        WistiaClient client = getWistiaClient(account);
        return client == null ? null : client.getEmbedCode(getPublishedUrl(mediaId, account));
    }

    @Override
    public Map<String, String> getStats(String mediaId, String account) {
        WistiaClient client = getWistiaClient(account);
        if (client == null) {
            return null;
        }

        Stats stats = client.getMediaStats(mediaId);
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

    public List<Project> getProjects(String account) {
        WistiaClient client = getWistiaClient(account);
        return client == null ? Collections.emptyList() : client.getProjects();
    }
}
