/******************************************************************************
 * Copyright © 2013-2016 The Nxt Core Developers.                             *
 *                                                                            *
 * See the AUTHORS.txt, DEVELOPER-AGREEMENT.txt and LICENSE.txt files at      *
 * the top-level directory of this distribution for the individual copyright  *
 * holder information and the developer policies on copyright and licensing.  *
 *                                                                            *
 * Unless otherwise agreed in a custom licensing agreement, no part of the    *
 * Nxt software, including this file, may be copied, modified, propagated,    *
 * or distributed except according to the terms contained in the LICENSE.txt  *
 * file.                                                                      *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************/

package dbn.http;

import dbn.Account;
import dbn.Attachment;
import dbn.Currency;
import dbn.DbnException;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

/**
 * Publish exchange offer for {@link dbn.CurrencyType#EXCHANGEABLE} currency
 * <p>
 * Parameters
 * <ul>
 * <li>currency - currency id of an active currency
 * <li>buyRateDQT - DBN amount for buying a currency unit specified in DQT
 * <li>sellRateDQT - DBN amount for selling a currency unit specified in DQT
 * <li>initialBuySupply - Initial number of currency units offered to buy by the publisher
 * <li>initialSellSupply - Initial number of currency units offered for sell by the publisher
 * <li>totalBuyLimit - Total number of currency units which can be bought from the offer
 * <li>totalSellLimit - Total number of currency units which can be sold from the offer
 * <li>expirationHeight - Blockchain height at which the offer is expired
 * </ul>
 *
 * <p>
 * Publishing an exchange offer internally creates a buy offer and a counter sell offer linked together.
 * Typically the buyRateDQT specified would be less than the sellRateDQT thus allowing the publisher to make profit
 *
 * <p>
 * Each {@link CurrencyBuy} transaction which matches this offer reduces the sell supply and increases the buy supply
 * Similarly, each {@link CurrencySell} transaction which matches this offer reduces the buy supply and increases the sell supply
 * Therefore the multiple buy/sell transaction can be issued against this offer during it's lifetime.
 * However, the total buy limit and sell limit stops exchanging based on this offer after the accumulated buy/sell limit is reached
 * after possibly multiple exchange operations.
 *
 * <p>
 * Only one exchange offer is allowed per account. Publishing a new exchange offer when another exchange offer exists
 * for the account, removes the existing exchange offer and publishes the new exchange offer
 */
public final class PublishExchangeOffer extends CreateTransaction {

    static final PublishExchangeOffer instance = new PublishExchangeOffer();

    private PublishExchangeOffer() {
        super(new APITag[] {APITag.MS, APITag.CREATE_TRANSACTION}, "currency", "buyRateDQT", "sellRateDQT",
                "totalBuyLimit", "totalSellLimit", "initialBuySupply", "initialSellSupply", "expirationHeight");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws DbnException {
        Currency currency = ParameterParser.getCurrency(req);
        long buyRateDQT = ParameterParser.getLong(req, "buyRateDQT", 0, Long.MAX_VALUE, true);
        long sellRateDQT= ParameterParser.getLong(req, "sellRateDQT", 0, Long.MAX_VALUE, true);
        long totalBuyLimit = ParameterParser.getLong(req, "totalBuyLimit", 0, Long.MAX_VALUE, true);
        long totalSellLimit = ParameterParser.getLong(req, "totalSellLimit", 0, Long.MAX_VALUE, true);
        long initialBuySupply = ParameterParser.getLong(req, "initialBuySupply", 0, Long.MAX_VALUE, true);
        long initialSellSupply = ParameterParser.getLong(req, "initialSellSupply", 0, Long.MAX_VALUE, true);
        int expirationHeight = ParameterParser.getInt(req, "expirationHeight", 0, Integer.MAX_VALUE, true);
        Account account = ParameterParser.getSenderAccount(req);

        Attachment attachment = new Attachment.MonetarySystemPublishExchangeOffer(currency.getId(), buyRateDQT, sellRateDQT,
                totalBuyLimit, totalSellLimit, initialBuySupply, initialSellSupply, expirationHeight);
        return createTransaction(req, account, attachment);
    }

}
