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

package org.nuxeo.ecm.social.publishing;

import org.nuxeo.ecm.core.api.DocumentModel;
import java.util.Map;

/**
 * @since 7.3
 */
public interface SocialPublishingService {

    /**
     * Return a list of the available social media services for the given document
     */
    String[] getAvailableProviders(DocumentModel doc);

    /**
     * Schedules an upload
     * @param doc the Document to upload
     * @param provider the id of the social media provider
     * @return the id of the publishing work
     */
    String publish(DocumentModel doc, String provider, String account, Map<String, String> options);

    SocialMediaProvider getProvider(String provider);
}
