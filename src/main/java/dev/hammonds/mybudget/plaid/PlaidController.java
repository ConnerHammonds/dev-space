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

import com.plaid.client.model.Products;
import com.plaid.client.model.CountryCode;
import com.plaid.client.model.LinkTokenCreateRequest;
import com.plaid.client.model.LinkTokenCreateRequestUser;
import com.plaid.client.model.LinkTokenCreateResponse;

public class PlaidController {

  

}
