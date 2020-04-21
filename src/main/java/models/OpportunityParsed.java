package models;

import java.util.Objects;

public class OpportunityParsed {

    private String opportunityId;
    private String accountId;
    private String hubspotId;

    public OpportunityParsed() {
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

    public String getHubspotId() {
        return hubspotId;
    }

    public void setHubspotId(String hubspotId) {
        this.hubspotId = hubspotId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpportunityParsed that = (OpportunityParsed) o;
        return Objects.equals(opportunityId, that.opportunityId) &&
                Objects.equals(accountId, that.accountId) &&
                Objects.equals(hubspotId, that.hubspotId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opportunityId, accountId, hubspotId);
    }

    @Override
    public String toString() {
        return "OpportunityParsed{" +
                "opportunityId='" + opportunityId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", hubspotId='" + hubspotId + '\'' +
                '}';
    }


}

