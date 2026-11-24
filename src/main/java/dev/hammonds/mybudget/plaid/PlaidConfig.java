package dev.hammonds.mybudget.plaid;

import java.util.HashMap;

import com.plaid.client.ApiClient;
import com.plaid.client.request.PlaidApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlaidConfig {
  
  @Bean
  public PlaidApi plaidClient(PlaidProperties envVariables) {
    HashMap<String, String> apiKeys = new HashMap<String, String>();
    apiKeys.put("clientId", envVariables.clientId());
    apiKeys.put("secret", envVariables.secret());
    
    ApiClient apiClient = new ApiClient(apiKeys);
    apiClient.setPlaidAdapter(envVariables.baseUrl());
    PlaidApi client = apiClient.createService(PlaidApi.class);
    return client;
  }

}
