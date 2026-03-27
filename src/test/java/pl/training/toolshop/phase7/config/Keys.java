package pl.training.toolshop.phase7.config;

// Stale kluczy sesji — eliminuja hardkodowane stringi i zapobiegaja literowkom
// Toolshop: ULID IDs, JWT Bearer, koszyk/faktury/ulubione
public final class Keys {

  private Keys() {}

  public static final String ACCESS_TOKEN = "accessToken";
  public static final String PRODUCT_ID = "productId";
  public static final String PRODUCT_NAME = "productName";
  public static final String PRODUCT_PRICE = "productPrice";
  public static final String PRODUCT_IDS = "productIds";
  public static final String CART_ID = "cartId";
  public static final String INVOICE_ID = "invoiceId";
  public static final String FAVORITE_ID = "favoriteId";
  public static final String IS_AUTHENTICATED = "isAuthenticated";
  public static final String CURRENT_PAGE = "currentPage";
  public static final String HAS_MORE_PAGES = "hasMorePages";
}
