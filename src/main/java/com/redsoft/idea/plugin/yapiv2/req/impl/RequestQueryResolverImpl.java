package com.redsoft.idea.plugin.yapiv2.req.impl;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiParameter;
import com.redsoft.idea.plugin.yapiv2.constant.SpringMVCConstants;
import com.redsoft.idea.plugin.yapiv2.util.TypeUtils;
import com.redsoft.idea.plugin.yapiv2.model.ValueWrapper;
import com.redsoft.idea.plugin.yapiv2.model.YApiParam;
import com.redsoft.idea.plugin.yapiv2.model.YApiQuery;
import com.redsoft.idea.plugin.yapiv2.req.PsiParamFilter;
import com.redsoft.idea.plugin.yapiv2.req.SimpleRequestBodyParamResolver;
import com.redsoft.idea.plugin.yapiv2.util.DesUtils;
import com.redsoft.idea.plugin.yapiv2.util.PsiAnnotationUtils;
import com.redsoft.idea.plugin.yapiv2.util.PsiUtils;
import com.redsoft.idea.plugin.yapiv2.util.ValidUtils;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class RequestQueryResolverImpl implements SimpleRequestBodyParamResolver {

    private final Project project;

    public RequestQueryResolverImpl(Project project) {
        this.project = project;
    }

    @NotNull
    @Override
    public PsiParamFilter getPsiParamFilter(@NotNull PsiMethod m,
            @NotNull YApiParam target) {
        if (this.noBody(target)) {
            return p -> true;
        }
        PsiParameter[] parameters = m.getParameterList().getParameters();
        return this.hasRequestBody(parameters) ? (psiParameter -> PsiAnnotationUtils
                .isNotAnnotatedWith(psiParameter, SpringMVCConstants.RequestBody))
                : (p -> false);
    }

    @Override
    public void doResolverItem(@NotNull PsiMethod m, @NotNull PsiParameter param,
            @NotNull YApiParam target) {
        Set<YApiQuery> results = new LinkedHashSet<>();
        String typeClassName = param.getType().getCanonicalText();
        String typeName = param.getType().getPresentableText();
        //如果是基本类型
        if (TypeUtils.isBasicType(typeClassName)) {
            PsiAnnotation psiAnnotation = PsiAnnotationUtils
                    .findAnnotation(param, SpringMVCConstants.RequestParam);
            YApiQuery yapiQuery = new YApiQuery();
            if (psiAnnotation != null) {
                ValueWrapper valueWrapper = this.handleParamAnnotation(param, psiAnnotation);
                yapiQuery.full(valueWrapper);
            } else {//没有注解
                yapiQuery.setRequired(ValidUtils.notNullOrBlank(param) ? "1" : "0");
                yapiQuery.setName(param.getName());
                yapiQuery.setExample(TypeUtils.getDefaultValue(typeName)
                        .toString());
            }
            yapiQuery.setDesc(DesUtils.getParamDesc(m, param.getName()) + "("
                    + typeName + ")");
            results.add(yapiQuery);
        } else {
            PsiClass psiClass = PsiUtils.findPsiClass(this.project, typeClassName);
            for (PsiField field : Objects.requireNonNull(psiClass).getAllFields()) {
                if (
                        Objects.requireNonNull(field.getModifierList())
                                .hasModifierProperty(PsiModifier.STATIC)) {
                    continue;
                }
                YApiQuery query = new YApiQuery();
                query.setRequired(ValidUtils.notNullOrBlank(field) ? "1" : "0");
                query.setName(field.getName());
                query.setDesc(DesUtils.getLinkRemark(field, project));
                String typePkName = field.getType().getCanonicalText();
                if (TypeUtils.isBasicType(typePkName)) {
                    query.setExample(
                            TypeUtils.getDefaultValueByPackageName(typePkName)
                                    .toString());
                }
                results.add(query);
            }
        }
        Set<YApiQuery> apiQueries = target.getParams();
        if (Objects.isNull(apiQueries)) {
            apiQueries = new HashSet<>();
            target.setParams(apiQueries);
        }
        apiQueries.addAll(results);
    }
}
