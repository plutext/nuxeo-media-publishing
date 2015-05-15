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

import org.nuxeo.ecm.media.publishing.adapter.PublishableMedia;
import org.nuxeo.ecm.media.publishing.upload.MediaPublishingProgressListener;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interface to be implemented by Media Publishing Providers.
 *
 * @since 7.3
 */
public interface MediaPublishingProvider {

    /**
     * Upload the media
     */
    String upload(PublishableMedia media, MediaPublishingProgressListener progressListener, String account, Map<String, String> options) throws IOException;

    /**
     * Retrieve the URL for the published media
     */
    String getPublishedUrl(String mediaId, String account);

    /**
     * Retrieve the HTML code for embedding the media
     */
    String getEmbedCode(String mediaId, String account);

    /**
     * Retrieve a map of statistics (depends on the provider)
     */
    Map<String, String> getStats(String mediaId, String account);
}
