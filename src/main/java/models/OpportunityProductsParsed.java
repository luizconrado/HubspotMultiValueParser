package models;

import java.util.Objects;

public class OpportunityProductsParsed {

    private String opportunityId;
    private String accountId;
    private String productName;

    public OpportunityProductsParsed() {
    }

    public String getOpportunityId() {
        return opportunityId;
    }

    public void setOpportunityId(String opportunityId) {
        this.opportunityId = opportunityId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpportunityProductsParsed that = (OpportunityProductsParsed) o;
        return Objects.equals(opportunityId, that.opportunityId) &&
                Objects.equals(accountId, that.accountId) &&
                Objects.equals(productName, that.productName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opportunityId, accountId, productName);
    }

    @Override
    public String toString() {
        return "OpportunityProductsParsed{" +
                "opportunityId='" + opportunityId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", productName='" + productName + '\'' +
                '}';
    }
}
