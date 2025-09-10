package com.github.istin.dmtools.job;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.di.ServerManagedIntegrationsModule;
import dagger.Component;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.inject.Singleton;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbstractJob<T, R> implements Job<T, R>{

    private static final Logger logger = LogManager.getLogger(AbstractJob.class);

    @Getter
    @Setter
    protected AI ai;
    

    /**
     * Default constructor
     */
    public AbstractJob() {
    }
    
    /**
     * Constructor with AI
     * @param ai The AI instance to use
     */
    public AbstractJob(AI ai) {
        this.ai = ai;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getParamsClass() {
        return (Class<T>) getTemplateParameterClass(getClass());
    }

    private Class<?> getTemplateParameterClass(Class<?> clazz) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length > 0) {
                return (Class<?>) typeArguments[0];
            }
        }
        throw new IllegalArgumentException("Class does not have a template parameter.");
    }
    
    @Override
    public R runJob(T params) throws Exception {
        return executeJob(params);
    }
    
    /**
     * Executes the job with the given parameters.
     * @param params The job parameters
     * @throws Exception If an error occurs
     */
    protected R executeJob(T params) throws Exception {
        // Default implementation calls the old runJob method for backwards compatibility
        // This allows existing job implementations to continue working without changes
        return runJobImpl(params);
    }
    
    /**
     * Legacy implementation of runJob.
     * This method is called by executeJob for backwards compatibility.
     * @param params The job parameters
     * @throws Exception If an error occurs
     */
    protected R runJobImpl(T params) throws Exception {
        // This method should be overridden by subclasses that don't implement executeJob
        throw new UnsupportedOperationException("Either executeJob or runJobImpl must be implemented");
    }
    
    /**
     * Initializes the job for the specified execution mode with optional resolved integrations.
     * This method handles dependency injection based on the execution mode.
     * 
     * @param mode The execution mode (STANDALONE or SERVER_MANAGED)
     * @param resolvedIntegrations Pre-resolved integrations (null for standalone mode)
     */
    protected void initializeForMode(ExecutionMode mode, JSONObject resolvedIntegrations) {
        if (mode == ExecutionMode.STANDALONE) {
            // Use existing Dagger components (current behavior)
            // Each subclass will handle its own injection in standalone mode
            initializeStandalone();
        } else if (mode == ExecutionMode.SERVER_MANAGED) {
            // Use dynamic module with pre-resolved integrations
            // Each subclass will handle its own injection in server-managed mode
            initializeServerManaged(resolvedIntegrations);
        }
    }
    
    /**
     * Initializes the job for standalone execution.
     * Subclasses should override this to provide their specific injection logic.
     */
    protected void initializeStandalone() {
        // Default implementation - subclasses should override this
        // For backward compatibility, jobs that don't override this will work as before
    }
    
    /**
     * Initializes the job for server-managed execution.
     * Subclasses should override this to provide their specific injection logic.
     * @param resolvedIntegrations Pre-resolved integrations from the server
     */
    protected void initializeServerManaged(JSONObject resolvedIntegrations) {
        // Default implementation - subclasses should override this
        throw new UnsupportedOperationException("Server-managed execution not implemented for this job type");
    }
    
    /**
     * Component interface for server-managed execution.
     * Jobs that support server-managed mode should create a similar component.
     */
    @Singleton
    @Component(modules = {ServerManagedIntegrationsModule.class})
    public interface ServerManagedComponent {
        // Injection methods will be defined by specific job implementations
    }
    
    // =====================================================
    // JavaScript Execution Support
    // =====================================================
    
    /**
     * Creates a fluent JavaScript executor for clean execution syntax
     * 
     * Usage:
     * js(jsCode)
     *   .mcp(trackerClient, ai, confluence, sourceCode)
     *   .withJobContext(params, ticket, response)
     *   .with("initiator", "user@example.com")
     *   .execute();
     * 
     * @param jsCode JavaScript code to execute
     * @return JavaScriptExecutor for fluent configuration
     */
    protected JavaScriptExecutor js(String jsCode) {
        return new JavaScriptExecutor(jsCode);
    }
}
