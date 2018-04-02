package ski.serwon.yamlparser;

import org.yaml.snakeyaml.Yaml;
import ski.serwon.model.PressureSensor;
import ski.serwon.model.TemperatureSensor;
import ski.serwon.repository.PressureSensorRepository;
import ski.serwon.repository.TemperatureSensorRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/***
 * Class is used to download and translate yaml file as shown
 * in the example into lists of {@link PressureSensor} and {@link TemperatureSensor}
 *
 * @see <a href="https://github.com/relayr/pdm-test/blob/master/sensors.yml">Example</a>
 *
 * @author Pawel Serwonski
 */
public class GithubYamlParser {

    /**
     * Adds lists of sensors readed from *.yml file on Github to repositories.
     *
     * WARNING: sequence of saving to repository {@link PressureSensor} list before
     * reading {@link TemperatureSensor} is necessary to keep proper references
     * in {@link TemperatureSensor#masterSensor}
     *
     * @param githubYamlFileUrl URL to *.yml file places on Github server
     * @param pressureSensorRepository repository with pressure sensors
     * @param temperatureSensorRepository repository with temperature sensors
     */
    public static void populateTemperatureAndPressureSensorRepositories(
            String githubYamlFileUrl,
            PressureSensorRepository pressureSensorRepository,
            TemperatureSensorRepository temperatureSensorRepository) {
        List<LinkedHashMap> allSensorsFromGithubFile = getAllSensorsInLinkedHashMaps(githubYamlFileUrl);
        List<PressureSensor> pressureSensors = getPressureSensorsFromLinkedHashMaps(allSensorsFromGithubFile);
        pressureSensorRepository.saveAll(pressureSensors);

        List<TemperatureSensor> temperatureSensors = getTemperatureSensorfFromLinkedHashMaps(allSensorsFromGithubFile,
                pressureSensorRepository);
        temperatureSensorRepository.saveAll(temperatureSensors);
    }

    /**
     * Creates {@link List<TemperatureSensor>} objects based on values from {@link List<LinkedHashMap>}
     * @param linkedHashMaps list with maps containgn key-value pairs necessary
     *                       to create {@link TemperatureSensor} object
     * @param masterSensorsRepository repository containing objects necessary
     *                                to initialize {@link TemperatureSensor#masterSensor}
     * @return list of created sensors
     *
     * @see #createTemperatureSensorFromLinkedHashMap(LinkedHashMap, PressureSensorRepository)
     */
    private static List<TemperatureSensor> getTemperatureSensorfFromLinkedHashMaps(List<LinkedHashMap> linkedHashMaps
            , PressureSensorRepository masterSensorsRepository) {
        List<TemperatureSensor> temperatureSensors = new ArrayList<>();
        linkedHashMaps.stream().filter(map -> map.containsValue("temperature"))
                .forEach(map -> {
                    TemperatureSensor sensor = createTemperatureSensorFromLinkedHashMap(map, masterSensorsRepository);
                    temperatureSensors.add(sensor);
                });
        return temperatureSensors;
    }

    /**
     * Creates {@link List<PressureSensor>} objects based on values from {@link List<LinkedHashMap>}
     * @param linkedHashMaps list with maps containgn key-value pairs necessary to create {@link PressureSensor} object
     * @return list of created sensors
     *
     * @see #createPressureSensorFromLinkedHashMap(LinkedHashMap)
     */
    private static List<PressureSensor> getPressureSensorsFromLinkedHashMaps(List<LinkedHashMap> linkedHashMaps) {
        List<PressureSensor> pressureSensors = new ArrayList<>();
        linkedHashMaps.stream().filter(map -> map.containsValue("pressure"))
                .forEach(map -> {
                    PressureSensor sensor = createPressureSensorFromLinkedHashMap(map);
                    pressureSensors.add(sensor);
                });
        return pressureSensors;
    }

    /**
     * Creates {@link PressureSensor} object based on values from {@link LinkedHashMap}
     * @param map map with key-value pairs necessary to create {@link PressureSensor} object
     * @return created sensor
     */
    private static PressureSensor createPressureSensorFromLinkedHashMap(LinkedHashMap map) {
        int id = Integer.parseInt((String) map.get("id"));
        String engineId = (String) map.get("engine");
        int currentValue = (int) map.get("value");
        int minValue = (int) map.get("min_value");
        int maxValue = (int) map.get("max_value");

        return new PressureSensor(id, engineId, minValue, maxValue, currentValue);
    }

    /**
     * Creates {@link TemperatureSensor} object based on values from {@link LinkedHashMap}
     * @param map map with key-value pairs necessary to create {@link TemperatureSensor} object
     * @param masterSensorsRepository repository containing object necessary
     *  to initialize {@link TemperatureSensor#masterSensor}
     * @return created sensor
     */
    private static TemperatureSensor createTemperatureSensorFromLinkedHashMap(LinkedHashMap map
            , PressureSensorRepository masterSensorsRepository) {
        int id = Integer.parseInt((String) map.get("id"));
        int masterSensorId = Integer.parseInt((String) map.get("master-sensor-id"));
        int currentValue = (int) map.get("value");
        int minValue = (int) map.get("min_value");
        int maxValue = (int) map.get("max_value");
        Optional<PressureSensor> optionalMasterSensor = masterSensorsRepository.findById(masterSensorId);
        PressureSensor masterSensor = null;

        if (optionalMasterSensor.isPresent()) {
            masterSensor = optionalMasterSensor.get();
        }
        return new TemperatureSensor(id, masterSensor, currentValue, minValue, maxValue);
    }

    //TODO: make connection link to github cleaner

    /***
     * Connects to page containing raw version of file passed in argument
     * and loads content of the file
     * @param githubLink Link to Github page containing *.yml file
     * @return {@link List<LinkedHashMap>} every {@link LinkedHashMap}
     * is one set of key-value pairs from *.yml file
     */
    private static List<LinkedHashMap> getAllSensorsInLinkedHashMaps(String githubLink) {
        String githubRawLink = githubLink.replaceFirst("github.com", "raw.githubusercontent.com");
        githubRawLink = githubRawLink.replaceFirst("/blob", "");
        URL yamlFileUrl;
        URLConnection githubConnection;
        try {
            yamlFileUrl = new URL(githubRawLink);
            githubConnection = yamlFileUrl.openConnection();
        } catch (IOException e) {
            throw new RuntimeException("Passed Github link has incorrect form");
        }

        Object uncastedSensorList = loadUncastedListFromUrlConnection(githubConnection);
        List<LinkedHashMap> sensorList = (List<LinkedHashMap>) uncastedSensorList;
        return sensorList;
    }

    /**
     * Loads content from opened {@link URLConnection} to *.yml file.
     *
     *
     * @param connection opened connection to page with *.yml file
     * @return List of {@link LinkedHashMap} with definitions from
     * yaml file returned list is uncasted {@link Object}
     *
     * @see Yaml#loadAll(InputStream)
     */
    private static Object loadUncastedListFromUrlConnection(URLConnection connection) {
        try (InputStream inputStream = connection.getInputStream()) {
            Yaml yaml = new Yaml();
            Iterable<Object> iterable = yaml.loadAll(inputStream);
            if (iterable.iterator().hasNext()) {
                return iterable.iterator().next();
            } else {
                throw new RuntimeException("File seems to be empty.");
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException("Page not found. Check link and your Internet connection.");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found. Check link.");
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

}
