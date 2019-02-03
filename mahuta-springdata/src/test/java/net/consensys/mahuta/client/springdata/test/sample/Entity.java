package net.consensys.mahuta.client.springdata.test.sample;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString @Getter @Setter
public class Entity {
    
    private static final ObjectMapper mapper = new ObjectMapper();

    private String id;

    private String hash;

    private String name;

    private int age;
    
    private Set<String> tags;
    
    public String toJSON() throws JsonProcessingException {
        return mapper.writeValueAsString(this);
    }
    
    public Map<String, Object> toMap() {

        return ImmutableMap.of(
                "name", name, 
                "age", age, 
                "tags", tags);
    }

}
