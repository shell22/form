[#assign cms=JspTaglibs["cms-taglib"]]

<div ${model.style!} >
[@cms.editBar /]
    [#if content.title?has_content]
        <label id="${content.controlName}_label" class="${content.editLength!}" for="${content.controlName}">
            <span>
            [#if !model.isValid()]
                <em>${i18n['form.error.field']}</em>
            [/#if]
            ${content.title!}
            [#if content.mandatory]
                <dfn title="required">${model.requiredSymbol!}</dfn>
            [/#if]
            </span>
    [/#if]
    [#if content.rows == 1]
        <input type="password" name="${content.controlName}" id="${content.controlName}" value="${content.value!?html}"/>
    [#else]
        <textarea id="${content.controlName}" name="${content.controlName}" rows="${content.rows}">${content.value!?html}</textarea>
    [/#if]
    [#if content.title?has_content]
      </label>
    [/#if]
    [#if content.description?has_content]
      <span>${content.description}</span>
    [/#if]
</div>