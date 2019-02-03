package net.consensys.mahuta.client.springdata.test.sample;
import java.util.Set;

import com.google.common.collect.Sets;

import net.andreinc.mockneat.MockNeat;

public final class Factory {
    private static final MockNeat mockNeat = MockNeat.threadLocal();

    public static final String TAG1 = "hello";
    public static final String TAG2 = "new";
    public static final String TAG3 = "world";


    public static Entity getEntity() {
        return getEntity(null);
    }

    public static Entity getEntity(String id) {
        return getEntity(id, mockNeat.names().get(), mockNeat.ints().range(18, 100).get());
    }

    public static Entity getEntity(String id, String name, int age) {
        return getEntity(id, name, age, Sets.newHashSet(TAG1, TAG2, TAG3));
    }

    public static Entity getEntity(String id, String name, int age, Set<String> tags) {
        Entity entity = new Entity();
        entity.setAge(age);
        entity.setName(name);
        entity.setId(id);
        entity.setTags(tags);

        return entity;
    }

}