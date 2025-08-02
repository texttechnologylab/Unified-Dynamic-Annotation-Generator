package uni.textimager.sandbox.generators;


public interface CategoryNumberMappingInterface extends CategoryMappingInterface, NumberMappingInterface {
    void setFractionMode(double fractionMode);
    String getNumberString(String category);
    double getNumber(String category);
    double getTotal();
    String generateJSONCategoricalChart();
}
