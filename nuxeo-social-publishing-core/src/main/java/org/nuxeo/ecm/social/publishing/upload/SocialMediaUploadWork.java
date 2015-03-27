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

package org.nuxeo.ecm.social.publishing.upload;

import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.versioning.VersioningService;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.ecm.social.publishing.SocialMediaConstants;
import org.nuxeo.ecm.social.publishing.SocialMediaProvider;
import org.nuxeo.ecm.social.publishing.adapter.SocialMedia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Work for asynchronous social media upload.
 *
 * @since 7.2
 */
public class SocialMediaUploadWork extends AbstractWork {
    public static final String CATEGORY_VIDEO_UPLOAD = "socialMediaUpload";

    private final String serviceId;
    private final SocialMediaProvider service;
    private CoreSession loginSession;
    private String account;

    public SocialMediaUploadWork(String serviceId, SocialMediaProvider service, String repositoryName, String docId, CoreSession loginSession, String account) {
        super(getIdFor(repositoryName, docId, serviceId));
        this.serviceId = serviceId;
        this.service = service;
        this.loginSession = loginSession;
        this.account = account;
        setDocument(repositoryName, docId);
    }

    public static String getIdFor(String repositoryName, String docId, String provider) {
        return "social_media_" + provider + "_upload_" + repositoryName + "_" + docId;
    }

    @Override
    public String getCategory() {
        return CATEGORY_VIDEO_UPLOAD;
    }

    @Override
    public String getTitle() {
        return "Video Upload: " + docId;
    }

    @Override
    public void work() {
        final IdRef idRef = new IdRef(docId);
        new UnrestrictedSessionRunner(repositoryName) {
            @Override
            public void run() throws ClientException {
                final DocumentModel doc = session.getDocument(idRef);
                SocialMedia media = doc.getAdapter(SocialMedia.class);

                SocialMediaUploadProgressListener listener = new SocialMediaUploadProgressListener() {
                    @Override
                    public void onStart() {
                        setProgress(Progress.PROGRESS_0_PC);
                    }

                    @Override
                    public void onProgress(double progress) {
                        setProgress(new Progress(new Float(progress)));
                    }

                    @Override
                    public void onComplete() {
                        setProgress(Progress.PROGRESS_100_PC);
                    }

                    @Override
                    public void onError() {
                        setStatus("Error");
                    }
                };
                try {
                    String mediaId = service.upload(media, listener, account);
                    ArrayList<HashMap<String, Object>> providers = media.getProviders();
                    HashMap<String, Object> entry = new HashMap<>(3);
                    entry.put(SocialMediaConstants.ID_PROPERTY_NAME, mediaId);
                    entry.put(SocialMediaConstants.PROVIDER_PROPERTY_NAME, serviceId);
                    entry.put(SocialMediaConstants.ACCOUNT_PROPERTY_NAME, account);

                    boolean providerExists = false;
                    // Check if provider already exists
                    // if so replace entry, otherwise add
                    for (HashMap<String, Object> e : providers) {
                        if (e.containsValue(serviceId)) {
                            e.replace(SocialMediaConstants.ID_PROPERTY_NAME, mediaId);
                            providerExists = true;
                        }
                    }

                    if (!providerExists) {
                        providers.add(entry);
                    }

                    doc.setPropertyValue(SocialMediaConstants.PROVIDERS_PROPERTY_NAME, providers);

                    // We don't want to erase the current version
                    doc.putContextData(VersioningService.VERSIONING_OPTION, VersioningOption.NONE);
                    doc.putContextData(VersioningService.DISABLE_AUTO_CHECKOUT, Boolean.TRUE);

                    doc.getCoreSession().saveDocument(doc);
                    session.save();
                } catch (IOException e) {
                    throw new ClientException("Failed to upload social media", e);
                }

            }
        }.runUnrestricted();

    }
}
