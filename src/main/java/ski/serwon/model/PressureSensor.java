package ski.serwon.model;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class PressureSensor extends Sensor {
    private String engineId;

    @OneToMany
    private List<TemperatureSensor> temperatureSensors;

    public PressureSensor(int id, String engineId, int minValueForSensor, int maxValueForSensor, int currentValue) {
        super(id, minValueForSensor, maxValueForSensor, currentValue);
        this.engineId = engineId;
    }

    public PressureSensor() {
    }

    public String getEngineId() {
        return engineId;
    }

}
