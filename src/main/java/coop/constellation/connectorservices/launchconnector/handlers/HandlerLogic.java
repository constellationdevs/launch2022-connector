package coop.constellation.connectorservices.launchconnector.handlers;


import java.util.Map;

import com.xtensifi.dspco.ConnectorMessage;
import com.xtensifi.dspco.UserData;

@FunctionalInterface
public interface HandlerLogic {
   
    void generateResponse(final Map<String, String> parms, UserData userData, ConnectorMessage cm) throws Exception;
}