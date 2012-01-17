package jobs;

import models.Article;
import models.Quote;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class Bootstrap extends Job {

    @Override
    public void doJob() {
        if (Play.configuration.getProperty("application.mode").equals("dev")) {
            if (Article.count() == 0 ) {
                new Article("Genius").save();
                new Article("PS360").save();
                new Article("Sony Alpha 77").save();

                new Quote("There is a driving force more powerful than steam, electricity and nuclear energy: the will").save();
            }
        }
    }
}
