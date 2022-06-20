package coop.constellation.connectorservices.launchconnector.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.stereotype.Controller;

import com.xtensifi.connectorservices.common.logging.ConnectorLogging;
import com.xtensifi.dspco.ConnectorMessage;

import coop.constellation.connectorservices.launchconnector.handlers.HelloWorldHandler;
import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@Controller
@AllArgsConstructor
@RequestMapping("/externalConnector/LaunchConnector/1.0.0")
public class LaunchConnectorController extends ConnectorControllerBase {
    

    private final ConnectorLogging clog;
    private final HelloWorldHandler helloWorldHandler;

    /**
     * Following method is required in order for your controllers to pass health
     * checks.
     * If the server cannot call awsping and get the expected response yur app will
     * not be active.
     */
    @GetMapping("/awsping")
    public String getAWSPing() {
        return "{ping: 'pong'}";
    }

    @PostMapping(path = "/helloworld", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ConnectorMessage helloworld(@RequestBody ConnectorMessage connectorMessage) {
        clog.info(connectorMessage, "hello world hit");
        final ConnectorMessage response  = handleConnectorMessage("",connectorMessage, helloWorldHandler);

        return response;

    }
}