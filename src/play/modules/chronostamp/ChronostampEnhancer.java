package play.modules.chronostamp;

import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;

import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;

import play.Logger;
 
import java.util.Date;
 

public class ChronostampEnhancer extends Enhancer {
 
	@Override
	public void enhanceThisClass(ApplicationClass appClass) throws Exception {
		
		CtClass ctClass = makeClass(appClass);
		
		ConstPool constpool = ctClass.getClassFile().getConstPool();
		AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
		Annotation annot = null;
		
		String code = null;
		EnumMemberValue enumValue = null;

		// Only enhance model classes.
		if (!ctClass.subtypeOf(classPool.get("play.db.jpa.JPABase"))) {
			return;
		}

		// Only enhance model classes with Entity annotation.
		if (!hasAnnotation(ctClass, "javax.persistence.Entity")) {
			return;
		}
		
		// Skip enhance model classes if are annotated with @NoChronostamp
		if (hasAnnotation(ctClass, NoChronostamp.class.getName())) {
			return;	
		} 
		
		// Only enhance model classes without created_at and updated_at attributes
		for (CtField ctField : ctClass.getDeclaredFields()) {
			if (ctField.getName().equals("created_at") || ctField.getName().equals("updated_at")) {
				return;
			}
	    }
		
		// ----- Add created_at property ----- 
		CtField created_at = CtField.make("private java.util.Date created_at = new java.util.Date();", ctClass);
		ctClass.addField(created_at);
		
		// ----- Add updated_at property -----
		CtField updated_at = CtField.make("private java.util.Date updated_at = new java.util.Date(created_at.getTime());", ctClass);
		ctClass.addField(updated_at);
		
		// ----- Add annotation @Temporal(TemporalType.TIMESTAMP) to created_at and updated_at fields -----
		//attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
		annot = new Annotation("javax.persistence.Temporal", constpool);
		enumValue = new EnumMemberValue(constpool);
		enumValue.setType("javax.persistence.TemporalType");
		enumValue.setValue("TIMESTAMP");
		annot.addMemberValue("value", enumValue);
		attr.addAnnotation(annot);
		
		created_at.getFieldInfo().addAttribute(attr);
		updated_at.getFieldInfo().addAttribute(attr);
		
/*		
		// ----- Add onCreate() method -----
		code =
		    "public void onCreate() { " +
			"created_at = new java.util.Date(); " +
			"updated_at = new java.util.Date(created_at.getTime()); " +
		    "}";
		final CtMethod onCreate = CtMethod.make(code, ctClass);
		ctClass.addMethod(onCreate);
		
		// ----- Add annotation @PrePersist to onCreate() method -----
		//attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
		annot = new Annotation("javax.persistence.PrePersist", constpool);
		attr.addAnnotation(annot);
		
		onCreate.getMethodInfo().addAttribute(attr);
*/		

		// Check if there's a method annotated with @PreUpdate
		CtMethod methodWithPreUpdateAnnot = null;
		for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
			if (hasAnnotation(ctMethod, javax.persistence.PreUpdate.class.getName())) {
				methodWithPreUpdateAnnot = ctMethod;
			}
		}
		
		if (methodWithPreUpdateAnnot != null) {
			methodWithPreUpdateAnnot.insertBefore("updated_at = new java.util.Date();");	
		} else {
			
			// ----- Add onUpdate() method -----
			code =
				"public void onUpdate() { " +
				"updated_at = new java.util.Date(); " +
				"}";
			final CtMethod onUpdate = CtMethod.make(code, ctClass);
			ctClass.addMethod(onUpdate);
			
			// ----- Add annotation @PreUpdate to onUpdate() method -----
			//attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
			annot = new Annotation("javax.persistence.PreUpdate", constpool);
			attr.addAnnotation(annot);
			
			onUpdate.getMethodInfo().addAttribute(attr);
		}
		

		
		
		
	
		// ----- GETTERS -----
	
		// ----- Add getCreated_at() method -----
		code = "public java.util.Date getCreated_at() { " +
			"return this.created_at; }";
		final CtMethod getCreated_at = CtMethod.make(code, ctClass);
		ctClass.addMethod(getCreated_at);
		
		// ----- Add getCreatedAt() method -----
		code = "public java.util.Date getCreatedAt() { " +
			"return this.created_at; }";
		final CtMethod getCreatedAt = CtMethod.make(code, ctClass);
		ctClass.addMethod(getCreatedAt);
		
		// ----- Add getUpdated_at() method -----
		code = "public java.util.Date getUpdated_at() { " +
			"return this.updated_at; }";
		final CtMethod getUpdated_at = CtMethod.make(code, ctClass);
		ctClass.addMethod(getUpdated_at);
		
		// ----- Add getUpdatedAt() method -----
		code = "public java.util.Date getUpdatedAt() { " +
			"return this.updated_at; }";
		final CtMethod getUpdatedAt = CtMethod.make(code, ctClass);
		ctClass.addMethod(getUpdatedAt);



		// Done - Enhance Class.
		appClass.enhancedByteCode = ctClass.toBytecode();
		ctClass.defrost();
	}
	
	
	
	/**
	 * Test if a method has the provided annotation
	 * @param ctMethod the javassist method representation
	 * @param annotation fully qualified name of the annotation class eg."javax.persistence.Entity"
	 * @return true if field has the annotation
	 * @throws java.lang.ClassNotFoundException
	 */
    private boolean hasAnnotation(CtMethod ctMethod, String annotation) throws ClassNotFoundException {
        for (Object object : ctMethod.getAvailableAnnotations()) {
            java.lang.annotation.Annotation ann = (java.lang.annotation.Annotation) object;
            if (ann.annotationType().getName().equals(annotation)) {
                return true;
            }
        }
        return false;
    }
}
