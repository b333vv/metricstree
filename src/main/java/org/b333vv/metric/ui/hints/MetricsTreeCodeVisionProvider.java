package org.b333vv.metric.ui.hints;


import com.intellij.codeInsight.codeVision.CodeVisionAnchorKind;
import com.intellij.codeInsight.codeVision.CodeVisionEntry;
import com.intellij.codeInsight.codeVision.CodeVisionRelativeOrdering;
import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry;
import com.intellij.codeInsight.hints.InlayHintsUtils;
import com.intellij.codeInsight.hints.codeVision.DaemonBoundCodeVisionProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import kotlin.Pair;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import org.b333vv.metric.builder.ClassModelBuilder;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.FileElement;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.util.SettingsService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.intellij.ide.util.gotoByName.ChooseByNamePopup.createPopup;

public class MetricsTreeCodeVisionProvider implements DaemonBoundCodeVisionProvider {

    private static final String GROUP_ID = "org.b333vv.metric";
    private static final String ID = "metricsTree";
    private static final String NAME = "Metrics Tree Code Vision";

    private final MetricsTreeCodeVisionSettings settings;

    public MetricsTreeCodeVisionProvider() {
        this.settings = MetricsTreeCodeVisionSettings.getInstance();
    }

    @Override
    public @NotNull CodeVisionAnchorKind getDefaultAnchor() {
        return CodeVisionAnchorKind.Top;
    }

    @Override
    public @Nls @NotNull String getName() {
        return NAME;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }

    @NotNull
    @Override
    public String getGroupId() {
        return GROUP_ID;
    }

    public boolean isEnabled() {
        return settings.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        settings.setEnabled(enabled);
    }

    @NotNull
    @Override
    public List<CodeVisionRelativeOrdering> getRelativeOrderings() {
        return List.of(CodeVisionRelativeOrdering.CodeVisionRelativeOrderingLast.INSTANCE);
    }

    @Override
    public @NotNull List<Pair<TextRange, CodeVisionEntry>> computeForEditor(@NotNull Editor editor, @NotNull PsiFile file) {
        List<Pair<TextRange, CodeVisionEntry>> lenses = new ArrayList<>();
        String languageId = file.getLanguage().getID();
        if (!"JAVA".equalsIgnoreCase(languageId)) {
            return lenses;
        }

        if (!settings.isEnabled()) {
            return lenses;
        }

        if (!(file instanceof PsiJavaFile psiJavaFile)) {
            return lenses;
        }

        FileElement javaFile = CachedValuesManager.getCachedValue(psiJavaFile, () -> {
            ClassModelBuilder classModelBuilder = new ClassModelBuilder(psiJavaFile.getProject());
            FileElement jf = classModelBuilder.buildJavaFile(psiJavaFile);
            return CachedValueProvider.Result.create(jf, psiJavaFile);
        });

        SyntaxTraverser<PsiElement> traverser = SyntaxTraverser.psiTraverser(file);
        for (PsiElement element : traverser) {
            Pair<String, List<MetricType>> hint = null;
            if (element instanceof PsiClass) {
                hint = getHintForClass(element, javaFile);
            }
            TextRange range = InlayHintsUtils.INSTANCE.getTextRangeWithoutLeadingCommentsAndWhitespaces(element);
            if (hint != null) {
//                lenses.add(new Pair<>(range, new TextCodeVisionEntry(hint, getId(), null, "", "", List.of())));

                lenses.add(new Pair(range, new ClickableTextCodeVisionEntry(hint.component1(), getId(),
                        new MyClickHandler((PsiClass) element, hint.component2()), null, hint.component1(), "", List.of())));
            }
        }
        return lenses;
    }

    private Pair<String, List<MetricType>> getHintForClass(PsiElement psiClass, FileElement javaFile) {
        Optional<ClassElement> ojc = javaFile.classes().filter(c -> c.getName().equals(((PsiClass) psiClass).getName())).findFirst();
        List<MetricType> metricTypes = new ArrayList<>();
        if (ojc.isPresent()) {
            String hint = ojc.get().metrics()
                    .filter(m -> psiClass.getProject().getService(SettingsService.class).isNotRegularValue(m.getType(), m.getPsiValue()))
                    .peek(m -> {
                        metricTypes.add(m.getType());
                    })
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            return new Pair<>(hint, metricTypes);
        }
        return null;
    }

    @Override
    public void handleClick(@NotNull Editor editor, @NotNull TextRange textRange, @NotNull CodeVisionEntry entry) {
//        if (entry instanceof CodeVisionPredefinedActionEntry) {
//            ((CodeVisionPredefinedActionEntry)entry).onClick(editor);
//        }
    }

    static class MyClickHandler implements Function2<MouseEvent, Editor, Unit> {

        private final PsiClass psiClass;
        private final List<MetricType> metricTypes;

        public MyClickHandler(PsiClass value1, List<MetricType> value2) {
            psiClass = value1;
            metricTypes = value2;
        }

        public Unit invoke(MouseEvent event, Editor editor) {
//            TextRange range = InlayHintsUtils.INSTANCE.getTextRangeWithoutLeadingCommentsAndWhitespaces(psiClass);
//            int startOffset = range.getStartOffset();
//            int endOffset = range.getEndOffset();
//            editor.getSelectionModel().setSelection(startOffset, endOffset);
//            AnAction action1 = ActionManager.getInstance().getAction("MyPlugin.Action1");
//            AnAction action2 = ActionManager.getInstance().getAction("MyPlugin.Action2");
//            DefaultActionGroup actionGroup = new DefaultActionGroup(List.of(action1, action2));
//            ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(null, actionGroup,
//                    EditorUtil.getEditorDataContext(editor), JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true);


//            ListPopup popup = JBPopupFactory.getInstance().createListPopup(null, new MetricsTreePopup(psiClass, metricTypes);
//            popup.show(new RelativePoint(event));

             return null;
        }
    }

//    private String getHintForMethod(PsiElement psiMethod, JavaFile javaFile) {
//        Optional<JavaClass> ojc = javaFile.classes().filter(c -> c.getName().equals(((PsiMethod) psiMethod).getClass().getName())).findFirst();
//        if (ojc.isPresent()) {
//            var ojm = ojc.getProfiles().methods().filter(m -> m
//                    .getPsiMethod()
//                    .getSignature(PsiSubstitutor.EMPTY)
//                    .equals(((PsiMethod) psiMethod)
//                            .getSignature(PsiSubstitutor.EMPTY))).findFirst();
//            if (ojm.isPresent()) {
//                var hint = ojm.getProfiles().metrics()
//                        .filter(m -> MetricsService.isNotRegularValue(m.getType(), m.getValue()))
//                        .map(Object::toString)
//                        .collect(Collectors.joining(", "));
//                MetricsUtils.getConsole().info("Hint for method " + ojm.getProfiles().getName() + ": " + hint);
//            }
//        }
//        return "";
//    }
}