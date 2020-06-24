package org.dockbox.corona.core.model;

import java.util.Date;

public class UserData {

    private final String id;
    private final String firstName;
    private final String lastName;
    private final String BSN;
    private final Date birthDate;

    public UserData(String id, String firstName, String lastName, String BSN, Date birthDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.BSN = BSN;
        this.birthDate = birthDate;
    }

    protected UserData(Builder builder) {
        id = builder.id;
        firstName = builder.firstName;
        lastName = builder.lastName;
        birthDate = builder.birthDate;
        BSN = builder.BSN;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getBSN() {
        return BSN;
    }

    public Date getBirthDate() {
        return birthDate;
    }


    public static class Builder {
        private String id;
        private String firstName;
        private String lastName;
        private Date birthDate;
        private String BSN;

        public Builder() {
        }

        public Builder withId(String val) {
            id = val;
            return this;
        }

        public Builder withFirstName(String val) {
            firstName = val;
            return this;
        }

        public Builder withLastName(String val) {
            lastName = val;
            return this;
        }

        public Builder withBirthDate(Date val) {
            birthDate = val;
            return this;
        }

        public Builder withBSN(String val) {
            BSN = val;
            return this;
        }

        public UserData build() {
            return new UserData(this);
        }
    }

    @Override
    public String toString() {
        return "UserData{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", BSN='" + BSN + '\'' +
                ", birthDate=" + birthDate +
                '}';
    }
}
