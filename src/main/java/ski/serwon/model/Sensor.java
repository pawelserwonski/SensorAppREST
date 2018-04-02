package ski.serwon.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Sensor {
    @Id
    private Integer id;
    private int minValueForSensor;
    private int maxValueForSensor;
    private int currentValue;

    public Sensor(Integer id, int minValueForSensor, int maxValueForSensor, int currentValue) {
        this.id = id;
        this.minValueForSensor = minValueForSensor;
        this.maxValueForSensor = maxValueForSensor;
        this.currentValue = currentValue;
    }

    public Sensor() {
    }

    public Integer getId() {
        return id;
    }

    public int getMinValueForSensor() {
        return minValueForSensor;
    }

    public int getMaxValueForSensor() {
        return maxValueForSensor;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
    }
}
