package ski.serwon.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class TemperatureSensor extends Sensor {

    @ManyToOne
    private PressureSensor masterSensor;

    public TemperatureSensor(int id, PressureSensor masterSensor, int currentValue, int minValueForSensor, int maxValueForSensor) {
        super(id, minValueForSensor, maxValueForSensor, currentValue);
        this.masterSensor = masterSensor;
    }

    public TemperatureSensor() {
    }

    public PressureSensor getMasterSensor() {
        return masterSensor;
    }
}
