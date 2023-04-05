package io.javalor.roastery;

import io.javalor.componentscanner.ComponentScanner;
import io.javalor.componentscanner.annotation.IncludedInComponentScan;
import io.javalor.roastery.annotation.AutoInjectd;
import io.javalor.roastery.annotation.Qualifier;

import java.util.Collections;
import java.util.Set;


public class Main {



    public static void main(String[] args) {

        ComponentScanner componentScanner = new ComponentScanner();

      Set<Class<?>> classSet = componentScanner.getScannedClass();


        classSet.forEach(System.out::println);
        DependencyInjector<ConstructorInject> constructorInjector = DependencyInjector.forClass(ConstructorInject.class, classSet);
        DependencyInjector<SetterInject> setterInjector = DependencyInjector.forClass(SetterInject.class, classSet);
        DependencyInjector<FieldInject> fieldInjector = DependencyInjector.forClass(FieldInject.class, classSet);

        try {
            ConstructorInject constructorInject = constructorInjector.getClassInstance();
            SetterInject setterInject = setterInjector.getClassInstance();
            FieldInject fieldInject = fieldInjector.getClassInstance();

            constructorInject.say();
            setterInject.say();
            fieldInject.say();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ConstructorInject {


    private IDependency dependency;

    @AutoInjectd
    public ConstructorInject(@Qualifier(Dependency.class) IDependency dependency) {
        setBeanB(dependency);
    }


    private void setBeanB(IDependency dependency) {
        this.dependency = dependency;
    }

    public void say() {
        System.out.println("From "+this.getClass().getName());
        dependency.say();
    }
}


class SetterInject {


    private IDependency dependency;


    public SetterInject() {

    }

    @AutoInjectd
    private void setBeanB(IDependency dependency) {
        this.dependency = dependency;
    }

    public void say() {
        System.out.println("From "+this.getClass().getName());
        dependency.say();
    }
}

class FieldInject {

    @AutoInjectd
    private IDependency sdede;



    public FieldInject() {

    }


    private void setBeanB(IDependency dependency) {
        this.sdede = dependency;
    }

    public void say() {
        System.out.println("From "+this.getClass().getName());
        sdede.say();
    }
}

interface IDependency {
    void say();
}
@IncludedInComponentScan
@javax.ejb.Stateless
class Dependency implements IDependency {

    public Dependency() {
    }

    public void say() {
        System.out.println("From "+this.getClass().getName());
        System.out.println("Bean B");
    }
}

