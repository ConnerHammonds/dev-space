/*
 * DB will proabably have an "Items" table. An item refers to a login
 * at an institution. One item could have several accounts (savings,checking,etc)
 *
 * My app will call Plaid's APi and receive a Link. That link will be forwarded to the client.
 * Link-token provides a public-token which can be exchanged for an access-token.
 *
 * The access-token is what the app will use to get information regarding that user's Item.
 */

package dev.hammonds.mybudget.plaid;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.plaid.client.request.PlaidApi;
import com.plaid.client.model.LinkTokenCreateRequest;
import com.plaid.client.model.LinkTokenCreateRequestUser;
import com.plaid.client.model.Products;
import com.plaid.client.model.CountryCode;
import com.plaid.client.model.LinkTokenCreateResponse;

import retrofit2.Response;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/plaid/")
public class PlaidController {

  private final PlaidApi client;

  //constructor that injects the client
  public PlaidController(PlaidApi client) {
    this.client = client;
  }

  @PostMapping("/link-token")
  String linkToken() {
    //Create params for request (will need to pull from db later)
    LinkTokenCreateRequestUser user = new LinkTokenCreateRequestUser()
      .clientUserId("test_id");
    
    List<Products> products = List.of(Products.TRANSACTIONS);
    List<CountryCode> countryCodes = List.of(CountryCode.US);

    //Create request
    LinkTokenCreateRequest request = new LinkTokenCreateRequest()
      .user(user)
      .clientName("MyBudget")
      .products(products)
      .countryCodes(countryCodes)
      .language("en");
   
    try {
      Response<LinkTokenCreateResponse> response = client
        .linkTokenCreate(request)
        .execute();
    
      if (response.isSuccessful() == true) {
          return response.body().getLinkToken();
      }
      else {
          return response.errorBody().string();
      }

      }

    catch (IOException e) {     //Eventually need to log the exception and return a server error code
      return "Network Failure";
    }

  }

}
