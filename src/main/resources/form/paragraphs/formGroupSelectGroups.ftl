[#assign cms=JspTaglibs["cms-taglib"]]
[#if mgnl.editMode]
    <div style="float:right;height:20px;width:100px">[@cms.editBar /]</div>
[/#if]
[#assign parent = content?parent]
[#assign parentmodel = model.parentModel]
[#if !mgnl.editMode]
<optgroup id="${parent?parent.controlName}_${content.controlName}" label="${content.title}"  >
[#else]
<p>${content.title}</p>
<select multiple="true" >
[/#if]
    [#list content.labels?split("\r\n") as label]
        [#assign selected=""]
        [#assign data=label?split(":")]
        [#list parentmodel.value?split("*") as modelValue]
            [#if modelValue == data[1]]
                [#assign selected="selected"]
                [#break]
            [/#if]
        [/#list]
        <option value="${data[1]}" ${selected} >${data[0]}</option>
    [/#list]
[#if !mgnl.editMode]
</optgroup>
[#else]
</select>
[/#if]


