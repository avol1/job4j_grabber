package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SqlRuParse {
    public static void main(String[] args) throws Exception {
        Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers").get();
        Elements row = doc.select("td[style].altCol");
        for (Element td : row) {
            Element parent = td.parent();
            System.out.println(parent.child(1).getElementsByIndexEquals(0).attr("href"));
            System.out.println(parent.child(1).getElementsByIndexEquals(0).text());
            System.out.println(parent.child(5).text());
        }
    }
}
