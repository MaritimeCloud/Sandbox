package dk.dma.enumgenerator.test;

public final class Speed {

    private final double speed;

    private final SpeedUnit unit;

    Speed(double speed, SpeedUnit unit) {
        this.speed = speed;
        this.unit = unit;
    }

    public SpeedUnit getUnit() {
        return unit;
    }

    /**
     * Returns the speed in kilometers per hour.
     *
     * @param speed
     *            the speed to convert
     * @return the converted speed
     */
    public double toKilometersPerHour() {
        return unit.toKilometersPerHour(speed);
    }

    /**
     * Returns the speed in knots.
     *
     * @param speed
     *            the speed to convert
     * @return the converted speed
     */
    public double toKnots() {
        return unit.toKnots(speed);
    }

    /**
     * Returns the speed in metres per second.
     *
     * @param speed
     *            the speed to convert
     * @return the converted speed
     */
    public double toMetresPerSecond() {
        return unit.toMetresPerSecond(speed);
    }

    /**
     * Returns the speed in miles per hour.
     *
     * @param speed
     *            the speed to convert
     * @return the converted speed
     */
    public double toMilesPerHour() {
        return unit.toMilesPerHour(speed);
    }
}
