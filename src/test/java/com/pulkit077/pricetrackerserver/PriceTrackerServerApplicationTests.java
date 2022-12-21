package com.pulkit077.pricetrackerserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.NumberFormat;
import java.util.Locale;

@SpringBootTest
class PriceTrackerServerApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void pTest() {
        String url = "https://www.flipkart.com/realme-10-pro-5g-dark-matter-128-gb/p/itm1e672d12a252e?pid=MOBGK8VHWBRKYFJD&lid=LSTMOBGK8VHWBRKYFJDTEBOFU&marketplace=FLIPKART&store=tyy%2F4io&srno=b_1_1&otracker=clp_bannerads_1_10.bannerAdCard.BANNERADS_Realme-10-Pro5g-sale-is-on_mobile-phones-big-saving-days-dec22-eidj8fs-store_I9339Z3O3SD2&fm=organic&iid=ef39580e-7142-4a08-a964-2d3e7c6f7078.MOBGK8VHWBRKYFJD.SEARCH&ppt=clp&ppn=mobile-phones-big-saving-days-dec22-eidj8fs-store&ssid=c71fmoj0800000001671645980747";

        try {
            Document doc = Jsoup
                    .connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.79 Safari/537.36")
                    .get();
            System.out.println("doc = " + doc);
//            Elements price = doc.select(".a-price-whole");
            Elements price = doc.select("div._30jeq3._16Jk6d");
            NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
            String priceText = price.first().text();
            priceText = priceText.substring(1);
            Integer currentPrice = numberFormat.parse(priceText).intValue();
            System.out.println(currentPrice);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
