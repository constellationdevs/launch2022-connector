package coop.constellation.connectorservices.launchconnector.helpers;



import com.xtensifi.connectorservices.common.logging.ConnectorLogging;
import com.xtensifi.connectorservices.common.workflow.*;
import com.xtensifi.dspco.ConnectorMessage;
import lombok.AllArgsConstructor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@AllArgsConstructor
public class MockConnectorHubService implements ConnectorHubService {

    private Function<ConnectorState, ConnectorState> mockResponseLoader;
    private final ConnectorLogging logger;

    @Override
    public Function<ConnectorRequestParams, ConnectorState> callConnectorAsync() {
        return ConnectorState::new;
    }

    @Override
    public Function<ConnectorState, ConnectorState> callConnectorUsingState() {
        return connectorState -> connectorState;
    }

    @Override
    public Function<ConnectorState, ConnectorMessage> completeAsync() {
        return connectorState -> {
            connectorState.getConnectorMessage().setResponse(connectorState.getResponse());
            return connectorState.getConnectorMessage();
        };
    }

    @Override
    public Function<ConnectorMessage, ConnectorMessage> complete() {
        return connectorMessage -> connectorMessage;
    }

    @Override
    public Function<ConnectorState, ConnectorState> waitForConnectorResponse() {
        return state -> mockResponseLoader.apply(state);
    }

    @Override
    public TokenData callConnector(ConnectorRequestParams connectorRequestParams) throws ConnectorHubServiceException {
        return new TokenData("mock token");
    }

    @Override
    public ConnectorMessage complete(ConnectorState connectorState) throws ConnectorHubServiceException {
        return connectorState.getConnectorMessage();
    }

    @Override
    public ConnectorMessage complete(ConnectorMessage connectorMessage) throws ConnectorHubServiceException {
        return connectorMessage;
    }

    @Override
    public ResponseEntity<String> handleResponse(CloseableHttpResponse response) throws IOException {
        ResponseEntity<String> responseEntity;
        try {
            if (response.getStatusLine().getStatusCode() == 204) {
                responseEntity = ResponseEntity.status(204).body("");
            } else {
                String body = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : "";
                responseEntity = ResponseEntity.status(response.getStatusLine().getStatusCode()).body(body);
            }
        } finally {
            response.close();
        }

        return responseEntity;
    }

    @Override
    public CompletableFuture<ConnectorState> executeConnector(ConnectorMessage connectorMessage, ConnectorRequestData connectorRequestData) {
        Function<ConnectorRequestParams, ConnectorState> asyncConnectorRequest = this.callConnectorAsync();
        Function<ConnectorState, ConnectorState> pollAsyncConnectorRequest = this.waitForConnectorResponse();
        return this.initAsyncConnectorRequest(connectorMessage, connectorRequestData).thenApply(asyncConnectorRequest).thenApplyAsync(pollAsyncConnectorRequest);
    }

    @Override
    public CompletableFuture<ConnectorRequestParams> initAsyncConnectorRequest(ConnectorMessage connectorMessage, ConnectorRequestData connectorRequestData) {
        return CompletableFuture.supplyAsync(() -> {
            ConnectorRequestParams connectorRequestParams = new ConnectorRequestParams(connectorMessage, connectorRequestData);
            TokenData tokenData = new TokenData("mock token");
            connectorRequestParams.setWriterToken(tokenData);
            return connectorRequestParams;
        });
    }

    @Override
    public ConnectorState prepareNextConnector(ConnectorRequestData nextConnector, ConnectorState connectorState) {
        ConnectorRequestParams connectorRequestParams = new ConnectorRequestParams(connectorState.getConnectorRequestParams().getConnectorMessage(), nextConnector);
        connectorRequestParams.setWriterToken(connectorState.getConnectorRequestParams().getWriterToken());
        connectorState.setConnectorRequestParams(connectorRequestParams);
        return connectorState;
    }

    @Override
    public ConnectorState createConnectorState(ConnectorRequestData connectorRequestData, ConnectorMessage connectorMessage) {
        ConnectorRequestParams connectorRequestParams = new ConnectorRequestParams(connectorMessage, connectorRequestData);
        TokenData token = new TokenData("mock token");
        connectorRequestParams.setWriterToken(token);
        return new ConnectorState(connectorRequestParams);
    }

    @Override
    public ConnectorMessage handleAsyncFlowError(Throwable exception, ConnectorMessage connectorMessage, String additionalGeneralErrorMessage) {
        if (exception.getCause() instanceof ValidationException) {
            String var10000 = exception.getCause().getMessage();
            String detailedError = "[" + var10000 + "] Detailed Description [" + ((ValidationException)exception.getCause()).getDescription() + "]";
            logger.error(connectorMessage, "Validation Exception in Workflow connector " + additionalGeneralErrorMessage + " : " + detailedError);
            connectorMessage.setResponse("Validation Error: " + detailedError);
            connectorMessage.getResponseStatus().setStatus(((ValidationException)exception.getCause()).getMessage());
            connectorMessage.getResponseStatus().setStatusCode(((ValidationException)exception.getCause()).getStatus());
            connectorMessage.getResponseStatus().setStatusDescription(((ValidationException)exception.getCause()).getDescription());

            try {
                this.complete(connectorMessage);
                return connectorMessage;
            } catch (ConnectorHubServiceException var6) {
                logger.error(connectorMessage, "Serious unknown error during Workflow connector (" + additionalGeneralErrorMessage + ") : " + detailedError);
                throw new RuntimeException("Serious unknown error during Workflow connector (" + additionalGeneralErrorMessage + ") : " + exception);
            }
        } else {
            logger.error(connectorMessage, "Serious unknown error during Workflow connector (" + additionalGeneralErrorMessage + ") : " + exception);
            throw new RuntimeException("Serious unknown error during Workflow connector (" + additionalGeneralErrorMessage + ") : " + exception);
        }
    }
}


