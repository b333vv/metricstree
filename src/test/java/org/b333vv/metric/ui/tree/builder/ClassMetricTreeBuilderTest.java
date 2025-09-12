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

package org.b333vv.metric.ui.tree.builder;

import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.b333vv.metric.builder.ClassModelBuilder;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.FileElement;
import org.b333vv.metric.ui.tree.node.ClassNode;

import javax.swing.tree.DefaultTreeModel;

public class ClassMetricTreeBuilderTest extends LightJavaCodeInsightFixtureTestCase {
    private FileElement javaFile;
    private ClassElement rootJavaClass;
    private DefaultTreeModel treeModel;
    private ClassNode rootClassNode;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureByFiles("Object.java", "HashMap.java", "AbstractMap.java");
        PsiJavaFile psiJavaFile = (PsiJavaFile) myFixture.findClass("java.util.HashMap").getContainingFile();
        ClassModelBuilder classModelBuilder = new ClassModelBuilder(getProject());
        javaFile = classModelBuilder.buildJavaFile(psiJavaFile);
        rootJavaClass = javaFile.classes().findFirst().get();

        ClassMetricTreeBuilder classMetricTreeBuilder = new ClassMetricTreeBuilder(javaFile, getProject());
        treeModel = classMetricTreeBuilder.createMetricTreeModel();
        rootClassNode = (ClassNode) treeModel.getRoot();
    }

    @Override
    protected String getTestDataPath() {
        return "testData";
    }

    public void testRootNode() {
        assertEquals(rootJavaClass, rootClassNode.getJavaClass());
    }

    public void testNodeCount() {
        assertEquals(rootJavaClass.innerClasses().count()
                + rootJavaClass.methods().count()
                + rootJavaClass.metrics().count(),
                treeModel.getChildCount(rootClassNode));
    }
}