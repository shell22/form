/**
 * This file Copyright (c) 2014-2017 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.form.setup;

import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.PropertyValueDelegateTask;
import info.magnolia.module.delta.RemovePropertyTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.WarnTask;
import info.magnolia.repository.RepositoryConstants;

/**
 * Migration task for 'validation' fields.
 */
public class MigrateValidationSelectFieldToTwinColSelectFieldTask extends ArrayDelegateTask {

    protected final static String WARNING_MESSAGE_FORMAT = "Unable to migrate validation field of dialog because path '%s' did not exists.";

    public MigrateValidationSelectFieldToTwinColSelectFieldTask(String name, String[] fields) {
        super(name);

        for (String field : fields) {
            addTask(new NodeExistsDelegateTask(String.format("Migrate 'validation' field '%s' to multi select field.", field), field,
                    new ArrayDelegateTask("",
                            new RemovePropertyTask(String.format("Remove property 'buttonLabel' for validation field in '%s'", field), field, "buttonLabel"),
                            new RemovePropertyTask(String.format("Remove property 'type' for validation field in '%s'", field), field, "type"),
                            new PropertyValueDelegateTask("", field, "class", "info.magnolia.ui.form.field.definition.SelectFieldDefinition", false,
                                    new SetPropertyTask(RepositoryConstants.CONFIG, field, "class", "info.magnolia.ui.form.field.definition.TwinColSelectFieldDefinition")),
                            new SetPropertyTask(RepositoryConstants.CONFIG, field, "leftColumnCaption", "dialog.form.edit.tabMain.validation.leftColumnCaption"),
                            new SetPropertyTask(RepositoryConstants.CONFIG, field, "rightColumnCaption", "dialog.form.edit.tabMain.validation.rightColumnCaption")
                    ), new WarnTask("Unable to migrate validation field", String.format(WARNING_MESSAGE_FORMAT, field)))
            );
        }
    }

}
