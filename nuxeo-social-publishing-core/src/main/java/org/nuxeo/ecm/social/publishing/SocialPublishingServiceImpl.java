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
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.work.api.Work;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.ecm.social.publishing.upload.SocialMediaUploadWork;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

public class SocialPublishingServiceImpl extends DefaultComponent implements SocialPublishingService {

    protected static final Log log = LogFactory.getLog(SocialPublishingServiceImpl.class);

    public static final String PROVIDER_EP = "providers";

    protected SocialMediaProviderRegistry providers = new SocialMediaProviderRegistry();

    @Override
    public String[] getAvailableProviders(DocumentModel doc) {
        return providers.getServices().toArray(new String[]{});
    }

    public SocialMediaProvider getProvider(String provider) {
        SocialMediaProviderDescriptor descriptor = providers.lookup(provider);
        return (SocialMediaProvider) Framework.getService(descriptor.getService());
    }

    @Override
    public String publish(DocumentModel doc, String serviceId, String account) {
        SocialMediaProvider service = getProvider(serviceId);
        WorkManager workManager = Framework.getLocalService(WorkManager.class);
        if (workManager == null) {
            throw new RuntimeException("No WorkManager available");
        }

        Work work = new SocialMediaUploadWork(serviceId, service, doc.getRepositoryName(), doc.getId(), doc.getCoreSession(), account);
        workManager.schedule(work, WorkManager.Scheduling.IF_NOT_RUNNING_OR_SCHEDULED);
        return work.getId();
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        if (PROVIDER_EP.equals(extensionPoint)) {
            SocialMediaProviderDescriptor provider = (SocialMediaProviderDescriptor) contribution;
            providers.addContribution(provider);
        }
    }
}
