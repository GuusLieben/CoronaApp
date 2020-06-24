package org.dockbox.corona.core.model;

public class Contact {

    private final int idUser1;
    private final int idUser2;

    public Contact(int idUser1, int idUser2) {
        this.idUser1 = idUser1;
        this.idUser2 = idUser2;
    }

    private Contact(Builder builder) {
        idUser1 = builder.idUser1;
        idUser2 = builder.idUser2;
    }

    public int getIdUser1() {
        return idUser1;
    }

    public int getIdUser2() {
        return idUser2;
    }

    public static class Builder {
        private int idUser1;
        private int idUser2;

        public Builder() {
        }

        public Builder withIdUser1(int val) {
            idUser1 = val;
            return this;
        }

        public Builder withIdUser2(int val) {
            idUser2 = val;
            return this;
        }

        public Contact build() {
            return new Contact(this);
        }

    }
}
