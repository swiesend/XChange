package com.xeiam.xchange.bter.service.polling;

import java.io.IOException;
import java.math.BigDecimal;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.bter.BTERUtils;
import com.xeiam.xchange.bter.dto.BTERBaseResponse;
import com.xeiam.xchange.bter.dto.BTEROrderType;
import com.xeiam.xchange.bter.dto.trade.BTEROpenOrders;
import com.xeiam.xchange.bter.dto.trade.BTEROrderStatus;
import com.xeiam.xchange.bter.dto.trade.BTERPlaceOrderReturn;
import com.xeiam.xchange.bter.dto.trade.BTERTradeHistoryReturn;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.trade.LimitOrder;

public class BTERPollingTradeServiceRaw extends BTERBasePollingService {

  /**
   * Constructor
   *
   * @param exchange
   */
  public BTERPollingTradeServiceRaw(Exchange exchange) {

    super(exchange);
  }

  /**
   * Submits a Limit Order to be executed on the BTER Exchange for the desired market defined by {@code CurrencyPair}. WARNING - BTER will return true
   * regardless of whether or not an order actually gets created. The reason for this is that orders are simply submitted to a queue in their
   * back-end. One example for why an order might not get created is because there are insufficient funds. The best attempt you can make to confirm
   * that the order was created is to poll {@link #getBTEROpenOrders}. However if the order is created and executed before it is caught in its open
   * state from calling {@link #getBTEROpenOrders} then the only way to confirm would be confirm the expected difference in funds available for your
   * account.
   *
   * @param limitOrder
   * @return String order id of submitted request.
   * @throws IOException
   */
  public String placeBTERLimitOrder(LimitOrder limitOrder) throws IOException {

    BTEROrderType type = (limitOrder.getType() == Order.OrderType.BID) ? BTEROrderType.BUY : BTEROrderType.SELL;

    return placeBTERLimitOrder(limitOrder.getCurrencyPair(), type, limitOrder.getLimitPrice(), limitOrder.getTradableAmount());
  }

  /**
   * Submits a Limit Order to be executed on the BTER Exchange for the desired market defined by {@code currencyPair}. WARNING - BTER will return true
   * regardless of whether or not an order actually gets created. The reason for this is that orders are simply submitted to a queue in their
   * back-end. One example for why an order might not get created is because there are insufficient funds. The best attempt you can make to confirm
   * that the order was created is to poll {@link #getBTEROpenOrders}. However if the order is created and executed before it is caught in its open
   * state from calling {@link #getBTEROpenOrders} then the only way to confirm would be confirm the expected difference in funds available for your
   * account.
   *
   * @param currencyPair
   * @param orderType
   * @param rate
   * @param amount
   * @return String order id of submitted request.
   * @throws IOException
   */
  public String placeBTERLimitOrder(CurrencyPair currencyPair, BTEROrderType orderType, BigDecimal rate, BigDecimal amount) throws IOException {

    String pair = String.format("%s_%s", currencyPair.baseSymbol, currencyPair.counterSymbol).toLowerCase();
    BTERPlaceOrderReturn orderId = bter.placeOrder(pair, orderType, rate, amount, apiKey, signatureCreator, exchange.getNonceFactory());

    return handleResponse(orderId).getOrderId();
  }

  public boolean cancelOrder(String orderId) throws IOException {

    BTERBaseResponse cancelOrderResult = bter.cancelOrder(orderId, apiKey, signatureCreator, exchange.getNonceFactory());

    return handleResponse(cancelOrderResult).isResult();
  }

  public BTEROpenOrders getBTEROpenOrders() throws IOException {

    BTEROpenOrders bterOpenOrdersReturn = bter.getOpenOrders(apiKey, signatureCreator, exchange.getNonceFactory());

    return handleResponse(bterOpenOrdersReturn);
  }

  public BTEROrderStatus getBTEROrderStatus(String orderId) throws IOException {

    BTEROrderStatus orderStatus = bter.getOrderStatus(orderId, apiKey, signatureCreator, exchange.getNonceFactory());

    return handleResponse(orderStatus);
  }

  public BTERTradeHistoryReturn getBTERTradeHistory(CurrencyPair currencyPair) throws IOException {

    BTERTradeHistoryReturn bterTradeHistoryReturn = bter.getUserTradeHistory(apiKey, signatureCreator, exchange.getNonceFactory(),
        BTERUtils.toPairString(currencyPair));

    return handleResponse(bterTradeHistoryReturn);
  }
}
