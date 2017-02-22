package io.pivotal.jay.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestOperations;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

/**
 * Created by leec43 on 2/15/17.
 */
@RestController
public class RouteServiceController {

    static final String FORWARDED_URL = "X-CF-Forwarded-Url";

    static final String PROXY_METADATA = "X-CF-Proxy-Metadata";

    static final String PROXY_SIGNATURE = "X-CF-Proxy-Signature";

    private final static Logger logger = LoggerFactory.getLogger(Controller.class);

    private final RestOperations restOperations;

    //private RateLimiter rateLimiter;

    @Autowired
    public RouteServiceController(RestOperations restOperations) {
        this.restOperations = restOperations;
        //this.rateLimiter = rateLimiter;
    }

    @RequestMapping(headers = {FORWARDED_URL, PROXY_METADATA, PROXY_SIGNATURE})
    ResponseEntity<?> service(RequestEntity<byte[]> incoming, HttpServletRequest request) {

        String requestPath = incoming.getHeaders().getFirst(FORWARDED_URL);

        logger.info("Incoming Request: {}", incoming);
        logger.info("FORWARDED_URL: {}", incoming.getHeaders().getFirst(FORWARDED_URL));
        logger.info("Requested contextPath: {} ", requestPath.substring(incoming.getHeaders().getFirst(FORWARDED_URL).lastIndexOf("/")));

        if("/B".equals(requestPath.substring(incoming.getHeaders().getFirst(FORWARDED_URL).lastIndexOf("/"))))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);


//        if(rateLimiter.rateLimitRequest(incoming)){
//            logger.debug("Rate Limit imposed");
//            return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
//        };
        RequestEntity<?> outgoing = getOutgoingRequest(incoming);
        logger.info("Outgoing Request: {}", outgoing);

        return this.restOperations.exchange(outgoing, byte[].class);
    }

    private static RequestEntity<?> getOutgoingRequest(RequestEntity<?> incoming) {
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(incoming.getHeaders());
        URI uri = headers.remove(FORWARDED_URL).stream()
                .findFirst()
                .map(URI::create)
                .orElseThrow(() -> new IllegalStateException(String.format("No %s header present", FORWARDED_URL)));

        return new RequestEntity<>(incoming.getBody(), headers, incoming.getMethod(), uri);
    }
}
