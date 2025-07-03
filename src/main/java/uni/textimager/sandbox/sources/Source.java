package uni.textimager.sandbox.sources;

import uni.textimager.sandbox.generators.CategoryNumberMapping;
import uni.textimager.sandbox.generators.Generator;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

public class Source implements SourceInterface {

    @Override
    public <T extends Generator> Collection<T> createGeneratorCombi(Collection<Class<T>> generatorClasses) {
        return List.of();
    }

    @Override
    public <T extends Generator> T createGenerator(Class<T> generatorClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (generatorClass == CategoryNumberMapping.class) {
            System.out.println("test");
        }
        return generatorClass.getDeclaredConstructor().newInstance();
    }


    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Source s = new Source();
        CategoryNumberMapping m = s.createGenerator(CategoryNumberMapping.class);
    }
}
