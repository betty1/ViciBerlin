package de.beuth.bva.viciberlin.model;

/**
 * Created by betty on 02/02/16.
 */
public class ComparableZipcode implements Comparable<ComparableZipcode> {

    float deviation;
    String name;

    public ComparableZipcode(String name, float deviation) {
        this.deviation = deviation;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public float getDeviation() {
        return deviation;
    }

    @Override
    public int compareTo(ComparableZipcode another) {
        if(this.deviation < another.deviation){
            return -1;
        } else if(this.deviation > another.deviation){
            return 1;
        }
        return 0;
    }

    @Override
    public String toString(){
        return name + ": " + deviation;
    }
}
