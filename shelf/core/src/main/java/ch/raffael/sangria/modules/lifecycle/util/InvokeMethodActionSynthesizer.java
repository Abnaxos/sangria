package ch.raffael.sangria.modules.lifecycle.util;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

import ch.raffael.sangria.dynamic.asm.Opcodes;
import ch.raffael.sangria.dynamic.asm.Type;
import ch.raffael.sangria.dynamic.asm.commons.GeneratorAdapter;

import ch.raffael.sangria.ConfigurationRuntimeException;
import ch.raffael.sangria.modules.lifecycle.Action;
import ch.raffael.sangria.dynamic.ClassCache;
import ch.raffael.sangria.dynamic.ClassSynthesizer;
import ch.raffael.sangria.dynamic.MemberCache;
import ch.raffael.sangria.dynamic.ReflectionException;

import static ch.raffael.sangria.dynamic.asm.Type.getType;
import static ch.raffael.sangria.dynamic.asm.commons.Method.getMethod;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class InvokeMethodActionSynthesizer {

    private final MemberCache<Method, ActionSynthesizer> cache =
            ClassCache.builder().concurrencyLevel(1).newMemberCache(ActionSynthesizer::new);

    private final Injector injector;

    public InvokeMethodActionSynthesizer(Injector injector) {
        this.injector = injector;
    }

    public Action actionFor(Method method) {
        return cache.get(method).newInstance(injector);
    }

    private static class ActionSynthesizer extends ClassSynthesizer {

        public static final Type T_PROVIDERS = getType(Provider[].class);
        public static final String F_PROVIDERS = "providers";

        private final ActionGenerator actionGenerator = new ActionGenerator();

        private final Method targetMethod;
        private final MethodHandle targetHandle;

        private final Key[] keys;

        private ActionSynthesizer(Method targetMethod) {
            super(targetMethod.getDeclaringClass().getClassLoader());
            linkGenerator(actionGenerator);
            this.targetMethod = targetMethod;
            try {
                targetHandle = MethodHandles.lookup().unreflect(targetMethod);
            }
            catch ( IllegalAccessException e ) {
                throw new ReflectionException(e);
            }
            Parameter[] parameters = targetMethod.getParameters();
            keys = new Key[parameters.length];
            for ( int i = 0; i < parameters.length; i++ ) {
                Parameter param = parameters[i];
                Annotation annotation = null;
                for ( Annotation a : param.getAnnotations() ) {
                    if ( a.annotationType().getAnnotation(BindingAnnotation.class) != null ) {
                        if ( annotation != null ) {
                            throw new ConfigurationRuntimeException(targetMethod + ", parameter " + param.getName() + ": Multiple binding annotations");
                        }
                        annotation = a;
                    }
                }
                if ( annotation == null ) {
                    keys[i] = Key.get(TypeLiteral.get(param.getParameterizedType()));
                }
                else {
                    keys[i] = Key.get(TypeLiteral.get(param.getParameterizedType()), annotation);
                }
            }
        }

        private Action newInstance(Injector injector) {
            Provider[] providers = new Provider[keys.length];
            for ( int i = 0; i < providers.length; i++ ) {
                providers[i] = injector.getProvider(keys[i]);
            }
            return factory(actionGenerator, $Factory.class).newInstance(providers);
        }

        private class ActionGenerator extends ClassGenerator implements Opcodes {

            public ActionGenerator() {
                super(classifiedType(targetMethod.getDeclaringClass(), "LifecycleAction"));
                configure()
                        .interfaces(Action.class)
                        .factoryBaseClass($Factory.class);
                prebind("targetMethod", targetHandle);
            }

            @Override
            protected void generate() {
                field(ACC_FINAL, F_PROVIDERS, T_PROVIDERS).visitEnd();
                genConstructor();
                genPerform();
            }

            private void genConstructor() {
                GeneratorAdapter gen = method(0, getMethod("void <init>(" + Provider.class.getName() + "[])"));
                gen.visitCode();
                gen.loadThis();
                gen.invokeConstructor(getType(Object.class), getMethod("void <init>()"));
                gen.loadThis();
                gen.loadArg(0);
                gen.putField(targetType(), F_PROVIDERS, T_PROVIDERS);
                gen.returnValue();
                gen.endMethod();
            }

            private void genPerform() {
                GeneratorAdapter gen = method(ACC_PUBLIC | ACC_FINAL, getMethod("void perform(Object)"));
                gen.visitCode();
                gen.loadArg(0);
                for ( int i = 0; i < keys.length; i++ ) {
                    gen.loadThis();
                    gen.getField(targetType(), F_PROVIDERS, T_PROVIDERS);
                    gen.push(i);
                    gen.arrayLoad(getType(Provider.class));
                    gen.invokeVirtual(getType(Provider.class), getMethod("Object get()"));
                }
                gen.invokeDynamic("targetMethod", targetHandle.type().toMethodDescriptorString(), PreboundClassValues.CONSTANT_BOOTSTRAP);
                gen.returnValue();
            }

        }

    }

    public static abstract class $Factory {
        protected $Factory() {
        }
        protected abstract Action newInstance(Provider[] providers);
    }

}
