package ski.serwon.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ski.serwon.model.PressureSensor;
import ski.serwon.model.TemperatureSensor;
import ski.serwon.repository.PressureSensorRepository;
import ski.serwon.repository.TemperatureSensorRepository;
import ski.serwon.yamlparser.GithubYamlParser;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
@ComponentScan("ski.serwon")
@EntityScan("ski.serwon.model")
@EnableJpaRepositories(basePackages = "ski.serwon.repository")
public class SpringBootWebApplication extends SpringBootServletInitializer {
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(SpringBootWebApplication.class);
    }

    @Bean
    public CommandLineRunner lineRunner(PressureSensorRepository pressureSensorRepository,
                                        TemperatureSensorRepository temperatureSensorRepository) {
        String link = environment.getProperty("config");

        if (link != null) {
            GithubYamlParser.populateTemperatureAndPressureSensorRepositories(
                    link, pressureSensorRepository, temperatureSensorRepository);
        } else {
            logger.log(Level.WARNING, "Argument with Github link to *.yml file not passed." +
                    " Server starts with empty repositories.");
        }
        return null;
    }

}
