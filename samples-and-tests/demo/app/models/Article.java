package models;

import play.db.jpa.Model;

import javax.persistence.Entity;

@Entity
public class Article extends Model {

    public String name;

    public Article(String name) {
        this.name = name;
    }
}
