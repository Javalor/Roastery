package io.javalor.roastery;

import io.javalor.roastery.annotation.AutoInjectd;

import java.util.HashSet;
import java.util.Set;


public class Main {



    public static void main(String[] args) {
        Set<Class<?>> classSet = new HashSet<>();

        classSet.add(SetterInject.class);
        classSet.add(Dependency.class);

        AutoInjector<ConstructorInject> constructorInjector = AutoInjector.forClass(ConstructorInject.class, classSet);
        AutoInjector<SetterInject> setterInjector = AutoInjector.forClass(SetterInject.class, classSet);
        AutoInjector<FieldInject> fieldInjector = AutoInjector.forClass(FieldInject.class, classSet);

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


    private Dependency dependency;

    @AutoInjectd
    public ConstructorInject(Dependency dependency) {
        setBeanB(dependency);
    }


    private void setBeanB(Dependency dependency) {
        this.dependency = dependency;
    }

    public void say() {
        System.out.println("From "+this.getClass().getName());
        dependency.say();
    }
}


class SetterInject {


    private Dependency dependency;


    public SetterInject() {

    }

    @AutoInjectd
    private void setBeanB(Dependency dependency) {
        this.dependency = dependency;
    }

    public void say() {
        System.out.println("From "+this.getClass().getName());
        dependency.say();
    }
}

class FieldInject {

    @AutoInjectd
    private Dependency dependency;


    public FieldInject() {

    }


    private void setBeanB(Dependency dependency) {
        this.dependency = dependency;
    }

    public void say() {
        System.out.println("From "+this.getClass().getName());
        dependency.say();
    }
}

class Dependency {

    public Dependency() {
    }

    public void say() {
        System.out.println("From "+this.getClass().getName());
        System.out.println("Bean B");
    }
}

