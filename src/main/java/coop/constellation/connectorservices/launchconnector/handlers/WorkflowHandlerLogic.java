package coop.constellation.connectorservices.launchconnector.handlers;

import com.xtensifi.connectorservices.common.workflow.ConnectorState;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

/**
 * Interface for the custom logic to generate a response for a workflow method
 */
@FunctionalInterface
public interface WorkflowHandlerLogic {


    String generateResponse(final Map<String, String> parms, final ConnectorState connectorState) throws IOException, ParseException;


}

