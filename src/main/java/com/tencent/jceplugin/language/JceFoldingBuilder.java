/**
 * Tencent is pleased to support the open source community by making Tars available.
 *
 * Copyright (C) 2016THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.tencent.jceplugin.language;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.tencent.jceplugin.language.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class JceFoldingBuilder extends FoldingBuilderEx implements DumbAware {
    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
        if (!(root instanceof JceFile)) {
            return new FoldingDescriptor[0];
        }
        JceFile jceFile = (JceFile) root;
        List<JceModuleInfo> moduleList = JceUtil.findModules(jceFile, false);
        List<JceInterfaceInfo> interfaceList = JceUtil.findInterface(jceFile);
        List<JceEnumMember> enumMemberList = JceUtil.findEnumMember(jceFile);
        List<JceEnumType> enumTypeList = JceUtil.findEnum(jceFile);
        List<JceStructType> structList = JceUtil.findStruct(jceFile);
        List<JceNamedElement> blocks = new ArrayList<>();
        blocks.addAll(moduleList);
        blocks.addAll(interfaceList);
        blocks.addAll(enumMemberList);
        blocks.addAll(enumTypeList);
        blocks.addAll(structList);
        // Initialize the list of folding regions
        List<FoldingDescriptor> descriptors = new ArrayList<>();
        for (JceNamedElement block : blocks) {
            if (block.getTextRange().isEmpty()) {
                continue;
            }
            if (block.getNameIdentifier() == null) {
                //语法错误，缺少标识符
                continue;
            }
            FoldingGroup group = FoldingGroup.newGroup("jce");
            PsiElement openBlock = PsiTreeUtil.findSiblingForward(block.getFirstChild(), JceTypes.OPEN_BLOCK, null);
            PsiElement closeBlock = PsiTreeUtil.findSiblingForward(block.getFirstChild(), JceTypes.CLOSE_BLOCK, null);
            if (closeBlock == null || openBlock == null) {
                //语法错误，缺少{或}
                continue;
            }
            TextRange range = new TextRange(openBlock.getTextRange().getEndOffset(), closeBlock.getTextRange().getStartOffset());
            if (range.isEmpty()) {
                continue;
            }
            descriptors.add(new FoldingDescriptor(block.getNode(), range, group) {
                @Override
                public String getPlaceholderText() {
                    return "...";
                }
            });
        }
        return descriptors.toArray(new FoldingDescriptor[0]);
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode astNode) {
        return "...";
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode astNode) {
        return false;
    }

//    @Override
//    public boolean isCollapsedByDefault(@NotNull FoldingDescriptor foldingDescriptor) {
//        return false;
//    }
}
