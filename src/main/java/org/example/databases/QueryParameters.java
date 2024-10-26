package org.example.databases;

import org.example.domain.*;
import org.example.exceptions.FattyAcidCreation_Exception;
import org.example.exceptions.InvalidFormula_Exception;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.SQLException;
import java.util.*;

public class QueryParameters {
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public QueryParameters() {
        new Database();
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(Database.dataSource);
    }

    public LinkedHashSet<MSLipid> findLipidsInDatabase(LipidType lipidType, double precursorIonMZ, Set<Double> neutralLossAssociatedIonMZs, String adduct) throws InvalidFormula_Exception, SQLException, FattyAcidCreation_Exception {
        Set<Double> fattyAcidMasses = Database.calculateFattyAcidMassesFromNeutralLosses(precursorIonMZ, neutralLossAssociatedIonMZs, adduct);
        LipidSkeletalStructure lipidSkeletalStructure = new LipidSkeletalStructure(lipidType);
        Formula formulaSkeleton = new Formula(lipidSkeletalStructure.getFormula().toString());

        List<FattyAcid> fattyAcids = new ArrayList<>();
        for (double fattyAcidMass : fattyAcidMasses) {
            Iterator<FattyAcid> iterator = Database.getFattyAcidsFromDatabase(fattyAcidMass).iterator();
            if (iterator.hasNext()) {
                FattyAcid fattyAcid = iterator.next();
                fattyAcids.add(fattyAcid);
                formulaSkeleton.addFattyAcidToFormula(fattyAcid);
            } else {
                System.err.println("Fatty Acid with mass " + fattyAcidMass + " not found");
            }
        }

        StringBuilder queryBuilder = new StringBuilder(
                """
                        SELECT
                            cv.compound_id,
                            cv.compound_name,
                            cv.formula,
                            cv.mass,
                            cv.num_chains,
                            cv.number_carbons,
                            cv.double_bonds,
                            cv.lipid_type
                        FROM
                            (SELECT *
                             FROM compounds_view
                             WHERE lipid_type LIKE :lipidType
                             AND number_carbons = :totalNumCarbons
                             AND double_bonds = :totalDoubleBonds
                             AND formula = :formulaSkeleton) AS cv
                        INNER JOIN
                            compound_chain ON cv.compound_id = compound_chain.compound_id
                        INNER JOIN
                            chains ON chains.chain_id = compound_chain.chain_id
                        """);
        MapSqlParameterSource paramMap = new MapSqlParameterSource();
        paramMap.addValue("lipidType", "%" + lipidType + "(%");

        switch (lipidType) {
            case TG -> {
                return findTGsInDatabase(queryBuilder, paramMap, fattyAcids, lipidType, precursorIonMZ, adduct, formulaSkeleton);
            }
            case DG -> {
                return findDGsInDatabase(queryBuilder, paramMap, fattyAcids, formulaSkeleton);
            }
            case MG -> {
                return findMGsInDatabase(queryBuilder, paramMap, fattyAcids, formulaSkeleton);
            }
        }
        return null;
    }

    private LinkedHashSet<MSLipid> findMGsInDatabase(StringBuilder queryBuilder, MapSqlParameterSource paramMap, List<FattyAcid> fattyAcids, Formula formulaSkeleton) {
        queryBuilder.append("""
                WHERE
                    (chains.num_carbons, chains.double_bonds) IN ((:numCarbons1, :doubleBonds1))
                GROUP BY
                    cv.compound_id, cv.compound_name, cv.formula, cv.mass, cv.num_chains, cv.number_carbons, cv.double_bonds, cv.lipid_type
                HAVING\s
                    SUM(CASE WHEN chains.num_carbons = :numCarbons1 AND chains.double_bonds = :doubleBonds1 THEN 1 ELSE 0 END) = 1;""");
        paramMap.addValue("totalNumCarbons", fattyAcids.get(0).getCarbonAtoms());
        paramMap.addValue("totalDoubleBonds", fattyAcids.get(0).getDoubleBonds());
        paramMap.addValue("formulaSkeleton", formulaSkeleton.toString());
        paramMap.addValue("numCarbons1", fattyAcids.get(0).getCarbonAtoms());
        paramMap.addValue("doubleBonds1", fattyAcids.get(0).getDoubleBonds());
        return new LinkedHashSet<>(namedJdbcTemplate.query(queryBuilder.toString(), paramMap, new MSLipidRowMapper()));
    }

    private LinkedHashSet<MSLipid> findDGsInDatabase(StringBuilder queryBuilder, MapSqlParameterSource paramMap, List<FattyAcid> fattyAcids, Formula formulaSkeleton) throws InvalidFormula_Exception {
        switch (fattyAcids.size()) {
            case 1:
                formulaSkeleton.addFattyAcidToFormula(fattyAcids.get(0));
                queryBuilder.append("""
                        WHERE
                            (chains.num_carbons, chains.double_bonds) IN ((:numCarbons1, :doubleBonds1))
                            AND compound_chain.number_chains IN (2)
                        GROUP BY
                            cv.compound_id, cv.compound_name, cv.formula, cv.mass, cv.num_chains, cv.number_carbons, cv.double_bonds, cv.lipid_type
                        HAVING\s
                            SUM(CASE WHEN chains.num_carbons = :numCarbons1 AND chains.double_bonds = :doubleBonds1 THEN 1 ELSE 0 END) = 1;""");
                paramMap.addValue("totalNumCarbons", fattyAcids.get(0).getCarbonAtoms() * 2);
                paramMap.addValue("totalDoubleBonds", fattyAcids.get(0).getDoubleBonds() * 2);
                paramMap.addValue("formulaSkeleton", formulaSkeleton.toString());
                paramMap.addValue("numCarbons1", fattyAcids.get(0).getCarbonAtoms());
                paramMap.addValue("doubleBonds1", fattyAcids.get(0).getDoubleBonds());
                return new LinkedHashSet<>(namedJdbcTemplate.query(queryBuilder.toString(), paramMap, new MSLipidRowMapper()));
            case 2:
                queryBuilder.append("""
                        WHERE
                            (chains.num_carbons, chains.double_bonds) IN ((:numCarbons1, :doubleBonds1), (:numCarbons2, :doubleBonds2))
                        GROUP BY
                            cv.compound_id, cv.compound_name, cv.formula, cv.mass, cv.num_chains, cv.number_carbons, cv.double_bonds, cv.lipid_type
                        HAVING\s
                            SUM(CASE WHEN chains.num_carbons = :numCarbons1 AND chains.double_bonds = :doubleBonds1 THEN 1 ELSE 0 END) = 1
                            AND SUM(CASE WHEN chains.num_carbons = :numCarbons2 AND chains.double_bonds = :doubleBonds2 THEN 1 ELSE 0 END) = 1;""");
                paramMap.addValue("totalNumCarbons", fattyAcids.get(0).getCarbonAtoms() + fattyAcids.get(1).getCarbonAtoms());
                paramMap.addValue("totalDoubleBonds", fattyAcids.get(0).getDoubleBonds() + fattyAcids.get(1).getDoubleBonds());
                paramMap.addValue("formulaSkeleton", formulaSkeleton.toString());
                paramMap.addValue("numCarbons1", fattyAcids.get(0).getCarbonAtoms());
                paramMap.addValue("doubleBonds1", fattyAcids.get(0).getDoubleBonds());
                paramMap.addValue("numCarbons2", fattyAcids.get(1).getCarbonAtoms());
                paramMap.addValue("doubleBonds2", fattyAcids.get(1).getDoubleBonds());
                return new LinkedHashSet<>(namedJdbcTemplate.query(queryBuilder.toString(), paramMap, new MSLipidRowMapper()));
        }
        return null;
    }

    private LinkedHashSet<MSLipid> findTGsInDatabase(StringBuilder queryBuilder, MapSqlParameterSource paramMap, List<FattyAcid> fattyAcids, LipidType lipidType, double precursorIon, String adduct, Formula formulaSkeleton) throws InvalidFormula_Exception {
        LipidSkeletalStructure lipidSkeletalStructure = new LipidSkeletalStructure(lipidType);
        switch (fattyAcids.size()) {
            case 1:
                formulaSkeleton.addFattyAcidToFormula(fattyAcids.get(0));
                formulaSkeleton.addFattyAcidToFormula(fattyAcids.get(0));
                queryBuilder.append("""
                        WHERE
                            (chains.num_carbons, chains.double_bonds) IN ((:numCarbons1, :doubleBonds1))
                            AND compound_chain.number_chains IN (3)
                        GROUP BY
                            cv.compound_id, cv.compound_name, cv.formula, cv.mass, cv.num_chains, cv.number_carbons, cv.double_bonds, cv.lipid_type
                        HAVING\s
                            SUM(CASE WHEN chains.num_carbons = :numCarbons1 AND chains.double_bonds = :doubleBonds1 THEN 1 ELSE 0 END) = 1;""");
                paramMap.addValue("totalNumCarbons", fattyAcids.get(0).getCarbonAtoms() * 3);
                paramMap.addValue("totalDoubleBonds", fattyAcids.get(0).getDoubleBonds() * 3);
                paramMap.addValue("formulaSkeleton", formulaSkeleton.toString());
                paramMap.addValue("numCarbons1", fattyAcids.get(0).getCarbonAtoms());
                paramMap.addValue("doubleBonds1", fattyAcids.get(0).getDoubleBonds());
                return new LinkedHashSet<>(namedJdbcTemplate.query(queryBuilder.toString(), paramMap, new MSLipidRowMapper()));
            case 2:
                fattyAcids = getFAsFromMass(lipidType, precursorIon, fattyAcids, adduct);
                lipidSkeletalStructure = new LipidSkeletalStructure(lipidType);
                formulaSkeleton = new Formula(lipidSkeletalStructure.getFormula().toString());
                formulaSkeleton.addFattyAcidToFormula(Objects.requireNonNull(fattyAcids).get(0));
                formulaSkeleton.addFattyAcidToFormula(fattyAcids.get(1));
                formulaSkeleton.addFattyAcidToFormula(fattyAcids.get(2));

                queryBuilder.append("""
                        WHERE
                            (chains.num_carbons, chains.double_bonds) IN ((:numCarbons1, :doubleBonds1), (:numCarbons2, :doubleBonds2))
                            AND compound_chain.number_chains IN (1,2)
                        GROUP BY
                            cv.compound_id, cv.compound_name, cv.formula, cv.mass, cv.num_chains, cv.number_carbons, cv.double_bonds, cv.lipid_type
                        HAVING\s
                            SUM(CASE WHEN chains.num_carbons = :numCarbons1 AND chains.double_bonds = :doubleBonds1 THEN 1 ELSE 0 END) = 1
                            AND SUM(CASE WHEN chains.num_carbons = :numCarbons2 AND chains.double_bonds = :doubleBonds2 THEN 1 ELSE 0 END) = 1;""");
                paramMap.addValue("totalNumCarbons", fattyAcids.get(0).getCarbonAtoms() + fattyAcids.get(1).getCarbonAtoms() + fattyAcids.get(2).getCarbonAtoms());
                paramMap.addValue("totalDoubleBonds", fattyAcids.get(0).getDoubleBonds() + fattyAcids.get(1).getDoubleBonds() + fattyAcids.get(2).getDoubleBonds());
                paramMap.addValue("formulaSkeleton", formulaSkeleton.toString());
                paramMap.addValue("numCarbons1", fattyAcids.get(0).getCarbonAtoms());
                paramMap.addValue("doubleBonds1", fattyAcids.get(0).getDoubleBonds());
                paramMap.addValue("numCarbons2", fattyAcids.get(1).getCarbonAtoms());
                paramMap.addValue("doubleBonds2", fattyAcids.get(1).getDoubleBonds());
                return new LinkedHashSet<>(namedJdbcTemplate.query(queryBuilder.toString(), paramMap, new MSLipidRowMapper()));
            case 3:
                queryBuilder.append("""
                        WHERE
                            (chains.num_carbons, chains.double_bonds) IN ((:numCarbons1, :doubleBonds1), (:numCarbons2, :doubleBonds2), (:numCarbons3, :doubleBonds3))
                            AND compound_chain.number_chains IN (1)
                        GROUP BY
                            cv.compound_id, cv.compound_name, cv.formula, cv.mass, cv.num_chains, cv.number_carbons, cv.double_bonds, cv.lipid_type
                        HAVING\s
                            SUM(CASE WHEN chains.num_carbons = :numCarbons1 AND chains.double_bonds = :doubleBonds1 THEN 1 ELSE 0 END) = 1
                            AND SUM(CASE WHEN chains.num_carbons = :numCarbons2 AND chains.double_bonds = :doubleBonds2 THEN 1 ELSE 0 END) = 1
                            AND SUM(CASE WHEN chains.num_carbons = :numCarbons3 AND chains.double_bonds = :doubleBonds3 THEN 1 ELSE 0 END) = 1;""");
                paramMap.addValue("totalNumCarbons", fattyAcids.get(0).getCarbonAtoms() + fattyAcids.get(1).getCarbonAtoms() + fattyAcids.get(2).getCarbonAtoms());
                paramMap.addValue("totalDoubleBonds", fattyAcids.get(0).getDoubleBonds() + fattyAcids.get(1).getDoubleBonds() + fattyAcids.get(2).getDoubleBonds());
                paramMap.addValue("formulaSkeleton", formulaSkeleton.toString());
                paramMap.addValue("numCarbons1", fattyAcids.get(0).getCarbonAtoms());
                paramMap.addValue("doubleBonds1", fattyAcids.get(0).getDoubleBonds());
                paramMap.addValue("numCarbons2", fattyAcids.get(1).getCarbonAtoms());
                paramMap.addValue("doubleBonds2", fattyAcids.get(1).getDoubleBonds());
                paramMap.addValue("numCarbons3", fattyAcids.get(2).getCarbonAtoms());
                paramMap.addValue("doubleBonds3", fattyAcids.get(2).getDoubleBonds());
                return new LinkedHashSet<>(namedJdbcTemplate.query(queryBuilder.toString(), paramMap, new MSLipidRowMapper()));
            default:
                LinkedHashSet<MSLipid> lipidResults = new LinkedHashSet<>();
                List<List<FattyAcid>> fattyAcidCombinations = findCombinationsOfFAs(fattyAcids);
                double expectedMass = precursorIon - Adduct.getAdductMass(adduct) - PeriodicTable.elements_Map.get(Element.H);

                for (List<FattyAcid> combination : fattyAcidCombinations) {
                    StringBuilder queryBuilder2 = new StringBuilder();
                    queryBuilder2.append(queryBuilder.toString());
                    MapSqlParameterSource paramMap2 = new MapSqlParameterSource();
                    paramMap2.addValue("lipidType", "%" + lipidType + "(%");
                    if (Math.abs(predictLipidMass(lipidType, combination) - expectedMass) < 0.5d) {
                        Formula testFormula = new Formula(lipidSkeletalStructure.getFormula().toString());
                        int carbonAtoms = 0;
                        int doubleBonds = 0;

                        for (FattyAcid fattyAcid : combination) {
                            testFormula.addFattyAcidToFormula(fattyAcid);
                            carbonAtoms += fattyAcid.getCarbonAtoms();
                            doubleBonds += fattyAcid.getDoubleBonds();
                        }

                        queryBuilder2.append("""
                                WHERE
                                    (chains.num_carbons, chains.double_bonds) IN ((:numCarbons1, :doubleBonds1), (:numCarbons2, :doubleBonds2), (:numCarbons3, :doubleBonds3))
                                GROUP BY
                                    cv.compound_id, cv.compound_name, cv.formula, cv.mass, cv.num_chains, cv.number_carbons, cv.double_bonds, cv.lipid_type
                                HAVING\s
                                    SUM(CASE WHEN chains.num_carbons = :numCarbons1 AND chains.double_bonds = :doubleBonds1 THEN 1 ELSE 0 END) = 1
                                    AND SUM(CASE WHEN chains.num_carbons = :numCarbons2 AND chains.double_bonds = :doubleBonds2 THEN 1 ELSE 0 END) = 1
                                    AND SUM(CASE WHEN chains.num_carbons = :numCarbons3 AND chains.double_bonds = :doubleBonds3 THEN 1 ELSE 0 END) = 1;""");

                        paramMap2.addValue("totalNumCarbons", carbonAtoms);
                        paramMap2.addValue("totalDoubleBonds", doubleBonds);
                        paramMap2.addValue("formulaSkeleton", testFormula.toString());
                        paramMap2.addValue("numCarbons1", combination.get(0).getCarbonAtoms());
                        paramMap2.addValue("doubleBonds1", combination.get(0).getDoubleBonds());
                        paramMap2.addValue("numCarbons2", combination.get(1).getCarbonAtoms());
                        paramMap2.addValue("doubleBonds2", combination.get(1).getDoubleBonds());
                        paramMap2.addValue("numCarbons3", combination.get(2).getCarbonAtoms());
                        paramMap2.addValue("doubleBonds3", combination.get(2).getDoubleBonds());

                        lipidResults.addAll(namedJdbcTemplate.query(queryBuilder2.toString(), paramMap2, new MSLipidRowMapper()));
                    }
                }
                return lipidResults;
        }
    }

    private List<FattyAcid> getFAsFromMass(LipidType lipidType, double precursorIon, List<FattyAcid> fattyAcids, String adduct) {
        double expectedMass = precursorIon - Adduct.getAdductMass(adduct) - PeriodicTable.elements_Map.get(Element.H);
        double tolerance = 0.5d;

        for (int i = 0; i < 2 && i < fattyAcids.size(); i++) {
            List<FattyAcid> testFattyAcids = new ArrayList<>(fattyAcids);
            testFattyAcids.add(fattyAcids.get(i));
            double predictedMass = predictLipidMass(lipidType, testFattyAcids);

            if (Math.abs(predictedMass - expectedMass) < tolerance) {
                List<FattyAcid> result = new ArrayList<>(fattyAcids);
                result.add(fattyAcids.get(i));
                return result;
            }
        }
        return null;
    }

    private double predictLipidMass(LipidType lipidType, List<FattyAcid> fattyAcids) {
        LipidSkeletalStructure lipidSkeletalStructure = new LipidSkeletalStructure(lipidType);
        double mass = lipidSkeletalStructure.getMass(lipidSkeletalStructure.getFormula());
        for (FattyAcid fattyAcid : fattyAcids) {
            mass += fattyAcid.getMass(fattyAcid.getFormula());
            mass -= PeriodicTable.waterMass;
        }
        return mass;
    }

    public static List<List<FattyAcid>> findCombinationsOfFAs(List<FattyAcid> fattyAcids) {
        List<List<FattyAcid>> finalFattyAcidsList = new ArrayList<>();
        int n = fattyAcids.size();
        int combinationLength = 3;  // Length of each combination
        generateCombinations(fattyAcids, new ArrayList<>(), finalFattyAcidsList, 0, n, combinationLength);
        return finalFattyAcidsList;
    }

    private static void generateCombinations(List<FattyAcid> fattyAcids, List<FattyAcid> currentCombination,
                                             List<List<FattyAcid>> finalFattyAcidsList, int start, int n, int combinationLength) {
        if (currentCombination.size() == combinationLength) {
            finalFattyAcidsList.add(new ArrayList<>(currentCombination));
            return;
        }

        for (int i = start; i < n; i++) {
            currentCombination.add(fattyAcids.get(i));
            generateCombinations(fattyAcids, currentCombination, finalFattyAcidsList, i, n, combinationLength);
            currentCombination.remove(currentCombination.size() - 1);
        }
    }
}
