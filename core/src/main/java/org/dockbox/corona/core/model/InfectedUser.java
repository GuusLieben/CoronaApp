package org.dockbox.corona.core.model;

import java.util.Date;

public class InfectedUser extends UserData {

    private final Date dateOfInfection;

    public InfectedUser(String id, String firstName, String lastName, String BSN, Date birthDate, Date dateOfInfection) {
        super(id, firstName, lastName, BSN, birthDate);
        this.dateOfInfection = dateOfInfection;
    }

    private InfectedUser(Builder builder) {
        super(builder);
        this.dateOfInfection = builder.dateOfInfection;
    }

    public Date getDateOfInfection() {
        return dateOfInfection;
    }

    public static final class Builder extends UserData.Builder {

        private Date dateOfInfection;

        public Builder() {
        }

        public InfectedUser.Builder withDateOfInfection(Date val) {
            dateOfInfection = val;
            return this;
        }

        public InfectedUser build() {
            return new InfectedUser(this);
        }
    }

    @Override
    public String toString() {
        return super.toString() + "InfectedUser{" +
                "dateOfInfection=" + dateOfInfection +
                '}';
    }
}
