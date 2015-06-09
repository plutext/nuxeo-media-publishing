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

import org.nuxeo.ecm.core.api.DocumentModel;
import java.util.Map;

/**
 * @since 7.3
 */
public interface MediaPublishingService {

    /**
     * Return a list of the available media publishing services for the given document
     */
    String[] getAvailableProviders(DocumentModel doc);

    /**
     * Schedules an upload
     * @param doc the Document to upload
     * @param provider the id of the media publishing provider
     * @return the id of the publishing work
     */
    String publish(DocumentModel doc, String provider, String account, Map<String, String> options);

    /**
     * Return the provider with the given name.
     */
    MediaPublishingProvider getProvider(String provider);
}
