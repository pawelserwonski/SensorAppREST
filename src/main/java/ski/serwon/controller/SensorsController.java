package ski.serwon.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ski.serwon.exception.SensorNotFoundException;
import ski.serwon.exception.ValueOutOfBoundsException;
import ski.serwon.model.PressureSensor;
import ski.serwon.model.Sensor;
import ski.serwon.model.TemperatureSensor;
import ski.serwon.repository.PressureSensorRepository;
import ski.serwon.repository.TemperatureSensorRepository;

import java.util.Optional;

@RestController
@RequestMapping("/sensors/{sensorId}")
public class SensorsController {

    TemperatureSensorRepository temperatureSensorRepository;
    PressureSensorRepository pressureSensorRepository;

    @Autowired
    public SensorsController(TemperatureSensorRepository temperatureSensorRepository,
                             PressureSensorRepository pressureSensorRepository) {
        this.temperatureSensorRepository = temperatureSensorRepository;
        this.pressureSensorRepository = pressureSensorRepository;
    }

    @GetMapping
    Sensor getSensor(@PathVariable int sensorId) {
        Optional<TemperatureSensor> temperatureSensor = temperatureSensorRepository.findById(sensorId);
        if (temperatureSensor.isPresent()) {
            return temperatureSensor.get();
        }

        Optional<PressureSensor> pressureSensor = pressureSensorRepository.findById(sensorId);
        if (pressureSensor.isPresent()) {
            return pressureSensor.get();
        }

        throw new SensorNotFoundException("Sensor with id: " + sensorId + " not found in database");

    }

    @PostMapping
    ResponseEntity<?> updateValue(@PathVariable int sensorId, @RequestBody SensorsControllerMessageFromClient message) {
        Sensor sensor = getSensor(sensorId);
        boolean operationPerformed = performOperationOnSensor(sensor, message);

        if (sensor instanceof TemperatureSensor) {
            temperatureSensorRepository.save((TemperatureSensor) sensor);
        } else {
            pressureSensorRepository.save((PressureSensor) sensor);
        }

        return operationPerformed ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    private boolean performOperationOnSensor(Sensor sensor, SensorsControllerMessageFromClient message) {
        switch (message.getOperation()) {
            case "set":
                performSetOperationOnSensor(sensor, message.getValueParsedToInt());
                break;
            case "increment":
                performIncrementOperationOnSensor(sensor, message.getValueParsedToInt());
                break;
            case "decrement":
                performIncrementOperationOnSensor(sensor, -message.getValueParsedToInt());
                break;
            default:
                return false;
        }
        return true;
    }

    private void performIncrementOperationOnSensor(Sensor sensor, int incrementation) {
        int newValue = sensor.getCurrentValue() + incrementation;
        performSetOperationOnSensor(sensor, newValue);
    }

    private void performSetOperationOnSensor(Sensor sensor, int value) {
        if (value > sensor.getMaxValueForSensor()
                || value < sensor.getMinValueForSensor()) {
            throw new ValueOutOfBoundsException(
                    "Passed value is out of bounds for sensor with id: " + sensor.getId());
        }

        sensor.setCurrentValue(value);
    }
}
