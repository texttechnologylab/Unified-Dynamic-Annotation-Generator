package uni.textimager.sandbox.sources;

import uni.textimager.sandbox.generators.Generator;

import java.util.Collection;

public interface SourceInterface {
    <T extends Generator> Collection<T> createGenerators();
}
