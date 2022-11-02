package com.openathena;
class geodataAxisParams {
    double start;
    double end;
    double stepwiseIncrement;
    long numOfSteps;

    public void calcEndValue() {
        end = start + stepwiseIncrement * numOfSteps;
    }
}
