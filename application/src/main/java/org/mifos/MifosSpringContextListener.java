/*
 * Copyright (c) 2005-2010 Grameen Foundation USA
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */

package org.mifos;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.mifos.accounts.financial.util.helpers.FinancialRules;
import org.mifos.core.ClasspathResource;
import org.mifos.framework.util.helpers.FilePaths;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;

/**
 * I extend {@link ContextLoaderListener} so we can cater for the approach used to customise {@link FinancialRules}.
 */
public class MifosSpringContextListener extends ContextLoaderListener {

    @Override
    protected void customizeContext(ServletContext servletContext, ConfigurableWebApplicationContext applicationContext) {
        super.customizeContext(servletContext, applicationContext);

        List<String> configLocations = new ArrayList<String>();

        String[] oldConfigLocations = applicationContext.getConfigLocations();
        for (String config : oldConfigLocations) {
            configLocations.add(config);
        }

        if (null != ClasspathResource.findResource(FilePaths.SPRING_CONFIG_CUSTOM_BEANS)) {
            configLocations.add(FilePaths.SPRING_CONFIG_CUSTOM_BEANS);
        }

        applicationContext.setConfigLocations(configLocations.toArray(new String[configLocations.size()]));
    }
}