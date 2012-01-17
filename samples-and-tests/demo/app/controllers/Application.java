package controllers;

import play.mvc.Controller;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class Application extends Controller {

    public static void index() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        List<models.Article> articles = models.Article.findAll();

        // Since created_at and updated_at fields are injected at run time
        // they must be invoked by using reflection
//        Date created_at = (Date) PropertyUtils.getProperty(items.get(0), "created_at");
//        Date updated_at = (Date) PropertyUtils.getProperty(items.get(0), "updated_at");
//
//        Logger.info("created at: " + created_at.toString());
//        Logger.info("updated at: " + updated_at.toString());

        models.Quote quote = (models.Quote) models.Quote.findAll().get(0);

        render(articles, quote);
    }

}