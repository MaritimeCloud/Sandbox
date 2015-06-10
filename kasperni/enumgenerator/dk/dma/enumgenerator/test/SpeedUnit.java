package dk.dma.enumgenerator.test;

public enum SpeedUnit {
    /** Kilometers per Hour */
    KILOMETERS_PER_HOUR {
        public double toKilometersPerHour(double speed) {
            return speed;
        }
        public double toKnots(double speed) {
            return speed / 1.852D;
        }
        public double toMetresPerSecond(double speed) {
            return speed / 3.6D;
        }
        public double toMilesPerHour(double speed) {
            return speed / 1.609344D;
        }
    },

    /** Knots */
    KNOTS {
        public double toKilometersPerHour(double speed) {
            return speed * 1.852D;
        }
        public double toKnots(double speed) {
            return speed;
        }
        public double toMetresPerSecond(double speed) {
            return speed / 1.9438444924406D;
        }
        public double toMilesPerHour(double speed) {
            return speed * 1.1507794480235425D;
        }
    },

    /** Metres per Second */
    METRES_PER_SECOND {
        public double toKilometersPerHour(double speed) {
            return speed * 3.6D;
        }
        public double toKnots(double speed) {
            return speed * 1.9438444924406D;
        }
        public double toMetresPerSecond(double speed) {
            return speed;
        }
        public double toMilesPerHour(double speed) {
            return speed / 0.44704D;
        }
    },

    /** Miles Per Hour */
    MILES_PER_HOUR {
        public double toKilometersPerHour(double speed) {
            return speed * 1.609344D;
        }
        public double toKnots(double speed) {
            return speed / 1.1507794480235425D;
        }
        public double toMetresPerSecond(double speed) {
            return speed * 0.44704D;
        }
        public double toMilesPerHour(double speed) {
            return speed;
        }
    };

    public abstract double toKilometersPerHour(double speed);

    public abstract double toKnots(double speed);

    public abstract double toMetresPerSecond(double speed);

    public abstract double toMilesPerHour(double speed);
}
