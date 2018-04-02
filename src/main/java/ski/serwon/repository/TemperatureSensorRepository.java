package ski.serwon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ski.serwon.model.TemperatureSensor;

import java.util.Collection;


public interface TemperatureSensorRepository extends JpaRepository<TemperatureSensor, Integer> {
    Collection<TemperatureSensor> getAllByCurrentValueIsGreaterThan(int temperatureThreshold);
}
