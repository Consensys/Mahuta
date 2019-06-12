package net.consensys.mahuta.client.springdata.test.sample;

import static com.monitorjbl.json.Match.match;

import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableMap;
import com.monitorjbl.json.JsonView;
import com.monitorjbl.json.JsonViewSerializer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.consensys.mahuta.springdata.annotation.Fulltext;
import net.consensys.mahuta.springdata.annotation.Hash;
import net.consensys.mahuta.springdata.annotation.IPFSDocument;
import net.consensys.mahuta.springdata.annotation.Indexfield;

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

        SimpleModule module = new SimpleModule();
        module.addSerializer(JsonView.class, new JsonViewSerializer());
        mapper.registerModule(module);
        
        JsonView<Entity> view = JsonView.with(this).onClass(this.getClass(), match().exclude("hash"));
        
        return mapper.writeValueAsString(view);
    }
    
    public Map<String, Object> toMap() {

        return ImmutableMap.of(
                "name", name, 
                "age", age, 
                "tags", tags);
    }

}
