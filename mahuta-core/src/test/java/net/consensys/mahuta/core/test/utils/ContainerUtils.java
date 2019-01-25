package net.consensys.mahuta.core.test.utils;

import java.time.Duration;
import java.util.Map;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.shaded.com.google.common.collect.Maps;

import com.google.common.collect.ImmutableMap;

public class ContainerUtils {

    public static final String DOCKER_IMAGE_IPFS = "ipfs/go-ipfs:latest";
    public static final String DOCKER_IMAGE_ELASTICSEARCH = "docker.elastic.co/elasticsearch/elasticsearch-oss:6.5.4";

    public enum ContainerType { IPFS, ELASTICSEARCH };
    
    public static final Map<ContainerType, ContainerDefinition> containersRegistry = ImmutableMap.of(
                    ContainerType.IPFS, new ContainerDefinition(
                            DOCKER_IMAGE_IPFS, 
                            5001, 4001, 8080), 
                    ContainerType.ELASTICSEARCH, new ContainerDefinition(
                            DOCKER_IMAGE_ELASTICSEARCH, 
                            ImmutableMap.of("cluster-name", "docker-cluster"), 
                            Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(1800)), 9300));
    
    private static final Map<String, GenericContainer<?>> containers = Maps.newHashMap();
    
    public static void startContainer(String name, ContainerType type) {
        GenericContainer<?> container = new GenericContainer<>(containersRegistry.get(type).image)
                .withExposedPorts(containersRegistry.get(type).exposedPorts);
        

        if(containersRegistry.get(type).waitStrategy != null) {
            container.waitingFor(containersRegistry.get(type).waitStrategy);
        }
        
        if(containersRegistry.get(type).envVars != null) {
            containersRegistry.get(type).envVars.forEach((key, value) -> {
                container.addEnv(key, value);
            });
        }
        
        container.start();
        
        containers.put(name, container);
    }
    public static void stopAll() {
        containers.forEach((name, container) -> container.stop());
    }
    
    public static String getHost(String name) {
        return containers.get(name).getContainerIpAddress();
    }
    
    public static Integer getPort(String name) {
        return containers.get(name).getFirstMappedPort();
    }
    
    public static String getConfig(String name, String key) {
        return containers.get(name).getEnvMap().entrySet().stream()
                .filter(e->e.getKey().contentEquals(key))
                .findFirst()
                .map(e->e.getValue())
                .orElseThrow(() -> new RuntimeException("env variable '"+key+"' not set in container " + name));
    }
    
    static class ContainerDefinition {
        private String image;
        private Integer[] exposedPorts;
        private Map<String, String> envVars;
        private WaitStrategy waitStrategy;
        
        public ContainerDefinition(String image, Integer... exposedPorts) {
            this(image, null, null, exposedPorts);
        }
        public ContainerDefinition(String image, Map<String, String> envVars, Integer... exposedPorts) {
            this(image, envVars, null, exposedPorts);
        }
        public ContainerDefinition(String image, Map<String, String> envVars, WaitStrategy waitStrategy, Integer... exposedPorts) {
            this.image = image;
            this.exposedPorts = exposedPorts;
            this.envVars = envVars;
            this.waitStrategy = waitStrategy;
        }
    }
    
}
