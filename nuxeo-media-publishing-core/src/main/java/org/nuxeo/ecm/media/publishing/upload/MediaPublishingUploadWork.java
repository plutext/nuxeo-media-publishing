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

package org.nuxeo.ecm.media.publishing.upload;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.api.event.DocumentEventCategories;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventProducer;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.versioning.VersioningService;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.ecm.media.publishing.MediaPublishingConstants;
import org.nuxeo.ecm.media.publishing.MediaPublishingProvider;
import org.nuxeo.ecm.media.publishing.adapter.PublishableMedia;
import org.nuxeo.runtime.api.Framework;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Work for asynchronous media upload.
 *
 * @since 7.3
 */
public class MediaPublishingUploadWork extends AbstractWork {
    public static final String CATEGORY_VIDEO_UPLOAD = "mediaPublishingUpload";

    private final String serviceId;
    private final MediaPublishingProvider service;
    private CoreSession loginSession;
    private String account;
    private Map<String, String> options;

    private static final Log log = LogFactory.getLog(MediaPublishingUploadWork.class);

    public MediaPublishingUploadWork(String serviceId, MediaPublishingProvider service, String repositoryName,
        String docId, CoreSession loginSession, String account, Map<String, String> options) {
        super(getIdFor(repositoryName, docId, serviceId));
        this.serviceId = serviceId;
        this.service = service;
        this.loginSession = loginSession;
        this.account = account;
        this.options = options;
        setDocument(repositoryName, docId);
    }

    public static String getIdFor(String repositoryName, String docId, String provider) {
        return "media_" + provider + "_upload_" + repositoryName + "_" + docId;
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
            public void run() {
                final DocumentModel doc = session.getDocument(idRef);
                PublishableMedia media = doc.getAdapter(PublishableMedia.class);

                MediaPublishingProgressListener listener = new MediaPublishingProgressListener() {
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
                    String mediaId = service.upload(media, listener, account, options);
                    ArrayList<Map<String, Object>> providers = media.getProviders();
                    HashMap<String, Object> entry = new HashMap<>(3);
                    entry.put(MediaPublishingConstants.ID_PROPERTY_NAME, mediaId);
                    entry.put(MediaPublishingConstants.PROVIDER_PROPERTY_NAME, serviceId);
                    entry.put(MediaPublishingConstants.ACCOUNT_PROPERTY_NAME, account);

                    boolean providerExists = false;
                    // Check if provider already exists
                    // if so replace entry, otherwise add
                    for (Map<String, Object> e : providers) {
                        if (e.containsValue(serviceId)) {
                            e.replace(MediaPublishingConstants.ID_PROPERTY_NAME, mediaId);
                            e.replace(MediaPublishingConstants.ACCOUNT_PROPERTY_NAME, account);
                            providerExists = true;
                        }
                    }

                    if (!providerExists) {
                        providers.add(entry);
                    }

                    doc.setPropertyValue(MediaPublishingConstants.PROVIDERS_PROPERTY_NAME, providers);

                    // We don't want to erase the current version
                    doc.putContextData(VersioningService.VERSIONING_OPTION, VersioningOption.NONE);
                    doc.putContextData(VersioningService.DISABLE_AUTO_CHECKOUT, Boolean.TRUE);

                    // Track media publication in document history
                    DocumentEventContext ctx = new DocumentEventContext(loginSession, loginSession.getPrincipal(), doc);
                    ctx.setComment("Published to " + serviceId);
                    ctx.setCategory(DocumentEventCategories.EVENT_DOCUMENT_CATEGORY);

                    EventProducer evtProducer = Framework.getService(EventProducer.class);
                    Event event = ctx.newEvent(DocumentEventTypes.DOCUMENT_PUBLISHED);
                    evtProducer.fireEvent(event);
                    doc.getCoreSession().saveDocument(doc);
                    session.save();
                } catch (IOException e) {
                    throw new NuxeoException("Failed to upload media", e);
                }

            }
        }.runUnrestricted();

    }
}
