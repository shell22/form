/**
 * This file Copyright (c) 2010-2017 Magnolia International
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
package info.magnolia.module.form.engine;

import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.rendering.model.RenderingModel;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Used to redirect to a page with the form state token, effectively continuing the form on another page.
 */
public class RedirectWithTokenView implements View {

    private static final long serialVersionUID = 1071927966275788202L;
    private final String uuid;
    private final String token;

    public RedirectWithTokenView(Node content, String token) {
        this.uuid = NodeUtil.getNodeIdentifierIfPossible(content);
        this.token = token;
    }

    public RedirectWithTokenView(String uuid, String token) {
        this.uuid = uuid;
        this.token = token;
    }

    @Override
    public String execute() throws RepositoryException, IOException {
        String workspace = ((WebContext) MgnlContext.getInstance()).getAggregationState().getRepository();
        FormStateUtil.sendRedirectWithToken(uuid, token, workspace);
        return RenderingModel.SKIP_RENDERING;
    }
}
