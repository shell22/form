/**
 * This file Copyright (c) 2008-2017 Magnolia International
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
package info.magnolia.module.form.validators;

/**
 * Base class for validators. Kept in a registry in the module class. The actual validation is performed by FormModel.
 *
 * @see info.magnolia.module.form.FormModule
 * @see info.magnolia.module.form.templates.components.FormModel
 */
public class Validator {

    private String name;
    private String i18nBasename;
    private String errorMessage;

    /**
     * @deprecated since 2.2.11 use {@link #validateWithResult(String)} instead. This method should just keep validation logic and be protected.
     */
    @Deprecated
    public boolean validate(String value) {
        return true;
    }

    public ValidationResult validateWithResult(String value) {
        if (errorMessage == null) {
            return new ValidationResult(validate(value));
        } else {
            return new ValidationResult(validate(value), errorMessage);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getI18nBasename() {
        return i18nBasename;
    }

    public void setI18nBasename(String i18nBasename) {
        this.i18nBasename = i18nBasename;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
