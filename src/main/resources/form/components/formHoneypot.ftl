
[#if cmsfn.editMode]
   <div cms:edit="box"></div>
[/#if]

<div id="mgnlhp">
    <label>${i18n['paragraph.formHoneypot.label']}</label>
    <input type="text" name="${content.controlName}" value="${model.value!}"/>
</div>