package models;

import java.util.Objects;

public class ContactParsed {

    private String contactId;
    private String accountId;
    private String hubspotId;

    public ContactParsed() {
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
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
        ContactParsed that = (ContactParsed) o;
        return Objects.equals(contactId, that.contactId) &&
                Objects.equals(accountId, that.accountId) &&
                Objects.equals(hubspotId, that.hubspotId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contactId, accountId, hubspotId);
    }

    @Override
    public String toString() {
        return "ContactParsed{" +
                "contactId='" + contactId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", hubspotId='" + hubspotId + '\'' +
                '}';
    }
}

