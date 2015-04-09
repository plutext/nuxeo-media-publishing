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

package org.nuxeo.ecm.social.publishing.adapter;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;

import java.util.ArrayList;
import java.util.Map;

/**
 * @since 7.3
 */
public interface SocialMedia {
    String getProvider() throws ClientException;

    boolean isPublishedByProvider(String provider) throws ClientException;

    ArrayList getProviders() throws ClientException;

    void setProvider(String name) throws ClientException;

    String getId(String provider) throws ClientException;

    String getAccount(String provider) throws ClientException;

    void setId(String id) throws ClientException;

    String getTitle();

    String getDescription();

    Blob getBlob();

    String getUrl(String provider);

    String getEmbedCode(String provider);

    Map<String, String> getStats(String provider);
}
