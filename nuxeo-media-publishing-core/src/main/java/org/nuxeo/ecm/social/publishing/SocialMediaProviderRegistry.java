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

import org.nuxeo.runtime.model.ContributionFragmentRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @since 7.3
 */
public class SocialMediaProviderRegistry extends ContributionFragmentRegistry<SocialMediaProviderDescriptor> {

    protected final Map<String, SocialMediaProviderDescriptor> providers = new HashMap<>();

    @Override
    public SocialMediaProviderDescriptor clone(SocialMediaProviderDescriptor source) {
        SocialMediaProviderDescriptor copy = new SocialMediaProviderDescriptor();
        // TODO
        return copy;
    }

    @Override
    public void contributionRemoved(String name, SocialMediaProviderDescriptor origContrib) {
        providers.remove(name);
    }

    @Override
    public void contributionUpdated(String name, SocialMediaProviderDescriptor contrib,
        SocialMediaProviderDescriptor newOrigContrib) {
        if (newOrigContrib.isEnabled()) {
            providers.put(name, newOrigContrib);
        } else {
            providers.remove(name);
        }
    }

    @Override
    public String getContributionId(SocialMediaProviderDescriptor contrib) {
        return contrib.getId();
    }

    @Override
    public void merge(SocialMediaProviderDescriptor src, SocialMediaProviderDescriptor dst) {
        // TODO
    }

    public Set<String> getServices() {
        return providers.keySet();
    }

    public SocialMediaProviderDescriptor lookup(String provider) {
        return providers.get(provider);
    }
}

