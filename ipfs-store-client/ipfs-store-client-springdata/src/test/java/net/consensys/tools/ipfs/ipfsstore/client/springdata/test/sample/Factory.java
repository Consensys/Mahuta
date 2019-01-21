package net.consensys.tools.ipfs.ipfsstore.client.springdata.test.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import wiremock.com.google.common.collect.Sets;

public final class Factory {

    public static final String NAME = "Greg";
    public static final int AGE = 30;
    public static final String ID = "6a45be44986e4364897800853f117d0d";
    public static final String TAG1 = "hello";
    public static final String TAG2 = "new";
    public static final String TAG3 = "world";


    public static Entity getEntity() {
        return getEntity(null);
    }

    public static Entity getEntity(String id) {
        return getEntity(id, NAME, AGE);
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
