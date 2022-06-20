package coop.constellation.connectorservices.launchconnector.helpers;

import com.xtensifi.connectorservices.common.logging.ConnectorLogging;
import com.xtensifi.dspco.ConnectorMessage;

/**
 * This can be used as the ConnectorLogging implementation for local development
 * by setting up a spring bean with it while in the "local" profile.
 */
public class StdoutConnectorLogging extends ConnectorLogging {
    @Override
    public void debug(ConnectorMessage connectorMessage, String message) {
        System.out.println(message);
    }

    @Override
    public void error(ConnectorMessage connectorMessage, String message) {
        System.out.println(message);
    }

    @Override
    public void fatal(ConnectorMessage connectorMessage, String message) {
        System.out.println(message);
    }

    @Override
    public void info(ConnectorMessage connectorMessage, String message) {
        System.out.println(message);
    }

    @Override
    public void trace(ConnectorMessage connectorMessage, String message) {
        System.out.println(message);
    }

    @Override
    public void warn(ConnectorMessage connectorMessage, String message) {
        System.out.println(message);
    }
}

