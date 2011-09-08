package play.modules.chronostamp;

import play.PlayPlugin;
import play.classloading.ApplicationClasses.ApplicationClass;
 
 /**
 * Play plugin to extend Models with properties for tracking timestamp:
 * Date created_at
 * Date updated_at
 */
public class ChronostampPlugin extends PlayPlugin {
 
	@Override
	public void enhance(ApplicationClass appClass) throws Exception {
		new ChronostampEnhancer().enhanceThisClass(appClass);
	}
}
