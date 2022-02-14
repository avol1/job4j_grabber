package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.Parse;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.SqlRuDateTimeParser;
import ru.job4j.model.Post;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SqlRuParse implements Parse {
    private static final int MAX_PAGES = 5;
    private static final String JAVA_SEARCH_PATTERN = "^\\bjava\\b.*|.*\\bjava\\b.*|.*\\bjava$";

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        SqlRuParse parser = new SqlRuParse(new SqlRuDateTimeParser());
        System.out.println(parser.list("https://www.sql.ru/forum/job-offers/"));
    }

    private final DateTimeParser dateTimeParser;

    public SqlRuParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        int currentPage = 1;

        try {
            while (currentPage <= MAX_PAGES) {
                Document doc = Jsoup.connect(link + currentPage).get();
                Elements row = doc.select("td[style].altCol");
                for (Element td : row) {
                    Element parent = td.parent();
                    if (!parent.child(1).text().toLowerCase().matches(
                            JAVA_SEARCH_PATTERN)) {
                        continue;
                    }
                    String vacancyLink = parent.child(1).getElementsByIndexEquals(0).attr("href");
                    posts.add(detail(vacancyLink));
                }
                currentPage++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post detail(String link) {
        try {
            Document doc = Jsoup.connect(link).get();

            String title = doc.select(".messageHeader").get(0).ownText();
            String description = doc.select(".msgBody").get(1).text();
            String dateRaw = doc.select(".msgFooter").get(0).text();

            LocalDateTime created = dateTimeParser.parse(
                    dateRaw.substring(0, dateRaw.indexOf('[')).trim());

            return new Post(title, link, description, created);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
