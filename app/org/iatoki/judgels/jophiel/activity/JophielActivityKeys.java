package org.iatoki.judgels.jophiel.activity;

public final class JophielActivityKeys {

    public static final NoEntityActivityKey LOGIN = new NoEntityActivityKey() {
        @Override
        public String getKeyAction() {
            return "LOGIN";
        }

        @Override
        public String toString() {
            return "log in.";
        }
    };

    public static final NoEntityActivityKey LOGOUT = new NoEntityActivityKey() {
        @Override
        public String getKeyAction() {
            return "LOGOUT";
        }

        @Override
        public String toString() {
            return "log out.";
        }
    };

    private JophielActivityKeys() {
        // prevent instantiation
    }
}
