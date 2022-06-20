package coop.constellation.connectorservices.launchconnector.helpers;

import com.xtensifi.dspco.ConnectorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;

public interface ConnectorResponseEntityBuilder {
    ResponseEntity<ConnectorMessage> build(HttpStatus status, CompletableFuture<ConnectorMessage> message);
}
