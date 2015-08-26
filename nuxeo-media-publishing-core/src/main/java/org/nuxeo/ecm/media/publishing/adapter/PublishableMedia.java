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

package org.nuxeo.ecm.media.publishing.adapter;

import org.nuxeo.ecm.core.api.Blob;

import java.util.List;
import java.util.Map;

/**
 * @since 7.3
 */
public interface PublishableMedia {
    Map<String, Object> getProvider(String provider);

    void putProvider(Map<String, Object> provider);

    void removeProvider(String provider);

    boolean isPublishedByProvider(String provider);

    List getProviders();

    void setProviders(List<Map<String,Object>> providers);

    String getId(String provider);

    String getAccount(String provider);

    void setId(String id);

    String getTitle();

    String getDescription();

    Blob getBlob();

    String getUrl(String provider);

    String getEmbedCode(String provider);

    Map<String, String> getStats(String provider);
}
