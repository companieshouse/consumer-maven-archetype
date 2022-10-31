package ${package};

import java.util.Objects;

/**
 * Contains all parameters required by {@link Service service implementations}.
 */
public class ServiceParameters {

    private final String data;

    public ServiceParameters(String data) {
        this.data = data;
    }

    /**
     * Get data attached to the ServiceParameters object.
     *
     * @return A string representing data that has been attached to the ServiceParameters object.
     */
    public String getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServiceParameters)) {
            return false;
        }
        ServiceParameters that = (ServiceParameters) o;
        return Objects.equals(getData(), that.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getData());
    }
}
