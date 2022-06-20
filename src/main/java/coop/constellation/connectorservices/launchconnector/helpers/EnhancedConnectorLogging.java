package coop.constellation.connectorservices.launchconnector.helpers;

import com.xtensifi.connectorservices.common.logging.ConnectorLogging;
import com.xtensifi.cufx.CustomData;
import com.xtensifi.cufx.ValuePair;
import com.xtensifi.dspco.ConnectorMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;
    /**
     * A simple wrapper around ConnectorLogging that looks for global params and
     * application.properties settings: eclEnabled = true in the global params will
     * turn logging on without needing to redeploy connector connector.local=TRUE
     * will send messages to slf4j otherwise they will go to the original
     * ConnectorLogging
     * Error and Fatal always log
     */
    @Slf4j
    public class EnhancedConnectorLogging extends ConnectorLogging {

        private final boolean connectorLocal;

        /**
         *
         */
        public EnhancedConnectorLogging() {
            super();
            boolean connectorLocalFlag;
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream("application.properties")) {
                Properties properties = new Properties();
                properties.load(stream);

                connectorLocalFlag = properties.getProperty("connector.local", "FALSE").equalsIgnoreCase("TRUE");
            } catch (Exception ex) {
                connectorLocalFlag = false;
            }
            connectorLocal = connectorLocalFlag;
        }

        /**
         * This method enables the connector logging to be turned on or off with a
         * global connector param, 'eclEnabled' eclEnabled = true --> logging is turned
         * on. eclEnabled = false (or any other value) --> logging is turned off. If the
         * value pair is missing, logging is turned on by default
         *
         * @param connectorMessage
         */
        public boolean isEclEnabled(ConnectorMessage connectorMessage) {
            Boolean eclEnabled = false;
            try {
                CustomData params = connectorMessage.getConnectorParametersResponse().getParameters();

                List<ValuePair> list = params.getValuePair();
                ValuePair eclEnabledPair = list.stream()
                        .filter(val -> val.getName().equalsIgnoreCase("EnableConnectorLogging")).findFirst().orElse(null);
                if (eclEnabledPair != null) {
                    eclEnabled = eclEnabledPair.getValue().equalsIgnoreCase("true");
                }
            } catch (Exception e) {
                eclEnabled = false;
            }

            return eclEnabled;

        }

        /**
         *
         * @param connectorMessage
         * @param message
         */
        @Override
        public void debug(ConnectorMessage connectorMessage, String message) {
            if (isEclEnabled(connectorMessage)) {
                if (!connectorLocal) {
                    super.debug(connectorMessage, message);
                } else {
                    log.debug(message);
                }
            }
        }

        /**
         *
         * @param connectorMessage
         * @param message
         */
        @Override
        public void error(ConnectorMessage connectorMessage, String message) {

            if (!connectorLocal) {
                super.error(connectorMessage, message);
            } else {
                log.error(message);
            }

        }

        /**
         *
         * @param connectorMessage
         * @param message
         */
        @Override
        public void fatal(ConnectorMessage connectorMessage, String message) {

            if (!connectorLocal) {
                super.fatal(connectorMessage, message);
            } else {
                log.error(message);
            }

        }

        /**
         *
         * @param connectorMessage
         * @param message
         */
        @Override
        public void info(ConnectorMessage connectorMessage, String message) {
            if (isEclEnabled(connectorMessage)) {
                if (!connectorLocal) {
                    super.info(connectorMessage, message);
                } else {
                    log.info(message);
                }
            }
        }

        /**
         *
         * @param connectorMessage
         * @param message
         */
        @Override
        public void trace(ConnectorMessage connectorMessage, String message) {
            if (isEclEnabled(connectorMessage)) {
                if (!connectorLocal) {
                    super.trace(connectorMessage, message);
                } else {
                    log.trace(message);
                }
            }
        }

        /**
         *
         * @param connectorMessage
         * @param message
         */
        @Override
        public void warn(ConnectorMessage connectorMessage, String message) {
            if (isEclEnabled(connectorMessage)) {
                if (!connectorLocal) {
                    super.warn(connectorMessage, message);
                } else {
                    log.warn(message);
                }
            }
        }
    }
