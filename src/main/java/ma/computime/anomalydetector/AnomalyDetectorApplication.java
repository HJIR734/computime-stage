// Emplacement : src/main/java/ma/computime/anomalydetector/AnomalyDetectorApplication.java

package ma.computime.anomalydetector;

// Imports de Spring Boot
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient; 
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value; 

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class AnomalyDetectorApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.out.println("--- Application running in UTC timezone ---");
    }
    
    public static void main(String[] args) {
        SpringApplication.run(AnomalyDetectorApplication.class, args);
    }

    
    @Bean
    public RestClient restClient(@Value("${ia.api.base-url}") String baseUrl) {
        System.out.println("--- Configuration du RestClient pour communiquer avec l'API IA sur l'URL : " + baseUrl + " ---");
        return RestClient.builder()
                .baseUrl(baseUrl) 
                .build();
    }
    
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}