package net.consensys.tools.ipfs.ipfsstore.client.springdata.test.sample;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class Factory {

    public static final String NAME = "Greg";
    public static final int AGE = 30;
    public static final String ID = "6a45be44986e4364897800853f117d0d";


    public static Entity getEntity() {
        return getEntity(null);
    }

    public static Entity getEntity(String id) {
        return getEntity(id, NAME, AGE);
    }

    public static Entity getEntity(String id, String name, int age) {
        Entity entity = new Entity();
        entity.setAge(age);
        entity.setName(name);
        entity.setId(id);

        return entity;
    }

    public static List<Entity> getEntities(int total) {
        List<Entity> entities = new ArrayList<Entity>();
        for (int i = 0; i < total; i++) entities.add(getEntity());

        return entities;
    }

    public static Page<Entity> getEntities(int total, int pageNo, int pageSize) {
        return getEntities(total, PageRequest.of(pageNo, pageNo));
    }

    public static Page<Entity> getEntities(int total, Pageable pageable) {
        return new PageImpl<Entity>(getEntities(pageable.getPageSize()), pageable, total);
    }
}
