/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b333vv.metric.export;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.*;
import org.b333vv.metric.model.metric.Metric;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class XmlReportBuilder {
    private final Project project;

    public XmlReportBuilder(Project project) {
        this.project = project;
    }

    public void buildAndExport(String fileName, JavaProject javaProject) {
        File xmlOutputFile = new File(fileName);
        try {
            Document outputDocument = createDocument(javaProject);
            if (outputDocument == null) {
                return;
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(outputDocument);
            StreamResult result = new StreamResult(xmlOutputFile);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            this.project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(e.getMessage());
        }
        if (xmlOutputFile.exists()) {
            this.project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC)
                    .printInfo("Project, packages, classes and methods metrics have been exported in "
                            + xmlOutputFile.getAbsolutePath());
        }
    }

    @Nullable
    private Document createDocument(JavaProject javaProject) {
        Document doc = null;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();
            Element projectElement = doc.createElement("Project");
            doc.appendChild(projectElement);
            addMetricsForNode(javaProject, projectElement, doc);
            Element packagesElement = doc.createElement("Packages");
            projectElement.appendChild(packagesElement);
            List<JavaPackage> sortedPackages = javaProject.packages().collect(Collectors.toList());
            for (JavaPackage packageNode : sortedPackages) {
                Element packageElement = doc.createElement("Package");
                packageElement.setAttribute("name", packageNode.getName());
                packagesElement.appendChild(packageElement);
                addMetricsForNode(packageNode, packageElement, doc);
                addPackages(packageNode, packageElement, doc);
            }
        } catch (ParserConfigurationException e) {
            this.project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(e.getMessage());
        }
        return doc;
    }

    private void addPackages(JavaPackage parentNode, Element parentElement, Document doc) {
        List<JavaPackage> sortedPackages = parentNode.subPackages().collect(Collectors.toList());
        for (JavaPackage javaPackage : sortedPackages) {
            Element packageElement = doc.createElement("Package");
            packageElement.setAttribute("name", javaPackage.getName());
            parentElement.appendChild(packageElement);
            addMetricsForNode(javaPackage, packageElement, doc);
            addPackages(javaPackage, packageElement, doc);
        }
        addJavaFiles(parentNode, parentElement, doc);
    }

    private void addJavaFiles(JavaPackage parentNode, Element parentElement, Document doc) {
        List<JavaFile> sortedFiles = parentNode.files().collect(Collectors.toList());
        for (JavaFile f : sortedFiles) {
            if (f.classes().count() > 1) {
                Element fileElement = doc.createElement("File");
                fileElement.setAttribute("name", f.getName());
                parentElement.appendChild(fileElement);
                List<JavaClass> sortedClasses = f.classes().collect(Collectors.toList());
                addClasses(doc, fileElement, sortedClasses);
            } else if (f.classes().findFirst().isPresent()) {
                JavaClass javaClass = f.classes().findFirst().get();
                Element classElement = doc.createElement("Class");
                classElement.setAttribute("name", javaClass.getName());
                parentElement.appendChild(classElement);
                addMetricsForNode(javaClass, classElement, doc);
                addSubClasses(javaClass, classElement, doc);
                addMethods(javaClass, classElement, doc);
            }
        }
    }

    private void addClasses(Document doc, Element fileElement, List<JavaClass> sortedClasses) {
        for (JavaClass c : sortedClasses) {
            Element classElement = doc.createElement("Class");
            classElement.setAttribute("name", c.getName());
            fileElement.appendChild(classElement);
            addMetricsForNode(c, classElement, doc);
            addSubClasses(c, classElement, doc);
            addMethods(c, classElement, doc);
        }
    }

    private void addSubClasses(JavaClass parentClass, Element parentElement, Document doc) {
        List<JavaClass> sortedClasses = parentClass.innerClasses().collect(Collectors.toList());
        addClasses(doc, parentElement, sortedClasses);
    }

    private void addMethods(JavaClass javaClass, Element parentElement, Document doc) {
        List<JavaMethod> sortedMethods = javaClass.methods().collect(Collectors.toList());
        for (JavaMethod m : sortedMethods) {
            Element methodElement = doc.createElement("Method");
            methodElement.setAttribute("name", m.getName());
            parentElement.appendChild(methodElement);
            addMetricsForNode(m, methodElement, doc);
        }
    }

    private void addMetricsForNode(JavaCode node, Node parentElement, Document doc) {
        Element metricsContainer = doc.createElement("Metrics");
        List<Metric> sortedMetrics = node.metrics().collect(Collectors.toList());
        for (Metric metric : sortedMetrics) {
            Element metricsElement = doc.createElement("Metric");
            metricsElement.setAttribute("name", metric.getType().name());
            metricsElement.setAttribute("description", metric.getType().description());
            metricsElement.setAttribute("value", metric.getFormattedValue());
            metricsContainer.appendChild(metricsElement);
        }
        parentElement.appendChild(metricsContainer);
    }
}
