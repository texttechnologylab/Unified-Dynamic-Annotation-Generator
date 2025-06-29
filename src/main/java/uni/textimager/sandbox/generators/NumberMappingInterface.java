package uni.textimager.sandbox.generators;

public interface NumberMappingInterface {
    void setNumberSuffix();
    void add(int num);
    void add(double num);
    void subtract(int num);
    void subtract(double num);
    void multiply(int num);
    void multiply(double num);
    void divideBy(int num);
    void divideBy(double num);
    void round(int digits);
}
