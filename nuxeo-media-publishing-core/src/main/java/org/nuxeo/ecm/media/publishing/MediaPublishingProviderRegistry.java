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

import org.nuxeo.runtime.model.ContributionFragmentRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @since 7.3
 */
public class MediaPublishingProviderRegistry extends ContributionFragmentRegistry<MediaPublishingProviderDescriptor> {

    protected final Map<String, MediaPublishingProviderDescriptor> providers = new HashMap<>();

    @Override
    public MediaPublishingProviderDescriptor clone(MediaPublishingProviderDescriptor source) {
        MediaPublishingProviderDescriptor copy = new MediaPublishingProviderDescriptor();
        // TODO
        return copy;
    }

    @Override
    public void contributionRemoved(String name, MediaPublishingProviderDescriptor origContrib) {
        providers.remove(name);
    }

    @Override
    public void contributionUpdated(String name, MediaPublishingProviderDescriptor contrib,
        MediaPublishingProviderDescriptor newOrigContrib) {
        if (newOrigContrib.isEnabled()) {
            providers.put(name, newOrigContrib);
        } else {
            providers.remove(name);
        }
    }

    @Override
    public String getContributionId(MediaPublishingProviderDescriptor contrib) {
        return contrib.getId();
    }

    @Override
    public void merge(MediaPublishingProviderDescriptor src, MediaPublishingProviderDescriptor dst) {
        // TODO
    }

    public Set<String> getServices() {
        return providers.keySet();
    }

    public MediaPublishingProviderDescriptor lookup(String provider) {
        return providers.get(provider);
    }
}

