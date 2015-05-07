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

package org.nuxeo.ecm.social.publishing.youtube;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;
import com.google.api.services.youtube.model.VideoStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.platform.oauth2.providers.NuxeoOAuth2ServiceProvider;
import org.nuxeo.ecm.platform.oauth2.providers.OAuth2ServiceProviderRegistry;
import org.nuxeo.ecm.social.publishing.SocialMediaProvider;
import org.nuxeo.ecm.social.publishing.adapter.SocialMedia;
import org.nuxeo.ecm.social.publishing.upload.SocialMediaUploadProgressListener;
import org.nuxeo.ecm.webengine.oauth2.WEOAuthConstants;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

import java.io.IOException;
import java.util.*;

/**
 * Youtube Social Media Provider Service
 *
 * @since 7.3
 */
public class YouTubeService extends DefaultComponent implements SocialMediaProvider {
    private static final Log log = LogFactory.getLog(YouTubeService.class);

    public static final String CONFIGURATION_EP = "configuration";

    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    private String providerName;

    private String clientId;

    private String clientSecret;

    private String accountEmail;

    private NuxeoOAuth2ServiceProvider oauth2Provider;

    @Override
    public void registerContribution(Object contribution,
        String extensionPoint, ComponentInstance contributor) {
        if (CONFIGURATION_EP.equals(extensionPoint)) {
            YouTubeConfigurationDescriptor config = (YouTubeConfigurationDescriptor) contribution;
            providerName = config.getProvider();
            clientId = config.getClientId();
            clientSecret = config.getClientSecret();
            accountEmail = config.getAccountEmail();
        }
    }

    protected OAuth2ServiceProviderRegistry getOAuth2ServiceProviderRegistry() {
        return Framework.getLocalService(OAuth2ServiceProviderRegistry.class);
    }

    public String getAuthorizationURL(String serverURL) {
        AuthorizationCodeFlow flow = oauth2Provider.getAuthorizationCodeFlow(HTTP_TRANSPORT, JSON_FACTORY);
        if (serverURL.endsWith("/")) {
            serverURL = serverURL.substring(0, serverURL.length() - 1);
        }
        String redirectUrl = serverURL + WEOAuthConstants.getDefaultCallbackURL(oauth2Provider.getServiceName());

        // redirect to the authorization flow
        AuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl();
        authorizationUrl.setRedirectUri(redirectUrl);

        // request offline access and force consent screen
        return authorizationUrl.build();
    }

    protected NuxeoOAuth2ServiceProvider getOAuth2ServiceProvider() throws ClientException {
        // Register the system wide OAuth2 provider
        if (oauth2Provider == null) {
            OAuth2ServiceProviderRegistry oauth2ProviderRegistry = getOAuth2ServiceProviderRegistry();

            if (oauth2ProviderRegistry != null) {

                oauth2Provider = oauth2ProviderRegistry.getProvider(providerName);

                if (oauth2Provider == null) {
                    try {
                        oauth2Provider = oauth2ProviderRegistry.addProvider(
                            providerName,
                            GoogleOAuthConstants.TOKEN_SERVER_URL,
                            GoogleOAuthConstants.AUTHORIZATION_SERVER_URL + "?access_type=offline&approval_prompt=force",
                            clientId, clientSecret,
                            Arrays.asList(YouTubeScopes.YOUTUBE));
                    } catch (Exception e) {
                        throw new ClientException(e.getMessage());
                    }
                } else {
                    log.warn("Provider "
                        + providerName
                        + " is already in the Database, XML contribution  won't overwrite it");
                }
            }

            log.warn("Please got to " + getAuthorizationURL("http://localhost:8080") + " to start the authorization flow");

        }
        return oauth2Provider;
    }

    @Override
    public void applicationStarted(ComponentContext context) {
        getOAuth2ServiceProvider();
    }

    public YouTubeClient getYouTubeClient(String account) throws ClientException {
        YouTubeClient youTubeClient;
        Credential credential = null;

        // Use system wide OAuth2 provider
        if (getOAuth2ServiceProvider() != null) {
            AuthorizationCodeFlow flow = getOAuth2ServiceProvider().getAuthorizationCodeFlow(HTTP_TRANSPORT, JSON_FACTORY);
            try {
                credential = flow.loadCredential(account);
            } catch (IOException e) {
                throw new ClientException(e.getMessage());
            }
        }

        if (credential != null && credential.getAccessToken() != null) {
            youTubeClient = new YouTubeClient(credential);
        } else {
            throw new ClientException("Failed to get YouTube credentials");
        }

        return youTubeClient;
    }

    public YouTubeClient getYouTubeClient(CoreSession session) throws ClientException {
        return getYouTubeClient(session.getPrincipal().getName());
    }

    @Override
    public String upload(SocialMedia media, SocialMediaUploadProgressListener progressListener, String account, Map<String, String> options) throws IOException {

        MediaHttpUploaderProgressListener mediaUploaderListener = new MediaHttpUploaderProgressListener() {
            @Override
            public void progressChanged(MediaHttpUploader uploader) throws IOException {
                switch (uploader.getUploadState()) {
                case INITIATION_STARTED:
                case INITIATION_COMPLETE:
                    progressListener.onStart();
                    break;
                case MEDIA_IN_PROGRESS:
                    progressListener.onProgress(uploader.getProgress());
                    break;
                case MEDIA_COMPLETE:
                    progressListener.onComplete();
                    break;
                case NOT_STARTED:
                    log.info("Upload Not Started!");
                    break;
                }
            }
        };

        Video youtubeVideo = new Video();

        VideoStatus status = new VideoStatus();
        String privacyStatus = options.get("privacyStatus");
        if (privacyStatus != null) {
            status.setPrivacyStatus(privacyStatus);
        } else {
            status.setPrivacyStatus("unlisted");
        }
        youtubeVideo.setStatus(status);

        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle(media.getTitle());
        snippet.setDescription(media.getDescription());

        List<String> tags = new ArrayList<>();
        String inputTags = options.get("tags");
        if (inputTags != null) {
            tags.addAll(Arrays.asList(inputTags.split("\\s*,\\s*")));
        }
        snippet.setTags(tags);

        youtubeVideo.setSnippet(snippet);

        // upload original video
        Blob blob = media.getBlob();

        String mimeType = blob.getMimeType();
        long length = blob.getLength();
        youtubeVideo = getYouTubeClient(account).upload(youtubeVideo, blob.getStream(), mimeType, length, mediaUploaderListener);

        return youtubeVideo.getId();
    }

    @Override
    public String getPublishedUrl(String mediaId, String account) {
        return "https://www.youtube.com/watch?v=" + mediaId;
    }

    @Override
    public String getEmbedCode(String mediaId, String account) {
        return (mediaId == null) ? null : "https://www.youtube.com/embed/" + mediaId;
    }

    @Override
    public Map<String, String> getStats(String mediaId, String account) {
        try {
            VideoStatistics stats = getYouTubeClient(account).getStatistics(mediaId);
            if (stats == null) {
                return null;
            }

            Map<String, String> map = new HashMap<>();
            map.put("Views", stats.getViewCount().toString());
            map.put("Comments", stats.getCommentCount().toString());
            map.put("Likes", stats.getLikeCount().toString());
            map.put("Dislikes", stats.getDislikeCount().toString());
            map.put("Favorites", stats.getFavoriteCount().toString());
            return map;
        } catch (IOException e) {
//            throw new ClientException(e.getMessage());
            return null;
        }
    }

    @Override
    public List getProjects(String account) {
        return null;
    }
}
