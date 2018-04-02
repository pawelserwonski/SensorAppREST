package ski.serwon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ski.serwon.model.PressureSensor;

import java.util.Collection;


public interface PressureSensorRepository extends JpaRepository<PressureSensor, Integer> {
    Collection<PressureSensor> getAllByCurrentValueLessThan(int pressureThreshold);
}
