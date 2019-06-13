package net.consensys.mahuta.client.springdata.test.sample;

import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.consensys.mahuta.springdata.annotation.Fulltext;
import net.consensys.mahuta.springdata.annotation.Hash;
import net.consensys.mahuta.springdata.annotation.IPFSDocument;
import net.consensys.mahuta.springdata.annotation.Indexfield;
import net.consensys.mahuta.springdata.utils.JsonIgnoreHashMixIn;

@ToString @Getter @Setter
@IPFSDocument(index = "entity", indexConfiguration = "index_mapping.json", indexContent = true)
public class Entity {
    
    private static final ObjectMapper mapper = new ObjectMapper();

    @Id
    private String id;

    @Hash
    private String hash;

    @Fulltext
    private String name;

    @JsonProperty("_age")
    @Indexfield("_age")
    private int age;

    @Fulltext("_tags")
    @JsonProperty("_tags")
    @Indexfield("_tags")
    private Set<String> tags;
    
    public String toJSON() throws JsonProcessingException {

        mapper.addMixIn(this.getClass(), JsonIgnoreHashMixIn.class);
        return mapper.writeValueAsString(this);
    }
    
    public Map<String, Object> toMap() {

        return ImmutableMap.of(
                "name", name, 
                "age", age, 
                "tags", tags);
    }

}
