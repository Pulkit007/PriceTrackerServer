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
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

@Service
public class ProductService {

    public static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    public static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");

    @Autowired
    private ProductRepository productRepository;

    public void trackAndSendReply(String senderNumber, String receiverNumber, String url) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        String message = addProductForTrackingAndGetMessage(url, senderNumber);

        Message.creator(
                new PhoneNumber(senderNumber),
                new PhoneNumber(receiverNumber),
                message
        ).create();
    }

    public String addProductForTrackingAndGetMessage(String url, String userNumber) {
        try {

            // Check is product is already present in the database and is getting tracked
            Product product = productRepository.findProductByUrl(url);
            if(product != null) {
                product.usersTracking.add(userNumber);
                productRepository.save(product);
                return ("Tracer initialize for user " + userNumber + " for Product " + product.name);
            }

            //loading the HTML to a Document Object
            Document doc = Jsoup
                    .connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .get();

            Elements productPriceSpan = doc.select(".a-price-whole");
            Elements productTitleSpan = doc.select("#productTitle");

            if(productTitleSpan.size() == 0) {
                return "Invalid Product URL, kindly send only the Amazon product URL.";
            }

            NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
            int currentPrice = 0;

            // Converting productPrice fetched from HTML document to a valid format
            if(productPriceSpan.size() != 0) {
                try {
                    currentPrice = numberFormat.parse(productPriceSpan.first().text()).intValue();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            String productTitle = productTitleSpan.first().text();

            //Making Product object with correct details;
            product = new Product(productTitle, url, currentPrice, userNumber);
            productRepository.save(product);

            return ("Tracker initialized for user " + userNumber + " for Product " + productTitle);
        } catch (IOException e) {
            e.printStackTrace();
            return "Invalid Product URL, kindly send only the Amazon product URL.";
        }
    }
}
