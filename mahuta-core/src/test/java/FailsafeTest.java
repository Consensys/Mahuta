import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import io.ipfs.api.IPFS;
import io.ipfs.multihash.Multihash;
import lombok.extern.slf4j.Slf4j;
import net.consensys.mahuta.core.service.storage.ipfs.IPFSService;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.Timeout;
import net.jodah.failsafe.TimeoutExceededException;

@Slf4j
public class FailsafeTest {
    
    @Test(expected = TimeoutExceededException.class)
    public void timeoutUsingThreadSleep() {
        
        Timeout<Object> timeoutPolicy = Timeout.of(Duration.ofSeconds(3));
        
        Failsafe.with(timeoutPolicy)
            .onComplete(e -> log.info("event completed after {} attempts", e.getAttemptCount()))
            .onFailure(e -> log.info("event failed (attempt: {})", e.getAttemptCount(), e.getFailure()))
            .run(() -> {
                log.info("[START]");
                Thread.sleep(4000);
                log.info("[END]");
            });
    }
    
    @Test(expected = TimeoutExceededException.class)
    public void timeoutWithHTTPCall() {
        
        Timeout<Object> timeoutPolicy = Timeout.of(Duration.ofSeconds(3))
                //.withCancel(false)
                .onFailure(e->log.info("timeout failure", e.getFailure()))
                .onSuccess(e -> log.info("timeout success"));
        
        Failsafe.with(timeoutPolicy)
            .onComplete(e -> log.info("event completed"))
            .onFailure(e -> log.info("event failed", e.getFailure()))
            .run(() -> {
                log.info("[START]");

                URL target = new URL("https://www.urlthatdonotexisteqeqeq.com");
                HttpURLConnection conn = (HttpURLConnection) target.openConnection();
                conn.setRequestMethod("GET");

                InputStream in = conn.getInputStream();

                String content = IOUtils.toString(in, StandardCharsets.UTF_8.name());
                log.info("[END] content={}", content);
            });

    }
    
    @Test
    public void testIpfs() throws IOException {
        log.info("[START]");
        IPFS ipfs = new IPFS("localhost", 5001);
        byte[] content = ipfs.cat(Multihash.fromBase58("QmSVudzGkE4wLw6ppThaTtHGeHnzfaVte9uPaWCxzwybfa"));
        log.info("[END] content: {}", new String(content));
    }
    
    @Test
    public void testIpfs2() throws IOException {
        log.info("[START]");
        IPFSService ipfs = IPFSService.connect();
        OutputStream os = ipfs.read("QmSVudzGkE4wLw6ppThaTtHGeHnzfaVte9uPaWCxzwybfa");
        log.info("[END] content: {}", os.toString());
    }
    
    @Test
    public void testIpfs3() throws IOException {
        int[] i = {0};
        IPFSService ipfs = IPFSService.connect();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            
            @Override
            public void run() {
                log.info("[START] i={}", i[0]++);
                OutputStream os = ipfs.read("QmSVudzGkE4wLw6ppThaTtHGeHnzfaVte9uPaWCxzwybfa");
                log.info("[END] content: {}", os.toString());
            }
        }, 0, 60000);
        
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                System.out.println("InterruptedException Exception" + e.getMessage());
            }
        }
        
    }
    
    
    
    
}

