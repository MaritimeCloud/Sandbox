package dk.dma.enumgenerator.test;

public final class Temperature {

    private final double temperature;

    private final TemperatureUnit unit;

    Temperature(double temperature, TemperatureUnit unit) {
        this.temperature = temperature;
        this.unit = unit;
    }

    public TemperatureUnit getUnit() {
        return unit;
    }

    /**
     * Returns the temperature in celcius.
     *
     * @param temperature
     *            the temperature to convert
     * @return the converted temperature
     */
    public double toCelcius() {
        return unit.toCelcius(temperature);
    }

    /**
     * Returns the temperature in fahrenheit.
     *
     * @param temperature
     *            the temperature to convert
     * @return the converted temperature
     */
    public double toFahrenheit() {
        return unit.toFahrenheit(temperature);
    }

    /**
     * Returns the temperature in kelvin.
     *
     * @param temperature
     *            the temperature to convert
     * @return the converted temperature
     */
    public double toKelvin() {
        return unit.toKelvin(temperature);
    }
}
