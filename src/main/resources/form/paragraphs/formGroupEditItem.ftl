[#assign cms=JspTaglibs["cms-taglib"]]
[#if mgnl.editMode]
        <div style="float:right;height:20px;width:100px">[@cms.editBar /]</div>
[/#if]

[#if content.title?has_content]
    <label ${model.style!} id="${content.controlName}_label" for="${content.controlName}">
        <span>
            [#if model.style?index_of("error") > -1]
                <em>${i18n['form.error.field']}</em>
            [/#if]
            ${content.title}
            [#if content.mandatory]
                 <dfn title="required">*</dfn>
            [/#if]
        </span>
[/#if]
    <input type="text" name="${content.controlName}" id="${content.controlName}" value="${model.value!}"/>

[#if content.title?has_content]
    </label>
[/#if]
[#if content.description?has_content]
    <span>${content.description}</span>
[/#if]
