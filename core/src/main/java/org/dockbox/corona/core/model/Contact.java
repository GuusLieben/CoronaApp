package org.dockbox.corona.core.model;

import java.time.LocalDateTime;

public class Contact {

    private final String idUser1;
    private final String idUser2;
    private final LocalDateTime dateOfContact;

    public Contact(String idUser1, String idUser2, LocalDateTime dateOfContact) {
        this.idUser1 = idUser1;
        this.idUser2 = idUser2;
        this.dateOfContact = dateOfContact;
    }

    private Contact(Builder builder) {
        idUser1 = builder.idUser1;
        idUser2 = builder.idUser2;
        dateOfContact = builder.dateOfContact;
    }

    public String getIdUser1() {
        return idUser1;
    }

    public String getIdUser2() {
        return idUser2;
    }

    public static class Builder {
        private String idUser1;
        private String idUser2;
        private LocalDateTime dateOfContact;

        public Builder() {
        }

        public Builder withIdUser1(String val) {
            idUser1 = val;
            return this;
        }

        public Builder withIdUser2(String val) {
            idUser2 = val;
            return this;
        }

        public Builder withDateOfContact(LocalDateTime val) {
            dateOfContact = val;
            return this;
        }

        public Contact build() {
            return new Contact(this);
        }

    }
}
