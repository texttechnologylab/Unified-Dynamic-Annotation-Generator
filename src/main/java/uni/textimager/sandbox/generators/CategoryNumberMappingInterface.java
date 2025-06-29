package uni.textimager.sandbox.generators;


public interface CategoryNumberMappingInterface extends CategoryMappingInterface, NumberMappingInterface {
    void setDivideByTotal(boolean divideByTotal);
    double getNumber(String category);
}
