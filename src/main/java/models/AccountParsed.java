package models;

import java.util.Objects;

public class AccountParsed {


    private String accountId;
    private String hubspotId;

    public AccountParsed() {
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
        AccountParsed that = (AccountParsed) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(hubspotId, that.hubspotId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, hubspotId);
    }

    @Override
    public String toString() {
        return "AccountParsed{" +
                "accountId='" + accountId + '\'' +
                ", hubspotId='" + hubspotId + '\'' +
                '}';
    }

}

