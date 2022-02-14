package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.utils.SqlRuDateTimeParser;
import ru.job4j.html.SqlRuParse;
import ru.job4j.quartz.AlertRabbit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class SimpleGrab implements Grab {
    private static Properties configuration = new Properties();

    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException, InterruptedException {
        initConfig();

        scheduler.start();
        JobDataMap data = new JobDataMap();
        data.put("parse", parse);
        data.put("store", store);
        JobDetail job = newJob(SimpleGrab.SimpleJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(
                        Integer.parseInt(
                                configuration.getProperty("grab.interval")))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
        Thread.sleep(20000);
        scheduler.shutdown();
    }

    public static void initConfig() {
        try (InputStream in = AlertRabbit.class.getClassLoader()
                .getResourceAsStream("grab.properties")) {
            configuration.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SimpleGrab simpleGrab = new SimpleGrab();
        try {
            simpleGrab.init(
                    new SqlRuParse(new SqlRuDateTimeParser()),
                    new MemStore(),
                    StdSchedulerFactory.getDefaultScheduler());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class SimpleJob implements Job {

        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            Parse parse = (Parse) jobExecutionContext.getJobDetail().getJobDataMap().get("parse");
            Store store = (Store) jobExecutionContext.getJobDetail().getJobDataMap().get("store");

            Locale.setDefault(Locale.ENGLISH);

            parse.list("https://www.sql.ru/forum/job-offers/")
                    .forEach(store::save);
        }
    }
}
