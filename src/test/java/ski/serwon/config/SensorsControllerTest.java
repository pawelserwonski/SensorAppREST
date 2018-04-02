package ski.serwon.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ski.serwon.controller.SensorsControllerMessageFromClient;
import ski.serwon.model.PressureSensor;
import ski.serwon.model.TemperatureSensor;
import ski.serwon.repository.PressureSensorRepository;
import ski.serwon.repository.TemperatureSensorRepository;

import java.nio.charset.Charset;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SpringBootWebApplication.class)
@WebAppConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SensorsControllerTest {

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("UTF-8"));

    private MockMvc mockMvc;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    private PressureSensor masterSensor;

    private int currentPressureValue = 2500;
    private int currentTemperatureValue = 25;
    private int pressureSensorId = 1;
    private int tempSensorId = 1;

    @Autowired
    private PressureSensorRepository pressureSensorRepository;

    @Autowired
    private TemperatureSensorRepository temperatureSensorRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private void setMappingJackson2HttpMessageConverter(HttpMessageConverter<?>[] converters) {
        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters)
                .stream()
                .filter(httpMessageConverter -> httpMessageConverter instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);

        assertNotNull(this.mappingJackson2HttpMessageConverter, "instance of JSON message converter is required to perform tests");
    }

    @BeforeAll
    private void setupRepositoriesForTests() {
        temperatureSensorRepository.deleteAllInBatch();
        pressureSensorRepository.deleteAllInBatch();
    }

    @BeforeEach
    public void setupTests() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        this.masterSensor = pressureSensorRepository.save(new PressureSensor(
                1, "100", 1000, 5000, currentPressureValue));
        this.temperatureSensorRepository.save(new TemperatureSensor(10, masterSensor, currentTemperatureValue, 0, 50));
    }

    @Test
    public void wrongTypeOfPathVariableTest() throws Exception {
        mockMvc.perform(get("/sensors/thisIsCertainlyNotInt"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void sensorNoFoundTest() throws Exception {
        mockMvc.perform(get("/sensors/123654"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getSensorTest() throws Exception {
        mockMvc.perform(get("/sensors/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.id", is(pressureSensorId)))
                .andExpect(jsonPath("$.minValueForSensor", is(1000)))
                .andExpect(jsonPath("$.maxValueForSensor", is(5000)))
                .andExpect(jsonPath("$.currentValue", is(2500)))
                .andExpect(jsonPath("$.engineId", is("100")));
    }

    @Test
    public void wrongRequestedBodyTest() throws Exception {
        mockMvc.perform(post("/sensors/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(
                        new SensorsControllerMessageFromClient("wrong", "wrong")
                )))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void wrongContentTypeTest() throws Exception {
        mockMvc.perform(post("/sensors/10")
                .contentType(MediaType.APPLICATION_ATOM_XML)
                .content("wrongType"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    public void incrementSensorValueTest() throws Exception {
        mockMvc.perform(post("/sensors/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(
                        new SensorsControllerMessageFromClient("increment", "5")
                )))
                .andExpect(status().isOk());

        mockMvc.perform(get("/sensors/10"))
                .andExpect(jsonPath("$.currentValue", is(currentTemperatureValue + 5)));

    }

    @Test
    public void decrementSensorValueTest() throws Exception {
        mockMvc.perform(post("/sensors/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(
                        new SensorsControllerMessageFromClient("decrement", "5")
                )))
                .andExpect(status().isOk());

        mockMvc.perform(get("/sensors/10"))
                .andExpect(jsonPath("$.currentValue", is(currentTemperatureValue - 5)));
    }

    @Test
    public void setSensorValueTest() throws Exception {
        mockMvc.perform(post("/sensors/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(
                        new SensorsControllerMessageFromClient("set", "18")
                )))
                .andExpect(status().isOk());

        mockMvc.perform(get("/sensors/10"))
                .andExpect(jsonPath("$.currentValue", is(18)));
    }

    @Test
    public void incrementOverMaxValueTest() throws Exception {
        mockMvc.perform(post("/sensors/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(
                        new SensorsControllerMessageFromClient("increment", "100")
                )))
                .andExpect(status().isForbidden());
    }

    @Test
    public void decrementBelowMinValueTest() throws Exception {
        mockMvc.perform(post("/sensors/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(
                        new SensorsControllerMessageFromClient("decrement", "100")
                )))
                .andExpect(status().isForbidden());
    }

    @Test
    public void setOverMaxValueTest() throws Exception {
        mockMvc.perform(post("/sensors/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(
                        new SensorsControllerMessageFromClient("set", "100")
                )))
                .andExpect(status().isForbidden());
    }

    @Test
    public void setBelowMinValueTest() throws Exception {
        mockMvc.perform(post("/sensors/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(
                        new SensorsControllerMessageFromClient("set", "-100")
                )))
                .andExpect(status().isForbidden());
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
