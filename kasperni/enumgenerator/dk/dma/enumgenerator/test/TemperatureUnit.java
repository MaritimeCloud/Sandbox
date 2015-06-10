package dk.dma.enumgenerator.test;

public enum TemperatureUnit {
    CELCIUS {
        public double toCelcius(double temperature) {
            return temperature;
        }
        public double toFahrenheit(double temperature) {
            return temperature * 9D / 5D + 32;
        }
        public double toKelvin(double temperature) {
            return temperature + 273.15d;
        }
    },

    FAHRENHEIT {
        public double toCelcius(double temperature) {
            return (temperature - 32) * 5D / 9D;
        }
        public double toFahrenheit(double temperature) {
            return temperature;
        }
        public double toKelvin(double temperature) {
            return (temperature + 459.67d) * 5D / 9D;
        }
    },

    KELVIN {
        public double toCelcius(double temperature) {
            return temperature - 273.15d;
        }
        public double toFahrenheit(double temperature) {
            return temperature * 9D / 5D - 459.67d;
        }
        public double toKelvin(double temperature) {
            return temperature;
        }
    };

    public abstract double toCelcius(double temperature);

    public abstract double toFahrenheit(double temperature);

    public abstract double toKelvin(double temperature);
}
