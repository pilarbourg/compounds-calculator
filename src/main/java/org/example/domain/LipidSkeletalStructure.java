package org.example.domain;

public class LipidSkeletalStructure extends ChemicalCompound {
    private final LipidType lipidType;

    public LipidSkeletalStructure(LipidType lipidType) {
        this.lipidType = lipidType;
        this.formula = LipidTypeCharacteristics.lipidHeadStructure.get(lipidType).getFormula();
        mass = getMass(this.formula);
    }

    public LipidType getLipidType() {
        return lipidType;
    }

    @Override
    public String toString() {
        return "LipidSkeletalStructure{" +
                "lipidType=" + lipidType +
                ", formula=" + formula +
                ", mass=" + mass +
                '}';
    }
}