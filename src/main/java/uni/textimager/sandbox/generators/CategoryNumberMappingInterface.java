package uni.textimager.sandbox.generators;


import java.awt.*;

public interface CategoryNumberMappingInterface extends CategoryMappingInterface, NumberMappingInterface {
    void setFractionMode(double fractionMode);
    String getNumberString(String category);
    double getNumber(String category);
    double getTotal();
    String generateJSONCategoricalChart(Color fixedColor);
    String generateJSONCategoricalChart();
}
