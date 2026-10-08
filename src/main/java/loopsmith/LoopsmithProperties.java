package loopsmith;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loopsmith")
public record LoopsmithProperties(Ollama ollama, GraphHopper graphhopper) {

    public record Ollama(String baseUrl, String model, int timeoutMs) {
    }

    public record GraphHopper(String baseUrl, int timeoutMs, int candidateSeeds) {
    }
}
