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
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.wrapper.I18nNodeWrapper;
import info.magnolia.module.form.processors.FormProcessorFailedException;
import info.magnolia.module.form.templates.components.multistep.NavigationUtils;
import info.magnolia.rendering.context.RenderingContext;

import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

/**
 * Implements a rendering and form submission algorithm that keeps state in session for multiple pages. Subclasses
 * implement extension hooks to provide the actual views used.
 */
public abstract class FormEngine {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private FormState formState;

    protected boolean redirectWithParams = false;

    public RenderingContext context;

    @Inject
    public FormEngine(RenderingContext context){
        this.context = context;
    }

    public View handleRequest(Node content) throws RepositoryException {

        if (!isFormSubmission()) {
            String formStateToken;
            try {
                formStateToken = getFormStateToken();
            } catch (FormStateTokenMissingException e) {
                return handleTokenMissing();
            }

            try {
                formState = getFormState(formStateToken);
            } catch (NoSuchFormStateException e) {
                return handleNoSuchFormState(e.getToken());
            }

            //remove conditional states that will be stepped over
            Iterator iterator = formState.getSteps().entrySet().iterator();
            int currentlyExecutingStep = formState.getCurrentlyExecutingStep();
            FormStepState nextStep = formState.getStep(NodeUtil.getNodeIdentifierIfPossible(content));
            int counter = 0;
            while (iterator.hasNext()) {
                counter++;
                Map.Entry pair = (Map.Entry) iterator.next();
                if (pair.getValue().equals(nextStep)) {
                    break;
                }
                if (counter > currentlyExecutingStep) {
                    iterator.remove();
                }
            }


            View view = formState.getView();
            formState.setView(null);
            if (view == null) {
                return getFormView(nextStep);
            }
            if (formState.isEnded()) {
                destroyFormState();
            }
            return view;
        }

        String formStateToken;
        final Node mainContent = new I18nNodeWrapper(context.getMainContent());
        try {
            formStateToken = getFormStateToken();
        } catch (FormStateTokenMissingException e) {
            // Cant post without a token... should never happen
            // Redirect the user to this page
            return new RedirectView(mainContent);
        }

        try {
            formState = getFormState(formStateToken);
        } catch (NoSuchFormStateException e) {
            return handleNoSuchFormStateOnSubmit(e.getToken());
        }

        View view = null;
        if(isBackButton()) {
            log.debug("User pressed back button. Currently executing step number is {}", formState.getCurrentlyExecutingStep());
            String pageUuid = getPreviousPage();
            if(pageUuid == null) {
                return getFormView(formState.getStep(NodeUtil.getNodeIdentifierIfPossible(content)));
            }
            //update current step count
            formState.setCurrentlyExecutingStep(formState.getCurrentlyExecutingStep()-1);
            log.debug("Updated currently executing step. Now is {}", formState.getCurrentlyExecutingStep());
            Node parent = new I18nNodeWrapper(NavigationUtils.findParagraphParentPage(NodeUtil.getNodeByIdentifier(content.getSession().getWorkspace().getName(), pageUuid)));
            if (isRedirectWithParams()) {
                return new RedirectWithTokenAndParametersView(parent, formStateToken);
            }
            return new RedirectWithTokenView(parent, formStateToken);
        }
        view = processSubmission(content);

        formState.setView(null);

        if (view instanceof EndView) {
            destroyFormState();
            return view;
        }

        if (view instanceof RedirectWithTokenView) {
            return view;
        }

        if (view instanceof RedirectWithTokenAndParametersView) {
            return view;
        }

        formState.setView(view);
        if (isRedirectWithParams()) {
            return new RedirectWithTokenAndParametersView(mainContent, formState.getToken());
        }
        return new RedirectWithTokenView(mainContent, formState.getToken());
    }

    public boolean isRedirectWithParams() {
        return redirectWithParams;
    }

    protected FormState createAndSetFormState(String formToken) {
        return FormStateUtil.createAndSetFormState(formToken);
    }

    /**
     * @deprecated since 2.3.2 - use {@link #createAndSetFormState(String)} instead.
     */
    @Deprecated
    protected FormState createAndSetFormState() {
        return FormStateUtil.createAndSetFormState();
    }

    protected String getFormStateToken() throws FormStateTokenMissingException, RepositoryException {
        return FormStateUtil.getFormStateToken();
    }

    protected FormState getFormState(String formStateToken) throws NoSuchFormStateException {
        return FormStateUtil.getFormState(formStateToken);
    }

    protected void destroyFormState() {
        FormStateUtil.destroyFormState(formState);
    }

    /**
     * Performs the processing of submitted values. If this method returns a RedirectView this is treated like an exit
     * and the formState is removed from session.
     *
     * @param content
     */
    private View processSubmission(Node content) throws RepositoryException {

        // Validate the input parameters and collect FormField instances
        FormStepState step = getFormDataBinder().bindAndValidate(content);

        // Add the submitted fields to formState
        formState.addStep(step);

        // If validation failed proceed and render page
        if (!step.isValid()) {
            return getValidationFailedView(step);
        }

        // Validation succeeded

        View validationSuccessfulView = getValidationSuccessfulView(formState);
        if (validationSuccessfulView != null) {
            //on success  we move to the next step, increase count by one
            formState.setCurrentlyExecutingStep(formState.getCurrentlyExecutingStep()+1);
            log.debug("Updated currently executing step. Now is {}", formState.getCurrentlyExecutingStep());
            return validationSuccessfulView;
        }

        if (StringUtils.isNotEmpty(MgnlContext.getParameter("field"))) {
            return getValidationFailedView(step);
        }

        formState.setEnded(true);

        // Execute processors
        try {
            executeProcessors(getFormState().getValues());
        } catch (FormProcessorFailedException e) {
            return getProcessorFailedView(e.getMessage());
        } catch (Exception e) {
            log.error("FormProcessor threw unexpected exception", e);
            return getProcessorFailedView(null);
        }
        // Render page with success message
        return getSuccessView();
    }

    public FormState getFormState() {
        return formState;
    }

    public void setFormState(FormState formState){
        this.formState = formState;
    }

    protected boolean isFormSubmission() {
        return MgnlContext.getWebContext().getRequest().getMethod().equals("POST");
    }

    /**
     * @return true if the user hit the back button in the form. This relies on the presence of a request param called
     * <code>mgnlFormBackButtonPressed</code>. Check formSubmit.ftl script to see how it works on the client-side.
     */
    protected boolean isBackButton() {
        return MgnlContext.getWebContext().getRequest().getParameter("mgnlFormBackButtonPressed") != null;
    }

    /**
     * Called when the form is to be rendered and there's no token provided. The default behaviour is to return a view
     * that renders the form without creating a new form state.
     */
    protected View handleTokenMissing() throws RepositoryException {
        return getFormView(null);
    }

    /**
     * Called when the form is to be rendered.
     *
     * @param step is null when we render the page for the first time. I.e. when no validation has taken place.
     */
    protected abstract View getFormView(FormStepState step) throws RepositoryException;

    /**
     * Called when validation has been performed and there were no validation errors. Override this method to add multi
     * step support.
     */
    protected View getValidationSuccessfulView(FormState formState) throws RepositoryException {
        // Redirect to the next step if there is one
        String nextStep = getNextPage();
        if (StringUtils.isNotEmpty(nextStep)) {
            if (isRedirectWithParams()) {
                return new RedirectWithTokenAndParametersView(nextStep, formState.getToken());
            }
            return new RedirectWithTokenView(nextStep, formState.getToken());
        }
        return null;
    }

    /**
     * Returns the UUID of the page to redirect to when validation succeeds or null to proceed to executing processors.
     */
    protected String getNextPage() throws RepositoryException {
        return null;
    }

    /**
     * Called when validation fails.
     */
    protected View getValidationFailedView(FormStepState step) throws RepositoryException {
        return getFormView(step);
    }

    /**
     * Called when a processor failed.
     *
     * @param errorMessage can be null in case another exception than FormProcessorFailedException is thrown by processor.
     */
    protected abstract View getProcessorFailedView(String errorMessage) throws RepositoryException;

    /**
     * Called when validation was successful and all processors executed successfully.
     */
    protected abstract View getSuccessView() throws RepositoryException;

    /**
     * Called when the form was to be rendered for a supplied form token but there is no state in the session. This
     * typically happens when the user navigates back after having completed the form or if the user returns later
     * via a bookmark or via browser history.
     */
    protected abstract View handleNoSuchFormState(String formStateToken) throws RepositoryException;

    /**
     * Called when a submission occurs with a form state token but there is no formState in session. This typically
     * happens when the user has waited so long to complete the form that the session timed out.
     */
    protected View handleNoSuchFormStateOnSubmit(String formStateToken) throws RepositoryException {
        // Redirect to the current page _with_ the invalid token, this will render an error message.
        return new RedirectWithTokenView(context.getMainContent(), formStateToken);
    }

    protected abstract FormDataBinder getFormDataBinder();

    protected abstract void executeProcessors(Map<String, Object> parameters) throws RepositoryException, FormProcessorFailedException;

    /**
     * Returns the UUID of the previous page or null if there is no such page (i.e. there's one or no steps in current form state).
     */
    protected String getPreviousPage() {
        if(formState.getSteps().isEmpty() || formState.getCurrentlyExecutingStep() == 0) {
            return null;
        }
        String uuid = Iterables.get(formState.getSteps().entrySet(), formState.getCurrentlyExecutingStep() -1).getKey();
        log.debug("returning previous page uuid {}", uuid);

        return uuid;
    }
}
