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
package info.magnolia.module.form.processors;

import info.magnolia.jcr.util.PropertyUtil;

import java.util.Map;

import javax.jcr.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends a contact mail, any files submitted are sent as attachments.
 *
 * @author tmiyar
 */
public class SendContactEMailProcessor extends AbstractEMailFormProcessor {

    private static final Logger log = LoggerFactory.getLogger(SendContactEMailProcessor.class);

    @Override
    public void internalProcess(Node content, Map<String, Object> parameters) throws FormProcessorFailedException {
        try {
            String from = PropertyUtil.getString(content,"contactMailFrom");
            String subject = PropertyUtil.getString(content,"contactMailSubject");
            String to = PropertyUtil.getString(content,"contactMailTo");
            String contentType = PropertyUtil.getString(content,"contentType");
            //For control edit and new control DialogRadioSwitch, keep old param for compatibility
            String body = PropertyUtil.getString(content, "contactMailBody", PropertyUtil.getString(content, "contentType"+contentType));

            sendMail(body, from, subject, to, contentType, parameters);

        } catch (Exception e) {
            log.error("Contact email", e);
            throw new FormProcessorFailedException("SendContactEMailProcessor.errorMessage");
        }
    }
}
