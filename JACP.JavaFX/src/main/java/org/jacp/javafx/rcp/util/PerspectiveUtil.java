package org.jacp.javafx.rcp.util;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacp.api.annotations.component.Component;
import org.jacp.api.annotations.component.DeclarativeView;
import org.jacp.api.annotations.component.Stateless;
import org.jacp.api.annotations.component.View;
import org.jacp.api.annotations.perspective.Perspective;
import org.jacp.api.component.IComponentHandle;
import org.jacp.api.component.IComponentView;
import org.jacp.api.component.ISubComponent;
import org.jacp.api.component.Injectable;
import org.jacp.api.dialog.Scope;
import org.jacp.api.exceptions.AnnotationNotFoundException;
import org.jacp.api.launcher.Launcher;
import org.jacp.javafx.rcp.component.*;
import org.jacp.javafx.rcp.context.JACPContextImpl;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 21.09.13
 * Time: 16:26
 * Contains utility methods for perspective class
 */
public class PerspectiveUtil {

    private static final Logger LOGGER = Logger.getLogger(PerspectiveUtil.class.getName());

    private final Launcher<?> launcher;

    public PerspectiveUtil(final Launcher<?> launcher) {
        this.launcher = launcher;
    }

    /**
     * Returns an PerspectiveUtil instance.
     * @param launcher
     * @return The PerspectiveUtil instance.
     */
    public static PerspectiveUtil getInstance(final Launcher<?> launcher) {
        return new PerspectiveUtil(launcher);
    }

    /**
     * Returns all declared subcomponents for an Perspective annotation.
     * @param perspectiveAnnotation
     * @return  a list of al declared subcomponent instances.
     */
    public List<ISubComponent<EventHandler<Event>, Event, Object>> createSubcomponents(final Perspective perspectiveAnnotation) {
        final List<? extends Injectable> handlers = getInjectAbles(perspectiveAnnotation);
        if (handlers == null) Collections.emptyList();
        return handlers.parallelStream().map(this::mapToSubcomponent).collect(Collectors.toList());
    }

    /**
     * Returns a list of all declared Injectables.
     * @param perspectiveAnnotation
     * @return
     */
    private List<Injectable> getInjectAbles(final Perspective perspectiveAnnotation) {
        final List<String> componentIds = CommonUtil.getNonEmtyStringListFromArray(getComponentIds(perspectiveAnnotation));
        return componentIds.parallelStream().map(this::mapToInjectAbleComponent).collect(Collectors.toList());
    }

    /**
     * Returns an Injectable Class from Classpath by ID.
     * @param id
     * @return
     */
    private Injectable mapToInjectAbleComponent(final String id) {
        final Class componentClass = ClassRegistry.getComponentClassById(id);
        final Scope scope = getCorrectScopeOfComponent(componentClass);
        final Object component = launcher.registerAndGetBean(componentClass, id, scope);
        if (Injectable.class.isAssignableFrom(component.getClass())) {
            return Injectable.class.cast(component);
        } else {
            throw new InvalidParameterException("Only Injectable components are allowed");
        }
    }

    /**
     * Returns the correct scope.
     * @param componentClass
     * @return The Scope of a component.
     */
    private Scope getCorrectScopeOfComponent(final Class componentClass) {
        return componentClass.isAnnotationPresent(Stateless.class) ?
                Scope.PROTOTYPE :
                Scope.SINGLETON;
    }

    /**
     * Returns all component  id's from Perspective annotation
     * @param perspectiveAnnotation
     * @return all declared component id's from perspective annotation.
     */
    private String[] getComponentIds(final Perspective perspectiveAnnotation) {
        if (perspectiveAnnotation != null) {
            return perspectiveAnnotation.components();
        } else {
            throw new IllegalArgumentException("No perspective annotatation found");
        }
    }

    /**
     * Maps an Injectable interface to it's corresponding ISubComponent,
     * This means that the Injectable will be wrapped to it's component type. This can be either a FXComponents, a Stateful- or a StatelessComponent.
     * @param handler
     * @return a subcomponent
     */
    private ISubComponent<EventHandler<Event>, Event, Object> mapToSubcomponent(final Injectable handler) {
        if (IComponentView.class.isAssignableFrom(handler.getClass())) {
            return new EmbeddedFXComponent(IComponentView.class.cast(handler));
        } else if (IComponentHandle.class.isAssignableFrom(handler.getClass())) {
            return handler.getClass().isAnnotationPresent(Stateless.class) ?
                    new EmbeddedStatelessCallbackComponent(IComponentHandle.class.cast(handler)) :
                    new EmbeddedStatefulComponent(IComponentHandle.class.cast(handler));
        } else {
            throw new InvalidParameterException("No useable component interface found");
        }

    }


    /**
     * Set meta attributes defined in annotations.
     *
     * @param component ; the component containing metadata.
     */
    public static void  handleComponentMetaAnnotation(final ISubComponent<EventHandler<Event>, Event, Object> component) {
        final IComponentHandle<?,EventHandler<Event>,Event,Object> handler = component.getComponentHandle();
        if(handler==null)return;
        final Component componentAnnotation = handler.getClass().getAnnotation(Component.class);
        if(componentAnnotation==null)throw new IllegalArgumentException("no @Component annotation found.");
        final DeclarativeView declarativeComponent = handler.getClass()
                .getAnnotation(DeclarativeView.class);
        if (declarativeComponent != null && FXComponent.class.isAssignableFrom(handler.getClass())) {
            handleDeclarativeComponentAnnotation(component,componentAnnotation, declarativeComponent);
            return;
        }
        if(CallbackComponent.class.isAssignableFrom(handler.getClass())){
            handleCallbackAnnotation(component, componentAnnotation);
            log("register CallbackComponent with annotations : " + componentAnnotation.id());
            return;
        }
        final View viewComponent = handler.getClass()
                .getAnnotation(View.class);
        if (viewComponent !=null && FXComponent.class.isAssignableFrom(handler.getClass())) {
            handleComponentAnnotation(component,viewComponent, componentAnnotation);
            log("register component with annotations : " + componentAnnotation.id());
            return;
        }

        if(FXComponent.class.isAssignableFrom(handler.getClass()) && declarativeComponent==null && viewComponent==null) {
            throw new AnnotationNotFoundException("FXComponents must declare either @View or @DeclarativeView! no valid annotation found for component:"+componentAnnotation.id());
        }

    }

    /**
     * Handle all metadata for an declarative component.
     * @param component, The target component.
     * @param componentAnnotation, The @Component annotation.
     * @param declarativeComponent, The @Declarative component annotation.
     */
    private static void handleDeclarativeComponentAnnotation(final ISubComponent<EventHandler<Event>, Event, Object> component, final Component componentAnnotation, final DeclarativeView declarativeComponent) {
        setInitialLayoutTarget(component, declarativeComponent.initialTargetLayoutId());
        setLocale(component, componentAnnotation.localeID());
        setResourceBundleLocation(component, componentAnnotation.resourceBundleLocation());
        handleBaseAttributes(component, componentAnnotation.id(), componentAnnotation.active(),
                componentAnnotation.name());
        AFXComponent.class.cast(component).setViewLocation(declarativeComponent.viewLocation());
    }

    /**
     * Handle a callback components metadata.
     * @param component, The target component.
     * @param callbackAnnotation, The callback annotation.
     */
    private static void handleCallbackAnnotation(final ISubComponent<EventHandler<Event>, Event, Object> component, final Component callbackAnnotation) {
        handleBaseAttributes(component, callbackAnnotation.id(), callbackAnnotation.active(),
                callbackAnnotation.name());
    }

    /**
     * Set all metadata from @View and @Component annotation to the target component.
     * @param component, The target component.
     * @param viewComponent, The @View annotation.
     * @param componentAnnotation, The @Component annotation.
     */
    private static void handleComponentAnnotation(final ISubComponent<EventHandler<Event>, Event, Object> component,final View viewComponent,  final Component componentAnnotation) {
        handleBaseAttributes(component, componentAnnotation.id(), componentAnnotation.active(),
                componentAnnotation.name());
        handleComponentAnnotation(viewComponent,componentAnnotation, component);
    }

    /**
     * set base component members
     *
     * @param component, the component where the base attributes are set
     * @param id, the component id
     * @param active, is component active
     * @param name , the components name
     */
    private static void handleBaseAttributes(final ISubComponent<EventHandler<Event>, Event, Object> component, final String id, final boolean active,
                                      final String name) {
        if (id != null) JACPContextImpl.class.cast(component.getContext()).setId(id);
        component.getContext().setActive(active);
        if (name != null) JACPContextImpl.class.cast(component.getContext()).setName(name);
    }

    /**
     * set component members
     *
     * @param  viewComponent, the @View annotation
     * @param componentAnnotation, the @Component annotation
     * @param component, the component
     */
    private static void handleComponentAnnotation(final View viewComponent, final Component componentAnnotation, final ISubComponent<EventHandler<Event>, Event, Object> component) {
        setInitialLayoutTarget(component, viewComponent.initialTargetLayoutId());
        setLocale(component, componentAnnotation.localeID());
        setResourceBundleLocation(component, componentAnnotation.resourceBundleLocation());
        log("register component with annotations : " + componentAnnotation.id());
    }

    /**
     * Set the resource bundle location to component.
     * @param component, The target component.
     * @param bundleLocation, The bundle location.
     */
    private static void setResourceBundleLocation(final ISubComponent<EventHandler<Event>, Event, Object> component, String bundleLocation) {
        if (component.getResourceBundleLocation() != null)
            component.setResourceBundleLocation(bundleLocation);
    }

    /**
     * Set the locale id to component.
     * @param component, The target component.
     * @param locale , The target value.
     */
    private static void setLocale(final ISubComponent<EventHandler<Event>, Event, Object> component, String locale) {
        if (component.getLocaleID() != null)
            component.setLocaleID(locale);
    }


    /**
     * Set the targetLayout on context object, if none was set before.
     * @param component The target components.
     * @param value The target value.
     */
    private static void setInitialLayoutTarget(final ISubComponent<EventHandler<Event>, Event, Object> component, String value) {
        final String targetLayout = JACPContextImpl.class.cast(component.getContext()).getTargetLayout();
        if (targetLayout==null)
            component.getContext().setTargetLayout(value);
    }

    private static void log(final String message) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(">> " + message);
        }
    }
}
