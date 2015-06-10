package dk.dma.enumgenerator.test;

public final class Length {

    private final double length;

    private final LengthUnit unit;

    Length(double length, LengthUnit unit) {
        this.length = length;
        this.unit = unit;
    }

    public LengthUnit getUnit() {
        return unit;
    }

    /**
     * Returns the length in kilometers.
     *
     * @param length
     *            the length to convert
     * @return the converted length
     */
    public double toKilometers() {
        return unit.toKilometers(length);
    }

    /**
     * Returns the length in meters.
     *
     * @param length
     *            the length to convert
     * @return the converted length
     */
    public double toMeters() {
        return unit.toMeters(length);
    }

    /**
     * Returns the length in miles.
     *
     * @param length
     *            the length to convert
     * @return the converted length
     */
    public double toMiles() {
        return unit.toMiles(length);
    }

    /**
     * Returns the length in nautical miles.
     *
     * @param length
     *            the length to convert
     * @return the converted length
     */
    public double toNauticalMiles() {
        return unit.toNauticalMiles(length);
    }
}
