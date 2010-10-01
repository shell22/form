/**
 * This file Copyright (c) 2008-2010 Magnolia International
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
package info.magnolia.module.form.processors;

import info.magnolia.context.MgnlContext;
import info.magnolia.module.form.processing.FormProcessor;
import info.magnolia.module.mail.MailModule;
import info.magnolia.module.mail.MgnlMailFactory;
import info.magnolia.module.mail.templates.MgnlEmail;
import info.magnolia.module.mail.util.MailUtil;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base implementation for FormProcessors that send mail.
 *
 * @author tmiyar
 */
public abstract class BaseFormProcessorImpl implements FormProcessor {

    private String name;

    private boolean enabled;

    protected void sendMail(String body, String from, String subject, String to, String contentType) throws Exception {

        MgnlMailFactory mgnlMailFactory = MailModule.getInstance().getFactory();

        Map parameters = getParameters();
        List attachments = MailUtil.createAttachmentList();

        MgnlEmail email = mgnlMailFactory.getEmailFromType(parameters, "freemarker", contentType, attachments);
        email.setFrom(from);
        email.setSubject(subject);
        email.setToList(to);
        email.setBody(body);

        MailModule.getInstance().getHandler().sendMail(email);
    }

    protected Map getParameters() {
        Map result = new HashMap();
        for (String key : MgnlContext.getParameters().keySet()) {
            result.put(key, StringUtils.join(MgnlContext.getParameterValues(key), "__"));
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
