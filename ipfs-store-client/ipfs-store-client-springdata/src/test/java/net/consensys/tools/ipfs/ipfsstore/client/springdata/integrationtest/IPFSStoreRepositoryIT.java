package net.consensys.tools.ipfs.ipfsstore.client.springdata.integrationtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.consensys.tools.ipfs.ipfsstore.client.java.IPFSStore;
import net.consensys.tools.ipfs.ipfsstore.client.springdata.IPFSStoreRepository;
import net.consensys.tools.ipfs.ipfsstore.client.springdata.test.sample.Entity;
import net.consensys.tools.ipfs.ipfsstore.client.springdata.test.sample.Factory;
import net.consensys.tools.ipfs.ipfsstore.client.springdata.test.sample.TestRepository;

@RunWith(SpringJUnit4ClassRunner.class)
public class IPFSStoreRepositoryIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(IPFSStoreRepositoryIT.class);

    private String index;
    private Set<String> indexFields;
    private IPFSStoreRepository<Entity, String> underTest;

    @BeforeClass
    public static void setupEnvironment() throws IOException {
        //StubIPFSStoreService.start();
    }

    @Before
    public void setUp() throws InterruptedException {
        index = "entity";

        indexFields = new HashSet<>();
        indexFields.add("id");
        indexFields.add("name");
        indexFields.add("age");

        underTest = new TestRepository(new IPFSStore("http://localhost:8040"), index, indexFields, Entity.class);
    }

    @AfterClass
    public static void teardownEnvironment() {
        //StubIPFSStoreService.stop();
    }

    @Test
    public void run() throws InterruptedException {


        String id = UUID.randomUUID().toString();
        String name = "greg";
        int age = 10;

        Entity e = underTest.save(Factory.getEntity(id, name, age));
        assertEquals(id, e.getId());

        Thread.sleep(2000);

        Entity e1 = underTest.findOne(id);
        assertEquals(id, e1.getId());
        assertEquals(name, e1.getName());
        assertEquals(age, e1.getAge());


        Page<Entity> page = underTest.findAll(new PageRequest(0, 20));
        assertTrue(page.getTotalElements() > 0);


        Page<Entity> page2 = underTest.findByfullTextSearch("greg", new PageRequest(0, 20));
        assertTrue(page2.getTotalElements() > 0);

        e1.setAge(30);
        underTest.save(e1);


    }

}
