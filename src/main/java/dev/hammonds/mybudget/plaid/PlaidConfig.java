package dev.hammonds.mybudget.plaid;

import com.plaid.client.ApiClient;
import com.plaid.client.request.PlaidApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class PlaidConfig {


  @Bean
  public PlaidApi plaidApi(PlaidProperties properties) {
  // build credentials map (hashmap)
    Map<String, String> apiKeys = new HashMap<>();
    apiKeys.put("clientId", properties.clientId());
    apiKeys.put("secret", properties.secret());
    
  // create the ApiClient
    ApiClient apiClient = new ApiClient(apiKeys);

  // call setPlaidAdapteraccept sandbox base url to start
    apiClient.setPlaidAdapter(ApiClient.Sandbox);

  // Return result of createService()
    PlaidApi plaidClient = apiClient.createService(PlaidApi.class);
    return plaidClient;
  }
}



