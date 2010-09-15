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

package org.mifos.customers.struts.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.mifos.accounts.struts.action.AccountAppAction;
import org.mifos.customers.util.helpers.CustomerConstants;
import org.mifos.framework.util.helpers.CloseSession;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.framework.util.helpers.TransactionDemarcate;
import org.mifos.security.util.ActionSecurity;
import org.mifos.security.util.SecurityConstants;

public class CustomerAction extends AccountAppAction {

    private static final Logger logger = Logger.getLogger(CustomerAction.class);

    public CustomerAction() throws Exception {
        super();
    }

    public static ActionSecurity getSecurity() {
        ActionSecurity security = new ActionSecurity("customerAction");
        security.allow("forwardWaiveChargeDue", SecurityConstants.VIEW);
        security.allow("forwardWaiveChargeOverDue", SecurityConstants.VIEW);
        security.allow("waiveChargeDue", SecurityConstants.VIEW);
        security.allow("waiveChargeOverDue", SecurityConstants.VIEW);
        security.allow("getAllActivity", SecurityConstants.VIEW);
        security.allow("getAllClosedAccounts", SecurityConstants.VIEW);
        return security;
    }

    @TransactionDemarcate(validateAndResetToken = true)
    @CloseSession
    public ActionForward forwardWaiveChargeDue(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String type = request.getParameter("type");
        return mapping.findForward("waive" + type + "Charges_Success");
    }

    @TransactionDemarcate(validateAndResetToken = true)
    @CloseSession
    public ActionForward forwardWaiveChargeOverDue(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String type = request.getParameter("type");
        return mapping.findForward("waive" + type + "Charges_Success");
    }

    @TransactionDemarcate(joinToken = true)
    public ActionForward getAllActivity(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        logger.debug("In CustomerAction::getAllActivity()");
        String type = request.getParameter("type");
        String globalCustNum = request.getParameter("globalCustNum");
        SessionUtils.setCollectionAttribute(CustomerConstants.CLIENTRECENTACCACTIVITYLIST, getCustomerBusinessService()
                .getAllActivityView(globalCustNum), request);
        return mapping.findForward("view" + type + "Activity");
    }
}
