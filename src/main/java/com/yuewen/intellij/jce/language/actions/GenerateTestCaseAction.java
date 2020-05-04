package com.yuewen.intellij.jce.language.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.JBIterable;
import com.yuewen.intellij.jce.language.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GenerateTestCaseAction extends AnAction implements DumbAware {
    @Override
    public void update(@NotNull AnActionEvent event) {
        // Using the event, evaluate the context, and enable or disable the action.
        JBIterable<VirtualFile> files = getJceFiles(event);
        if (!files.isEmpty()) {
            PsiElement element = getPsiElement(event);
            PsiElement parent = PsiTreeUtil.findFirstParent(element, e -> e instanceof JceStructType
                    || e instanceof JceFunctionInfo || e instanceof JceInterfaceInfo || e instanceof JceModuleInfo);
            if (parent != null) {
                event.getPresentation().setEnabledAndVisible(true);
                return;
            }
        }
        event.getPresentation().setEnabledAndVisible(false);
    }

    @Nullable
    private static PsiElement getPsiElement(@NotNull AnActionEvent event) {
        PsiElement element = event.getData(CommonDataKeys.PSI_ELEMENT);
        if (element == null) {
            PsiFile jceFile = event.getData(CommonDataKeys.PSI_FILE);
            Caret caret = event.getData(CommonDataKeys.CARET);
            if (jceFile instanceof JceFile && caret != null) {
                element = jceFile.findElementAt(caret.getOffset());
            }
        }
        return element;
    }

    @NotNull
    private static JBIterable<VirtualFile> getJceFiles(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        JBIterable<VirtualFile> files = JBIterable.of(e.getData(LangDataKeys.VIRTUAL_FILE_ARRAY));
        if (project == null || files.isEmpty()) {
            return JBIterable.empty();
        }
        PsiManager manager = PsiManager.getInstance(project);
        return files.filter(o -> manager.findFile(o) instanceof JceFile);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // Using the event, implement an action. For example, create and show a dialog.
        // Using the event, create and show a dialog
        StringBuilder dlgMessageBuilder = new StringBuilder();
        String dlgTitle = "";
        PsiElement element = getPsiElement(event);
        PsiElement parent = PsiTreeUtil.findFirstParent(element, e -> e instanceof JceStructType
                || e instanceof JceFunctionInfo || e instanceof JceInterfaceInfo || e instanceof JceModuleInfo);
        if (parent instanceof JceStructType) {
            //结构体测试用例
            dlgTitle = "Test case for struct " + ((JceStructType) parent).getName();
            dlgMessageBuilder.append(generateTestCase((JceStructType) parent));
        } else if (parent instanceof JceFunctionInfo) {
            //function
            dlgTitle = "Test case for function " + ((JceFunctionInfo) parent).getName();
            dlgMessageBuilder.append(generateTestCase((JceFunctionInfo) parent));
        } else if (parent instanceof JceModuleInfo) {
            //module
            dlgTitle = "Test case for struct(s) in " + ((JceModuleInfo) parent).getName();
            List<JceStructType> structTypeList = ((JceModuleInfo) parent).getStructTypeList();
            for (JceStructType jceStructType : structTypeList) {
                dlgMessageBuilder.append("结构体名\n").append(jceStructType.getName());
                dlgMessageBuilder.append("\n");
                dlgMessageBuilder.append(generateTestCase(jceStructType));
                dlgMessageBuilder.append("\n\n");
            }
        } else if (parent instanceof JceInterfaceInfo) {
            dlgTitle = "Test case for function(s) in " + ((JceInterfaceInfo) parent).getName();
            List<JceFunctionInfo> structTypeList = ((JceInterfaceInfo) parent).getFunctionInfoList();
            for (JceFunctionInfo jceStructType : structTypeList) {
                dlgMessageBuilder.append(generateTestCase(jceStructType));
                dlgMessageBuilder.append("\n\n");
            }
        }
        JTextField textField = new JTextField(dlgMessageBuilder.toString(), 0);
        textField.setEditable(false);
        Messages.showTextAreaDialog(textField, dlgTitle, "DescriptionDialogEditor");
    }

    private String generateTestCase(JceFunctionInfo functionInfo) {
        StringBuilder functionTestCase = new StringBuilder();
        //函数名
        functionTestCase.append("函数名")
                .append("\n")
                .append(functionInfo.getName())
                .append("\n");
        //返回值
        JceReturnType returnType = functionInfo.getReturnType();
        boolean isVoid = "void".equalsIgnoreCase(returnType.getText());
        if (isVoid || returnType.getFieldType() != null) {
            functionTestCase.append("返回值")
                    .append("\n")
                    .append(isVoid ? "void" : generateFieldType(returnType.getFieldType()))
                    .append("\n");
        }
        JceFunctionParamList functionParamList = functionInfo.getFunctionParamList();
        if (functionParamList != null && !functionParamList.getFunctionParamList().isEmpty()) {
            //参数列表，区分一下输入参数和输出参数
            List<JceFieldType> inFieldTypeList = new ArrayList<>();
            List<JceFieldType> outFieldTypeList = new ArrayList<>();
            List<JceFunctionParam> paramList = functionParamList.getFunctionParamList();
            for (JceFunctionParam functionParam : paramList) {
                JceFieldTypeModifier fieldTypeModifier = functionParam.getFieldTypeModifier();
                if (fieldTypeModifier != null && "out".equalsIgnoreCase(fieldTypeModifier.getText())) {
                    //出参
                    outFieldTypeList.add(functionParam.getFieldType());
                } else {
                    //入参
                    inFieldTypeList.add(functionParam.getFieldType());
                }
            }
            if (!inFieldTypeList.isEmpty()) {
                //入参不为空
                functionTestCase.append("输入参数").append("\n");
                List<String> inTypeString = new ArrayList<>(inFieldTypeList.size());
                for (JceFieldType inFieldType : inFieldTypeList) {
                    inTypeString.add(generateFieldType(inFieldType));
                }
                functionTestCase.append(String.join("|", inTypeString));
                functionTestCase.append("\n");
            }
            if (!outFieldTypeList.isEmpty()) {
                //出参不为空
                functionTestCase.append("输出参数").append("\n");
                List<String> outTypeString = new ArrayList<>(outFieldTypeList.size());
                for (JceFieldType outFieldType : outFieldTypeList) {
                    outTypeString.add(generateFieldType(outFieldType));
                }
                functionTestCase.append(String.join("|", outTypeString));
                functionTestCase.append("\n");
            }
        }
        return functionTestCase.toString();
    }

    private static String generateFieldType(@NotNull JceFieldType fieldType) {
        if (fieldType.getBuiltInTypes() != null) {
            //内建类型
            return fieldType.getBuiltInTypes().getText();
        } else if (fieldType.getMapType() != null) {
            //map类型 map<a,b>
            List<JceFieldType> mapFieldTypeList = fieldType.getMapType().getFieldTypeList();
            if (mapFieldTypeList.size() != 2) {
                //语法错误，map的类型必须等于2
                return null;
            }
            StringBuilder mapType = new StringBuilder("map<");
            List<String> mapTypeList = new ArrayList<>(mapFieldTypeList.size());
            for (JceFieldType mapFieldType : mapFieldTypeList) {
                mapTypeList.add(generateFieldType(mapFieldType));
            }
            mapType.append(String.join(",", mapTypeList))
                    .append(">");
            return mapType.toString();
        } else if (fieldType.getVectorType() != null) {
            //vector类型 vector<a,b>
            JceFieldType vectorFieldType = fieldType.getVectorType().getFieldType();
            if (vectorFieldType == null) {
                //语法错误
                return null;
            }
            return "vector<" + generateFieldType(vectorFieldType) + ">";
        } else {
            //引用类型，包括enum和struct
            PsiReference reference = fieldType.getReference();
            if (reference != null) {
                PsiElement resolve = reference.resolve();
                if (resolve != null) {
                    //检查是enum还是struct
                    if (resolve instanceof JceEnumType) {
                        //enum，就是int型
                        return "int";
                    } else if (resolve instanceof JceStructType) {
                        //结构体，继续生成
                        return generateTestCase((JceStructType) resolve);
                    }
                }
            }
        }
        return null;
    }

    private static String generateTestCase(JceStructType structType) {
        StringBuilder sb = new StringBuilder("struct<");
        List<String> fieldTypeList = new ArrayList<>(structType.getFieldInfoList().size());
        for (JceFieldInfo fieldInfo : structType.getFieldInfoList()) {
            JceFieldType fieldType = fieldInfo.getFieldType();
            if (fieldType == null) {
                //语法错误
                continue;
            }
            String fieldTypeString = generateFieldType(fieldType);
            if (fieldTypeString == null) {
                //语法错误
                continue;
            }
            fieldTypeList.add(fieldTypeString);
        }
        return sb.append(String.join(",", fieldTypeList))
                .append(">")
                .toString();
    }
}
