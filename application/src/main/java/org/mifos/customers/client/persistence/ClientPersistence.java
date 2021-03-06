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

package org.mifos.customers.client.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;

import org.hibernate.Hibernate;
import org.mifos.accounts.savings.persistence.SavingsPersistence;
import org.mifos.customers.client.business.ClientBO;
import org.mifos.customers.exceptions.CustomerException;
import org.mifos.customers.office.persistence.OfficePersistence;
import org.mifos.customers.persistence.CustomerPersistence;
import org.mifos.customers.util.helpers.CustomerConstants;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.persistence.Persistence;

public class ClientPersistence extends Persistence {

    private final CustomerPersistence customerPersistence = new CustomerPersistence();
    private final OfficePersistence officePersistence = new OfficePersistence();

    /**
     * Get a client by Id and inject any required dependencies
     */
    public ClientBO getClient(final Integer customerId) throws PersistenceException {
        return (ClientBO) getPersistentObject(ClientBO.class, customerId);
    }

    public Blob createBlob(final InputStream picture) throws PersistenceException {
        try {
            return Hibernate.createBlob(picture);
        } catch (IOException ioe) {
            throw new PersistenceException(ioe);
        }
    }

    public CustomerPersistence getCustomerPersistence() {
        return customerPersistence;
    }

    public OfficePersistence getOfficePersistence() {
        return officePersistence;
    }

    public void saveClient(final ClientBO clientBO) throws CustomerException {
        CustomerPersistence customerPersistence = new CustomerPersistence();
        customerPersistence.saveCustomer(clientBO);
        try {
            if (clientBO.getParentCustomer() != null) {
                customerPersistence.createOrUpdate(clientBO.getParentCustomer());
            }
            // seems fishy... why do savings accounts need updating here?
            new SavingsPersistence().persistSavingAccounts(clientBO);
        } catch (PersistenceException pe) {
            throw new CustomerException(CustomerConstants.CREATE_FAILED_EXCEPTION, pe);
        }
    }
}
