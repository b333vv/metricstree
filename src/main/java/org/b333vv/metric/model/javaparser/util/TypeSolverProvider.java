package org.b333vv.metric.model.javaparser.util;

import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.MemoryTypeSolver;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserInterfaceDeclaration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TypeSolverProvider {
    public TypeSolver getTypeSolver(Project project, List<CompilationUnit> allUnits) {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new ClassLoaderTypeSolver(this.getClass().getClassLoader()));

        // Create a MemoryTypeSolver and populate it with the pre-parsed compilation units
        MemoryTypeSolver memoryTypeSolver = new MemoryTypeSolver();
        
        for (CompilationUnit unit : allUnits) {
            try {
                // Add all class and interface declarations from this compilation unit
                unit.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                    try {
                        String qualifiedName = classDecl.getFullyQualifiedName().orElse(classDecl.getNameAsString());
                        
                        // Create the appropriate JavaParser declaration wrapper
                        ResolvedReferenceTypeDeclaration resolvedDecl;
                        if (classDecl.isInterface()) {
                            resolvedDecl = new JavaParserInterfaceDeclaration(classDecl, combinedTypeSolver);
                        } else {
                            resolvedDecl = new JavaParserClassDeclaration(classDecl, combinedTypeSolver);
                        }
                        
                        // Add to memory type solver
                        memoryTypeSolver.addDeclaration(qualifiedName, resolvedDecl);
                    } catch (Exception e) {
                        System.err.println("Failed to add class declaration to MemoryTypeSolver: " + classDecl.getNameAsString() + " - " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                System.err.println("Failed to process CompilationUnit for MemoryTypeSolver: " + e.getMessage());
            }
        }
        
        // Add the memory type solver FIRST so it has priority
        combinedTypeSolver.add(memoryTypeSolver);

        // Add source roots with the existing approach (as fallback)
        for (VirtualFile sourceRoot : ProjectRootManager.getInstance(project).getContentSourceRoots()) {
            try {
                combinedTypeSolver.add(new JavaParserTypeSolver(sourceRoot.toNioPath()));
            } catch (UnsupportedOperationException e) {
                // In test environment, temp filesystem can't be converted to NIO Path
                // This is OK now because we have MemoryTypeSolver handling the types
                System.out.println("Skipping temp filesystem source root: " + sourceRoot.getPath() + " (using MemoryTypeSolver instead)");
            }
        }

        // Add library dependencies (existing logic)
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
                        System.err.println("Failed to add library to TypeSolver: " + path + ", error: " + e.getMessage());
                    }
                }
            }
        }
        
        return combinedTypeSolver;
    }

    public TypeSolver getTypeSolver(Project project) {
        // THIS IS THE OLD METHOD
        System.err.println("WARNING: Using TypeSolverProvider without a complete list of CompilationUnits. Cross-file type resolution may be incomplete.");
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        // Add the current ClassLoader to resolve classes available at runtime (e.g., IntelliJ SDK)
        combinedTypeSolver.add(new ClassLoaderTypeSolver(this.getClass().getClassLoader()));

        // Add content source roots (including test data)
        for (VirtualFile sourceRoot : ProjectRootManager.getInstance(project).getContentSourceRoots()) {
            try {
                combinedTypeSolver.add(new JavaParserTypeSolver(sourceRoot.toNioPath()));
                System.out.println("Successfully added source root: " + sourceRoot.getPath());
            } catch (UnsupportedOperationException e) {
                // In test environment, temp filesystem can't be converted to NIO Path
                // Skip this source root for JavaParser resolution
                System.out.println("Skipping temp filesystem source root: " + sourceRoot.getPath() + " (" + e.getMessage() + ")");
            }
        }

        // Add library dependencies
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
                        System.err.println("Failed to add library to TypeSolver: " + path + ", error: " + e.getMessage());
                    }
                }
            }
        }

        return combinedTypeSolver;
    }
}
