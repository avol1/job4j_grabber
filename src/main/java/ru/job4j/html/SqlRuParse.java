package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.SqlRuDateTimeParser;
import ru.job4j.model.Post;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlRuParse {
    private static final int MAX_PAGES = 5;
    private static final String TIME_REGEX = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$";
    private static final SqlRuDateTimeParser PARSER = new SqlRuDateTimeParser();

    public static void main(String[] args) throws Exception {

        Locale.setDefault(Locale.ENGLISH);
        int currentPage = 1;

        while (currentPage <= MAX_PAGES) {
            Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers/" + currentPage).get();
            Elements row = doc.select("td[style].altCol");
            for (Element td : row) {
                Element parent = td.parent();
                System.out.println(createPost(parent));
            }
            currentPage++;
        }
    }

    private static Post createPost(Element element) throws IOException {
        String link = element.child(1).getElementsByIndexEquals(0).attr("href");
        String title = element.child(1).getElementsByIndexEquals(0).text();
        String description = "";
        LocalDateTime created = LocalDateTime.MIN;

        Document doc = Jsoup.connect(link).get();
        Elements rows = doc.select("td.msgBody");

        if (!rows.isEmpty()) {
            description = rows.get(0).parent().children().get(1).text();
        }

        rows = doc.select("td.msgFooter");

        if (!rows.isEmpty()) {
            Pattern pattern = Pattern.compile(TIME_REGEX);
            StringJoiner joiner = new StringJoiner(" ");
            String[] dateParts = rows.get(0).text().split(" ");

            for (String datePart : dateParts) {
                joiner.add(datePart);

                Matcher matcher = pattern.matcher(datePart);
                if (matcher.find()) {
                    break;
                }
            }
            created = PARSER.parse(joiner.toString());
        }

        return new Post(title, link, description, created);
    }
}
