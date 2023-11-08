package com.prog.gentlemens.cepstrumanalyzer.math;

import static java.lang.Math.pow;

import com.prog.gentlemens.cepstrumanalyzer.enums.NotesNames;

import java.util.HashMap;
import java.util.Map;

public final class HarmonicOperations {

    private HarmonicOperations() {
    }

    private static final Map<String, Integer> NOTES_FREQUENCIES = new HashMap<>();
    private static final int MINIMAL_FREQUENCY = 27;
    private static final int MAXIMAL_FREQUENCY = 4200;

    public static Map<String, Integer> getNotesFrequencies() {
        if (NOTES_FREQUENCIES.isEmpty()) {
            initializeNotesFrequencies();
        }
        return NOTES_FREQUENCIES;
    }

    public static String getClosestNote(double inputValue) {
        if (inputValue > MAXIMAL_FREQUENCY || inputValue < MINIMAL_FREQUENCY) {
            return "no note";
        }
        if (NOTES_FREQUENCIES.isEmpty()) {
            initializeNotesFrequencies();
        }

        return findTheNearestNote((int) inputValue);
    }

    private static String findTheNearestNote(int inputValue) {
        int notesDown = inputValue;
        int notesUp = inputValue;

        while (true) {
            for (Map.Entry<String, Integer> entry : NOTES_FREQUENCIES.entrySet()) {
                if (entry.getValue().equals(notesDown) || entry.getValue().equals(notesUp)) {
                    return entry.getKey();
                }
            }
            if (notesDown > MINIMAL_FREQUENCY) {
                notesDown = notesDown - 1;
            }
            if (notesUp < MAXIMAL_FREQUENCY) {
                notesUp = notesUp + 1;
            }
            if (notesDown < MINIMAL_FREQUENCY && notesUp > MAXIMAL_FREQUENCY) {
                return "no note";
            }
        }
    }

    private static void initializeNotesFrequencies() {
        /*
           In mathematical way the key frequencies's starting point is voice A0 with frequency
           equal 27.500 [Hz] and next key's frequency is equal to 27.500 * pow(2, 1/12)
           -> it is the most similar calculation and in this application it is used
           -> in real environment there are small differences and the multiplier between two
              closest voices is around 1.0595.
              
          The reason why the int value is used - instead of using float or double - is because
          it is more simply to calculate the distance to the nearest note without big impact on precision.
         */
        final double frequencyMultiplier = 1.0595;
        int octaveCounter = 0;

        for (int i = 0; i < 88; ++i) {
            // "C" note
            if (NotesNames.values()[i % 12].toString().equals("C")) {
                octaveCounter = octaveCounter + 1;
            }

            NOTES_FREQUENCIES.put(NotesNames.values()[i % 12].toString() + octaveCounter, (int) (27.500 * pow(frequencyMultiplier, i)));
        }
    }

}
