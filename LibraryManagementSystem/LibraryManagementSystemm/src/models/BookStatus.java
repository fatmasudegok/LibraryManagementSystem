package models;

public enum BookStatus {
    AVAILABLE,
    LOANED,
    OUT_OF_STOCK;

    @Override
    public String toString() {
        switch (this) {
            case AVAILABLE:
                return "MÜSAİT";
            case LOANED:
                return "ÖDÜNÇTE";
            case OUT_OF_STOCK:
                return "TÜKENDİ";
            default:
                return super.toString();
        }
    }
}