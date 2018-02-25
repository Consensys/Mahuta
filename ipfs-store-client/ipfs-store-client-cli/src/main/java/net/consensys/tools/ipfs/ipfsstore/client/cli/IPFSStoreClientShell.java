package net.consensys.tools.ipfs.ipfsstore.client.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.drapostolos.typeparser.TypeParser;

import net.consensys.tools.ipfs.ipfsstore.client.java.IPFSStore;
import net.consensys.tools.ipfs.ipfsstore.client.java.exception.IPFSStoreException;
import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;



@ShellComponent
public class IPFSStoreClientShell  {

    private static final Logger LOGGER = LoggerFactory.getLogger(IPFSStoreClientShell.class);
    
    private static final String NULL = "null";
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "8040";
    private static final String DEFAULT_SIZE = "10";
    
    private static final String ARGS_HOST = "--host";
    private static final String ARGS_PORT = "--port";
    private static final String ARGS_INPUT = "--input";
    private static final String ARGS_OUTPUT = "--output";
    private static final String ARGS_INDEX = "--index";
    private static final String ARGS_ID = "--id";
    private static final String ARGS_HASH = "--hash";
    private static final String ARGS_TYPE = "--content_type";
    private static final String ARGS_PAGE = "--page";
    private static final String ARGS_SIZE = "--size";
    private static final String ARGS_SORT = "--sort";
    private static final String ARGS_DIR = "--dir";
    private static final String ARGS_QUERY = "--query";
    
    private final ObjectMapper mapper ;
    
    @Autowired
    public IPFSStoreClientShell() { 
        mapper = new ObjectMapper();
    }

    
    
    @ShellMethod(value = "store", key = "store")
    public String store(
            @ShellOption(value=ARGS_HOST, defaultValue=DEFAULT_HOST) String host,
            @ShellOption(value=ARGS_PORT, defaultValue=DEFAULT_PORT) int port,
            @ShellOption(value=ARGS_INPUT) String input) throws IPFSStoreException {
        
        IPFSStore client = getClient(host, port);
        
        return client.store(input);
    }
    
    @ShellMethod(value = "index", key = "index")
    public String index(
            @ShellOption(value=ARGS_HOST, defaultValue=DEFAULT_HOST) String host,
            @ShellOption(value=ARGS_PORT, defaultValue=DEFAULT_PORT) int port,
            @ShellOption(value=ARGS_INPUT, defaultValue=NULL) String input,
            @ShellOption(value=ARGS_HASH, defaultValue=NULL) String hash,
            @ShellOption(value=ARGS_INDEX) String index,
            @ShellOption(value=ARGS_ID, defaultValue=NULL) String id,
            @ShellOption(value=ARGS_TYPE, defaultValue=NULL) String contentType) 
                    throws Exception {
        
        if(input.equals(NULL) && hash.equals(NULL)) {
            throw new Exception("--input OR --hash are expected");
        }
        
        IPFSStore client = getClient(host, port);
        
        if(!input.equals(NULL)) {
            
            InputStream is;
            try {
                is = new FileInputStream(input);
            } catch (FileNotFoundException e) {
                throw new Exception("Invalid --input: " + input, e);
            }
            
            return client.index(is, parseArgument(index), parseArgument(id), parseArgument(contentType));
            
        } else {
            return client.index(index, hash, id, contentType);
        } 
    }
    
    @ShellMethod(value = "fetch", key = "fetch")
    public String fetch(
            @ShellOption(value=ARGS_HOST, defaultValue=DEFAULT_HOST) String host,
            @ShellOption(value=ARGS_PORT, defaultValue=DEFAULT_PORT) int port,
            @ShellOption(value=ARGS_OUTPUT, defaultValue=NULL) String output,
            @ShellOption(value=ARGS_INDEX) String index,
            @ShellOption(value=ARGS_HASH) String hash) throws Exception {
        
        IPFSStore client = getClient(host, port);
        
        byte[] result = client.get(index, hash);
        
        if(parseArgument(output) == null) {
            return new String(result);
            
        } else {
            try {
                FileUtils.writeByteArrayToFile(new File(output), result);
                return null;
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                throw new Exception("Invalid output: " + output);
            }
        }
    }
    
    @ShellMethod(value = "search", key = "search")
    public String search(
            @ShellOption(value=ARGS_HOST, defaultValue=DEFAULT_HOST) String host,
            @ShellOption(value=ARGS_PORT, defaultValue=DEFAULT_PORT) int port,
            @ShellOption(value=ARGS_INDEX) String index,
            @ShellOption(value=ARGS_PAGE, defaultValue="1") int page,
            @ShellOption(value=ARGS_SIZE, defaultValue=DEFAULT_SIZE) int size,
            @ShellOption(value=ARGS_SORT, defaultValue=NULL) String sort,
            @ShellOption(value=ARGS_DIR, defaultValue="ASC") Sort.Direction dir,
            @ShellOption(value=ARGS_QUERY, defaultValue=NULL) String query) 
                    throws Exception {
        

        IPFSStore client = getClient(host, port);
        
        Page<Metadata> result = client.search(parseArgument(index), null, page, size, parseArgument(sort), dir);
        
        try {
          return mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
          LOGGER.error(e.getMessage(), e);
          throw new Exception(e.getMessage());
        }
    }
    

    private static IPFSStore getClient(String host, Integer port) {
        return new IPFSStore("http://"+host+":"+port);
    }

    private static String parseArgument(String arg) {
        arg = (arg.equals(NULL)) ? null : arg;
        return arg;
    }
    
}
