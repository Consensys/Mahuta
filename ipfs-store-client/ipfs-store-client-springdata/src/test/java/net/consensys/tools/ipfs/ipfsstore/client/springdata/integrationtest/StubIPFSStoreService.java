package net.consensys.tools.ipfs.ipfsstore.client.springdata.integrationtest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.github.tomakehurst.wiremock.WireMockServer;


public class StubIPFSStoreService {

    private static WireMockServer wireMockServer;
    public static final String index = "entity";
    public static final String hash = "QmWPCRv8jBfr9sDjKuB5sxpVzXhMycZzwqxifrZZdQ6K9o";
    public static final String id1 = "ABC";
    public static final String name1 = "Greg";
    public static final int age1 = 30;
    public static final String id2 = "DEF";
    public static final String name2 = "Isabelle";
    public static final int age2 = 28;

    static void start() {


        // MOCK
        String storeResponse =
                "{\n" +
                        "    \"hash\": \"" + hash + "\"\n" +
                        "}";
        String indexResponse =
                "{\n" +
                        "    \"index\": \"" + index + "\",\n" +
                        "    \"id\": \"" + id1 + "\",\n" +
                        "    \"hash\": \"" + hash + "\"\n" +
                        "}";

        String entity1Response =
                "{\n" +
                        "    \"name\": \"" + name1 + "\",\n" +
                        "    \"index\": " + age1 + ",\n" +
                        "    \"id\": \"" + id1 + "\",\n" +
                        "    \"hash\": \"" + hash + "\"\n" +
                        "}";
        String entity2Response =
                "{\n" +
                        "    \"name\": \"" + name2 + "\",\n" +
                        "    \"index\": " + age2 + ",\n" +
                        "    \"id\": \"" + id2 + "\",\n" +
                        "    \"hash\": \"" + hash + "\"\n" +
                        "}";
        String pageStore =
                "{\n" +
                        "    \"content\": [\n" +
                        entity1Response +
                        entity2Response +
                        "    ],\n" +
                        "    \"numberOfElements\": 2,\n" +
                        "    \"firstPage\": false,\n" +
                        "    \"lastPage\": true,\n" +
                        "    \"totalElements\": 2,\n" +
                        "    \"sort\": null,\n" +
                        "    \"totalPages\": 1,\n" +
                        "    \"size\": 10,\n" +
                        "    \"number\": 1\n" +
                        "}";


        wireMockServer = new WireMockServer(wireMockConfig().port(8040));
        wireMockServer.start();

        wireMockServer.addStubMapping(post(urlPathEqualTo("/ipfs-store/index"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(indexResponse)).build());

        wireMockServer.addStubMapping(post(urlPathEqualTo("/ipfs-store/store"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(storeResponse)).build());

        wireMockServer.addStubMapping(post(urlPathEqualTo("/ipfs-store/store_index"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(indexResponse)).build());

        wireMockServer.addStubMapping(get(urlPathEqualTo("/ipfs-store/fetch/entity/" + hash))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(entity1Response)).build());

        wireMockServer.addStubMapping(get(urlPathEqualTo("/ipfs-store/search/entity?page=1&size=2"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(pageStore)).build());
    }

    static void stop() {
        wireMockServer.stop();
    }
}