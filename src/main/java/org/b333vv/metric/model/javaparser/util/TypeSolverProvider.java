package org.b333vv.metric.model.javaparser.util;

import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.util.Arrays;

public class TypeSolverProvider {
    public TypeSolver getTypeSolver(Project project) {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        for (VirtualFile sourceRoot : ProjectRootManager.getInstance(project).getContentSourceRoots()) {
            combinedTypeSolver.add(new JavaParserTypeSolver(sourceRoot.toNioPath()));
        }

        for (Module module : ModuleManager.getInstance(project).getModules()) {
        for (VirtualFile classesRoot : OrderEnumerator.orderEntries(module).recursively().librariesOnly().getClassesRoots()) {
            if (!classesRoot.isDirectory()) {
                String path = classesRoot.getPath();
                if (path.contains("!/")) {
                    path = path.substring(0, path.indexOf("!/"));
                }
                try {
                    combinedTypeSolver.add(new JarTypeSolver(path));
                } catch (IOException e) {
                    // Log error, e.g., using Logger
                }
                }
        }
        }

        return combinedTypeSolver;
    }
}
