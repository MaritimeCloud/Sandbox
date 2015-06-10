package dk.dma.enumgenerator.test;

public enum LengthUnit {
    /** Kilometer as defined by the International Bureau of Weights and Measures. */
    KILOMETERS {
        public double toKilometers(double length) {
            return length;
        }
        public double toMeters(double length) {
            return length * 1000;
        }
        public double toMiles(double length) {
            return length / 1.609344D;
        }
        public double toNauticalMiles(double length) {
            return length / 1.852D;
        }
    },

    /** Meter as defined by the International Bureau of Weights and Measures. */
    METERS {
        public double toKilometers(double length) {
            return length / 1000;
        }
        public double toMeters(double length) {
            return length;
        }
        public double toMiles(double length) {
            return length / 1609.344D;
        }
        public double toNauticalMiles(double length) {
            return length / 1852;
        }
    },

    /** A mile is defined as 1609.344 meters. */
    MILES {
        public double toKilometers(double length) {
            return length * 1.609344D;
        }
        public double toMeters(double length) {
            return length * 1609.344D;
        }
        public double toMiles(double length) {
            return length;
        }
        public double toNauticalMiles(double length) {
            return length * 1.150779D;
        }
    },

    /** A nautical mile is defined as 1852 meters. */
    NAUTICAL_MILES {
        public double toKilometers(double length) {
            return length * 1.852D;
        }
        public double toMeters(double length) {
            return length * 1852;
        }
        public double toMiles(double length) {
            return length / 1.150779D;
        }
        public double toNauticalMiles(double length) {
            return length;
        }
    };

    public abstract double toKilometers(double length);

    public abstract double toMeters(double length);

    public abstract double toMiles(double length);

    public abstract double toNauticalMiles(double length);
}
