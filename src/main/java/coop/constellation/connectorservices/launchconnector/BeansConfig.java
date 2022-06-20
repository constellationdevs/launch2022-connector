package coop.constellation.connectorservices.launchconnector;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.xtensifi.connectorservices.common.logging.ConnectorLogging;

import coop.constellation.connectorservices.launchconnector.controller.BaseParamsSupplier;
import coop.constellation.connectorservices.launchconnector.helpers.EnhancedConnectorLogging;
import coop.constellation.connectorservices.launchconnector.helpers.StdoutConnectorLogging;

@Configuration
public class BeansConfig {
    
     // You can use this bean by running the app with a Spring profile called "local"
     @Bean
     @Profile("local")
     ConnectorLogging localConnectorLogging(){
         return new StdoutConnectorLogging();
     }
 
 
     @Bean
     @Profile("!local")
     ConnectorLogging connectorLogging(){
         return new EnhancedConnectorLogging();
     }
  /**
     * Set up extra params that this connector should use as a base for every request.
     */
    @Bean
    BaseParamsSupplier baseParamsSupplier(){
        return ()->
                Map.of(
                        "localCpConnectionInitSql", "SET TIME ZONE 'UTC';"
                );
    }

}
