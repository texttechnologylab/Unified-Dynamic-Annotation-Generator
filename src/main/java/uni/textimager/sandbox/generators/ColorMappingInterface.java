package uni.textimager.sandbox.generators;

import lombok.NonNull;

import java.awt.*;

public interface ColorMappingInterface extends GeneratorInterface {
    void multiplyByColor(@NonNull Color color);
}
