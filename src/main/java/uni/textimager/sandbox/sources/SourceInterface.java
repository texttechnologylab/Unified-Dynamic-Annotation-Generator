package uni.textimager.sandbox.sources;

import uni.textimager.sandbox.generators.Generator;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public interface SourceInterface {
    <T extends Generator> Collection<T> createGeneratorCombi(Collection<Class<T>> generatorClasses);
    <T extends Generator> T createGenerator(Class<T> generatorClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;
}
