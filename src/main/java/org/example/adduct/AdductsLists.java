package org.example.adduct;


import ceu.biolab.Adduct;
import ceu.biolab.IncorrectAdduct;
import ceu.biolab.IncorrectFormula;
import ceu.biolab.NotFoundElement;

import java.util.*;
import javax.faces.model.SelectItem;

public class AdductsLists {

    public static final List<SelectItem> LISTNEUTRALMODESFORCEMS;

    static {
        List<SelectItem> listNeutralModesTMP = new LinkedList<>();
        listNeutralModesTMP.add(new SelectItem(0, "Neutral"));
        LISTNEUTRALMODESFORCEMS = Collections.unmodifiableList(listNeutralModesTMP);
    }

    public static final List<SelectItem> LISTNEUTRALMODES;

    static {
        List<SelectItem> listNeutralModesTMP = new LinkedList<>();
        listNeutralModesTMP.add(new SelectItem(0, "Neutral"));
        listNeutralModesTMP.add(new SelectItem(1, "Positive Mode"));
        listNeutralModesTMP.add(new SelectItem(2, "Negative Mode"));
        LISTNEUTRALMODES = Collections.unmodifiableList(listNeutralModesTMP);
    }

    public static final List<SelectItem> LISTIONIZEDMODES;

    static {
        List<SelectItem> listIonizedModesTMP = new LinkedList<>();
        listIonizedModesTMP.add(new SelectItem(1, "Positive Mode"));
        listIonizedModesTMP.add(new SelectItem(2, "Negative Mode"));
        LISTIONIZEDMODES = Collections.unmodifiableList(listIonizedModesTMP);
    }


    public static final Map<String, Adduct> MAPADDUCTS;

    static {
        try {
            // TODO: Check charges for each adduct
            // Positive adducts
            MAPADDUCTS = new HashMap<String, Adduct>();
            MAPADDUCTS.put("[M+H]+", new Adduct("[M+H]+"));
            MAPADDUCTS.put("[M+2H]2+", new Adduct("[M+2H]2+")); // CHARGE_2TMP
            MAPADDUCTS.put("[2M+H]+", new Adduct("[2M+H]+"));
            MAPADDUCTS.put("[M+2H]+", new Adduct("[M+2H]2+"));
            MAPADDUCTS.put("[M+Na]+", new Adduct("[M+Na]+"));
            MAPADDUCTS.put("[M+K]+", new Adduct("[M+K]+"));
            MAPADDUCTS.put("[M+NH4]+", new Adduct("[M+NH4]+"));
            MAPADDUCTS.put("[M+H-H2O]+", new Adduct("[M+H-H2O]+"));
            MAPADDUCTS.put("[M+H+NH4]2+", new Adduct("[M+H+NH4]2+")); // CHARGE_2TMP
            MAPADDUCTS.put("[2M+Na]+", new Adduct("[2M+Na]+"));
            MAPADDUCTS.put("[M+H+HCOONa]+", new Adduct("[M+H+HCOONa]+"));
            MAPADDUCTS.put("[2M+H-H2O]+", new Adduct("[2M+H-H2O]+"));
            MAPADDUCTS.put("[M+3H]3+", new Adduct("[M+3H]3+"));
            MAPADDUCTS.put("[M+2H+Na]2+", new Adduct("[M+2H+Na]2+")); // CHARGE_2TMP
            MAPADDUCTS.put("[M+H+2K]2+", new Adduct("[M+H+2K]2+"));
            MAPADDUCTS.put("[M+H+2Na]2+", new Adduct("[M+H+2Na]2+"));
            MAPADDUCTS.put("[M+3Na]3+", new Adduct("[M+3Na]3+"));
            MAPADDUCTS.put("[M+H+Na]2+", new Adduct("[M+H+Na]2+")); // CHARGE_2TMP
            MAPADDUCTS.put("[M+H+K]2+", new Adduct("[M+H+K]2+")); // CHARGE_2TMP
            MAPADDUCTS.put("[M+ACN+2H]2+", new Adduct("[M+ACN+2H]2+")); // CHARGE_2TMP
            MAPADDUCTS.put("[M+2Na]2+", new Adduct("[M+2Na]2+")); // CHARGE_2TMP
            MAPADDUCTS.put("[M+2ACN+2H]2+", new Adduct("[M+2ACN+2H]2+")); // CHARGE_2TMP
            MAPADDUCTS.put("[M+3ACN+2H]2+", new Adduct("[M+3ACN+2H]2+")); // CHARGE_2TMP
            MAPADDUCTS.put("[M+CH3OH+H]+", new Adduct("[M+CH3OH+H]+"));
            MAPADDUCTS.put("[M+ACN+H]+", new Adduct("[M+ACN+H]+"));
            MAPADDUCTS.put("[M+2Na-H]+", new Adduct("[M+2Na-H]+"));
            MAPADDUCTS.put("[M+IsoProp+H]+", new Adduct("[M+IsoProp+H]+"));
            MAPADDUCTS.put("[M+ACN+Na]+", new Adduct("[M+ACN+Na]+"));
            MAPADDUCTS.put("[M+2K-H]+", new Adduct("[M+2K-H]+"));
            MAPADDUCTS.put("[M+DMSO+H]+", new Adduct("[M+DMSO+H]+"));
            MAPADDUCTS.put("[M+2ACN+H]+", new Adduct("[M+2ACN+H]+"));
            MAPADDUCTS.put("[M+IsoProp+Na+H]+", new Adduct("[M+IsoProp+Na+H]+"));
            MAPADDUCTS.put("[2M+NH4]+", new Adduct("[2M+NH4]+"));
            MAPADDUCTS.put("[2M+K]+", new Adduct("[2M+K]+"));
            MAPADDUCTS.put("[2M+ACN+H]+", new Adduct("[2M+ACN+H]+"));
            MAPADDUCTS.put("[2M+ACN+Na]+", new Adduct("[2M+ACN+Na]+"));
            MAPADDUCTS.put("[3M+H]+", new Adduct("[3M+H]+"));
            MAPADDUCTS.put("[3M+Na]+", new Adduct("[3M+Na]+"));
            MAPADDUCTS.put("[M+H-2H2O]+", new Adduct("[M+H-2H2O]+"));
            MAPADDUCTS.put("[M+NH4-H2O]+", new Adduct("[M+NH4-H2O]+"));
            MAPADDUCTS.put("[M+Li]+", new Adduct("[M+Li]+"));
            MAPADDUCTS.put("[2M+2H+3H2O]2+", new Adduct("[2M+2H+3H2O]2+")); // Check charge
            MAPADDUCTS.put("[M+H+CH3COOH]+", new Adduct("[M+H+CH3COOH]+"));
            MAPADDUCTS.put("[M+H+CH3COONa]+", new Adduct("[M+H+CH3COONa]+"));
            MAPADDUCTS.put("[M+F+H]+", new Adduct("[M+F+H]+"));
            MAPADDUCTS.put("[M+2H+Na]3+", new Adduct("[M+2H+Na]3+"));
            MAPADDUCTS.put("[M+H+2K]3+", new Adduct("[M+H+2K]3+"));
            MAPADDUCTS.put("[M+H+2Na]3+", new Adduct("[M+H+2Na]3+"));

            // Negative adducts
            MAPADDUCTS.put("[M-H]-", new Adduct("[M-H]-"));
            MAPADDUCTS.put("[M+Cl]-", new Adduct("[M+Cl]-"));
            MAPADDUCTS.put("[M+HCOOH-H]-", new Adduct("[M+HCOOH-H]-"));
            MAPADDUCTS.put("[M-H-H2O]-", new Adduct("[M-H-H2O]-"));
            MAPADDUCTS.put("[M-H+HCOONa]-", new Adduct("[M-H+HCOONa]-"));
            MAPADDUCTS.put("[M-H+CH3COONa]-", new Adduct("[M-H+CH3COONa]-"));
            MAPADDUCTS.put("[2M-H]-", new Adduct("[2M-H]-"));
            MAPADDUCTS.put("[M-3H]3-", new Adduct("[M-3H]3-"));
            MAPADDUCTS.put("[M-2H]2-", new Adduct("[M-2H]2-"));
            MAPADDUCTS.put("[M+Na-2H]-", new Adduct("[M+Na-2H]-"));
            MAPADDUCTS.put("[M+K-2H]-", new Adduct("[M+K-2H]-"));
            MAPADDUCTS.put("[M+CH3COOH-H]-", new Adduct("[M+CH3COOH-H]-"));
            MAPADDUCTS.put("[M+Br]-", new Adduct("[M+Br]-"));
            MAPADDUCTS.put("[M+TFA-H]-", new Adduct("[M+TFA-H]-"));
            MAPADDUCTS.put("[2M+HCOOH-H]-", new Adduct("[2M+HCOOH-H]-"));
            MAPADDUCTS.put("[2M+CH3COOH-H]-", new Adduct("[2M+CH3COOH-H]-"));
            MAPADDUCTS.put("[3M-H]-", new Adduct("[3M-H]-"));
            MAPADDUCTS.put("[M+F]-", new Adduct("[M+F]-"));
        } catch (IncorrectAdduct | NotFoundElement | IncorrectFormula e) {
            throw new RuntimeException(e);
        }
    }

    public static final Map<String, String> MAPNEUTRALADDUCTS;

    static {
        Map<String, String> mapNeutralAdductsTMP = new LinkedHashMap<>();
        mapNeutralAdductsTMP.put("M", "0");
        MAPNEUTRALADDUCTS = Collections.unmodifiableMap(mapNeutralAdductsTMP);
    }
}
