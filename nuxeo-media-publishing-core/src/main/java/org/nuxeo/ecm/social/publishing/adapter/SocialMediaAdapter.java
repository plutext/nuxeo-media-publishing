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
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.social.publishing.SocialMediaConstants;
import org.nuxeo.ecm.social.publishing.SocialMediaProvider;
import org.nuxeo.ecm.social.publishing.SocialPublishingService;
import org.nuxeo.runtime.api.Framework;

import java.util.ArrayList;
import java.util.Map;

/**
 * @since 7.3
 */
public class SocialMediaAdapter implements SocialMedia {
    final DocumentModel doc;

    public SocialMediaAdapter(DocumentModel doc) {
        this.doc = doc;
    }

    @Override
    public String getProvider() throws ClientException {
        return (String) doc.getPropertyValue(SocialMediaConstants.PROVIDER_PROPERTY_NAME);
    }

    @Override
    public boolean isPublishedByProvider(String provider) throws ClientException {
        ArrayList<Map<String, Object>> providers = getProviders();
        for (Map<String, Object> entry : providers) {
            if (entry.containsValue(provider))
                return true;
        }
        return false;
    }

    @Override
    public ArrayList getProviders() throws ClientException {
        return (ArrayList) doc.getPropertyValue(SocialMediaConstants.PROVIDERS_PROPERTY_NAME);
    }

    @Override
    public void setProvider(String name) throws ClientException {

    }

    @Override
    public String getId(String provider) throws ClientException {
        Map<String, Object> entry = getProviderEntry(provider);
        if (entry == null) {
            return null;
        }
        return (String) entry.get(SocialMediaConstants.ID_PROPERTY_NAME);
    }

    @Override
    public String getAccount(String provider) throws ClientException {
        Map<String, Object> entry = getProviderEntry(provider);
        if (entry == null) {
            return null;
        }
        return (String) entry.get(SocialMediaConstants.ACCOUNT_PROPERTY_NAME);
    }

    @Override
    public void setId(String id) throws ClientException {

    }

    @Override
    public String getTitle() {
        return doc.getTitle();
    }

    @Override
    public String getDescription() {
        return (String) doc.getPropertyValue("dc:description");
    }

    @Override
    public Blob getBlob() {
        return doc.getAdapter(BlobHolder.class).getBlob();
    }

    @Override
    public String getUrl(String provider) {

        return getSocialMediaProvider(provider).getPublishedUrl(getId(provider), getAccount(provider));
    }

    @Override
    public String getEmbedCode(String provider) {
        return getSocialMediaProvider(provider).getEmbedCode(getId(provider), getAccount(provider));
    }

    @Override
    public Map<String, String> getStats(String provider) {
        return getSocialMediaProvider(provider).getStats(getId(provider), getAccount(provider));
    }

    private SocialMediaProvider getSocialMediaProvider(String provider) {
        return getSocialPublishingService().getProvider(provider);
    }

    private SocialPublishingService getSocialPublishingService() {
        return Framework.getService(SocialPublishingService.class);
    }

    private Map<String, Object> getProviderEntry(String provider) {
        ArrayList<Map<String, Object>> providers = (ArrayList) doc.getPropertyValue(SocialMediaConstants.PROVIDERS_PROPERTY_NAME);
        for (Map<String, Object> entry : providers) {
            if (entry.containsValue(provider)) {
                return entry;
            }
        }
        return null;
    }
}
