package coop.constellation.connectorservices.launchconnector.helpers;

import com.xtensifi.connectorservices.common.workflow.ConnectorResponse;
import com.xtensifi.connectorservices.common.workflow.ConnectorState;
import com.xtensifi.connectorservices.common.workflow.CufxMapper;
import com.xtensifi.cufx.*;
import com.xtensifi.custom.cufx.TransactionContainer;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class WorkflowHelpers {

    public static TransactionList getTransactionListFromConnectorState(ConnectorState connectorState) throws IOException {

        try {
            // get the accounts response, and find the account that matches the secondary Id
            Optional<ConnectorResponse> optGetTransactionsResponse = connectorState.getConnectorResponseList().getResponses().stream().filter(
                    connectorResponse -> connectorResponse.getConnectorRequestData().getMethod().equals("getTransactions")
            ).findFirst();

            if (optGetTransactionsResponse.isPresent() && optGetTransactionsResponse.get().getHttpStatus().equals("200")) {
                ConnectorResponse getTransactionsResponse = optGetTransactionsResponse.get();
                String response = getTransactionsResponse.getResponse();
                TransactionContainer transactionContainer = CufxMapper.convertToTransactionContainer(response);
                TransactionMessage transactionMessage = transactionContainer.getTransactionMessage();
                TransactionList transactionList = transactionMessage.getTransactionList();

                return transactionList;

            } else {
                throw new IOException("No successful getTransactions response in connector state.");
            }
        } catch (NullPointerException npEx){
            throw new IOException("Couldn't get account. Null pointer exception encountered.", npEx);
        }
    }

    /**
     * Turn custom data into a map of names to values
     * @param customData cufx cusotm data
     * @return map of names to values
     */
    public static Map<String, String> customDataToMap(CustomData customData){
        return customData.getValuePair().stream().collect(Collectors.toMap(
                ValuePair::getName,
                ValuePair::getValue,
                (vp1,vp2)->vp2
        ));
    }
}
