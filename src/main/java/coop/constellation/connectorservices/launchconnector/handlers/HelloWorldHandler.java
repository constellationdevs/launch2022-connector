package coop.constellation.connectorservices.launchconnector.handlers;

import java.util.Map;

import com.xtensifi.dspco.ConnectorMessage;
import com.xtensifi.dspco.UserData;

public class HelloWorldHandler implements HandlerLogic {

    @Override
    public void generateResponse(Map<String, String> parms, UserData userData, ConnectorMessage cm) throws Exception {
        String response = "{\"response\":  {\"Hello\":  \"Launch 2022\"}}";


        cm.setResponse(response);
        
    }

    
    
}
