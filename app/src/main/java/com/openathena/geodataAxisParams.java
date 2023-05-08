package com.openathena;

import java.io.Serializable;

class geodataAxisParams implements Serializable {
    double start;
    double end;
    double stepwiseIncrement;
    long numOfSteps;

    public void calcEndValue() {
        end = start + stepwiseIncrement * (numOfSteps - 1);
    }
}
