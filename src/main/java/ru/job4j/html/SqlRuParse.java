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
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlRuParse implements Parse {
    private static final int MAX_PAGES = 5;
    private static final String TIME_REGEX = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$";

    public static void main(String[] args) throws Exception {

        Locale.setDefault(Locale.ENGLISH);

        int currentPage = 1;
        SqlRuParse parser = new SqlRuParse(new SqlRuDateTimeParser());

        while (currentPage <= MAX_PAGES) {
            System.out.println(parser.list("https://www.sql.ru/forum/job-offers/" + currentPage));
            currentPage++;
        }
    }

    private final DateTimeParser dateTimeParser;

    public SqlRuParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> posts = new ArrayList<>();

        Document doc = Jsoup.connect(link).get();
        Elements row = doc.select("td[style].altCol");
        for (Element td : row) {
            Element parent = td.parent();
            String vacancyLink = parent.child(1).getElementsByIndexEquals(0).attr("href");
            posts.add(detail(vacancyLink));
        }

        return posts;
    }

    @Override
    public Post detail(String link) throws IOException {
        Document doc = Jsoup.connect(link).get();
        String title = "";
        String description = "";
        LocalDateTime created = LocalDateTime.MIN;

        Elements rows = doc.select("td.messageHeader");
        if (!rows.isEmpty()) {
            title = rows.get(0).text();
        }

        rows = doc.select("td.msgBody");
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
            created = dateTimeParser.parse(joiner.toString());
        }

        return new Post(title, link, description, created);
    }
}
