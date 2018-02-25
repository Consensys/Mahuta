//package net.consensys.tools.ipfs.ipfsstore.client.cli;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Arrays;
//
//import org.apache.commons.io.FileUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Sort;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.github.drapostolos.typeparser.TypeParser;
//
//import net.consensys.tools.ipfs.ipfsstore.client.cli.model.Operation;
//import net.consensys.tools.ipfs.ipfsstore.client.java.IPFSStore;
//import net.consensys.tools.ipfs.ipfsstore.client.java.exception.IPFSStoreException;
//import net.consensys.tools.ipfs.ipfsstore.dto.Metadata;
//
//
//
//@SpringBootApplication
//public class IPFSStoreClientCLI  implements ApplicationRunner {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(IPFSStoreClientCLI.class);
//    private static final String DEFAULT_HOST = "localhost";
//    private static final int    DEFAULT_PORT = 8040;
//    private static final int    DEFAULT_SIZE = 10;
//    
//    private static final String ARGS_HOST = "host";
//    private static final String ARGS_PORT = "port";
//    private static final String ARGS_INPUT = "input";
//    private static final String ARGS_OUTPUT = "output";
//    private static final String ARGS_INDEX = "index";
//    private static final String ARGS_ID = "id";
//    private static final String ARGS_HASH = "hash";
//    private static final String ARGS_TYPE = "content_type";
//    private static final String ARGS_PAGE = "page";
//    private static final String ARGS_SIZE = "size";
//    private static final String ARGS_SORT = "sort";
//    private static final String ARGS_DIR = "dir";
//    private static final String ARGS_QUERY = "query";
//    
//    private final TypeParser parser ;
//    private final ObjectMapper mapper ;
//    
//    @Autowired
//    public IPFSStoreClientCLI() { 
//        parser = TypeParser.newBuilder().build();
//        mapper = new ObjectMapper();
//    }
//
//    public static void main(String... args) throws Exception {
//        SpringApplication app = new SpringApplication(IPFSStoreClientCLI.class);
//        app.setWebEnvironment(false); 
//        app.run(args);
//    }
// 
//    /**
//     * 
//     * cli store --input=<file path> --host==xxx --port=8084
//     * cli index --index=documents --hash=Qxxx --id=hello_doc --content_type=application --author=gregoire --title=Hello doc
//     * cli index  --input=<file path> --index=documents --id=hello_doc --content_type=application --author=gregoire --title=Hello doc
//     * cli get --index=documents --hash=Qxxx --output=xxx
//     * cli search --index=documents --page=1 --size=10 --sort=id dir=ASC --query=""
//     * 
//     */
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        LOGGER.info("Application started with command-line arguments: {}", Arrays.toString(args.getSourceArgs()));
//        
//        try {
//            execute(args);
//        }catch (Exception e) {
//            LOGGER.error(e.getMessage());
//        }
//    }
//    
//    /**
//     * Execute the command
//     * 
//     * @param args  Application Arguments 
//     * 
//     * @throws ArgumentNotFoundException
//     * @throws IPFSStoreException
//     * @throws InvalidArgumentException
//     * @throws UnexpectedException 
//     */
//    private void execute(ApplicationArguments args) throws ArgumentNotFoundException, IPFSStoreException, InvalidArgumentException, UnexpectedException {
//        
//        // Variables
//        Operation operation = null;
//        String output = null;
//        String input = null;
//        String hash = null;
//        String index = null;
//        String id = null;
//        String contentType = null;
//        Integer page = null;
//        Integer size = null;
//        String sort = null;
//        Sort.Direction dir = null;
//        String query = null;
//        
//        
//        
//        // ########################
//        // Operation
//        if(args.getNonOptionArgs() == null || args.getNonOptionArgs().size() == 0) {
//            throw new InvalidArgumentException("No operation, use --help");
//        }
//
//        try {
//            operation = Operation.valueOf(args.getNonOptionArgs().get(0));
//        } catch (IllegalArgumentException ex) {
//            throw new InvalidArgumentException("Unknow operation: " +  args.getNonOptionArgs().get(0));
//        }
//        LOGGER.debug("operation="+operation);
//
//        
//        
//        // ########################
//        // Endpoint
//        String host = parseArgs(args, ARGS_HOST, String.class, false, DEFAULT_HOST);
//        Integer port = parseArgs(args, ARGS_PORT, Integer.class, false, DEFAULT_PORT);
//        // Generate the client
//        IPFSStore client = getClient(host, port);
//        
//
//        
//        switch (operation) {
//
//        // ########################
//        // store
//        case store:
//            input   = parseArgs(args, ARGS_INPUT, String.class);
//            
//            hash    = client.store(input);
//            LOGGER.info("hash="+hash);
//            break;
//
//        // ########################
//        // index 
//        case index:
//            input       = parseArgs(args, ARGS_INPUT, String.class, false);
//            hash        = parseArgs(args, ARGS_HASH, String.class, false);
//            index       = parseArgs(args, ARGS_INDEX, String.class);
//            id          = parseArgs(args, ARGS_ID, String.class);
//            contentType = parseArgs(args, ARGS_TYPE, String.class);
//            
//            if(input == null && hash == null) {
//                throw new InvalidArgumentException("--input OR --hash are expected");
//            }
//
//            String idReturned = null;
//            if(input != null) {
//                
//                InputStream is;
//                try {
//                    is = new FileInputStream(input);
//                } catch (FileNotFoundException e) {
//                    LOGGER.error(e.getMessage(), e);
//                    throw new InvalidArgumentException("Invalid input: " + input);
//                }
//                
//                idReturned = client.index(is,  index, id, contentType);
//            } else {
//                idReturned = client.index( index, hash, id, contentType);
//            }
//            
//            LOGGER.info("id="+idReturned);
//            break;
//
//        // ########################
//        // fetch 
//        case fetch:
//            output       = parseArgs(args, ARGS_OUTPUT, String.class, false);
//            index       = parseArgs(args, ARGS_INDEX, String.class);
//            hash        = parseArgs(args, ARGS_HASH, String.class);
//            
//            byte[] result = client.get(index, hash);
//            
//            if(output == null) {
//                LOGGER.info("result="+new String(result));
//            } else {
//                try {
//                    FileUtils.writeByteArrayToFile(new File(output), result);
//                } catch (IOException e) {
//                    LOGGER.error(e.getMessage(), e);
//                    throw new InvalidArgumentException("Invalid output: " + output);
//                }
//            }
//            
//            break;
//
//            // ########################
//            // search 
//            case search:
//                index       = parseArgs(args, ARGS_INDEX, String.class);
//                page        = parseArgs(args, ARGS_PAGE, Integer.class, false, 1);
//                size        = parseArgs(args, ARGS_SIZE, Integer.class, false, DEFAULT_SIZE);
//                sort        = parseArgs(args, ARGS_SORT, String.class, false);
//                dir         = parseArgs(args, ARGS_DIR, Sort.Direction.class, false, Sort.Direction.ASC);
//                query       = parseArgs(args, ARGS_QUERY, String.class, false);
//
//                Page<Metadata> resultPage = client.search(index, null, page.intValue(), size.intValue(), sort, dir);
//
//            try {
//                LOGGER.info("result="+mapper.writeValueAsString(resultPage));
//            } catch (JsonProcessingException e) {
//                LOGGER.error(e.getMessage(), e);
//                throw new UnexpectedException(e.getMessage());
//            }
//                
//                break;
//
//        default:
//            LOGGER.error("Operation ["+operation+"] not supported");
//            break;
//        }
//        
//    }
//
//    /**
//     * Parse a mandatory attribute
//     * 
//     * @param args      Application Arguments
//     * @param argument  Argument name
//     * @param type      Type of the argument
//     * @return          Argument value
//     * 
//     * @throws ArgumentNotFoundException
//     */
//    private <T> T parseArgs(ApplicationArguments args, String argument, Class<T> type) throws ArgumentNotFoundException {
//        return this.parseArgs(args, argument, type, true);
//    }
//    
//    /**
//     * Parse an attribute
//     * 
//     * @param args      Application Arguments
//     * @param argument  Argument name
//     * @param type      Type of the argument
//     * @param mandatory throw an exception if mandatory and argument is not passed, return null if optional
//     * @return          Argument value
//     * 
//     * @throws ArgumentNotFoundException
//     */
//    private <T> T parseArgs(ApplicationArguments args, String argument, Class<T> type, boolean mandatory) throws ArgumentNotFoundException {
//        return this.parseArgs(args, argument, type, mandatory, null);
//    }
//    
//    /**
//     * Parse an attribute
//     * 
//     * @param args          Application Arguments
//     * @param argument      Argument name
//     * @param type          Type of the argument
//     * @param mandatory     throw an exception if mandatory and argument is not passed, return defaultValue if optional
//     * @param defaultValue  Default value if the argument is not passed
//     * @return              Argument value
//     * 
//     * @throws ArgumentNotFoundException
//     */
//    private <T> T parseArgs(ApplicationArguments args, String argument, Class<T> type, boolean mandatory, T defaultValue) throws ArgumentNotFoundException {
//        LOGGER.trace("parsing argument ["+argument+"] type ["+type.getName()+"]");
//        
//        if(!args.containsOption(argument)) {
//            LOGGER.warn("No argument '"+argument+"'");
//            
//            if(mandatory) {
//                throw new ArgumentNotFoundException(argument);
//            } else {
//                return defaultValue;
//            }
//        }
//        
//        T value = parser.parse(args.getOptionValues(argument).get(0), type);
//        LOGGER.debug("parsing argument ["+argument+"] type ["+type.getName()+"] --> " + value);
//        
//        return value;
//    }
//    
//    private static IPFSStore getClient(String host, Integer port) {
//        return new IPFSStore("http://"+host+":"+port);
//    }
//    
//    
//    
//    class ArgumentNotFoundException extends Exception {
//
//        private static final long serialVersionUID = -7821471654539522507L;
//        
//        public ArgumentNotFoundException(String argument) {
//            super("Argument ["+argument+"] not found");
//        }
//        
//    }
//    class InvalidArgumentException extends Exception {
//
//        private static final long serialVersionUID = -7821471654539522507L;
//        
//        public <T> InvalidArgumentException(String argument, T value) {
//            super("Argument ["+argument+"]="+value+" invalid");
//        }
//        public InvalidArgumentException(String message) {
//            super(message);
//        }
//        
//    }
//    class UnexpectedException extends Exception {
//
//        private static final long serialVersionUID = -7821471654539522507L;
//        
//        public UnexpectedException(String message) {
//            super(message);
//        }
//        
//    }
//    
//}
