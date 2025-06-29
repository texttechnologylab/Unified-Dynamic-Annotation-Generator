package uni.textimager.sandbox.generators;


import lombok.NonNull;

import java.util.HashMap;
import java.util.NoSuchElementException;

public class CategoryNumberMapping extends Generator implements CategoryNumberMappingInterface{
    private HashMap<String, Double> categoryNumberMap;

    private boolean fractionModeEnabled;
    private double fractionMode;
    private double total;
    private String numberSuffix;


    public CategoryNumberMapping(HashMap<String, Double> categoryNumberMap) {
        this.categoryNumberMap = categoryNumberMap;
        fractionModeEnabled = false;
        numberSuffix = null;
        calculateTotal();
    }

    public CategoryNumberMapping(CategoryNumberMapping copyOf) {
        this.categoryNumberMap = new HashMap<>(copyOf.categoryNumberMap);
        this.fractionModeEnabled = copyOf.fractionModeEnabled;
        this.fractionMode = copyOf.fractionMode;
        this.total = copyOf.total;
        this.numberSuffix = copyOf.numberSuffix;
    }

    @Override
    public void setFractionMode(double fractionMode) {
        fractionModeEnabled = true;
        this.fractionMode = fractionMode;
    }

    @Override
    public String getNumberString(@NonNull String category) {
        if (numberSuffix == null) {
            return Double.toString(getNumber(category));
        }
        return getNumber(category) + numberSuffix;
    }

    @Override
    public double getNumber(@NonNull String category) {
        Double num = categoryNumberMap.get(category);
        if (num == null) {
            throw new NoSuchElementException("Category not found: " + category);
        }
        if (fractionModeEnabled) {
            return num * (fractionMode / total);
        }
        return num;
    }

    @Override
    public double getTotal() {
        if (fractionModeEnabled) {
            return fractionMode;
        }
        return total;
    }

    @Override
    public void setNumberSuffix(String numberSuffix) {
        this.numberSuffix = numberSuffix;
    }

    @Override
    public void add(double num) {
        categoryNumberMap.replaceAll((key, value) -> value + num);
        calculateTotal();
    }

    @Override
    public void subtract(double num) {
        categoryNumberMap.replaceAll((key, value) -> value - num);
        calculateTotal();
    }

    @Override
    public void multiply(double num) {
        categoryNumberMap.replaceAll((key, value) -> value * num);
        calculateTotal();
    }

    @Override
    public void divideBy(double num) {
        categoryNumberMap.replaceAll((key, value) -> value / num);
        calculateTotal();
    }

    @Override
    public void round(int digits) {

    }

    @Override
    public CategoryNumberMapping copy() {
        return new CategoryNumberMapping(this);
    }

    private void calculateTotal() {
        total = categoryNumberMap.values().stream().mapToDouble(Double::doubleValue).sum();
    }
}
