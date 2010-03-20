/*
 * Copyright (c) 2005-2009 Grameen Foundation USA
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

package org.mifos.application.servicefacade;

import java.util.Locale;

import org.mifos.security.util.UserContext;

public class GroupCreation {

    private final Short officeId;
    private final String parentSystemId;
    private final UserContext userContext;

    public GroupCreation(Short officeId, String parentSystemId, UserContext userContext) {
        this.officeId = officeId;
        this.parentSystemId = parentSystemId;
        this.userContext = userContext;
    }

    public Short getOfficeId() {
        return this.officeId;
    }

    public String getParentSystemId() {
        return this.parentSystemId;
    }

    public UserContext getUserContext() {
        return this.userContext;
    }

    public Locale getPreferredLocale() {
        return this.userContext.getPreferredLocale();
    }

    public Short getUserId() {
        return this.userContext.getId();
    }

    public Short getUserLevelId() {
        return this.userContext.getLevelId();
    }
}