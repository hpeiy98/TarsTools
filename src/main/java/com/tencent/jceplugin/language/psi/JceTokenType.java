package com.tencent.jceplugin.language.psi;

import com.intellij.psi.tree.IElementType;
import com.tencent.jceplugin.language.JceLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class JceTokenType extends IElementType {
    public JceTokenType(@NotNull @NonNls String debugName) {
        super(debugName, JceLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "JceTokenType:" + super.toString();
    }
}