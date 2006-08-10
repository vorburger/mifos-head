/**

 * CustomerBO.java    version: xxx



 * Copyright (c) 2005-2006 Grameen Foundation USA

 * 1029 Vermont Avenue, NW, Suite 400, Washington DC 20005

 * All rights reserved.



 * Apache License
 * Copyright (c) 2005-2006 Grameen Foundation USA
 *

 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the

 * License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an explanation of the license

 * and how it is applied.

 *

 */

package org.mifos.application.customer.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.mifos.application.accounts.business.AccountBO;
import org.mifos.application.accounts.business.AccountNotesEntity;
import org.mifos.application.accounts.business.AccountStateEntity;
import org.mifos.application.accounts.business.AccountStateFlagEntity;
import org.mifos.application.accounts.business.AccountStateMachines;
import org.mifos.application.accounts.business.AccountStatusChangeHistoryEntity;
import org.mifos.application.accounts.business.CustomerAccountBO;
import org.mifos.application.accounts.exceptions.AccountException;
import org.mifos.application.accounts.loan.business.LoanBO;
import org.mifos.application.accounts.savings.business.SavingsBO;
import org.mifos.application.accounts.savings.util.helpers.SavingsConstants;
import org.mifos.application.accounts.util.helpers.AccountStates;
import org.mifos.application.accounts.util.helpers.AccountTypes;
import org.mifos.application.configuration.business.ConfigurationIntf;
import org.mifos.application.configuration.business.MifosConfiguration;
import org.mifos.application.configuration.util.helpers.ConfigurationConstants;
import org.mifos.application.customer.center.exception.StateChangeException;
import org.mifos.application.customer.dao.ViewClosedAccountsDAO;
import org.mifos.application.customer.exceptions.CustomerException;
import org.mifos.application.customer.exceptions.CustomerStateChangeException;
import org.mifos.application.customer.persistence.CustomerPersistence;
import org.mifos.application.customer.util.helpers.CustomerConstants;
import org.mifos.application.customer.util.helpers.CustomerLevel;
import org.mifos.application.customer.util.helpers.CustomerStatus;
import org.mifos.application.customer.util.helpers.IdGenerator;
import org.mifos.application.fees.business.FeeView;
import org.mifos.application.fees.exceptions.FeeException;
import org.mifos.application.fees.persistence.FeePersistence;
import org.mifos.application.fees.util.helpers.FeeConstants;
import org.mifos.application.master.persistence.service.MasterPersistenceService;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.office.business.OfficeBO;
import org.mifos.application.office.persistence.OfficePersistence;
import org.mifos.application.personnel.business.PersonnelBO;
import org.mifos.application.personnel.persistence.PersonnelPersistence;
import org.mifos.application.util.helpers.YesNoFlag;
import org.mifos.framework.business.BusinessObject;
import org.mifos.framework.business.service.ServiceFactory;
import org.mifos.framework.business.util.Address;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.framework.exceptions.StatesInitializationException;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.security.util.ActivityMapper;
import org.mifos.framework.security.util.UserContext;
import org.mifos.framework.security.util.resources.SecurityConstants;
import org.mifos.framework.struts.plugin.helper.EntityMasterConstants;
import org.mifos.framework.util.helpers.Money;
import org.mifos.framework.util.helpers.PersistenceServiceName;
import org.mifos.framework.util.helpers.StringUtils;

/**
 * A class that represents a customer entity after being created.
 * 
 * @author navitas
 */
public abstract class CustomerBO extends BusinessObject {

	private final Integer customerId;

	private String globalCustNum;

	private String displayName;

	private String displayAddress;

	private String externalId;

	private Short trained;

	private Date trainedDate;

	private Date mfiJoiningDate;

	private String searchId;

	private Integer maxChildCount;

	private Date customerActivationDate;

	private CustomerStatusEntity customerStatus;

	private Set<CustomerCustomFieldEntity> customFields;

	private Set<CustomerPositionEntity> customerPositions;

	private Set<CustomerFlagDetailEntity> customerFlags;

	private CustomerBO parentCustomer;

	private Set<AccountBO> accounts;

	private final CustomerLevelEntity customerLevel;

	private PersonnelBO personnel;

	private PersonnelBO formedByPersonnel;

	private final OfficeBO office;

	private CustomerAddressDetailEntity customerAddressDetail;

	private CustomerMeetingEntity customerMeeting;

	private Set<CustomerHierarchyEntity> customerHierarchies;

	private CustomerHistoricalDataEntity historicalData;

	private Short blackListed;
	
	public Set<CustomerNoteEntity> customerNotes;


	protected CustomerBO() {
		super();
		this.customerId = null;
		this.globalCustNum = null;
		this.customerLevel = null;
		this.office = null;
	}

	protected CustomerBO(UserContext userContext, String displayName,
			CustomerLevel customerLevel, CustomerStatus customerStatus,
			Address address, List<CustomFieldView> customFields,
			List<FeeView> fees, Short formedBy, Short officeId,
			CustomerBO parentCustomer, MeetingBO meeting, Short loanOfficerId)
			throws CustomerException {
		super(userContext);
		validateFields(displayName, customerStatus, officeId);
		this.customFields = new HashSet<CustomerCustomFieldEntity>();
		this.accounts = new HashSet<AccountBO>();
		this.customerNotes = new HashSet<CustomerNoteEntity>();
		this.office = new OfficePersistence().getOffice(officeId);

		this.displayName = displayName;
		this.customerLevel = new CustomerLevelEntity(customerLevel);

		if (address != null) {
			this.customerAddressDetail = new CustomerAddressDetailEntity(this,
					address);
			this.displayAddress = this.customerAddressDetail
					.getDisplayAddress();
		}

		if (parentCustomer != null)
			this.personnel = parentCustomer.getPersonnel();
		else 
			if(loanOfficerId != null)
				this.personnel = new PersonnelPersistence().getPersonnel(loanOfficerId);

		if (parentCustomer != null
				&& parentCustomer.getCustomerMeeting() != null)
			this.customerMeeting = createCustomerMeeting(parentCustomer
					.getCustomerMeeting().getMeeting());
		else
			this.customerMeeting = createCustomerMeeting(meeting);

		if(formedBy!=null)
			this.formedByPersonnel = new PersonnelPersistence().getPersonnel(formedBy);
		
		this.parentCustomer = parentCustomer;

		if (customFields != null)
			for (CustomFieldView customField : customFields)
				addCustomField(new CustomerCustomFieldEntity(customField
						.getFieldId(), customField.getFieldValue(), this));

		this.customerStatus = new CustomerStatusEntity(customerStatus);
		this.maxChildCount = 0;
		this.blackListed = YesNoFlag.NO.getValue();
		this.customerId = null;
		this.historicalData = null;
		this.customerFlags = null;

		this.accounts.add(createCustomerAccount(fees));

		// TODO: write code to create customer hierarchy and add
		this.setCreateDetails();
	}

	public boolean isBlackList() {
		return blackListed.equals(YesNoFlag.YES.getValue());
	}

	public Integer getCustomerId() {
		return customerId;
	}

	public CustomerLevelEntity getCustomerLevel() {
		return this.customerLevel;
	}

	public String getGlobalCustNum() {
		return this.globalCustNum;
	}

	public PersonnelBO getPersonnel() {
		return this.personnel;
	}

	public void setPersonnel(PersonnelBO personnel) {
		this.personnel = personnel;
	}

	public CustomerStatusEntity getCustomerStatus() {
		return customerStatus;
	}

	public void setCustomerStatus(CustomerStatusEntity customerStatus) {
		this.customerStatus = customerStatus;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayAddress() {
		return this.displayAddress;
	}

	public void setDisplayAddress(String displayAddress) {
		this.displayAddress = displayAddress;
	}

	public String getExternalId() {
		return this.externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public void setTrained(boolean trained) {
		this.trained = (short) (trained ? 1 : 0);
	}

	public boolean isTrained() {
		return trained.equals(YesNoFlag.YES.getValue());
	}

	public Date getTrainedDate() {
		return this.trainedDate;
	}

	public void setTrainedDate(Date trainedDate) {
		this.trainedDate = trainedDate;
	}

	public String getSearchId() {
		return this.searchId;
	}

	public void setSearchId(String searchId) {
		this.searchId = searchId;
	}

	public Integer getMaxChildCount() {
		return this.maxChildCount;
	}

	public void setMaxChildCount(Integer maxChildCount) {
		this.maxChildCount = maxChildCount;
	}

	public CustomerAddressDetailEntity getCustomerAddressDetail() {
		return customerAddressDetail;
	}

	public void setCustomerAddressDetail(
			CustomerAddressDetailEntity customerAddressDetail) {
		this.customerAddressDetail = customerAddressDetail;
	}

	public OfficeBO getOffice() {
		return office;
	}

	public Set<CustomerNoteEntity> getCustomerNotes() {
		return customerNotes;
	}

	private void setCustomerNotes(Set<CustomerNoteEntity> customerNotes) {
		this.customerNotes = customerNotes;
	}

	public void save() throws ApplicationException, CustomerException {
		try {
			new CustomerPersistence().createOrUpdate(this);
			String gCustNum = IdGenerator.generateSystemIdForCustomer(
					getOffice().getGlobalOfficeNum(), getCustomerId());
			globalCustNum = (gCustNum);
			new CustomerPersistence().createOrUpdate(this);
		} catch (HibernateException he) {
			throw new CustomerException(
					CustomerConstants.CREATE_FAILED_EXCEPTION, he);
		}
	}
	
	public void update() throws CustomerException{
		try {
			setUpdateDetails();
			new CustomerPersistence().createOrUpdate(this);
		} catch (HibernateException he) {
			throw new CustomerException(CustomerConstants.UPDATE_FAILED_EXCEPTION, he);
		}
	}

	public CustomerAccountBO getCustomerAccount() {
		for (AccountBO account : accounts) {
			if (account.getAccountType().getAccountTypeId().equals(
					Short.valueOf(AccountTypes.CUSTOMERACCOUNT.getValue())))
				return (CustomerAccountBO) account;
		}
		return null;
	}

	public List<LoanBO> getLoanAccounts() {
		List<LoanBO> loanAccounts = new ArrayList<LoanBO>();
		for (AccountBO account : accounts) {
			if (account.getAccountType().getAccountTypeId().equals(
					AccountTypes.LOANACCOUNT.getValue()))
				loanAccounts.add((LoanBO) account);
		}
		return loanAccounts;
	}

	public List<LoanBO> getActiveAndApprovedLoanAccounts(Date transactionDate) {
		List<LoanBO> loanAccounts = new ArrayList<LoanBO>();
		for (AccountBO account : accounts) {
			if (account.getAccountType().getAccountTypeId().equals(
					AccountTypes.LOANACCOUNT.getValue())) {
				short accounStateId = account.getAccountState().getId()
						.shortValue();
				LoanBO loan = (LoanBO) account;
				if (accounStateId == AccountStates.LOANACC_ACTIVEINGOODSTANDING
						|| accounStateId == AccountStates.LOANACC_BADSTANDING) {
					loanAccounts.add(loan);
				} else if (accounStateId == AccountStates.LOANACC_APPROVED
						|| accounStateId == AccountStates.LOANACC_DBTOLOANOFFICER) {
					if (transactionDate.compareTo(loan.getDisbursementDate()) >= 0)
						loanAccounts.add(loan);
				}
			}
		}
		return loanAccounts;
	}

	public List<SavingsBO> getActiveSavingsAccounts() {
		List<SavingsBO> savingsAccounts = new ArrayList<SavingsBO>();
		for (AccountBO account : accounts) {
			if (account.getAccountType().getAccountTypeId().equals(
					AccountTypes.SAVINGSACCOUNT.getValue())
					&& account.getAccountState().getId().shortValue() == AccountStates.SAVINGS_ACC_APPROVED)
				savingsAccounts.add((SavingsBO) account);
		}
		return savingsAccounts;
	}
	
	public List<CustomerNoteEntity> getRecentCustomerNotes() {
		List<CustomerNoteEntity> notes = new ArrayList<CustomerNoteEntity>();
		int count = 0;
		for (CustomerNoteEntity customerNote : getCustomerNotes()) {
			if (count > 2)
				break;
			notes.add(customerNote);
			count++;
		}
		return notes;
	}
	
	public CustomerMeetingEntity getCustomerMeeting() {
		return customerMeeting;
	}

	public void setCustomerMeeting(CustomerMeetingEntity customerMeeting) {
		this.customerMeeting = customerMeeting;
	}

	// TODO: write code to fetch active hierarchy for the customer
	public CustomerHierarchyEntity getActiveCustomerHierarchy() {
		return null;
	}

	public void addCustomerHierarchy(CustomerHierarchyEntity hierarchy) {
		if (hierarchy != null) {
			this.customerHierarchies.add(hierarchy);
		}
	}

	public Set<CustomerPositionEntity> getCustomerPositions() {
		return customerPositions;
	}

	private void setCustomerPositions(
			Set<CustomerPositionEntity> customerPositions) {
		this.customerPositions = customerPositions;

	}

	public void addCustomerPosition(CustomerPositionEntity customerPosition) {
		customerPosition.setCustomer(this);
		this.customerPositions.add(customerPosition);
	}

	private void setHistoricalData(CustomerHistoricalDataEntity historicalData) {

		if (historicalData != null) {
			historicalData.setMfiJoiningDate(mfiJoiningDate);
		}

		this.historicalData = historicalData;

	}

	public void setCustomerHistoricalData(
			CustomerHistoricalDataEntity historicalData) {
		if (historicalData != null) {
			this.mfiJoiningDate = historicalData.getMfiJoiningDate();
		}
		setHistoricalData(historicalData);
	}

	public CustomerHistoricalDataEntity getHistoricalData() {
		return historicalData;
	}

	public Set<CustomerFlagDetailEntity> getCustomerFlags() {
		return customerFlags;
	}

	private void setCustomerFlags(Set<CustomerFlagDetailEntity> customerFlag) {
		this.customerFlags = customerFlag;
	}

	public void addCustomerFlag(CustomerFlagDetailEntity customerFlag) {
		this.customerFlags.add(customerFlag);
	}

	public Date getMfiJoiningDate() {
		return mfiJoiningDate;
	}

	public void setMfiJoiningDate(Date mfiJoiningDate) {
		this.mfiJoiningDate = mfiJoiningDate;
	}

	public void setParentCustomer(CustomerBO parentCustomer) {
		this.parentCustomer = parentCustomer;
	}

	public CustomerBO getParentCustomer() {
		return parentCustomer;
	}

	public Set<AccountBO> getAccounts() {
		return accounts;
	}

	public void addCustomerAccount(CustomerAccountBO customerAccount) {
		customerAccount.setCustomer(this);
		this.accounts.add(customerAccount);
	}

	public Date getCustomerActivationDate() {
		return customerActivationDate;
	}

	public Set<CustomerCustomFieldEntity> getCustomFields() {
		return customFields;
	}

	public void addCustomField(CustomerCustomFieldEntity customField) {
		if (customField != null) {
			this.customFields.add(customField);
		}
	}

	public void setCustomField(CustomerCustomFieldEntity customerCustomFieldView) {
		this.customFields.add(customerCustomFieldView);
	}

	public List<CustomerBO> getChildren(Short customerLevel)
			throws PersistenceException, ServiceException {
		return new CustomerPersistence().getChildrenForParent(getSearchId(),
				getOffice().getOfficeId(), customerLevel);
	}

	public PersonnelBO getCustomerFormedByPersonnel() {
		return formedByPersonnel;
	}

	public void setCustomerFormedByPersonnel(
			PersonnelBO customerFormedByPersonnel) {
		this.formedByPersonnel = customerFormedByPersonnel;
	}

	public void adjustPmnt(String adjustmentComment)
			throws ApplicationException, SystemException {
		getCustomerAccount().adjustPmnt(adjustmentComment);
	}

	public abstract boolean isCustomerActive();

	@Override
	public Short getEntityID() {
		return EntityMasterConstants.Customer;
	}

	public void generatePortfolioAtRisk() throws PersistenceException,
			ServiceException {
	}

	public Money getBalanceForAccountsAtRisk() {
		Money amount = new Money();
		for (AccountBO account : getAccounts()) {
			if (account.getAccountType().getAccountTypeId().equals(
					AccountTypes.LOANACCOUNT.getValue())
					&& ((LoanBO) account).isAccountActive()) {
				LoanBO loan = (LoanBO) account;
				if (loan.hasPortfolioAtRisk())
					amount = amount.add(loan.getRemainingPrincipalAmount());
			}
		}
		return amount;
	}

	public Money getOutstandingLoanAmount() {
		Money amount = new Money();
		for (AccountBO account : getAccounts()) {
			if (account.getAccountType().getAccountTypeId().equals(
					AccountTypes.LOANACCOUNT.getValue())
					&& ((LoanBO) account).isAccountActive()) {
				amount = amount.add(((LoanBO) account)
						.getRemainingPrincipalAmount());
			}
		}
		return amount;
	}

	public Integer getActiveLoanCounts() {
		Integer countOfActiveLoans = 0;
		for (AccountBO account : getAccounts()) {
			if (account.getAccountType().getAccountTypeId().equals(
					AccountTypes.LOANACCOUNT.getValue())
					&& ((LoanBO) account).isAccountActive()) {
				countOfActiveLoans++;
			}
		}
		return countOfActiveLoans;
	}

	public Money getDelinquentPortfolioAmount() {
		Money amountOverDue = new Money();
		Money totalOutStandingAmount = new Money();
		for (AccountBO accountBO : getAccounts()) {
			if (accountBO.getAccountType().getAccountTypeId().equals(
					AccountTypes.LOANACCOUNT.getValue())
					&& ((LoanBO) accountBO).isAccountActive()) {
				amountOverDue = amountOverDue.add(((LoanBO) accountBO)
						.getTotalPrincipalAmountInArrears());
				totalOutStandingAmount = totalOutStandingAmount
						.add(((LoanBO) accountBO).getLoanSummary()
								.getOriginalPrincipal());
			}
		}
		if (totalOutStandingAmount.getAmountDoubleValue() != 0.0)
			return new Money(String.valueOf(amountOverDue
					.getAmountDoubleValue()
					/ totalOutStandingAmount.getAmountDoubleValue()));
		return new Money();
	}

	public CustomerPerformanceHistory getCustomerPerformanceHistory() {
		return getPerformanceHistory();
	}

	protected CustomerPerformanceHistory getPerformanceHistory() {
		return null;
	}

	public Money getSavingsBalance() {
		Money amount = new Money();
		for (AccountBO account : getAccounts()) {
			if (account.getAccountType().getAccountTypeId().equals(
					AccountTypes.SAVINGSACCOUNT.getValue())) {
				SavingsBO savingsBO = (SavingsBO) account;
				amount = amount.add(savingsBO.getSavingsBalance());
			}
		}
		return amount;
	}

	public void setCustomerActivationDate(Date customerActivationDate) {
		this.customerActivationDate = customerActivationDate;
	}

	public List<CustomerStatusEntity> getStatusList() {
		return null;
	}
	


	private void validateFields(String displayName,
			CustomerStatus customerStatus, Short officeId)
			throws CustomerException {
		if (StringUtils.isNullOrEmpty(displayName))
			throw new CustomerException(CustomerConstants.INVALID_NAME);
		if (customerStatus == null)
			throw new CustomerException(CustomerConstants.INVALID_STATUS);
		if (officeId == null)
			throw new CustomerException(CustomerConstants.INVALID_OFFICE);
	}

	protected void validateMeeting(MeetingBO meeting) throws CustomerException {
		if (meeting == null)
			throw new CustomerException(CustomerConstants.INVALID_MEETING);
	}

	protected void validateLO(Short loanOfficerId) throws CustomerException {
		if (loanOfficerId == null)
			throw new CustomerException(CustomerConstants.INVALID_LOAN_OFFICER);
	}

	private CustomerAccountBO createCustomerAccount(List<FeeView> fees)
			throws CustomerException {
		try {
			return new CustomerAccountBO(userContext, this, fees);
		} catch (AccountException ae) {
			throw new CustomerException(ae);
		}
	}

	private CustomerMeetingEntity createCustomerMeeting(MeetingBO meeting) {
		return meeting != null ? new CustomerMeetingEntity(this, meeting)
				: null;
	}

	public String getStatusName(Short localeId, Short statusId) throws ApplicationException, SystemException {
		return null;
	}

	public String getFlagName(Short flagId) throws ApplicationException, SystemException {
		return null;
	}
	
	public void changeStatus(Short newStatusId, Short flagId, String comment) throws SecurityException, ServiceException, PersistenceException, ApplicationException , SystemException{
		validateStatusChange(newStatusId);
		if (null != getPersonnel().getPersonnelId())
			checkPermissionForStatusChange(newStatusId, this.getUserContext(),
					flagId, getOffice().getOfficeId(), getPersonnel().getPersonnelId());
		else
			checkPermissionForStatusChange(newStatusId, this.getUserContext(),
					flagId, getOffice().getOfficeId(), this.getUserContext()
							.getId());
		MasterPersistenceService masterPersistenceService = (MasterPersistenceService) ServiceFactory
				.getInstance().getPersistenceService(
						PersistenceServiceName.MasterDataService);
		CustomerStatusEntity customerStatus = (CustomerStatusEntity) masterPersistenceService
				.findById(CustomerStatusEntity.class, newStatusId);
		customerStatus.setLocaleId(this.getUserContext().getLocaleId());
		CustomerStatusFlagEntity customerStatusFlagEntity = null;
		if (flagId != null) {
			customerStatusFlagEntity = (CustomerStatusFlagEntity) masterPersistenceService
					.findById(AccountStateFlagEntity.class, flagId);
		}
		CustomerNoteEntity customerNote = createCustomerNotes(comment);
		this.setCustomerStatus(customerStatus);
		this.addCustomerNotes(customerNote);
		if (customerStatusFlagEntity != null) {
			customerStatusFlagEntity.setLocaleId(this.getUserContext()
					.getLocaleId());
			CustomerFlagDetailEntity customerFlag = new CustomerFlagDetailEntity(this,customerStatusFlagEntity);
			customerFlag.setCreatedBy(this.getUserContext().getId());
			customerFlag.setCreatedDate(new Date());
			this.addCustomerFlag(customerFlag);
		}
	}
	
	protected abstract void validateStatusChange(Short newStatusId) throws ApplicationException, SystemException;
		
	public void addCustomerNotes(CustomerNoteEntity customerNote) {
		this.customerNotes.add(customerNote);
	
}

	private void checkPermissionForStatusChange(Short newState,
			UserContext userContext, Short flagSelected, Short recordOfficeId,
			Short recordLoanOfficerId) throws SecurityException {
		if (!isPermissionAllowed(newState, userContext, flagSelected,
				recordOfficeId, recordLoanOfficerId))
			throw new SecurityException(
					SecurityConstants.KEY_ACTIVITY_NOT_ALLOWED);
	}

	private boolean isPermissionAllowed(Short newState,
			UserContext userContext, Short flagSelected, Short recordOfficeId,
			Short recordLoanOfficerId) {
		return ActivityMapper.getInstance().isStateChangePermittedForAccount(
				newState.shortValue(),
				null != flagSelected ? flagSelected.shortValue() : 0,
				userContext, recordOfficeId, recordLoanOfficerId);
	}
	
	private CustomerNoteEntity createCustomerNotes(String comment)throws ServiceException {
		CustomerNoteEntity customerNote = new CustomerNoteEntity(comment,new java.sql.Date(System
				.currentTimeMillis()),this.getPersonnel(),this);
		return customerNote;
	}
			
}
