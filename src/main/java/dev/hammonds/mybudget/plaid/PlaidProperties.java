package dev.hammonds.mybudget.plaid;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "plaid")
public record PlaidProperties(String clientId,
                              String secret,
                              String environment) {}
