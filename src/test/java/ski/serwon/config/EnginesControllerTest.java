package ski.serwon.config;

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
import ski.serwon.model.PressureSensor;
import ski.serwon.model.TemperatureSensor;
import ski.serwon.repository.PressureSensorRepository;
import ski.serwon.repository.TemperatureSensorRepository;

import java.nio.charset.Charset;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SpringBootWebApplication.class)
@WebAppConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EnginesControllerTest {
    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf-8"));

    private MockMvc mockMvc;

    private PressureSensor masterSensor;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Autowired
    private TemperatureSensorRepository temperatureSensorRepository;

    @Autowired
    private PressureSensorRepository pressureSensorRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    void setMappingJackson2HttpMessageConverter(HttpMessageConverter<?>[] converters) {
        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters)
                .stream()
                .filter(httpMessageConverter -> httpMessageConverter instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);
        assertNotNull(this.mappingJackson2HttpMessageConverter, "instance of JSON message converter is required to perform tests");
    }

    @BeforeAll
    public void setupRepositoriesForTests() throws Exception {
        temperatureSensorRepository.deleteAllInBatch();
        pressureSensorRepository.deleteAllInBatch();
    }

    @BeforeEach
    public void setupTests() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        this.masterSensor = pressureSensorRepository.save(new PressureSensor(
                1, "100", 1000, 5000, 2500));
        this.temperatureSensorRepository.save(new TemperatureSensor(10, masterSensor, 25, 0, 50));
        this.temperatureSensorRepository.save(new TemperatureSensor(11, masterSensor, 15, -50, 100));
    }

    @Test
    public void requiredParamsNotPassed() throws Exception {
        mockMvc.perform(get("/engines"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void requiredParamPressureThresholdNotPassed() throws Exception {
        mockMvc.perform(get("/engines?temp_threshold=50"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void requiredParamTemperatureThresholdNotPassed() throws Exception {
        mockMvc.perform(get("/engines?pressure_threshold=3000"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void engineOk() throws Exception {
        mockMvc.perform(get("/engines?temp_threshold=30&pressure_threshold=2000"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    public void onlyPressureParameterOutOfBounds() throws Exception {
        mockMvc.perform(get("/engines?temp_threshold=30&pressure_threshold=3000"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    public void onlyTemperatureParameterOutOfBounds() throws Exception {
        mockMvc.perform(get("/engines?temp_threshold=5&pressure_threshold=2000"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    public void allSensorsOutOfBounds() throws Exception {
        mockMvc.perform(get("/engines?temp_threshold=5&pressure_threshold=3000"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(content().string("[\"100\"]"));
    }


    @Test
    public void pressureAndOneTemperatureSensorsOutOfBounds() throws Exception {
        mockMvc.perform(get("/engines?temp_threshold=20&pressure_threshold=3000"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(content().string("[\"100\"]"));
    }
}

