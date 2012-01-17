import org.apache.commons.beanutils.PropertyUtils;
import org.junit.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import play.test.*;
import models.*;

public class BasicTest extends UnitTest {

    @Before
    public void setup() {
        Fixtures.deleteAllModels();
        Fixtures.loadModels("data.yml");
    }

    @Test
    public void testMagicTimestampFields() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Article article = (Article) Article.findAll().get(0);
        assertNotNull(PropertyUtils.getProperty(article, "created_at"));
        assertNotNull(PropertyUtils.getProperty(article, "updated_at"));

        assertNotSame(new Date().getTime(), ((Date)PropertyUtils.getProperty(article, "created_at")).getTime());
        assertNotSame(new Date().getTime(), ((Date)PropertyUtils.getProperty(article, "updated_at")).getTime());

        assertEquals(((Date)PropertyUtils.getProperty(article, "created_at")).getTime(), ((Date)PropertyUtils.getProperty(article, "updated_at")).getTime());
        article.name = "Logitech";
        article.save();
        assertNotSame(((Date)PropertyUtils.getProperty(article, "created_at")).getTime(), ((Date)PropertyUtils.getProperty(article, "updated_at")).getTime());
    }

    @After
    public void doTestMagicTimestampFields() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Article article = (Article) Article.findAll().get(0);

        // Since created_at and updated_at fields are injected at run time
        // they must be invoked by using reflection
        Date created_at = (Date) PropertyUtils.getProperty(article, "created_at");
        Date updated_at = (Date) PropertyUtils.getProperty(article, "updated_at");
    }
}
