package com.redsoft.idea.plugin.yapiv2.base.impl;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiModifierListOwner;
import com.redsoft.idea.plugin.yapiv2.base.abs.AbstractPathResolver;
import com.redsoft.idea.plugin.yapiv2.constant.SpringMVCConstants;
import com.redsoft.idea.plugin.yapiv2.model.YApiParam;
import com.redsoft.idea.plugin.yapiv2.util.PathUtils;
import com.redsoft.idea.plugin.yapiv2.util.PsiAnnotationUtils;
import org.jetbrains.annotations.NotNull;

public class ClassPathResolverImpl extends AbstractPathResolver {

    @Override
    public void resolve(@NotNull PsiModifierListOwner m, @NotNull YApiParam target) {
        //获取类上面的RequestMapping 中的value
        PsiAnnotation psiAnnotation = PsiAnnotationUtils
                .findAnnotation(m, SpringMVCConstants.RequestMapping);
        if (psiAnnotation != null) {
            String consumes = PsiAnnotationUtils
                    .getPsiAnnotationAttributeValue(psiAnnotation, "consumes");
            target.setConsumes(consumes);
            target.setPath(PathUtils.pathFormat(this.getPathByAnnotation(psiAnnotation)));
        }
    }

}