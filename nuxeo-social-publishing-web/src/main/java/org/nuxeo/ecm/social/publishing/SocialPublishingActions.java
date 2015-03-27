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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Interpolator;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.work.api.Work;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.social.publishing.adapter.SocialMedia;
import org.nuxeo.ecm.social.publishing.upload.SocialMediaUploadWork;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;
import org.nuxeo.runtime.api.Framework;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Name("socialPublishing")
@Scope(ScopeType.EVENT)
public class SocialPublishingActions implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(SocialPublishingActions.class);

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In(create = true, required = false)
    protected transient FacesMessages facesMessages;

    @In(create = true)
    protected transient ResourcesAccessor resourcesAccessor;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    private static String selectedAccount;

    private Map<String, Object> providersStats;

    private Map<String, String> providersURL;

    private Map<String, String> providersEmbedCode;

    public SocialPublishingActions() {
        providersStats = new HashMap<>();
        providersEmbedCode = new HashMap<>();
        providersURL = new HashMap<>();
    }

    public String[] getAvailableServices(DocumentModel doc) {
        return getSocialPublishingService().getAvailableProviders(doc);
    }

    public UploadStatus getUploadStatus(DocumentModel doc, String uploadServiceName) {
        WorkManager workManager = Framework.getLocalService(WorkManager.class);

        String workId = SocialMediaUploadWork.getIdFor(doc.getRepositoryName(), doc.getId(), uploadServiceName);
        Work.State state = workManager.getWorkState(workId);

        if (state == null) {
            return null;
        } else {
            switch (state) {
                case SCHEDULED:
                    return new UploadStatus(UploadStatus.STATUS_UPLOAD_QUEUED, new Work.Progress(0));
                case RUNNING:
                    return new UploadStatus(UploadStatus.STATUS_UPLOAD_PENDING, new Work.Progress(0));
                default:
                    return null;
            }
        }
    }

    public boolean isPublished(DocumentModel doc, String provider) {
        SocialMedia media = doc.getAdapter(SocialMedia.class);
        return media != null && media.getId(provider) != null && media.isPublishedByProvider(provider);
    }

    public String getPublishedURL(DocumentModel doc, String provider) {
        String url = providersURL.get(provider);
        if (url == null) {
            SocialMedia media = doc.getAdapter(SocialMedia.class);
            url = media.getUrl(provider);
            providersURL.put(provider, url);
        }
        return url;
    }

    public void publish(String provider) {
        DocumentModel doc = navigationContext.getCurrentDocument();
        if (selectedAccount == null || selectedAccount.length() == 0) {
            return;
        }
        getSocialPublishingService().publish(doc, provider, selectedAccount);
    }

    public String getEmbedCode(DocumentModel doc, String provider) {
        String embedCode = providersEmbedCode.get(provider);
        if (embedCode == null) {
            SocialMedia media = doc.getAdapter(SocialMedia.class);
            embedCode = media.getEmbedCode(provider);
            providersEmbedCode.put(provider, embedCode);
        }
        return embedCode;
    }

    public Map<String, String> getStats(DocumentModel doc, String provider) {
        Map<String, String> stats = (Map<String, String>) providersStats.get(provider);
        if (stats == null) {
            SocialMedia media = doc.getAdapter(SocialMedia.class);
            stats = media.getStats(provider);
            providersStats.put(provider, stats);
        }

        return stats;
    }

    private SocialPublishingService getSocialPublishingService() {
        return Framework.getService(SocialPublishingService.class);
    }

    public String getStatusMessageFor(UploadStatus status) {
        if (status == null) {
            return "";
        }
        String i18nMessageTemplate = resourcesAccessor.getMessages().get(status.getMessage());
        if (i18nMessageTemplate == null) {
            return "";
        } else {
            return Interpolator.instance().interpolate(i18nMessageTemplate,
                    status.positionInQueue, status.queueSize, status.progress.getCurrent());
        }
    }

    public String getSelectedAccount() {
        return selectedAccount;
    }

    public void setSelectedAccount(String selectedAccount) {
        this.selectedAccount = selectedAccount;
    }

    /**
     * Data transfer object to report on the state of a video upload.
     */
    public class UploadStatus {
        public static final String STATUS_UPLOAD_QUEUED = "status.video.uploadQueued";

        public static final String STATUS_UPLOAD_PENDING = "status.video.uploadPending";

        public final String message;

        public final int positionInQueue;

        public final int queueSize;

        public final Work.Progress progress;

        public UploadStatus(String message, Work.Progress progress) {
            this(message, progress, 0, 0);
        }

        public UploadStatus(String message, Work.Progress progress, int positionInQueue, int queueSize) {
            this.message = message;
            this.progress = progress;
            this.positionInQueue = positionInQueue;
            this.queueSize = queueSize;
        }

        public String getMessage() {
            return message;
        }

        public Work.Progress getProgress() {
            return progress;
        }

        public int getPositionInQueue() {
            return positionInQueue;
        }

        public int getQueueSize() {
            return queueSize;
        }
    }

}
