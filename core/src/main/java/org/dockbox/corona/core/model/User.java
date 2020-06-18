package org.dockbox.corona.core.model;

import java.sql.Date;

public class User {

    private final String id;
    private final String firstName;
    private final String lastName;
    private final String BSN;
    private final Date birthDate;

    public User(String id, String firstName, String lastName, String BSN, Date birthDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.BSN = BSN;
        this.birthDate = birthDate;
    }

    private User(Builder builder) {
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


    public static final class Builder {
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

        public User build() {
            return new User(this);
        }
    }
}
