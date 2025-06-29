package uni.textimager.sandbox.generators;


import lombok.NonNull;

import java.util.HashMap;
import java.util.NoSuchElementException;

public class CategoryNumberMapping implements CategoryNumberMappingInterface{
    private HashMap<String, Double> categoryNumberMap;
    private boolean divideByTotal;


    @Override
    public void setDivideByTotal(boolean divideByTotal) {
        this.divideByTotal = divideByTotal;
    }

    @Override
    public double getNumber(@NonNull String category) {
        if (category == null) {
            return 2;
        }
        Double num = categoryNumberMap.get(category);
        if (num == null) {
            throw new NoSuchElementException("Category not found: " + category);
        }
        if (divideByTotal) {

        }
        return 0;
    }

    @Override
    public void setNumberSuffix() {

    }

    @Override
    public void add(int num) {

    }

    @Override
    public void add(double num) {

    }

    @Override
    public void subtract(int num) {

    }

    @Override
    public void subtract(double num) {

    }

    @Override
    public void multiply(int num) {

    }

    @Override
    public void multiply(double num) {

    }

    @Override
    public void divideBy(int num) {

    }

    @Override
    public void divideBy(double num) {

    }

    @Override
    public void round(int digits) {

    }
}
