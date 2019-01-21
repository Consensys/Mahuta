package net.consensys.mahuta.core.test.utils;

import java.util.Map;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.com.google.common.collect.Maps;

import com.google.common.collect.ImmutableMap;

public class ContainerUtils {
    
    public static final String DOCKER_IMAGE_IPFS = "ipfs/go-ipfs:latest";
    public static final String DOCKER_IMAGE_ELASTICSEARCH = "docker.elastic.co/elasticsearch/elasticsearch-oss:6.5.4";

    public enum ContainerType { IPFS, ELASTICSEARCH };
    
    public static final Map<ContainerType, ContainerDefinition> containersRegistry = ImmutableMap.of(
                    ContainerType.IPFS, new ContainerDefinition(DOCKER_IMAGE_IPFS, 5001, 4001, 8080), 
                    ContainerType.ELASTICSEARCH, new ContainerDefinition(DOCKER_IMAGE_ELASTICSEARCH, 9300));
    
    private static final Map<String, GenericContainer<?>> containers = Maps.newHashMap();
    
    public static void startContainer(String name, ContainerType type) {
        GenericContainer<?> container = new GenericContainer<>(containersRegistry.get(type).image)
                .withExposedPorts(containersRegistry.get(type).exposedPorts);
        
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
    
    static class ContainerDefinition {
        private String image;
        private Integer[] exposedPorts;
        
        public ContainerDefinition(String image, Integer... exposedPorts) {
            this.image = image;
            this.exposedPorts = exposedPorts;
        }
    }
    
}
