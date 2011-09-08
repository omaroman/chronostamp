package models;

import play.db.jpa.Model;
import play.modules.chronostamp.NoChronostamp;

import javax.persistence.Entity;

@Entity
@NoChronostamp
public class Quote extends Model {

    public String description;

    public Quote(String description) {
        this.description = description;
    }
}
