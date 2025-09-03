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

package org.b333vv.metric.model.visitor.type;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.util.ClassUtils;
import org.b333vv.metric.model.metric.value.Value;

import static org.b333vv.metric.model.metric.MetricType.SIZE2;

public class NumberOfAttributesAndMethodsVisitor extends JavaClassVisitor {
    @Override
    public void visitClass(PsiClass psiClass) {
        super.visitClass(psiClass);
        metric = Metric.of(SIZE2, Value.UNDEFINED);
        
        if (ClassUtils.isConcrete(psiClass)) {
            // 获取所有方法（包括继承的）和字段（包括继承的）
            PsiMethod[] methods = psiClass.getAllMethods();
            PsiField[] fields = psiClass.getAllFields();
            
            // 统计非静态方法的数量
            int operationsNumber = countNonStaticMethods(methods);
            
            // 统计非静态字段的数量
            int attributesNumber = countNonStaticFields(fields);
            
            // 计算总和并设置指标值
            metric = Metric.of(SIZE2, (long) operationsNumber + attributesNumber);
        }
    }

    /**
     * 统计非静态方法的数量
     * 
     * @param methods 方法数组
     * @return 非静态方法的数量
     */
    private int countNonStaticMethods(PsiMethod[] methods) {
        int count = 0;
        for (PsiMethod method : methods) {
            if (!method.hasModifierProperty(PsiModifier.STATIC)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 统计非静态字段的数量
     * 
     * @param fields 字段数组
     * @return 非静态字段的数量
     */
    private int countNonStaticFields(PsiField[] fields) {
        int count = 0;
        for (PsiField field : fields) {
            if (!field.hasModifierProperty(PsiModifier.STATIC)) {
                count++;
            }
        }
        return count;
    }
}