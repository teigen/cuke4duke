package cuke4duke.internal.java;

import cuke4duke.annotation.After;
import cuke4duke.annotation.Before;
import cuke4duke.annotation.Order;
import cuke4duke.annotation.Transform;
import cuke4duke.internal.java.annotation.StepDef;
import cuke4duke.internal.jvmclass.ClassAnalyzer;
import cuke4duke.internal.jvmclass.ClassLanguage;
import cuke4duke.internal.jvmclass.ObjectFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public class JavaAnalyzer implements ClassAnalyzer {
    private final MethodFormat methodFormat;

    public JavaAnalyzer() {
        this.methodFormat = new MethodFormat(System.getProperty("cuke4duke.methodFormat", "%c.%m(%a)"));
    }

    public void populateStepDefinitionsAndHooks(ObjectFactory objectFactory, ClassLanguage classLanguage) throws Throwable {
        for(Method method: getOrderedMethods(classLanguage)) {
            registerBeforeMaybe(method, classLanguage);
            registerAfterMaybe(method, classLanguage);
            registerStepDefinitionsFromAnnotations(method, classLanguage);
            registerTransformMaybe(method, classLanguage);
        }
    }

    private void registerTransformMaybe(Method method, ClassLanguage classLanguage) {
        if (method.isAnnotationPresent(Transform.class)) {
            classLanguage.addTransform(method.getReturnType(), new JavaTransform(classLanguage, method));
        }
    }

    public Class<?>[] alwaysLoad() {
        return new Class<?>[0];
    }

    private List<Method> getOrderedMethods(ClassLanguage classLanguage) {
        Set<Method> methods = new HashSet<Method>();
        for(Class<?> clazz : classLanguage.getClasses()) {
            methods.addAll(Arrays.asList(clazz.getMethods()));
        }
        List<Method> sortedMethods = new ArrayList<Method>(methods);
        Collections.sort(sortedMethods, new Comparator<Method>() {
            public int compare(Method m1, Method m2) {
                return order(m1) - order(m2);
            }

            private int order(Method m) {
                Order order = m.getAnnotation(Order.class);
                return (order == null) ? Integer.MAX_VALUE : order.value();
            }
        });
        return sortedMethods;
    }

    private void registerBeforeMaybe(Method method, ClassLanguage classLanguage) {
        if (method.isAnnotationPresent(Before.class)) {
            classLanguage.addBeforeHook(new JavaHook(classLanguage, method, method.getAnnotation(Before.class).value()));
        }
    }

    private void registerAfterMaybe(Method method, ClassLanguage classLanguage) {
        if (method.isAnnotationPresent(After.class)) {
            classLanguage.addAfterHook(new JavaHook(classLanguage, method, method.getAnnotation(After.class).value()));
        }
    }

    private void registerStepDefinitionsFromAnnotations(Method method, ClassLanguage classLanguage) throws Throwable {
        for(Annotation annotation: method.getAnnotations()) {
            if(annotation.annotationType().isAnnotationPresent(StepDef.class)) {
                Method value = annotation.getClass().getMethod("value");
                String regexpString = (String) value.invoke(annotation);
                if (regexpString != null) {
                    Pattern regexp = Pattern.compile(regexpString);
                    classLanguage.addStepDefinition(new JavaStepDefinition(classLanguage, method, regexp, methodFormat));
                }
            }
        }
    }
}
