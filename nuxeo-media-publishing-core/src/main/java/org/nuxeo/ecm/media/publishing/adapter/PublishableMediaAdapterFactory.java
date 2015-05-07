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

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.adapter.DocumentAdapterFactory;
import org.nuxeo.ecm.media.publishing.MediaPublishingConstants;

/**
 * Publishable Media Adapter Factory
 *
 * @since 7.3
 */
public class PublishableMediaAdapterFactory implements DocumentAdapterFactory {
    @Override
    public Object getAdapter(DocumentModel doc, Class itf) {
        if (doc.hasFacet(MediaPublishingConstants.PUBLISHABLE_MEDIA_FACET)) {
            return new PublishableMediaAdapter(doc);
        } else {
            return null;
        }
    }
}
