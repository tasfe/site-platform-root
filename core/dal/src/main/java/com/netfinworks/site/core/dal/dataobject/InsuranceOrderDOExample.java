package com.netfinworks.site.core.dal.dataobject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InsuranceOrderDOExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public InsuranceOrderDOExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("ID is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("ID is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("ID =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("ID <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("ID >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("ID >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("ID <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("ID <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("ID in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("ID not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("ID between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("ID not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andMemberIdIsNull() {
            addCriterion("MEMBER_ID is null");
            return (Criteria) this;
        }

        public Criteria andMemberIdIsNotNull() {
            addCriterion("MEMBER_ID is not null");
            return (Criteria) this;
        }

        public Criteria andMemberIdEqualTo(String value) {
            addCriterion("MEMBER_ID =", value, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdNotEqualTo(String value) {
            addCriterion("MEMBER_ID <>", value, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdGreaterThan(String value) {
            addCriterion("MEMBER_ID >", value, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdGreaterThanOrEqualTo(String value) {
            addCriterion("MEMBER_ID >=", value, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdLessThan(String value) {
            addCriterion("MEMBER_ID <", value, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdLessThanOrEqualTo(String value) {
            addCriterion("MEMBER_ID <=", value, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdLike(String value) {
            addCriterion("MEMBER_ID like", value, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdNotLike(String value) {
            addCriterion("MEMBER_ID not like", value, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdIn(List<String> values) {
            addCriterion("MEMBER_ID in", values, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdNotIn(List<String> values) {
            addCriterion("MEMBER_ID not in", values, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdBetween(String value1, String value2) {
            addCriterion("MEMBER_ID between", value1, value2, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdNotBetween(String value1, String value2) {
            addCriterion("MEMBER_ID not between", value1, value2, "memberId");
            return (Criteria) this;
        }

        public Criteria andBxgsidIsNull() {
            addCriterion("BXGSID is null");
            return (Criteria) this;
        }

        public Criteria andBxgsidIsNotNull() {
            addCriterion("BXGSID is not null");
            return (Criteria) this;
        }

        public Criteria andBxgsidEqualTo(String value) {
            addCriterion("BXGSID =", value, "bxgsid");
            return (Criteria) this;
        }

        public Criteria andBxgsidNotEqualTo(String value) {
            addCriterion("BXGSID <>", value, "bxgsid");
            return (Criteria) this;
        }

        public Criteria andBxgsidGreaterThan(String value) {
            addCriterion("BXGSID >", value, "bxgsid");
            return (Criteria) this;
        }

        public Criteria andBxgsidGreaterThanOrEqualTo(String value) {
            addCriterion("BXGSID >=", value, "bxgsid");
            return (Criteria) this;
        }

        public Criteria andBxgsidLessThan(String value) {
            addCriterion("BXGSID <", value, "bxgsid");
            return (Criteria) this;
        }

        public Criteria andBxgsidLessThanOrEqualTo(String value) {
            addCriterion("BXGSID <=", value, "bxgsid");
            return (Criteria) this;
        }

        public Criteria andBxgsidLike(String value) {
            addCriterion("BXGSID like", value, "bxgsid");
            return (Criteria) this;
        }

        public Criteria andBxgsidNotLike(String value) {
            addCriterion("BXGSID not like", value, "bxgsid");
            return (Criteria) this;
        }

        public Criteria andBxgsidIn(List<String> values) {
            addCriterion("BXGSID in", values, "bxgsid");
            return (Criteria) this;
        }

        public Criteria andBxgsidNotIn(List<String> values) {
            addCriterion("BXGSID not in", values, "bxgsid");
            return (Criteria) this;
        }

        public Criteria andBxgsidBetween(String value1, String value2) {
            addCriterion("BXGSID between", value1, value2, "bxgsid");
            return (Criteria) this;
        }

        public Criteria andBxgsidNotBetween(String value1, String value2) {
            addCriterion("BXGSID not between", value1, value2, "bxgsid");
            return (Criteria) this;
        }

        public Criteria andCompanyIsNull() {
            addCriterion("COMPANY is null");
            return (Criteria) this;
        }

        public Criteria andCompanyIsNotNull() {
            addCriterion("COMPANY is not null");
            return (Criteria) this;
        }

        public Criteria andCompanyEqualTo(String value) {
            addCriterion("COMPANY =", value, "company");
            return (Criteria) this;
        }

        public Criteria andCompanyNotEqualTo(String value) {
            addCriterion("COMPANY <>", value, "company");
            return (Criteria) this;
        }

        public Criteria andCompanyGreaterThan(String value) {
            addCriterion("COMPANY >", value, "company");
            return (Criteria) this;
        }

        public Criteria andCompanyGreaterThanOrEqualTo(String value) {
            addCriterion("COMPANY >=", value, "company");
            return (Criteria) this;
        }

        public Criteria andCompanyLessThan(String value) {
            addCriterion("COMPANY <", value, "company");
            return (Criteria) this;
        }

        public Criteria andCompanyLessThanOrEqualTo(String value) {
            addCriterion("COMPANY <=", value, "company");
            return (Criteria) this;
        }

        public Criteria andCompanyLike(String value) {
            addCriterion("COMPANY like", value, "company");
            return (Criteria) this;
        }

        public Criteria andCompanyNotLike(String value) {
            addCriterion("COMPANY not like", value, "company");
            return (Criteria) this;
        }

        public Criteria andCompanyIn(List<String> values) {
            addCriterion("COMPANY in", values, "company");
            return (Criteria) this;
        }

        public Criteria andCompanyNotIn(List<String> values) {
            addCriterion("COMPANY not in", values, "company");
            return (Criteria) this;
        }

        public Criteria andCompanyBetween(String value1, String value2) {
            addCriterion("COMPANY between", value1, value2, "company");
            return (Criteria) this;
        }

        public Criteria andCompanyNotBetween(String value1, String value2) {
            addCriterion("COMPANY not between", value1, value2, "company");
            return (Criteria) this;
        }

        public Criteria andNameIsNull() {
            addCriterion("NAME is null");
            return (Criteria) this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("NAME is not null");
            return (Criteria) this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("NAME =", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("NAME <>", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("NAME >", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("NAME >=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("NAME <", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("NAME <=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("NAME like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("NAME not like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("NAME in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("NAME not in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("NAME between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("NAME not between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andPolicytypeIsNull() {
            addCriterion("POLICYTYPE is null");
            return (Criteria) this;
        }

        public Criteria andPolicytypeIsNotNull() {
            addCriterion("POLICYTYPE is not null");
            return (Criteria) this;
        }

        public Criteria andPolicytypeEqualTo(String value) {
            addCriterion("POLICYTYPE =", value, "policytype");
            return (Criteria) this;
        }

        public Criteria andPolicytypeNotEqualTo(String value) {
            addCriterion("POLICYTYPE <>", value, "policytype");
            return (Criteria) this;
        }

        public Criteria andPolicytypeGreaterThan(String value) {
            addCriterion("POLICYTYPE >", value, "policytype");
            return (Criteria) this;
        }

        public Criteria andPolicytypeGreaterThanOrEqualTo(String value) {
            addCriterion("POLICYTYPE >=", value, "policytype");
            return (Criteria) this;
        }

        public Criteria andPolicytypeLessThan(String value) {
            addCriterion("POLICYTYPE <", value, "policytype");
            return (Criteria) this;
        }

        public Criteria andPolicytypeLessThanOrEqualTo(String value) {
            addCriterion("POLICYTYPE <=", value, "policytype");
            return (Criteria) this;
        }

        public Criteria andPolicytypeLike(String value) {
            addCriterion("POLICYTYPE like", value, "policytype");
            return (Criteria) this;
        }

        public Criteria andPolicytypeNotLike(String value) {
            addCriterion("POLICYTYPE not like", value, "policytype");
            return (Criteria) this;
        }

        public Criteria andPolicytypeIn(List<String> values) {
            addCriterion("POLICYTYPE in", values, "policytype");
            return (Criteria) this;
        }

        public Criteria andPolicytypeNotIn(List<String> values) {
            addCriterion("POLICYTYPE not in", values, "policytype");
            return (Criteria) this;
        }

        public Criteria andPolicytypeBetween(String value1, String value2) {
            addCriterion("POLICYTYPE between", value1, value2, "policytype");
            return (Criteria) this;
        }

        public Criteria andPolicytypeNotBetween(String value1, String value2) {
            addCriterion("POLICYTYPE not between", value1, value2, "policytype");
            return (Criteria) this;
        }

        public Criteria andSecuritymoneyIsNull() {
            addCriterion("SECURITYMONEY is null");
            return (Criteria) this;
        }

        public Criteria andSecuritymoneyIsNotNull() {
            addCriterion("SECURITYMONEY is not null");
            return (Criteria) this;
        }

        public Criteria andSecuritymoneyEqualTo(String value) {
            addCriterion("SECURITYMONEY =", value, "securitymoney");
            return (Criteria) this;
        }

        public Criteria andSecuritymoneyNotEqualTo(String value) {
            addCriterion("SECURITYMONEY <>", value, "securitymoney");
            return (Criteria) this;
        }

        public Criteria andSecuritymoneyGreaterThan(String value) {
            addCriterion("SECURITYMONEY >", value, "securitymoney");
            return (Criteria) this;
        }

        public Criteria andSecuritymoneyGreaterThanOrEqualTo(String value) {
            addCriterion("SECURITYMONEY >=", value, "securitymoney");
            return (Criteria) this;
        }

        public Criteria andSecuritymoneyLessThan(String value) {
            addCriterion("SECURITYMONEY <", value, "securitymoney");
            return (Criteria) this;
        }

        public Criteria andSecuritymoneyLessThanOrEqualTo(String value) {
            addCriterion("SECURITYMONEY <=", value, "securitymoney");
            return (Criteria) this;
        }

        public Criteria andSecuritymoneyLike(String value) {
            addCriterion("SECURITYMONEY like", value, "securitymoney");
            return (Criteria) this;
        }

        public Criteria andSecuritymoneyNotLike(String value) {
            addCriterion("SECURITYMONEY not like", value, "securitymoney");
            return (Criteria) this;
        }

        public Criteria andSecuritymoneyIn(List<String> values) {
            addCriterion("SECURITYMONEY in", values, "securitymoney");
            return (Criteria) this;
        }

        public Criteria andSecuritymoneyNotIn(List<String> values) {
            addCriterion("SECURITYMONEY not in", values, "securitymoney");
            return (Criteria) this;
        }

        public Criteria andSecuritymoneyBetween(String value1, String value2) {
            addCriterion("SECURITYMONEY between", value1, value2, "securitymoney");
            return (Criteria) this;
        }

        public Criteria andSecuritymoneyNotBetween(String value1, String value2) {
            addCriterion("SECURITYMONEY not between", value1, value2, "securitymoney");
            return (Criteria) this;
        }

        public Criteria andCurrencytypeIsNull() {
            addCriterion("CURRENCYTYPE is null");
            return (Criteria) this;
        }

        public Criteria andCurrencytypeIsNotNull() {
            addCriterion("CURRENCYTYPE is not null");
            return (Criteria) this;
        }

        public Criteria andCurrencytypeEqualTo(String value) {
            addCriterion("CURRENCYTYPE =", value, "currencytype");
            return (Criteria) this;
        }

        public Criteria andCurrencytypeNotEqualTo(String value) {
            addCriterion("CURRENCYTYPE <>", value, "currencytype");
            return (Criteria) this;
        }

        public Criteria andCurrencytypeGreaterThan(String value) {
            addCriterion("CURRENCYTYPE >", value, "currencytype");
            return (Criteria) this;
        }

        public Criteria andCurrencytypeGreaterThanOrEqualTo(String value) {
            addCriterion("CURRENCYTYPE >=", value, "currencytype");
            return (Criteria) this;
        }

        public Criteria andCurrencytypeLessThan(String value) {
            addCriterion("CURRENCYTYPE <", value, "currencytype");
            return (Criteria) this;
        }

        public Criteria andCurrencytypeLessThanOrEqualTo(String value) {
            addCriterion("CURRENCYTYPE <=", value, "currencytype");
            return (Criteria) this;
        }

        public Criteria andCurrencytypeLike(String value) {
            addCriterion("CURRENCYTYPE like", value, "currencytype");
            return (Criteria) this;
        }

        public Criteria andCurrencytypeNotLike(String value) {
            addCriterion("CURRENCYTYPE not like", value, "currencytype");
            return (Criteria) this;
        }

        public Criteria andCurrencytypeIn(List<String> values) {
            addCriterion("CURRENCYTYPE in", values, "currencytype");
            return (Criteria) this;
        }

        public Criteria andCurrencytypeNotIn(List<String> values) {
            addCriterion("CURRENCYTYPE not in", values, "currencytype");
            return (Criteria) this;
        }

        public Criteria andCurrencytypeBetween(String value1, String value2) {
            addCriterion("CURRENCYTYPE between", value1, value2, "currencytype");
            return (Criteria) this;
        }

        public Criteria andCurrencytypeNotBetween(String value1, String value2) {
            addCriterion("CURRENCYTYPE not between", value1, value2, "currencytype");
            return (Criteria) this;
        }

        public Criteria andApplicantIsNull() {
            addCriterion("APPLICANT is null");
            return (Criteria) this;
        }

        public Criteria andApplicantIsNotNull() {
            addCriterion("APPLICANT is not null");
            return (Criteria) this;
        }

        public Criteria andApplicantEqualTo(String value) {
            addCriterion("APPLICANT =", value, "applicant");
            return (Criteria) this;
        }

        public Criteria andApplicantNotEqualTo(String value) {
            addCriterion("APPLICANT <>", value, "applicant");
            return (Criteria) this;
        }

        public Criteria andApplicantGreaterThan(String value) {
            addCriterion("APPLICANT >", value, "applicant");
            return (Criteria) this;
        }

        public Criteria andApplicantGreaterThanOrEqualTo(String value) {
            addCriterion("APPLICANT >=", value, "applicant");
            return (Criteria) this;
        }

        public Criteria andApplicantLessThan(String value) {
            addCriterion("APPLICANT <", value, "applicant");
            return (Criteria) this;
        }

        public Criteria andApplicantLessThanOrEqualTo(String value) {
            addCriterion("APPLICANT <=", value, "applicant");
            return (Criteria) this;
        }

        public Criteria andApplicantLike(String value) {
            addCriterion("APPLICANT like", value, "applicant");
            return (Criteria) this;
        }

        public Criteria andApplicantNotLike(String value) {
            addCriterion("APPLICANT not like", value, "applicant");
            return (Criteria) this;
        }

        public Criteria andApplicantIn(List<String> values) {
            addCriterion("APPLICANT in", values, "applicant");
            return (Criteria) this;
        }

        public Criteria andApplicantNotIn(List<String> values) {
            addCriterion("APPLICANT not in", values, "applicant");
            return (Criteria) this;
        }

        public Criteria andApplicantBetween(String value1, String value2) {
            addCriterion("APPLICANT between", value1, value2, "applicant");
            return (Criteria) this;
        }

        public Criteria andApplicantNotBetween(String value1, String value2) {
            addCriterion("APPLICANT not between", value1, value2, "applicant");
            return (Criteria) this;
        }

        public Criteria andAtypeIsNull() {
            addCriterion("ATYPE is null");
            return (Criteria) this;
        }

        public Criteria andAtypeIsNotNull() {
            addCriterion("ATYPE is not null");
            return (Criteria) this;
        }

        public Criteria andAtypeEqualTo(String value) {
            addCriterion("ATYPE =", value, "atype");
            return (Criteria) this;
        }

        public Criteria andAtypeNotEqualTo(String value) {
            addCriterion("ATYPE <>", value, "atype");
            return (Criteria) this;
        }

        public Criteria andAtypeGreaterThan(String value) {
            addCriterion("ATYPE >", value, "atype");
            return (Criteria) this;
        }

        public Criteria andAtypeGreaterThanOrEqualTo(String value) {
            addCriterion("ATYPE >=", value, "atype");
            return (Criteria) this;
        }

        public Criteria andAtypeLessThan(String value) {
            addCriterion("ATYPE <", value, "atype");
            return (Criteria) this;
        }

        public Criteria andAtypeLessThanOrEqualTo(String value) {
            addCriterion("ATYPE <=", value, "atype");
            return (Criteria) this;
        }

        public Criteria andAtypeLike(String value) {
            addCriterion("ATYPE like", value, "atype");
            return (Criteria) this;
        }

        public Criteria andAtypeNotLike(String value) {
            addCriterion("ATYPE not like", value, "atype");
            return (Criteria) this;
        }

        public Criteria andAtypeIn(List<String> values) {
            addCriterion("ATYPE in", values, "atype");
            return (Criteria) this;
        }

        public Criteria andAtypeNotIn(List<String> values) {
            addCriterion("ATYPE not in", values, "atype");
            return (Criteria) this;
        }

        public Criteria andAtypeBetween(String value1, String value2) {
            addCriterion("ATYPE between", value1, value2, "atype");
            return (Criteria) this;
        }

        public Criteria andAtypeNotBetween(String value1, String value2) {
            addCriterion("ATYPE not between", value1, value2, "atype");
            return (Criteria) this;
        }

        public Criteria andAidcardIsNull() {
            addCriterion("AIDCARD is null");
            return (Criteria) this;
        }

        public Criteria andAidcardIsNotNull() {
            addCriterion("AIDCARD is not null");
            return (Criteria) this;
        }

        public Criteria andAidcardEqualTo(String value) {
            addCriterion("AIDCARD =", value, "aidcard");
            return (Criteria) this;
        }

        public Criteria andAidcardNotEqualTo(String value) {
            addCriterion("AIDCARD <>", value, "aidcard");
            return (Criteria) this;
        }

        public Criteria andAidcardGreaterThan(String value) {
            addCriterion("AIDCARD >", value, "aidcard");
            return (Criteria) this;
        }

        public Criteria andAidcardGreaterThanOrEqualTo(String value) {
            addCriterion("AIDCARD >=", value, "aidcard");
            return (Criteria) this;
        }

        public Criteria andAidcardLessThan(String value) {
            addCriterion("AIDCARD <", value, "aidcard");
            return (Criteria) this;
        }

        public Criteria andAidcardLessThanOrEqualTo(String value) {
            addCriterion("AIDCARD <=", value, "aidcard");
            return (Criteria) this;
        }

        public Criteria andAidcardLike(String value) {
            addCriterion("AIDCARD like", value, "aidcard");
            return (Criteria) this;
        }

        public Criteria andAidcardNotLike(String value) {
            addCriterion("AIDCARD not like", value, "aidcard");
            return (Criteria) this;
        }

        public Criteria andAidcardIn(List<String> values) {
            addCriterion("AIDCARD in", values, "aidcard");
            return (Criteria) this;
        }

        public Criteria andAidcardNotIn(List<String> values) {
            addCriterion("AIDCARD not in", values, "aidcard");
            return (Criteria) this;
        }

        public Criteria andAidcardBetween(String value1, String value2) {
            addCriterion("AIDCARD between", value1, value2, "aidcard");
            return (Criteria) this;
        }

        public Criteria andAidcardNotBetween(String value1, String value2) {
            addCriterion("AIDCARD not between", value1, value2, "aidcard");
            return (Criteria) this;
        }

        public Criteria andAphoneIsNull() {
            addCriterion("APHONE is null");
            return (Criteria) this;
        }

        public Criteria andAphoneIsNotNull() {
            addCriterion("APHONE is not null");
            return (Criteria) this;
        }

        public Criteria andAphoneEqualTo(String value) {
            addCriterion("APHONE =", value, "aphone");
            return (Criteria) this;
        }

        public Criteria andAphoneNotEqualTo(String value) {
            addCriterion("APHONE <>", value, "aphone");
            return (Criteria) this;
        }

        public Criteria andAphoneGreaterThan(String value) {
            addCriterion("APHONE >", value, "aphone");
            return (Criteria) this;
        }

        public Criteria andAphoneGreaterThanOrEqualTo(String value) {
            addCriterion("APHONE >=", value, "aphone");
            return (Criteria) this;
        }

        public Criteria andAphoneLessThan(String value) {
            addCriterion("APHONE <", value, "aphone");
            return (Criteria) this;
        }

        public Criteria andAphoneLessThanOrEqualTo(String value) {
            addCriterion("APHONE <=", value, "aphone");
            return (Criteria) this;
        }

        public Criteria andAphoneLike(String value) {
            addCriterion("APHONE like", value, "aphone");
            return (Criteria) this;
        }

        public Criteria andAphoneNotLike(String value) {
            addCriterion("APHONE not like", value, "aphone");
            return (Criteria) this;
        }

        public Criteria andAphoneIn(List<String> values) {
            addCriterion("APHONE in", values, "aphone");
            return (Criteria) this;
        }

        public Criteria andAphoneNotIn(List<String> values) {
            addCriterion("APHONE not in", values, "aphone");
            return (Criteria) this;
        }

        public Criteria andAphoneBetween(String value1, String value2) {
            addCriterion("APHONE between", value1, value2, "aphone");
            return (Criteria) this;
        }

        public Criteria andAphoneNotBetween(String value1, String value2) {
            addCriterion("APHONE not between", value1, value2, "aphone");
            return (Criteria) this;
        }

        public Criteria andBinsuredIsNull() {
            addCriterion("BINSURED is null");
            return (Criteria) this;
        }

        public Criteria andBinsuredIsNotNull() {
            addCriterion("BINSURED is not null");
            return (Criteria) this;
        }

        public Criteria andBinsuredEqualTo(String value) {
            addCriterion("BINSURED =", value, "binsured");
            return (Criteria) this;
        }

        public Criteria andBinsuredNotEqualTo(String value) {
            addCriterion("BINSURED <>", value, "binsured");
            return (Criteria) this;
        }

        public Criteria andBinsuredGreaterThan(String value) {
            addCriterion("BINSURED >", value, "binsured");
            return (Criteria) this;
        }

        public Criteria andBinsuredGreaterThanOrEqualTo(String value) {
            addCriterion("BINSURED >=", value, "binsured");
            return (Criteria) this;
        }

        public Criteria andBinsuredLessThan(String value) {
            addCriterion("BINSURED <", value, "binsured");
            return (Criteria) this;
        }

        public Criteria andBinsuredLessThanOrEqualTo(String value) {
            addCriterion("BINSURED <=", value, "binsured");
            return (Criteria) this;
        }

        public Criteria andBinsuredLike(String value) {
            addCriterion("BINSURED like", value, "binsured");
            return (Criteria) this;
        }

        public Criteria andBinsuredNotLike(String value) {
            addCriterion("BINSURED not like", value, "binsured");
            return (Criteria) this;
        }

        public Criteria andBinsuredIn(List<String> values) {
            addCriterion("BINSURED in", values, "binsured");
            return (Criteria) this;
        }

        public Criteria andBinsuredNotIn(List<String> values) {
            addCriterion("BINSURED not in", values, "binsured");
            return (Criteria) this;
        }

        public Criteria andBinsuredBetween(String value1, String value2) {
            addCriterion("BINSURED between", value1, value2, "binsured");
            return (Criteria) this;
        }

        public Criteria andBinsuredNotBetween(String value1, String value2) {
            addCriterion("BINSURED not between", value1, value2, "binsured");
            return (Criteria) this;
        }

        public Criteria andBtypeIsNull() {
            addCriterion("BTYPE is null");
            return (Criteria) this;
        }

        public Criteria andBtypeIsNotNull() {
            addCriterion("BTYPE is not null");
            return (Criteria) this;
        }

        public Criteria andBtypeEqualTo(String value) {
            addCriterion("BTYPE =", value, "btype");
            return (Criteria) this;
        }

        public Criteria andBtypeNotEqualTo(String value) {
            addCriterion("BTYPE <>", value, "btype");
            return (Criteria) this;
        }

        public Criteria andBtypeGreaterThan(String value) {
            addCriterion("BTYPE >", value, "btype");
            return (Criteria) this;
        }

        public Criteria andBtypeGreaterThanOrEqualTo(String value) {
            addCriterion("BTYPE >=", value, "btype");
            return (Criteria) this;
        }

        public Criteria andBtypeLessThan(String value) {
            addCriterion("BTYPE <", value, "btype");
            return (Criteria) this;
        }

        public Criteria andBtypeLessThanOrEqualTo(String value) {
            addCriterion("BTYPE <=", value, "btype");
            return (Criteria) this;
        }

        public Criteria andBtypeLike(String value) {
            addCriterion("BTYPE like", value, "btype");
            return (Criteria) this;
        }

        public Criteria andBtypeNotLike(String value) {
            addCriterion("BTYPE not like", value, "btype");
            return (Criteria) this;
        }

        public Criteria andBtypeIn(List<String> values) {
            addCriterion("BTYPE in", values, "btype");
            return (Criteria) this;
        }

        public Criteria andBtypeNotIn(List<String> values) {
            addCriterion("BTYPE not in", values, "btype");
            return (Criteria) this;
        }

        public Criteria andBtypeBetween(String value1, String value2) {
            addCriterion("BTYPE between", value1, value2, "btype");
            return (Criteria) this;
        }

        public Criteria andBtypeNotBetween(String value1, String value2) {
            addCriterion("BTYPE not between", value1, value2, "btype");
            return (Criteria) this;
        }

        public Criteria andBidcardIsNull() {
            addCriterion("BIDCARD is null");
            return (Criteria) this;
        }

        public Criteria andBidcardIsNotNull() {
            addCriterion("BIDCARD is not null");
            return (Criteria) this;
        }

        public Criteria andBidcardEqualTo(String value) {
            addCriterion("BIDCARD =", value, "bidcard");
            return (Criteria) this;
        }

        public Criteria andBidcardNotEqualTo(String value) {
            addCriterion("BIDCARD <>", value, "bidcard");
            return (Criteria) this;
        }

        public Criteria andBidcardGreaterThan(String value) {
            addCriterion("BIDCARD >", value, "bidcard");
            return (Criteria) this;
        }

        public Criteria andBidcardGreaterThanOrEqualTo(String value) {
            addCriterion("BIDCARD >=", value, "bidcard");
            return (Criteria) this;
        }

        public Criteria andBidcardLessThan(String value) {
            addCriterion("BIDCARD <", value, "bidcard");
            return (Criteria) this;
        }

        public Criteria andBidcardLessThanOrEqualTo(String value) {
            addCriterion("BIDCARD <=", value, "bidcard");
            return (Criteria) this;
        }

        public Criteria andBidcardLike(String value) {
            addCriterion("BIDCARD like", value, "bidcard");
            return (Criteria) this;
        }

        public Criteria andBidcardNotLike(String value) {
            addCriterion("BIDCARD not like", value, "bidcard");
            return (Criteria) this;
        }

        public Criteria andBidcardIn(List<String> values) {
            addCriterion("BIDCARD in", values, "bidcard");
            return (Criteria) this;
        }

        public Criteria andBidcardNotIn(List<String> values) {
            addCriterion("BIDCARD not in", values, "bidcard");
            return (Criteria) this;
        }

        public Criteria andBidcardBetween(String value1, String value2) {
            addCriterion("BIDCARD between", value1, value2, "bidcard");
            return (Criteria) this;
        }

        public Criteria andBidcardNotBetween(String value1, String value2) {
            addCriterion("BIDCARD not between", value1, value2, "bidcard");
            return (Criteria) this;
        }

        public Criteria andBphoneIsNull() {
            addCriterion("BPHONE is null");
            return (Criteria) this;
        }

        public Criteria andBphoneIsNotNull() {
            addCriterion("BPHONE is not null");
            return (Criteria) this;
        }

        public Criteria andBphoneEqualTo(String value) {
            addCriterion("BPHONE =", value, "bphone");
            return (Criteria) this;
        }

        public Criteria andBphoneNotEqualTo(String value) {
            addCriterion("BPHONE <>", value, "bphone");
            return (Criteria) this;
        }

        public Criteria andBphoneGreaterThan(String value) {
            addCriterion("BPHONE >", value, "bphone");
            return (Criteria) this;
        }

        public Criteria andBphoneGreaterThanOrEqualTo(String value) {
            addCriterion("BPHONE >=", value, "bphone");
            return (Criteria) this;
        }

        public Criteria andBphoneLessThan(String value) {
            addCriterion("BPHONE <", value, "bphone");
            return (Criteria) this;
        }

        public Criteria andBphoneLessThanOrEqualTo(String value) {
            addCriterion("BPHONE <=", value, "bphone");
            return (Criteria) this;
        }

        public Criteria andBphoneLike(String value) {
            addCriterion("BPHONE like", value, "bphone");
            return (Criteria) this;
        }

        public Criteria andBphoneNotLike(String value) {
            addCriterion("BPHONE not like", value, "bphone");
            return (Criteria) this;
        }

        public Criteria andBphoneIn(List<String> values) {
            addCriterion("BPHONE in", values, "bphone");
            return (Criteria) this;
        }

        public Criteria andBphoneNotIn(List<String> values) {
            addCriterion("BPHONE not in", values, "bphone");
            return (Criteria) this;
        }

        public Criteria andBphoneBetween(String value1, String value2) {
            addCriterion("BPHONE between", value1, value2, "bphone");
            return (Criteria) this;
        }

        public Criteria andBphoneNotBetween(String value1, String value2) {
            addCriterion("BPHONE not between", value1, value2, "bphone");
            return (Criteria) this;
        }

        public Criteria andStartdateIsNull() {
            addCriterion("STARTDATE is null");
            return (Criteria) this;
        }

        public Criteria andStartdateIsNotNull() {
            addCriterion("STARTDATE is not null");
            return (Criteria) this;
        }

        public Criteria andStartdateEqualTo(Date value) {
            addCriterion("STARTDATE =", value, "startdate");
            return (Criteria) this;
        }

        public Criteria andStartdateNotEqualTo(Date value) {
            addCriterion("STARTDATE <>", value, "startdate");
            return (Criteria) this;
        }

        public Criteria andStartdateGreaterThan(Date value) {
            addCriterion("STARTDATE >", value, "startdate");
            return (Criteria) this;
        }

        public Criteria andStartdateGreaterThanOrEqualTo(Date value) {
            addCriterion("STARTDATE >=", value, "startdate");
            return (Criteria) this;
        }

        public Criteria andStartdateLessThan(Date value) {
            addCriterion("STARTDATE <", value, "startdate");
            return (Criteria) this;
        }

        public Criteria andStartdateLessThanOrEqualTo(Date value) {
            addCriterion("STARTDATE <=", value, "startdate");
            return (Criteria) this;
        }

        public Criteria andStartdateIn(List<Date> values) {
            addCriterion("STARTDATE in", values, "startdate");
            return (Criteria) this;
        }

        public Criteria andStartdateNotIn(List<Date> values) {
            addCriterion("STARTDATE not in", values, "startdate");
            return (Criteria) this;
        }

        public Criteria andStartdateBetween(Date value1, Date value2) {
            addCriterion("STARTDATE between", value1, value2, "startdate");
            return (Criteria) this;
        }

        public Criteria andStartdateNotBetween(Date value1, Date value2) {
            addCriterion("STARTDATE not between", value1, value2, "startdate");
            return (Criteria) this;
        }

        public Criteria andEnddateIsNull() {
            addCriterion("ENDDATE is null");
            return (Criteria) this;
        }

        public Criteria andEnddateIsNotNull() {
            addCriterion("ENDDATE is not null");
            return (Criteria) this;
        }

        public Criteria andEnddateEqualTo(Date value) {
            addCriterion("ENDDATE =", value, "enddate");
            return (Criteria) this;
        }

        public Criteria andEnddateNotEqualTo(Date value) {
            addCriterion("ENDDATE <>", value, "enddate");
            return (Criteria) this;
        }

        public Criteria andEnddateGreaterThan(Date value) {
            addCriterion("ENDDATE >", value, "enddate");
            return (Criteria) this;
        }

        public Criteria andEnddateGreaterThanOrEqualTo(Date value) {
            addCriterion("ENDDATE >=", value, "enddate");
            return (Criteria) this;
        }

        public Criteria andEnddateLessThan(Date value) {
            addCriterion("ENDDATE <", value, "enddate");
            return (Criteria) this;
        }

        public Criteria andEnddateLessThanOrEqualTo(Date value) {
            addCriterion("ENDDATE <=", value, "enddate");
            return (Criteria) this;
        }

        public Criteria andEnddateIn(List<Date> values) {
            addCriterion("ENDDATE in", values, "enddate");
            return (Criteria) this;
        }

        public Criteria andEnddateNotIn(List<Date> values) {
            addCriterion("ENDDATE not in", values, "enddate");
            return (Criteria) this;
        }

        public Criteria andEnddateBetween(Date value1, Date value2) {
            addCriterion("ENDDATE between", value1, value2, "enddate");
            return (Criteria) this;
        }

        public Criteria andEnddateNotBetween(Date value1, Date value2) {
            addCriterion("ENDDATE not between", value1, value2, "enddate");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("STATUS is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("STATUS is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(String value) {
            addCriterion("STATUS =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(String value) {
            addCriterion("STATUS <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(String value) {
            addCriterion("STATUS >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(String value) {
            addCriterion("STATUS >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(String value) {
            addCriterion("STATUS <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(String value) {
            addCriterion("STATUS <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLike(String value) {
            addCriterion("STATUS like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotLike(String value) {
            addCriterion("STATUS not like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<String> values) {
            addCriterion("STATUS in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<String> values) {
            addCriterion("STATUS not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(String value1, String value2) {
            addCriterion("STATUS between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(String value1, String value2) {
            addCriterion("STATUS not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andSpeciltypeIsNull() {
            addCriterion("SPECILTYPE is null");
            return (Criteria) this;
        }

        public Criteria andSpeciltypeIsNotNull() {
            addCriterion("SPECILTYPE is not null");
            return (Criteria) this;
        }

        public Criteria andSpeciltypeEqualTo(String value) {
            addCriterion("SPECILTYPE =", value, "speciltype");
            return (Criteria) this;
        }

        public Criteria andSpeciltypeNotEqualTo(String value) {
            addCriterion("SPECILTYPE <>", value, "speciltype");
            return (Criteria) this;
        }

        public Criteria andSpeciltypeGreaterThan(String value) {
            addCriterion("SPECILTYPE >", value, "speciltype");
            return (Criteria) this;
        }

        public Criteria andSpeciltypeGreaterThanOrEqualTo(String value) {
            addCriterion("SPECILTYPE >=", value, "speciltype");
            return (Criteria) this;
        }

        public Criteria andSpeciltypeLessThan(String value) {
            addCriterion("SPECILTYPE <", value, "speciltype");
            return (Criteria) this;
        }

        public Criteria andSpeciltypeLessThanOrEqualTo(String value) {
            addCriterion("SPECILTYPE <=", value, "speciltype");
            return (Criteria) this;
        }

        public Criteria andSpeciltypeLike(String value) {
            addCriterion("SPECILTYPE like", value, "speciltype");
            return (Criteria) this;
        }

        public Criteria andSpeciltypeNotLike(String value) {
            addCriterion("SPECILTYPE not like", value, "speciltype");
            return (Criteria) this;
        }

        public Criteria andSpeciltypeIn(List<String> values) {
            addCriterion("SPECILTYPE in", values, "speciltype");
            return (Criteria) this;
        }

        public Criteria andSpeciltypeNotIn(List<String> values) {
            addCriterion("SPECILTYPE not in", values, "speciltype");
            return (Criteria) this;
        }

        public Criteria andSpeciltypeBetween(String value1, String value2) {
            addCriterion("SPECILTYPE between", value1, value2, "speciltype");
            return (Criteria) this;
        }

        public Criteria andSpeciltypeNotBetween(String value1, String value2) {
            addCriterion("SPECILTYPE not between", value1, value2, "speciltype");
            return (Criteria) this;
        }

        public Criteria andPidIsNull() {
            addCriterion("PID is null");
            return (Criteria) this;
        }

        public Criteria andPidIsNotNull() {
            addCriterion("PID is not null");
            return (Criteria) this;
        }

        public Criteria andPidEqualTo(String value) {
            addCriterion("PID =", value, "pid");
            return (Criteria) this;
        }

        public Criteria andPidNotEqualTo(String value) {
            addCriterion("PID <>", value, "pid");
            return (Criteria) this;
        }

        public Criteria andPidGreaterThan(String value) {
            addCriterion("PID >", value, "pid");
            return (Criteria) this;
        }

        public Criteria andPidGreaterThanOrEqualTo(String value) {
            addCriterion("PID >=", value, "pid");
            return (Criteria) this;
        }

        public Criteria andPidLessThan(String value) {
            addCriterion("PID <", value, "pid");
            return (Criteria) this;
        }

        public Criteria andPidLessThanOrEqualTo(String value) {
            addCriterion("PID <=", value, "pid");
            return (Criteria) this;
        }

        public Criteria andPidLike(String value) {
            addCriterion("PID like", value, "pid");
            return (Criteria) this;
        }

        public Criteria andPidNotLike(String value) {
            addCriterion("PID not like", value, "pid");
            return (Criteria) this;
        }

        public Criteria andPidIn(List<String> values) {
            addCriterion("PID in", values, "pid");
            return (Criteria) this;
        }

        public Criteria andPidNotIn(List<String> values) {
            addCriterion("PID not in", values, "pid");
            return (Criteria) this;
        }

        public Criteria andPidBetween(String value1, String value2) {
            addCriterion("PID between", value1, value2, "pid");
            return (Criteria) this;
        }

        public Criteria andPidNotBetween(String value1, String value2) {
            addCriterion("PID not between", value1, value2, "pid");
            return (Criteria) this;
        }

        public Criteria andLiabilitysIsNull() {
            addCriterion("LIABILITYS is null");
            return (Criteria) this;
        }

        public Criteria andLiabilitysIsNotNull() {
            addCriterion("LIABILITYS is not null");
            return (Criteria) this;
        }

        public Criteria andLiabilitysEqualTo(String value) {
            addCriterion("LIABILITYS =", value, "liabilitys");
            return (Criteria) this;
        }

        public Criteria andLiabilitysNotEqualTo(String value) {
            addCriterion("LIABILITYS <>", value, "liabilitys");
            return (Criteria) this;
        }

        public Criteria andLiabilitysGreaterThan(String value) {
            addCriterion("LIABILITYS >", value, "liabilitys");
            return (Criteria) this;
        }

        public Criteria andLiabilitysGreaterThanOrEqualTo(String value) {
            addCriterion("LIABILITYS >=", value, "liabilitys");
            return (Criteria) this;
        }

        public Criteria andLiabilitysLessThan(String value) {
            addCriterion("LIABILITYS <", value, "liabilitys");
            return (Criteria) this;
        }

        public Criteria andLiabilitysLessThanOrEqualTo(String value) {
            addCriterion("LIABILITYS <=", value, "liabilitys");
            return (Criteria) this;
        }

        public Criteria andLiabilitysLike(String value) {
            addCriterion("LIABILITYS like", value, "liabilitys");
            return (Criteria) this;
        }

        public Criteria andLiabilitysNotLike(String value) {
            addCriterion("LIABILITYS not like", value, "liabilitys");
            return (Criteria) this;
        }

        public Criteria andLiabilitysIn(List<String> values) {
            addCriterion("LIABILITYS in", values, "liabilitys");
            return (Criteria) this;
        }

        public Criteria andLiabilitysNotIn(List<String> values) {
            addCriterion("LIABILITYS not in", values, "liabilitys");
            return (Criteria) this;
        }

        public Criteria andLiabilitysBetween(String value1, String value2) {
            addCriterion("LIABILITYS between", value1, value2, "liabilitys");
            return (Criteria) this;
        }

        public Criteria andLiabilitysNotBetween(String value1, String value2) {
            addCriterion("LIABILITYS not between", value1, value2, "liabilitys");
            return (Criteria) this;
        }

        public Criteria andOtherinfoIsNull() {
            addCriterion("OTHERINFO is null");
            return (Criteria) this;
        }

        public Criteria andOtherinfoIsNotNull() {
            addCriterion("OTHERINFO is not null");
            return (Criteria) this;
        }

        public Criteria andOtherinfoEqualTo(String value) {
            addCriterion("OTHERINFO =", value, "otherinfo");
            return (Criteria) this;
        }

        public Criteria andOtherinfoNotEqualTo(String value) {
            addCriterion("OTHERINFO <>", value, "otherinfo");
            return (Criteria) this;
        }

        public Criteria andOtherinfoGreaterThan(String value) {
            addCriterion("OTHERINFO >", value, "otherinfo");
            return (Criteria) this;
        }

        public Criteria andOtherinfoGreaterThanOrEqualTo(String value) {
            addCriterion("OTHERINFO >=", value, "otherinfo");
            return (Criteria) this;
        }

        public Criteria andOtherinfoLessThan(String value) {
            addCriterion("OTHERINFO <", value, "otherinfo");
            return (Criteria) this;
        }

        public Criteria andOtherinfoLessThanOrEqualTo(String value) {
            addCriterion("OTHERINFO <=", value, "otherinfo");
            return (Criteria) this;
        }

        public Criteria andOtherinfoLike(String value) {
            addCriterion("OTHERINFO like", value, "otherinfo");
            return (Criteria) this;
        }

        public Criteria andOtherinfoNotLike(String value) {
            addCriterion("OTHERINFO not like", value, "otherinfo");
            return (Criteria) this;
        }

        public Criteria andOtherinfoIn(List<String> values) {
            addCriterion("OTHERINFO in", values, "otherinfo");
            return (Criteria) this;
        }

        public Criteria andOtherinfoNotIn(List<String> values) {
            addCriterion("OTHERINFO not in", values, "otherinfo");
            return (Criteria) this;
        }

        public Criteria andOtherinfoBetween(String value1, String value2) {
            addCriterion("OTHERINFO between", value1, value2, "otherinfo");
            return (Criteria) this;
        }

        public Criteria andOtherinfoNotBetween(String value1, String value2) {
            addCriterion("OTHERINFO not between", value1, value2, "otherinfo");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("CREATE_TIME is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("CREATE_TIME is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("CREATE_TIME =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("CREATE_TIME <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("CREATE_TIME >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("CREATE_TIME >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("CREATE_TIME <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("CREATE_TIME <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("CREATE_TIME in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("CREATE_TIME not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("CREATE_TIME between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("CREATE_TIME not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andMenuconIsNull() {
            addCriterion("MENUCON is null");
            return (Criteria) this;
        }

        public Criteria andMenuconIsNotNull() {
            addCriterion("MENUCON is not null");
            return (Criteria) this;
        }

        public Criteria andMenuconEqualTo(String value) {
            addCriterion("MENUCON =", value, "menucon");
            return (Criteria) this;
        }

        public Criteria andMenuconNotEqualTo(String value) {
            addCriterion("MENUCON <>", value, "menucon");
            return (Criteria) this;
        }

        public Criteria andMenuconGreaterThan(String value) {
            addCriterion("MENUCON >", value, "menucon");
            return (Criteria) this;
        }

        public Criteria andMenuconGreaterThanOrEqualTo(String value) {
            addCriterion("MENUCON >=", value, "menucon");
            return (Criteria) this;
        }

        public Criteria andMenuconLessThan(String value) {
            addCriterion("MENUCON <", value, "menucon");
            return (Criteria) this;
        }

        public Criteria andMenuconLessThanOrEqualTo(String value) {
            addCriterion("MENUCON <=", value, "menucon");
            return (Criteria) this;
        }

        public Criteria andMenuconLike(String value) {
            addCriterion("MENUCON like", value, "menucon");
            return (Criteria) this;
        }

        public Criteria andMenuconNotLike(String value) {
            addCriterion("MENUCON not like", value, "menucon");
            return (Criteria) this;
        }

        public Criteria andMenuconIn(List<String> values) {
            addCriterion("MENUCON in", values, "menucon");
            return (Criteria) this;
        }

        public Criteria andMenuconNotIn(List<String> values) {
            addCriterion("MENUCON not in", values, "menucon");
            return (Criteria) this;
        }

        public Criteria andMenuconBetween(String value1, String value2) {
            addCriterion("MENUCON between", value1, value2, "menucon");
            return (Criteria) this;
        }

        public Criteria andMenuconNotBetween(String value1, String value2) {
            addCriterion("MENUCON not between", value1, value2, "menucon");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}