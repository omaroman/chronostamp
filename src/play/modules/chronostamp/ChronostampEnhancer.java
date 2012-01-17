/**
 * Author: OMAROMAN
 * Date: 9/19/11
 * Time: 10:25 AM
 */

package play.modules.chronostamp;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.EnumMemberValue;
import net.parnassoft.playutilities.EnhancerUtility;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;

import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


public class ChronostampEnhancer extends Enhancer {

    private CtClass ctClass;
    private ConstPool constPool;
 
	@Override
	public void enhanceThisClass(ApplicationClass appClass) throws Exception {

        // Initialize member fields
        ctClass = makeClass(appClass);
        constPool = ctClass.getClassFile().getConstPool();

        if (!isEnhanceableThisClass()) {
            return; // Do NOT enhance this class
        }
        
        createChronostampFields();
        createMethodOnPreUpdate();
        createGettersMethods();

		// Done - Enhance Class.
		appClass.enhancedByteCode = ctClass.toBytecode();
		ctClass.defrost();
	}

    private boolean isEnhanceableThisClass() throws Exception {
        // Only enhance model classes.
        if (!EnhancerUtility.isAModel(classPool, ctClass)) {
            return false;
        }

        // Only enhance model classes with Entity annotation.
        if (!EnhancerUtility.isAnEntity(ctClass)) {
            return false;
        }

        // Skip enhance model classes if are annotated with @NoTracking
        if (isClassAnnotatedWithNoChronostamp()) {
            return false;
        }

        // Only enhance model classes without created_at and updated_at attributes
        if (EnhancerUtility.hasField(ctClass, "created_at") || EnhancerUtility.hasField(ctClass, "updated_at")) {
            return false;
        }

        return true;    // this class is enhanceable
    }

    private boolean isClassAnnotatedWithNoChronostamp() throws Exception {
        return EnhancerUtility.hasAnnotation(ctClass, NoChronostamp.class.getName());
    }
	
	private void createChronostampFields() throws CannotCompileException {
        EnumMemberValue enumValue;
        // ----- Add created_at property ----- 
        CtField created_at = CtField.make("private java.util.Date created_at = new java.util.Date();", ctClass);
        ctClass.addField(created_at);

        // ----- Add updated_at property -----
        CtField updated_at = CtField.make("private java.util.Date updated_at = new java.util.Date(created_at.getTime());", ctClass);
        ctClass.addField(updated_at);

        // ----- Add annotation @Temporal(TemporalType.TIMESTAMP) to created_at and updated_at fields -----
        AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        Annotation annotation = new Annotation(Temporal.class.getName(), constPool);
        enumValue = new EnumMemberValue(constPool);
        enumValue.setType(TemporalType.class.getName());
        enumValue.setValue("TIMESTAMP");
        annotation.addMemberValue("value", enumValue);
        attr.addAnnotation(annotation);

        created_at.getFieldInfo().addAttribute(attr);
        updated_at.getFieldInfo().addAttribute(attr);

//        // Annotate created_at & updated_at with @CRUD.Exclude
        attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        annotation = new Annotation(play.modules.crud.annotations.Exclude.class.getName(), constPool);
        attr.addAnnotation(annotation);
        created_at.getFieldInfo().addAttribute(attr);
        updated_at.getFieldInfo().addAttribute(attr);
    }

    private void createMethodOnPreUpdate() throws Exception {
        // ----- Add onPreUpdate() method -----

        // Check if there's a method annotated with @PreUpdate
        CtMethod methodWithPreUpdateAnnot = EnhancerUtility.getMethodAnnotatedWith(ctClass, PreUpdate.class.getName());

        if (methodWithPreUpdateAnnot != null) {
            methodWithPreUpdateAnnot.insertBefore("updated_at = new java.util.Date();");
        } else {
            // ----- Add onUpdate() method -----
            String code = "public void onPreUpdate() { updated_at = new java.util.Date(); }";
            final CtMethod onUpdate = CtMethod.make(code, ctClass);
            ctClass.addMethod(onUpdate);

            // ----- Add annotation @PreUpdate to onPreUpdate() method -----
            Annotation annotation = new Annotation(PreUpdate.class.getName(), constPool);
            AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
            attr.addAnnotation(annotation);

            onUpdate.getMethodInfo().addAttribute(attr);
        }
    }

    private void createGettersMethods() throws CannotCompileException {
        // ----- GETTERS -----

        String code;
        
        // ----- Add getCreated_at() method -----
        code = "public java.util.Date getCreated_at() { return this.created_at; }";
        final CtMethod getCreated_at = CtMethod.make(code, ctClass);
        ctClass.addMethod(getCreated_at);

        // ----- Add getCreatedAt() method -----
        code = "public java.util.Date getCreatedAt() { return this.created_at; }";
        final CtMethod getCreatedAt = CtMethod.make(code, ctClass);
        ctClass.addMethod(getCreatedAt);

        // ----- Add getUpdated_at() method -----
        code = "public java.util.Date getUpdated_at() { return this.updated_at; }";
        final CtMethod getUpdated_at = CtMethod.make(code, ctClass);
        ctClass.addMethod(getUpdated_at);

        // ----- Add getUpdatedAt() method -----
        code = "public java.util.Date getUpdatedAt() { return this.updated_at; }";
        final CtMethod getUpdatedAt = CtMethod.make(code, ctClass);
        ctClass.addMethod(getUpdatedAt);    
    }
	
//	/**
//     * THIS METHOD WAS DONATED TO PLAY!, NOW IT CLASHES (v1.2.4), SO IT'S NO NEEDED ANY LONGER
//	 * Test if a method has the provided annotation
//	 * @param ctMethod the javassist method representation
//	 * @param annotation fully qualified name of the annotation class eg."javax.persistence.Entity"
//	 * @return true if field has the annotation
//	 * @throws java.lang.ClassNotFoundException
//	 */
//    private boolean hasAnnotation(CtMethod ctMethod, String annotation) throws ClassNotFoundException {
//        for (Object object : ctMethod.getAvailableAnnotations()) {
//            java.lang.annotation.Annotation ann = (java.lang.annotation.Annotation) object;
//            if (ann.annotationType().getName().equals(annotation)) {
//                return true;
//            }
//        }
//        return false;
//    }
}
