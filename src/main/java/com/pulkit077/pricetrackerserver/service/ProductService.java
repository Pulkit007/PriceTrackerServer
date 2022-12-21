package com.pulkit077.pricetrackerserver.service;

import com.pulkit077.pricetrackerserver.model.Product;
import com.pulkit077.pricetrackerserver.repository.ProductRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

@Service
public class ProductService {

    // Twilio configurations and some constants
    public static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    public static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    public static final String INVALID_MESSAGE = "Invalid Product URL, kindly send only flipkart or amazon product URL.";

    @Autowired
    private ProductRepository productRepository;

    public void trackAndSendReply(String senderNumber, String receiverNumber, String url) {
        //Twilio Config
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        // message received after tracking
        String message = addProductForTrackingAndGetMessage(url, senderNumber);
        System.out.println("message = " + message);

        // Message created and send to the user
        Message.creator(
                new PhoneNumber(senderNumber),
                new PhoneNumber(receiverNumber),
                message
        ).create();
    }

    public String addProductForTrackingAndGetMessage(String url, String userNumber) {

        // Check is product is already present in the database and is getting tracked
        Product product = productRepository.findProductByUrl(url);
        if(product != null) {
            product.usersTracking.add(userNumber);
            productRepository.save(product);
            return ("Tracer initialize for user " + userNumber + " for Product " + product.name);
        }

        // Converting the string url to the proper format and checking the domain name for proper scraping
        URI uri;
        try {
            uri = new URI(url);
        }  catch (URISyntaxException e) {
            e.printStackTrace();
            return INVALID_MESSAGE;
        }

        String host = uri.getHost();
        String domainName = host.startsWith("www.") ? host.substring(4) : host;

        // Domains can "amazon.in" or "flipkart.com"
        if(domainName.equals("flipkart.com")) {
            return fetchFromFlipkart(url, userNumber);
        } else {
            return fetchFromAmazon(url, userNumber);
        }

    }

    public String fetchFromAmazon(String url, String userNumber) {
        try {
            //loading the HTML to a Document Object
            Document doc = Jsoup
                    .connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.79 Safari/537.36")
                    .get();

            // Scraping the required fields from the document
            Elements productPriceSpan = doc.select(".a-price-whole");
            Elements productTitleSpan = doc.select("#productTitle");

            if(productTitleSpan.size() == 0) {
                return INVALID_MESSAGE;
            }

            // Converting the price scraped from the document to a proper format
            NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
            int currentPrice = 0;

            if(productPriceSpan.size() != 0) {
                try {
                    String priceText = productPriceSpan.first().text();
                    currentPrice = numberFormat.parse(priceText).intValue();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            String productTitle = productTitleSpan.first().text();

            //Making Product object with correct details;
            Product product = new Product(productTitle, url, currentPrice, userNumber);
            productRepository.save(product);

            return ("Tracker initialized for user " + userNumber + " for Product " + productTitle);

        } catch (IOException e) {
            e.printStackTrace();
            return INVALID_MESSAGE;
        }
    }

    public String fetchFromFlipkart(String url, String userNumber) {
        try {
            //loading the HTML to a Document Object
            Document doc = Jsoup
                    .connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.79 Safari/537.36")
                    .get();

            // Scraping the required fields from the document
            Elements productPriceSpan = doc.select("div._30jeq3._16Jk6d");
            Elements productTitleSpan = doc.select(".B_NuCI");

            if(productTitleSpan.size() == 0) {
                return INVALID_MESSAGE;
            }

            // Converting the price scraped from the document to a proper format
            NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
            int currentPrice = 0;

            if(productPriceSpan.size() != 0) {
                try {
                    String priceText = productPriceSpan.first().text();
                    priceText = priceText.substring(1);
                    currentPrice = numberFormat.parse(priceText).intValue();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            String productTitle = productTitleSpan.first().text();

            //Making Product object with correct details;
            Product product = new Product(productTitle, url, currentPrice, userNumber);
            productRepository.save(product);

            return ("Tracker initialized for user " + userNumber + " for Product " + productTitle);

        } catch (IOException e) {
            e.printStackTrace();
            return INVALID_MESSAGE;
        }
    }
}
