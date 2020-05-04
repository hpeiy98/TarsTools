package com.yuewen.intellij.jce.language.presentation;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.yuewen.intellij.jce.language.psi.JceModuleInfo;
import com.yuewen.intellij.jce.language.psi.JceStructType;
import icons.JceIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JceStructPresentationProvider implements ItemPresentationProvider<JceStructType> {

    @Override
    public ItemPresentation getPresentation(@NotNull JceStructType element) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return element.getName();
            }

            @Nullable
            @Override
            public String getLocationString() {
                PsiElement module = PsiTreeUtil.findFirstParent(element, e -> e instanceof JceModuleInfo);
                if (module == null) {
                    return null;
                }
                return ((JceModuleInfo) module).getName();
            }

            @NotNull
            @Override
            public Icon getIcon(boolean b) {
                return JceIcons.STRUCT;
            }
        };
    }
}
