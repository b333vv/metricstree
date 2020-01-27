package org.jacoquev.util;

import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.code.JavaMethod;
import org.jacoquev.model.code.JavaPackage;
import org.jacoquev.model.code.JavaProject;
import org.jacoquev.model.metric.Meter;
import org.jacoquev.model.metric.Metric;

import java.util.HashSet;
import java.util.Set;

public class Runner {
    private Set<Meter<JavaProject>> projectMeters;
    private Set<Meter<JavaPackage>> packageMeters;
    private Set<Meter<JavaClass>> typeMeters;
    private Set<Meter<JavaMethod>> methodMeters;

    public Runner() {
        projectMeters = new HashSet<>();
        packageMeters = new HashSet<>();
        typeMeters = new HashSet<>();
        methodMeters = new HashSet<>();
    }

    public void registerProjectMeter(Meter<JavaProject> meter) {
        projectMeters.add(meter);
    }

    public void registerPackageMeter(Meter<JavaPackage> meter) {
        packageMeters.add(meter);
    }

    public void registerTypeMeter(Meter<JavaClass> meter) {
        typeMeters.add(meter);
    }

    public void registerMethodMeter(Meter<JavaMethod> meter) {
        methodMeters.add(meter);
    }

    public void run(JavaProject javaProject) {

        javaProject.getPackages().parallelStream().forEach(aPackage -> {

            aPackage.getTypes().parallelStream().forEach(type -> {

                type.getMethods().parallelStream().forEach(method -> {

                    methodMeters.parallelStream().forEach(methodMetricMeter -> {
                        Set<Metric> methodMetrics = methodMetricMeter.meter(method);
                        method.addMetrics(methodMetrics);
                    });
                });

                typeMeters.parallelStream().forEach(typeMetricMeter -> {
                    Set<Metric> classMetrics = typeMetricMeter.meter(type);
                    type.addMetrics(classMetrics);
                });
            });

            packageMeters.parallelStream().forEach(packageMetricMeter -> {
                Set<Metric> packageMetrics = packageMetricMeter.meter(aPackage);
                aPackage.addMetrics(packageMetrics);
            });

        });

        projectMeters.parallelStream().forEach(projectMetricMeter -> {
            Set<Metric> projectMetrics = projectMetricMeter.meter(javaProject);
            javaProject.addMetrics(projectMetrics);
        });

    }
}
