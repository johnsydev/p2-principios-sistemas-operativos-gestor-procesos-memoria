package minipcsimulator.services;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Clase que se encarga de parsear y verificar la sintaxis de las instrucciones en .asm.
 * @author johnsydev
 */
public class AsmParser {
    private static int numberLine = 0;
    private static ArrayList<ArrayList<String>> newLines = new ArrayList<>();

    /**
     * Verifica la sintaxis de las instrucciones en .asm y devuelve un ArrayList de ArrayLists con las instrucciones parseadas.
     * @param lines ArrayList de líneas de instrucciones en .asm.
     * @return ArrayList de ArrayLists con las instrucciones parseadas (en string).
     */
    public static ArrayList<ArrayList<String>> verifySyntax(ArrayList<String> lines) {
        AsmParser.numberLine = 0;
        AsmParser.newLines.clear();
        for (String line : lines) {
            AsmParser.numberLine++;
            if (line.trim().isEmpty()) {
                continue; 
            }
            if (!isValidInstruction(line)) {
                throw new RuntimeException("Error de sintaxis en la linea " + AsmParser.numberLine);
            }
        }
        return AsmParser.newLines;
    }

    /**
     * Divide una linea en un ArrayList de partes.
     * @param line La linea de código a dividir.
     * @return Un ArrayList de partes de la linea dividida por cada componente de la instrucción.
     */
    private static ArrayList<String> getLineArray(String line) {  
        String nline = line.trim();

        if (nline.startsWith(",") || nline.endsWith(",") || nline.contains(",,")) {
            throw new RuntimeException("Error de sintaxis en la linea " + AsmParser.numberLine);
        }
        // esto divide el array por espacios y comas
        ArrayList<String> parts = new ArrayList<>(Arrays.asList(nline.split("[,\\s]+")));
        AsmParser.newLines.add(parts);
        return parts;
    }

    /**
     * Verifica si un registro es válido.
     * @param register El registro a verificar.
     * @return true si el registro es válido, false en caso contrario.
    */
    public static boolean isValidRegister(String register) {
        ArrayList<String> validRegisters = new ArrayList<>(Arrays.asList(
            "AX", "BX", "CX", "DX", "AH", "AL"
        ));
        if (!validRegisters.contains(register)) {
            throw new RuntimeException("Error de sintaxis: Registro no reconocido en la línea " + AsmParser.numberLine);
        }
        return true;
    }

    /**
     * Verifica si un número es válido.
     * @param number El número a verificar.
     * @return true si el número es válido, false en caso contrario.
     */
    public static boolean isValidNumber(String number) {
        try {
            int num = Integer.parseInt(number);
            if (num < -127 || num > 127) {
                throw new RuntimeException("Error de sintaxis: Valor numérico fuera de rango soportado (-127 a 127) en la línea " + AsmParser.numberLine);
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidString(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        //validar comillas
        if (!str.startsWith("\"") || !str.endsWith("\"")) {
            return false;
        }
        //validar que la cadena no contenga comillas internas
        String innerContent = str.substring(1, str.length() - 1);
        if (innerContent.contains("\"")) {
            return false;
        }
        return true;
    }

    /**
     * Verifica si una instrucción es válida.
     * @param instruction La instrucción a verificar.
     * @return true si la instrucción es válida, false en caso contrario.
     */
    public static boolean isValidInstruction(String instruction) {
        ArrayList<String> parts = getLineArray(instruction);
        if (parts.isEmpty() || parts.size() < 1) return false;

        ArrayList<String> validInstructions = new ArrayList<>(Arrays.asList(
        "LOAD", "STORE", "ADD", "SUB", "MOV", "INC", "DEC", "SWAP", "INT", "JMP", "CMP", "JE", "JNE", "PARAM", "PUSH", "POP"
        ));

        ArrayList<String> validInterrupts = new ArrayList<>(Arrays.asList(
            "20H", "10H", "21H"
        ));

        ArrayList<String> validHexInterrupts = new ArrayList<>(Arrays.asList(
            "3ch", "3dh", "4dh", "40h", "41h"
        ));

        if (!validInstructions.contains(parts.get(0))) {
            throw new RuntimeException("Error de sintaxis: Instrucción no reconocida en la línea " + AsmParser.numberLine);
        }

        switch (parts.get(0)) {
            case "LOAD":
            case "STORE":
            case "ADD":
            case "SUB":
            case "PUSH":
            case "POP":
                if (parts.size() == 2 && isValidRegister(parts.get(1).trim())) {
                    return true;
                }
                else {
                    throw new RuntimeException("Error de sintaxis: Instrucción " + parts.get(0) + " requiere un registro válido en la línea " + AsmParser.numberLine);
                }
            case "MOV":
                if (parts.size() == 3 && isValidRegister(parts.get(1).trim()) && (isValidNumber(parts.get(2).trim()) || isValidString(parts.get(2).trim()) || validHexInterrupts.contains(parts.get(2).trim()) || isValidRegister(parts.get(2).trim()))) {
                    
                    // si se intenta mover una cadena a un registro que no sea DX
                    if (parts.size() == 3 && isValidString(parts.get(2).trim()) && !parts.get(1).trim().equals("DX")) {
                        throw new RuntimeException("Error de sintaxis: Instrucción MOV con cadena solo es válida para el registro DX en la línea " + AsmParser.numberLine);
                    }

                    // si se intenta mover una interrupción a un registro que no sea AH
                    if (parts.size() == 3 && validHexInterrupts.contains(parts.get(2).trim()) && !parts.get(1).trim().equals("AH")) {
                        throw new RuntimeException("Error de sintaxis: Instrucción MOV con interrupción hexadecimal solo es válida para el registro AH en la línea " + AsmParser.numberLine);
                    }
                    
                    return true;
                }
                else {
                    throw new RuntimeException("Error de sintaxis: Instrucción " + parts.get(0) + " requiere un registro y un valor numérico u otro registro válidos en la línea " + AsmParser.numberLine);
                }
            
            case "INC":
            case "DEC":
                if (parts.size() == 1 || (parts.size() == 2 && isValidRegister(parts.get(1).trim()))) {
                    return true;
                }
                else {
                    throw new RuntimeException("Error de sintaxis: Instrucción " + parts.get(0) + " requiere un registro válido en la línea " + AsmParser.numberLine);
                }

            case "SWAP":
            case "CMP":
                if (parts.size() == 3 && isValidRegister(parts.get(1).trim()) && isValidRegister(parts.get(2).trim())) {
                    return true;
                }
                else {
                    throw new RuntimeException("Error de sintaxis: Instrucción " + parts.get(0) + " requiere dos registros válidos en la línea " + AsmParser.numberLine);
                }

            case "JMP":
            case "JE":
            case "JNE":
                if (parts.size() == 2 && isValidNumber(parts.get(1).trim())) {
                    return true;
                }
                else {
                    throw new RuntimeException("Error de sintaxis: Instrucción " + parts.get(0) + " requiere un valor numérico válido en la línea " + AsmParser.numberLine);
                }

            case "PARAM":
                if ((parts.size() == 2 && isValidNumber(parts.get(1).trim()))
                    || (parts.size() == 3 && isValidNumber(parts.get(1).trim()) && isValidNumber(parts.get(2).trim()))
                    || (parts.size() == 4 && isValidNumber(parts.get(1).trim()) && isValidNumber(parts.get(2).trim()) && isValidNumber(parts.get(3).trim()))
                ) {
                    return true;
                }
                else {
                    if (parts.size() < 2 || parts.size() > 4) {
                        throw new RuntimeException("Error de sintaxis: Instrucción " + parts.get(0) + " requiere entre 1 y 3 valores numéricos válidos en la línea " + AsmParser.numberLine);
                    }
                    else {
                        throw new RuntimeException("Error de sintaxis: Instrucción " + parts.get(0) + " requiere valores numéricos válidos en la línea " + AsmParser.numberLine);
                    }
                }

            case "INT":
                if (parts.size() == 2 && validInterrupts.contains(parts.get(1).trim())) {
                    return true;
                }
                else {
                    throw new RuntimeException("Error de sintaxis: Interrupción no reconocida en la línea " + AsmParser.numberLine);
                }
            
            default:
                return false;
        }
    }
}
