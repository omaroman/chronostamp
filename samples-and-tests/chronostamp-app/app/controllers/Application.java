package controllers;

import play.mvc.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import models.*;

public class Application extends Controller {

    public static void index() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        List<Article> articles = Article.findAll();

        // Since created_at and updated_at fields are injected at run time
        // they must be invoked by using reflection
//        Date created_at = (Date) PropertyUtils.getProperty(items.get(0), "created_at");
//        Date updated_at = (Date) PropertyUtils.getProperty(items.get(0), "updated_at");
//
//        Logger.info("created at: " + created_at.toString());
//        Logger.info("updated at: " + updated_at.toString());

        Quote quote = (Quote) Quote.findAll().get(0);

        render(articles, quote);
    }

}