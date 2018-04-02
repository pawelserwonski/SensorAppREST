package ski.serwon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ski.serwon.model.PressureSensor;
import ski.serwon.repository.PressureSensorRepository;
import ski.serwon.repository.TemperatureSensorRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/engines")
public class EnginesController {
    private TemperatureSensorRepository temperatureSensorRepository;
    private PressureSensorRepository pressureSensorRepository;

    @Autowired
    public EnginesController(TemperatureSensorRepository temperatureSensorRepository, PressureSensorRepository pressureSensorRepository) {
        this.temperatureSensorRepository = temperatureSensorRepository;
        this.pressureSensorRepository = pressureSensorRepository;
    }

    @GetMapping
    Collection<String> getMalfunctioningEngines(@RequestParam("pressure_threshold") int pressureThreshold,
                                                @RequestParam("temp_threshold") int tempThreshold) {
        return temperatureSensorRepository.getAllByCurrentValueIsGreaterThan(tempThreshold)
                .stream()
                .filter(temperatureSensor -> {
                    PressureSensor masterSensor = temperatureSensor.getMasterSensor();
                    return masterSensor.getCurrentValue() < pressureThreshold;
                })
                .map(c -> c.getMasterSensor().getEngineId())
                .distinct()
                .collect(Collectors.toList());
    }


}
