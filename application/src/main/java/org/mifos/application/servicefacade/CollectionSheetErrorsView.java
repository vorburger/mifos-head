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

import java.util.List;

/**
 *
 */
public class CollectionSheetErrorsView {

    private final List<String> customerNames;
    private final List<String> savingsDepNames;
    private final List<String> savingsWithNames;

    public CollectionSheetErrorsView(List<String> customerNames, List<String> savingsDepNames,
            List<String> savingsWithNames) {
        this.customerNames = customerNames;
        this.savingsDepNames = savingsDepNames;
        this.savingsWithNames = savingsWithNames;
    }

    public List<String> getCustomerNames() {
        return this.customerNames;
    }

    public List<String> getSavingsDepNames() {
        return this.savingsDepNames;
    }

    public List<String> getSavingsWithNames() {
        return this.savingsWithNames;
    }
}