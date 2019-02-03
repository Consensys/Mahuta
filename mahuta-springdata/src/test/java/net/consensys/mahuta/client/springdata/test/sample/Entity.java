package net.consensys.mahuta.client.springdata.test.sample;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Entity {
    
    private static final ObjectMapper mapper = new ObjectMapper();

    private String id;

    private String hash;

    private String name;

    private int age;
    
    private Set<String> tags;
    
    public String toJSON() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Map<String, Object> toMap() {

        return ImmutableMap.of(
                "name", name, 
                "age", age, 
                "tags", tags);
    }

}
