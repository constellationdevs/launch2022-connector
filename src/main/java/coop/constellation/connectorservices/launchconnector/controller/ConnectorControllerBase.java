package coop.constellation.connectorservices.launchconnector.controller;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtensifi.connectorservices.common.logging.ConnectorLogging;
import com.xtensifi.connectorservices.common.workflow.ConnectorState;
import com.xtensifi.cufx.CustomData;
import com.xtensifi.cufx.TransactionList;
import com.xtensifi.cufx.ValuePair;
import com.xtensifi.dspco.ConnectorMessage;
import com.xtensifi.dspco.ConnectorParametersResponse;
import com.xtensifi.dspco.ExternalServicePayload;
import com.xtensifi.dspco.ResponseStatusMessage;
import com.xtensifi.dspco.UserData;

import coop.constellation.connectorservices.launchconnector.handlers.HandlerLogic;
import coop.constellation.connectorservices.launchconnector.handlers.WorkflowHandlerLogic;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConnectorControllerBase {

    private ObjectMapper objectMapper;
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");


    @Autowired
    public void setObjectMapper(ObjectMapper om){
        this.objectMapper = om;
    }

    private ConnectorLogging clog;
    @Autowired public void setConnectorLogging(ConnectorLogging cl){
        this.clog = cl;
    }



    private BaseParamsSupplier baseParamsSupplier;
    @Autowired public void setBaseParamsSupplier(BaseParamsSupplier supplier){
        this.baseParamsSupplier = supplier;
    }


    public Function<ConnectorState, ConnectorState> handleResponseEntity(WorkflowHandlerLogic handler) {
        return connectorState -> {
            ConnectorMessage connectorMessage = connectorState.getConnectorMessage();
            clog.info(connectorMessage, "inside handle response entity");

            final Map<String, String> allParams = getAllParams(connectorMessage, baseParamsSupplier.get());

            String response ="{}";
            try {
                response = handler.generateResponse(allParams, connectorState);
                clog.info(connectorMessage, "this is the final response " + response);

            } catch (Exception e) {
                clog.error(connectorState.getConnectorMessage(), e.getMessage());
            }

            connectorState.setResponse("{\"response\": " + response + "}");
            return connectorState;
        };
    }

    /**
     * Boilerplate method for handling the connector message
     * 
     * @param logPrefix     A prefix for log messages and stats reasons
     * @param connectorJson The raw JSON for the request connector message
     * @param handlerLogic  The custom logic for generating a response
     * @return a response connector message
     */
    ConnectorMessage handleConnectorMessage(final String logPrefix, final ConnectorMessage connectorMessage, final HandlerLogic handlerLogic) {
    
        
        ResponseStatusMessage responseStatusMessage = new ResponseStatusMessage();
        try {

            
            clog.info(connectorMessage, "this is the incoming json: " + objectMapper.writeValueAsString(connectorMessage));

            UserData userData = connectorMessage.getExternalServicePayload().getUserData();

            final Map<String, String> allParams = getAllParams(connectorMessage, baseParamsSupplier.get());

            // set default success message.
            responseStatusMessage.setStatus("Success");
            responseStatusMessage.setStatusCode("200");
            responseStatusMessage.setStatusDescription("Success");
            responseStatusMessage.setStatusReason(logPrefix + "Has responded.");

            handlerLogic.generateResponse(allParams, userData, connectorMessage);

        } catch (Exception ex) {
            clog.fatal(connectorMessage, "caught exception in controller base " + ex.getMessage());
            connectorMessage.setResponse("{}");

            responseStatusMessage = new ResponseStatusMessage() {
                {
                    setStatus("ERROR");
                    setStatusCode("500");
                    setStatusDescription("Failed");
                    setStatusReason(logPrefix + "Has Failed.");
                }
            };
        } finally {
            if (connectorMessage == null) {
                clog.warn(connectorMessage,
                        "Failed to create a connector message from the request, creating a new one for the response.");               
            }
            clog.info(new ConnectorMessage(), "setting final response status: " + responseStatusMessage.getStatus());
            connectorMessage.setResponseStatus(responseStatusMessage);
        }

        return connectorMessage;
    }



  


    /**
     * Get all the value pairs out of the connector message.
     * NOTE: if a name occurs more than once, only the first occurrance is returned.
     * @param connectorMessage the request connector message
     * @return a Map of the value pairs
     */
    public static Map<String, String> getAllParams(final ConnectorMessage connectorMessage, Map<String, String> baseParams) {
        final Map<String, String> allParams = new HashMap<>(baseParams);
        final ExternalServicePayload externalServicePayload = connectorMessage.getExternalServicePayload();
        final ConnectorParametersResponse connectorParametersResponse = connectorMessage.getConnectorParametersResponse();

        if (externalServicePayload != null) {
            final CustomData methodParams = externalServicePayload.getPayload();
            if(methodParams != null)
                for (ValuePair valuePair : methodParams.getValuePair()) {
                    allParams.putIfAbsent(valuePair.getName(), StringEscapeUtils.unescapeHtml4(valuePair.getValue()));
                }
        }
        if (connectorParametersResponse != null) {
            final CustomData otherParams = connectorParametersResponse.getParameters();
            if(otherParams != null) {
                for (ValuePair valuePair : otherParams.getValuePair()) {
                    allParams.putIfAbsent(valuePair.getName(), StringEscapeUtils.unescapeHtml4(valuePair.getValue()));
                }
            }
        }
        return allParams;
    }

    /**
     * This converts a platform, gregorian calendar date to a local date for the
     * purposes of comparing transaction dates for grouping and sorting.
     *
     * @return
     */
    static public LocalDate convertDateToLocalDate(TransactionList.Transaction transaction) {

        return transaction.getDateTimePosted().toGregorianCalendar().toZonedDateTime()
                .withZoneSameInstant(ZoneOffset.UTC).toLocalDate();
    }


    static public String getDateKeyString(Map.Entry<LocalDate, List<TransactionList.Transaction>> mapEntry) {
            return mapEntry.getKey().format(formatter);
        }

}

