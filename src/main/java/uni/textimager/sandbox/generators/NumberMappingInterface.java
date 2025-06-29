package uni.textimager.sandbox.generators;

public interface NumberMappingInterface extends GeneratorInterface {
    void setNumberSuffix(String numberSuffix);
    void add(double num);
    void subtract(double num);
    void multiply(double num);
    void divideBy(double num);
    void round(int digits);
}
