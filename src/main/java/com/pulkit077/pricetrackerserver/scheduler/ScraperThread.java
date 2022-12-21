package com.pulkit077.pricetrackerserver.scheduler;

import com.pulkit077.pricetrackerserver.model.Product;
import com.pulkit077.pricetrackerserver.repository.ProductRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

@Scope("prototype")
public class ScraperThread extends Thread{

    public static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    public static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    public static final String TWILIO_PHONE_NUMBER = System.getenv("TWILIO_PHONE_NUMBER");

    private Product product;
    private ProductRepository productRepository;
    public ScraperThread(Product product, ProductRepository productRepository) {
        this.product = product;
        this.productRepository = productRepository;
    }

    public void run() {
        System.out.println(product.name);
        try {
            //loading the HTML to a Document Object
            Document doc = Jsoup
                    .connect(product.url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.79 Safari/537.36")
                    .get();

            // Converting the string url to the proper format and checking the domain name for proper scraping
            URI uri = new URI(product.url);

            String host = uri.getHost();
            String domainName = host.startsWith("www.") ? host.substring(4) : host;

            Elements productPriceSpan = domainName.equals("flipkart.com") ?
                    doc.select("div._30jeq3._16Jk6d") :
                    doc.select(".a-price-whole");

            NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
            int currentPrice = 0;
            try {
                String price = productPriceSpan.first().text();
                if(domainName.equals("flipkart.com")) {
                    price = price.substring(1);
                }
                currentPrice = numberFormat.parse(price).intValue();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(product.prices.size() == 365) {
                product.prices.remove(0);
            }

            Boolean priceDropDetected = true;
            for(Integer price : product.prices) {
                if(price < currentPrice) {
                    priceDropDetected = false;
                    break;
                }
            }

            product.prices.add(currentPrice);
            productRepository.save(product);

            if(priceDropDetected) {
                Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

                for(String user : product.usersTracking) {
                    Message.creator(
                            new PhoneNumber(user),
                            new PhoneNumber(TWILIO_PHONE_NUMBER),
                            ("Price for your tracked product " + product.name + " has dropped to - " + currentPrice)
                    ).create();
                }
            }

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
